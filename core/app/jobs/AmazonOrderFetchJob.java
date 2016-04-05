package jobs;

import com.elcuk.mws.jaxb.ordertracking.*;
import helper.Currency;
import helper.J;
import helper.LogUtils;
import helper.Webs;
import models.Jobex;
import models.market.*;
import models.product.Product;
import org.apache.commons.lang.StringUtils;
import org.joda.time.DateTime;
import play.Logger;
import play.jobs.Job;

import javax.xml.bind.JAXB;
import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * 第三步, 补全遗漏信息;
 * <p/>
 * * 周期:
 * - 轮询周期: 1h
 * - Duration: 2h
 * - Job Interval: 24h
 * User: Wyatt                   Shipment Plan
 * Date: 12-1-8
 * Time: 上午5:59
 * @deprecated
 */
public class AmazonOrderFetchJob extends Job implements JobRequest.AmazonJob {
    @Override
    public void doJob() throws Exception {
        long begin = System.currentTimeMillis();
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
        if(LogUtils.isslow(System.currentTimeMillis() - begin, "AmazonOrderFetchJob")) {
            LogUtils.JOBLOG
                    .info(String.format("AmazonOrderFetchJob calculate.... [%sms]", System.currentTimeMillis() - begin));
        }
    }

    /**
     * 每 24 小时一次
     */
    @Override
    public int intervalHours() {
        return 8;
    }

    /**
     * 处理发现订单
     *
     * @param jobRequest
     */
    @Override
    public void callBack(final JobRequest jobRequest) {
        try {

            /**
             * 1. 对所有订单(大概 1w 个订单), 将订单分解成 1000 个一组依次进行
             * 2. 首先查找不存在的订单, 将新订单抓取回来
             * 3. 更新订单, 同时更新订单项目
             */
            List<Orderr> orders = AmazonOrderFetchJob.allOrderXML(
                    new File(jobRequest.path), jobRequest.account
            );
            List<Orderr> partOrders = orders.subList(
                    0, orders.size() > 1000 ? 1000 : orders.size()
            );
            while(orders.size() > 0) {
                AmazonOrderDiscover.saveOrUpdateOrders(partOrders, true);

                // 清理掉已经处理完成的 1000 个订单
                partOrders.clear();
                partOrders = orders.subList(0, (orders.size() > 1000 ? 1000 : orders.size()));
                Logger.info("Deal %s orders....", partOrders.size());
            }
            int hour = DateTime.now().getHourOfDay();


            if(hour >= 0 && hour <= 4) {
                /**
                 * 如果是早上执行则改为昨天的23:00,避免执行时间不准确
                 */
                jobRequest.requestDate = DateTime.now().plusDays(-1).withHourOfDay(23).withMinuteOfHour(0)
                        .withSecondOfMinute(0)
                        .toDate();
            } else if(hour > 18 && hour <= 24) {
                /**
                 * 如果是晚上执行则改为23:00,避免执行时间不准确
                 */
                jobRequest.requestDate = DateTime.now().withHourOfDay(23).withMinuteOfHour(0).withSecondOfMinute(0)
                        .toDate();
            } else if(hour >= 12 && hour <= 18) {
                /**
                 * 如果是下午执行则改为15:00,避免执行时间不准确
                 */
                jobRequest.requestDate = DateTime.now().withHourOfDay(15).withMinuteOfHour(0).withSecondOfMinute(0)
                        .toDate();
            } else {
                /**
                 * 如果是早上执行则改为7:00,避免执行时间不准确
                 */
                jobRequest.requestDate = DateTime.now().withHourOfDay(7).withMinuteOfHour(0).withSecondOfMinute(0)
                        .toDate();
            }
            jobRequest.save();

        } catch(Exception e) {
            Logger.warn("AmazonOrderFetchJob.callback error. %s", Webs.S(e));
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
                Orderr orderr = amzOrderToOrderr(odt, acc);
                if(orderr == null) continue;
                orderr.items = amzOrderItemsToOrderItems(
                        odt.getOrderItem(), orderr, acc
                );
                orders.add(orderr);
            } catch(Exception e) {
                errors.append(Webs.E(e)).append("<br><br>").append(J.json(odt)).append("<br><br>");
            }
        }

        if(errors.length() > 0)
            Webs.systemMail(
                    String.format("解析 %s 订单文件的错误.", file.getAbsolutePath()),
                    errors.toString()
            );

