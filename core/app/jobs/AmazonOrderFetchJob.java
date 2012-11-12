package jobs;

import com.elcuk.mws.jaxb.ordertracking.*;
import helper.Currency;
import helper.Dates;
import helper.J;
import helper.Webs;
import models.Jobex;
import models.Notification;
import models.market.*;
import models.product.Product;
import org.apache.commons.lang.StringUtils;
import org.joda.time.DateTime;
import play.Logger;
import play.db.helper.JpqlSelect;
import play.jobs.Every;
import play.jobs.Job;

import javax.xml.bind.JAXB;
import java.io.File;
import java.util.*;

/**
 * 每隔一段时间到 Amazon 上进行订单的抓取
 * //TODO 在处理好 Product, Listing, Selling 的数据以后再编写
 * * 周期:
 * - 轮询周期: 5mn
 * - Duration: 10mn
 * - Job Interval: 1h
 * User: Wyatt
 * Date: 12-1-8
 * Time: 上午5:59
 */
@Every("5mn")
public class AmazonOrderFetchJob extends Job implements JobRequest.AmazonJob {
    @Override
    public void doJob() throws Exception {
        if(!Jobex.findByClassName(AmazonOrderFetchJob.class.getName()).isExcute()) return;
        // 对每一个用户都是如此
        List<Account> accs = Account.openedSaleAcc();
        /**
         * 1. 检查对应的市场是否需要进行创建新的 Job, 需要则创建, 否则返回 null
         * 2. 处理需要进行发送请求的 Job;
         * 3. 获取需要更新状态的 Job, 并对这些 Job 进行状态更新;
         * 4. 获取需要获取 ReportId 的 Job, 并将这些 Job 进行 ReportId 更新;
         * 5. 获取需要获取 Report 文件的 Job, 并将这些 Job 进行 Report 文件下载.
         */

        // 1,2. 需要创建新的 Job
        for(Account acc : accs) {
            JobRequest job = JobRequest.checkJob(acc, this, acc.marketplaceId());
            if(job != null) job.request();
        }
        Logger.info("AmazonOrderFetchJob step1 done!");

        // 3. 更新状态的 Job
        JobRequest.updateState(type());
        Logger.info("AmazonOrderFetchJob step2 done!");

        // 4. 获取 ReportId
        JobRequest.updateReportId(type());
        Logger.info("AmazonOrderFetchJob step3 done!");

        // 5. 下载 report 文件
        JobRequest.downLoad(type());
        Logger.info("AmazonOrderFetchJob step4 done!");

        // 6. 处理下载好的文件
        JobRequest.dealWith(type(), this);
        Logger.info("AmazonOrderFetchJob step5 done!");
    }

    /**
     * 每小时发现一次新订单
     */
    @Override
    public int intervalHours() {
        return 1;
    }

    /**
     * 处理发现订单
     *
     * @param jobRequest
     */
    @Override
    public void callBack(final JobRequest jobRequest) {
        try {
            new Job() {
                @Override
                public void doJob() {
                    long begin = System.currentTimeMillis();
                    /**
                     * 1. 解析出文件中的所有 Orders.
                     * 2. 遍历所有的订单, 利用 hibernate 的二级缓存, 加载 Orderr 进行保存或者更新
                     */
                    List<Orderr> orders = AmazonOrderFetchJob.allOrderXML(new File(jobRequest.path), jobRequest.account); // 1. 解析出订单
                    // 分几个部分处理? 每个部分最多处理 1000 个订单
                    int part = new Double(Math.ceil(orders.size() / 1000f)).intValue();
                    List<Orderr> subList = orders.subList(0, orders.size() > 1000 ? 1000 : orders.size());
                    for(int i = 1; i <= part; i++) {
                        Map<String, Orderr> managerOrderMap = Orderr.list2Map(Orderr.find("orderId IN " + JpqlSelect.inlineParam(Orderr.ids(subList))).<Orderr>fetch());
                        for(Orderr order : subList) {
                            if(Orderr.count("orderId=?", order.orderId) <= 0) { //保存
                                order.save();
                                Logger.info("Save Order: " + order.orderId);
                            } else { //更新
                                Orderr managed = managerOrderMap.get(order.orderId);
                                // 如果订单已经为 CANCEL 了, 则不更新了
                                if(managed.state == Orderr.S.CANCEL) continue;
                                managed.updateAttrs(order);
                                Logger.info("Update Order: " + order.orderId);
                            }
                        }
                        // 查看 Java Doc, 将原始 List 中的这部分子 List 清除
                        subList.clear();
                        subList = orders.subList(0, orders.size() > 1000 ? 1000 : orders.size());
                    }

                    Notification.notifies(
                            String.format("%s 订单解析完成, 总共耗时: %s 秒, 拆分为 %s 部分处理, 共处理: %s 个订单,",
                                    jobRequest.path, ((System.currentTimeMillis() - begin) / 1000), part, orders.size()),
                            1, 4);
                }
            }.now();
        } catch(Exception e) {
            Logger.warn("AmazonOrderFetchJob.callback error.", Webs.S(e));
        }
    }