        return orders;
    }

    /**
     * Amazon 订单类型变为  Orderr
     *
     * @param odt
     * @param acc
     * @return
     */
    private static Orderr amzOrderToOrderr(OrderType odt, Account acc) {
        String amazonOrderId = odt.getAmazonOrderID().toUpperCase();
        if(StringUtils.startsWith(amazonOrderId, "S02") ||
                StringUtils.startsWith(amazonOrderId, "S01")) {
            Logger.info("OrderId {%s} Can Not Be Add to Normal Order", amazonOrderId);
            return null;
        }

        Orderr orderr = new Orderr();
        orderr.account = acc;
        orderr.orderId = amazonOrderId;
        if(odt.getSalesChannel().contains("WebStore"))
            orderr.market = M.AMAZON_DE;
        else
            orderr.market = M.val(odt.getSalesChannel());
        orderr.paymentDate = odt.getPurchaseDate().toGregorianCalendar().getTime();
        orderr.createDate = orderr.paymentDate;

        orderr.state = parseOrderState(odt.getOrderStatus());

        if(!Arrays.asList(Orderr.S.CANCEL, Orderr.S.PENDING).contains(orderr.state))
            orderr.shipDate = odt.getLastUpdatedDate().toGregorianCalendar().getTime();

        if(odt.getFulfillmentData() != null) {
            FulfillmentDataType ffdt = odt.getFulfillmentData();
            orderr.shipLevel = ffdt.getShipServiceLevel();

            if(ffdt.getAddress() != null) {
                AddressType addtype = ffdt.getAddress();
                orderr.city = addtype.getCity();
                orderr.province = addtype.getState();
                orderr.postalCode = addtype.getPostalCode();
                orderr.country = addtype.getCountry();
            }
        }
        return orderr;
    }

    /**
     * Amazon OrderItemType 变为 OrderItem
     *
     * @param orderItemTypes
     * @param orderr
     * @param acc
     * @return
     */
    private static List<OrderItem> amzOrderItemsToOrderItems(List<OrderItemType> orderItemTypes,
                                                             Orderr orderr,
                                                             Account acc) {
        List<OrderItem> orderItems = new ArrayList<OrderItem>();
        for(OrderItemType amzOrderItem : orderItemTypes) {
            OrderItem orderItem = new OrderItem();

            orderItem.id = String.format("%s_%s",
                    orderr.orderId, Product.merchantSKUtoSKU(amzOrderItem.getSKU()));

            orderItem.createDate = orderr.paymentDate;
            orderItem.order = orderr;
            orderItem.market = orderr.market;
            orderItem.listingName = amzOrderItem.getProductName();
            orderItem.quantity = amzOrderItem.getQuantity();

            // TODO 如果是来自 DE 账户的 IT 订单, 需要转移选择 IT 账户.
            // TODO 2014.3.30 日以后, 确定新 IT 市场账户启动则删除兼容代码
            if(orderr.market == M.AMAZON_IT) {
                orderItem.selling = Selling.findById(
                        Selling.sid(amzOrderItem.getSKU().toUpperCase(), orderr.market, Account.saleAccount(M.AMAZON_IT))
                );
            } else {
                orderItem.selling = Selling.findById(
                        Selling.sid(amzOrderItem.getSKU().toUpperCase(), orderr.market, acc));
            }
            orderItem.product = Product.findByMerchantSKU(amzOrderItem.getSKU());
            if(orderItem.selling == null) {
                String likeSellingId = "%" + orderItem.product + "%|" + orderr.market.nickName() +
                        "|" + acc.id;
                orderItem.selling = Selling.find("sellingId like ?", likeSellingId).first();
            }

            orderItem.quantity = amzOrderItem.getQuantity();

            // ItemPrice
            if(amzOrderItem.getItemPrice() != null) {
                ItemPriceType priceType = amzOrderItem.getItemPrice();
                for(ComponentType component : priceType.getComponent()) {
                    /**
                     * 1. Principal
                     * 2. Shipping
                     * 3. GiftWrap
                     */
                    String type = component.getType();
                    if("Principal".equalsIgnoreCase(type)) {
                        orderItem.price = component.getAmount().getValue();
                        orderItem.currency = Currency.valueOf(component.getAmount().getCurrency());
                    } else if("Shipping".equalsIgnoreCase(type)) {
                        orderItem.shippingPrice = component.getAmount().getValue();
                    } else if("GiftWrap".equalsIgnoreCase(type)) {
                        orderItem.giftWrap = component.getAmount().getValue();
                    }
                }
            }

            // Promotion
            if(amzOrderItem.getPromotion() != null) {
                PromotionType promotionType = amzOrderItem.getPromotion();
                orderItem.promotionIDs = promotionType.getPromotionIDs();
                orderItem.discountPrice = promotionType.getItemPromotionDiscount();
                if(promotionType.getShipPromotionDiscount() != null) {
                    if(orderItem.discountPrice == null) orderItem.discountPrice = 0f;
                    orderItem.discountPrice += promotionType.getShipPromotionDiscount();
                }
            }
            addToOrderitems(orderItems, orderItem);
        }
        return orderItems;
    }

    /**
     * 因为 Amazon 会将 Gift Wrap 与非 GiftWrap 分开, 所以这里我们需要合并
     *
     * @param items
     * @param newItem
     */
    private static void addToOrderitems(List<OrderItem> items, OrderItem newItem) {
        if(!items.contains(newItem)) {
            items.add(newItem);
        } else {
            for(OrderItem itm : items) {
                if(!itm.equals(newItem)) continue;
                Logger.info("Merge one OrderItem[%s], see details goto: %s",
                        itm.order.orderId,
                        itm.market.orderDetail(itm.order.orderId));
                if(newItem.discountPrice != null) {
                    if(itm.discountPrice == null) itm.discountPrice = 0f;
                    itm.discountPrice += newItem.discountPrice;
                }
                if(newItem.price != null) {
                    if(itm.price == null) itm.price = 0f;
                    itm.price += newItem.price;
                }
                if(newItem.usdCost != null) {
                    if(itm.usdCost == null) itm.usdCost = 0f;
                    itm.usdCost += newItem.usdCost;
                }
                if(newItem.giftWrap != null) {
                    if(itm.giftWrap == null) itm.giftWrap = 0f;
                    itm.giftWrap += newItem.giftWrap;
                }
                itm.quantity += newItem.quantity;
                if(StringUtils.isNotBlank(newItem.promotionIDs)) {
                    itm.promotionIDs += "," + newItem.promotionIDs;
                }
            }
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