    @Override
    public JobRequest.T type() {
        return JobRequest.T.ALL_FBA_ORDER_FETCH;
    }


    /**
     * 解析 XML 文件中记录的订单(注意:这里是如实反应 XML 中的信息,不要更新数据库), 这一部分主要提供的是 Amazon 的
     *
     * @param file
     * @return
     */
    public static List<Orderr> allOrderXML(File file, Account acc) {
        AmazonEnvelopeType envelopeType = JAXB.unmarshal(file, AmazonEnvelopeType.class);
        List<Orderr> orders = new ArrayList<Orderr>();
        StringBuilder errors = new StringBuilder();
        for(MessageType message : envelopeType.getMessage()) {
            OrderType odt = message.getOrder();
            try {

                String amazonOrderId = odt.getAmazonOrderID().toUpperCase();
                if(StringUtils.startsWith(amazonOrderId, "S02") || StringUtils.startsWith(amazonOrderId, "S01")) {
                    Logger.info("OrderId {%s} Can Not Be Add to Normal Order", amazonOrderId);
                    continue;
                }

                Orderr orderr = new Orderr();
                orderr.account = acc;
                orderr.orderId = amazonOrderId;
                orderr.market = M.val(odt.getSalesChannel());
                orderr.createDate = new DateTime(odt.getPurchaseDate().toGregorianCalendar().getTime(), Dates.timeZone(orderr.market)).toDate();
                orderr.state = parseOrderState(odt.getOrderStatus());

                if(orderr.state.ordinal() >= Orderr.S.PAYMENT.ordinal()) {
                    Date lastUpdateTime = new DateTime(odt.getLastUpdatedDate().toGregorianCalendar().getTime(), Dates.timeZone(orderr.market)).toDate();
                    orderr.paymentDate = lastUpdateTime;
                    orderr.shipDate = lastUpdateTime;


                    FulfillmentDataType ffdt = odt.getFulfillmentData();
                    orderr.shipLevel = ffdt.getShipServiceLevel();

                    AddressType addtype = ffdt.getAddress();
                    orderr.city = addtype.getCity(); // 在国外, 一般情况下只需要 City, State(Province), PostalCode 就可以定位具体地址了
                    orderr.province = addtype.getState();
                    orderr.postalCode = addtype.getPostalCode();
                    orderr.country = addtype.getCountry();
                }

                List<OrderItemType> oits = odt.getOrderItem();
                List<OrderItem> orderitems = new ArrayList<OrderItem>();

                Float totalAmount = 0f;
                Float shippingAmount = 0f;
                Map<String, Boolean> mailed = new HashMap<String, Boolean>();
                for(OrderItemType oid : oits) {
                    /**
                     * 0. 检查这个 order 是否需要进行补充 orderitem
                     * 1. 将 orderitem 的基本信息补充完全
                     * 2. 检查 orderitems List 中时候已经存在相同的产品了, 如果有这修改已经存在的产品的数量否则才添加新的
                     */
                    if(oid.getQuantity() < 0) continue;//只有数量为 0 这没必要记录, 但如果订单为 Cancel 还是有必要记录的

                    OrderItem oi = new OrderItem();
                    oi.market = orderr.market;
                    oi.order = orderr;
                    oi.listingName = oid.getProductName();
                    oi.quantity = oid.getQuantity();
                    oi.createDate = orderr.createDate; // 这个字段是从 Order 转移到 OrderItem 上的一个冗余字段, 方便统计使用


                    // 如果属于 UnUsedSKU 那么则跳过这个解析
                    if(Product.unUsedSKU(oid.getSKU())) continue;

                    String sku = Product.merchantSKUtoSKU(oid.getSKU());
                    Product product = Product.findById(sku);
                    Selling selling = Selling.findById(Selling.sid(oid.getSKU().toUpperCase(), orderr.market/*市场使用的是 Orderr 而非 Account*/, acc));
                    if(product != null) oi.product = product;
                    else {
                        String title = String.format("SKU[%s] is not in PRODUCT, it can not be happed!!", sku);
                        Logger.error(title);
                        if(!mailed.containsKey(sku)) {
                            Webs.systemMail(title, title);
                            mailed.put(sku, true);
                        }
                        continue; // 发生了这个错误, 这跳过这个 orderitem
                    }
                    if(selling != null) oi.selling = selling;
                    else {
                        String sid = Selling.sid(oid.getSKU().toUpperCase(), orderr.market, acc);
                        String title = String.format("Selling[%s] is not in SELLING, it can not be happed!", sid);
                        Logger.warn(title);
                        if(mailed.containsKey(sid)) {
                            Webs.systemMail(title, title);
                            mailed.put(sid, true);
                        }
                        continue;
                    }
                    oi.id = String.format("%s_%s", orderr.orderId, product.sku);

                    // price calculate
                    oi.price = oi.discountPrice = oi.feesAmaount = oi.shippingPrice = 0f; // 初始化值
                    if(orderr.state == Orderr.S.CANCEL) { //基本信息完成后, 如果订单是取消的, 那么价格等都设置为 0 , 不计入计算并
                        addIntoOrderItemList(orderitems, oi);
                        continue;
                    }

                    ItemPriceType ipt = oid.getItemPrice();
                    if(ipt == null) { //如果价格还没有出来, 表示在 Amazon 上数据还没有及时到位, 暂时不记录价格数据
                        Logger.warn("Order[%s] orderitem don`t have price yet.", orderr.orderId);
                    } else {
                        List<ComponentType> costs = oid.getItemPrice().getComponent();
                        for(ComponentType ct : costs) { // 价格在这个都要统一成为 GBP (英镑), 注意不是 EUR 欧元
                            AmountType at = ct.getAmount();
                            String compType = ct.getType().toLowerCase();
                            oi.currency = helper.Currency.valueOf(at.getCurrency());
                            if(oi.currency == null) oi.currency = Currency.USD;// 如果 Currency 为 null,则修补为 USD
                            if("principal".equals(compType)) {
                                oi.price = at.getValue();
                                totalAmount += oi.price;
                                oi.usdCost = oi.currency.toUSD(oi.price);
                            } else if("shipping".equals(compType)) {
                                oi.shippingPrice = at.getValue();
                                shippingAmount += oi.shippingPrice;
                            } else if("giftwrap".equals(compType)) {
                                oi.memo += String.format("\nGiftWrap: %s %s.", at.getValue(), oi.currency); //这个价格暂时不知道如何处理, 所以就直接记录到中性字段中
                            }
                        }

                        // 计算折扣了多少钱
                        PromotionType promotionType = oid.getPromotion();
                        if(promotionType != null) {
                            if(promotionType.getShipPromotionDiscount() != null) {
                                oi.shippingPrice = oi.shippingPrice - promotionType.getShipPromotionDiscount();
                            }
                            if(promotionType.getItemPromotionDiscount() != null) {
                                oi.discountPrice = promotionType.getItemPromotionDiscount();
                            }
                        }
                    }

                    addIntoOrderItemList(orderitems, oi);
                }

                orderr.totalAmount = totalAmount;
                orderr.shippingAmount = shippingAmount;
                orderr.items = orderitems;
                orders.add(orderr);
            } catch(Exception e) {
                errors.append(Webs.E(e)).append("\r\n").append(J.json(odt));
            }
        }
        if(StringUtils.isNotBlank(errors.toString()))
            Webs.systemMail(String.format("解析 %s 订单文件的错误.", file.getAbsolutePath()), errors.toString());
        return orders;
    }


    /**
     * 判断将 OrderItem 是否能够添加如已经存在的 List[OrderItem]
     *
     * @param list
     * @param oi
     * @return
     */
    private static boolean addIntoOrderItemList(List<OrderItem> list, OrderItem oi) {
        if(list.contains(oi)) {
            for(OrderItem item : list) {
                if(!item.equals(oi)) continue;
                item.quantity = item.quantity + oi.quantity;
                Logger.info("merge one orderItem[%s] belong to order %s, see the details goto %s",
                        oi.product.sku,
                        oi.order.orderId,
                        "https://sellercentral.amazon.co.uk/gp/orders-v2/details?ie=UTF8&orderID=" + oi.order.orderId
                );
            }
            return false;
        } else {
            list.add(oi);
            return true;
        }
    }

    private static Orderr.S parseOrderState(String orderState) {
        // {"Pending"=>226233, "Shipped"=>1284685, "Cancelled"=>28538, "Shipping"=>1342}, 半年的更新文件
        String orderSt = orderState.toLowerCase();
        if("pending".equals(orderSt)) {
            return Orderr.S.PENDING;
        } else if("shipped".equals(orderSt)) {
            return Orderr.S.SHIPPED;
        } else if("unshipped".equals(orderSt)) {
            return Orderr.S.PAYMENT;
        } else if("shipping".equals(orderSt)) {
            return Orderr.S.PAYMENT;
        } else if("cancelled".equals(orderSt)) {
            return Orderr.S.CANCEL;
        } else {
            return Orderr.S.PENDING;
        }
    }
}
