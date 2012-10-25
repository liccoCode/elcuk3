package helper;

import com.amazonservices.mws.FulfillmentInboundShipment._2010_10_01.FBAInboundServiceMWSClient;
import com.amazonservices.mws.FulfillmentInboundShipment._2010_10_01.FBAInboundServiceMWSConfig;
import com.amazonservices.mws.FulfillmentInboundShipment._2010_10_01.FBAInboundServiceMWSException;
import com.amazonservices.mws.FulfillmentInboundShipment._2010_10_01.MWSEndpoint;
import com.amazonservices.mws.FulfillmentInboundShipment._2010_10_01.model.*;
import com.jamonapi.Monitor;
import com.jamonapi.MonitorFactory;
import models.market.Account;
import models.procure.FBACenter;
import models.procure.FBAShipment;
import models.procure.ShipItem;
import org.apache.commons.lang.Validate;
import play.libs.F;
import play.utils.FastRuntimeException;

import java.util.*;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.regex.Pattern;

/**
 * Amazon FBA 操作
 * User: wyattpan
 * Date: 9/11/12
 * Time: 5:46 PM
 */
public class FBA {

    private static final Map<String, FBAInboundServiceMWSClient> CLIENT_CACHE = new HashMap<String, FBAInboundServiceMWSClient>();
    private static final Pattern pattern = Pattern.compile("将\\[运输项目\\] (.*) 从\\[运输单\\]");

    /**
     * 根据 Shipment 向 Amazon 创建 FBAShipment plan
     * <p/>
     * 会在这里处理 Amazon 的 distribute FC`s 的问题: 确保所有 item 在一个 Shipment 中, 否则抛出异常;
     * 不会再去修改数量, 尽量保证系统内的数据.
     *
     * @param account 上传使用的账户
     * @param items   需要上传的 ShipItems
     * @return 成功在 Amazon FC`s plan 好的 FBA Shipment
     */
    public static F.Option<FBAShipment> plan(Account account, List<ShipItem> items) throws FBAInboundServiceMWSException {
        Monitor monitor = MonitorFactory.start("FBA.plan");
        FBAShipment fbaShipment = new FBAShipment();
        try {
            CreateInboundShipmentPlanRequest plan = new CreateInboundShipmentPlanRequest();

            // Merchant Id
            plan.setSellerId(account.merchantId);
            // 自贴标
            plan.setLabelPrepPreference("SELLER_LABEL");
            // 我们的联系地址
            plan.setShipFromAddress(Account.address(account.type));

            // 要发送的货物
            plan.setInboundShipmentPlanRequestItems(new InboundShipmentPlanRequestItemList(FBA.shipItemsToInboundShipmentPlanItems(items)));

            CreateInboundShipmentPlanResponse response = client(account).createInboundShipmentPlan(plan);
            CreateInboundShipmentPlanResult result = response.getCreateInboundShipmentPlanResult();

            if(result.isSetInboundShipmentPlans()) {
                InboundShipmentPlanList planList = result.getInboundShipmentPlans();
                List<InboundShipmentPlan> members = planList.getMember();

                InboundShipmentPlan member = null;
                StringBuilder msg = new StringBuilder();
                /**
                 * 选择哪一个 ShipmentPlan? :
                 * 1. 寻找每一个 ShipmentPlan 确保我们运输的货物在这个 Plan 中都存在.
                 * 2. 找到都存在的第一个, 否则继续
                 * 3. 如果最后都没找到, 则报告异常, 把信息抛给前台, 让创建人知道, 让其进行处理.
                 */
                boolean isFind;
                for(InboundShipmentPlan waitCheckPlan : members) {
                    isFind = true;
                    try {
                        List<InboundShipmentPlanItem> itemMember = waitCheckPlan.getItems().getMember();
                        Map<String, InboundShipmentPlanItem> inboundItemMap = new HashMap<String, InboundShipmentPlanItem>();
                        for(InboundShipmentPlanItem inboundPlanItem : itemMember)
                            inboundItemMap.put(inboundPlanItem.getSellerSKU(), inboundPlanItem);

                        for(ShipItem itm : items) {
                            if(!inboundItemMap.containsKey(itm.unit.selling.merchantSKU)) {
                                msg.append(String.format("%s not in %s FC`s\r\n",
                                        itm.unit.selling.merchantSKU, waitCheckPlan.getDestinationFulfillmentCenterId()));
                                isFind = false;
                                break;
                            }
                        }
                        if(isFind) {
                            member = waitCheckPlan;
                            break;
                        }
                    } catch(Exception e) {
                        // 捕获没有 item 的异常, 不处理这个 member
                    }
                }
                if(member == null)
                    throw new FastRuntimeException(msg.toString());


                fbaShipment.account = account;
                fbaShipment.shipmentId = member.getShipmentId();
                fbaShipment.labelPrepType = member.getLabelPrepType();
                fbaShipment.shipItems = items;

                fbaShipment.centerId = member.getDestinationFulfillmentCenterId();

                // FBA 仓库自适应
                FBACenter center = FBACenter.findByCenterId(fbaShipment.centerId);
                if(center == null) {
                    Address fbaAddress = member.getShipToAddress();
                    center = new FBACenter(fbaShipment.centerId, fbaAddress.getAddressLine1(),
                            fbaAddress.getAddressLine2(), fbaAddress.getCity(),
                            fbaAddress.getName(), fbaAddress.getCountryCode(),
                            fbaAddress.getStateOrProvinceCode(), fbaAddress.getPostalCode()
                    ).save();
                    Webs.systemMail(String.format("Add a new FC`s %s", center.centerId),
                            center.toString());
                }
                fbaShipment.fbaCenter = center;

                /**
                 * Items 处理:
                 * 1. shipitem -> procureunit -> selling.fnsku 的同步检查
                 * 2. 上传的数量与实际 Amazon 接收的数量, 使用 Amazon 上的, 需要手动重新再添加回去;(体现出 Amazon 的不一样)
                 */
                InboundShipmentPlanItemList itemList = member.getItems();
                List<InboundShipmentPlanItem> itemMembers = itemList.getMember();
                for(ShipItem spitm : items) {
                    for(InboundShipmentPlanItem item : itemMembers) {
                        if(item.getSellerSKU().equals(spitm.unit.selling.merchantSKU)) {
                            spitm.fulfillmentNetworkSKU = item.getFulfillmentNetworkSKU();
                            spitm.updateSellingFNSku();
                        }
                    }
                }
            } else {
                Webs.systemMail("{WARN} FBAShipment Plan Error! " + Dates.date2Date(), "创建 FBAShipment 失败.");
                throw new FBAInboundServiceMWSException("创建 FBA Plan 失败.");
            }
        } finally {
            monitor.stop();
        }
        return F.Option.Some(fbaShipment);
    }

    /**
     * 根据 FBAShipment 向 Amazon 创建 FBA Shipment, 如果创建成功则返回 FBA 的 WORKING 状态, 否则返回 PLAN;
     * <br/>
     * ps: 创建成功了则会设置 FBAShipment 的 title
     *
     * @param fbashipment
     * @return
     * @throws FBAInboundServiceMWSException
     */
    public static FBAShipment.S create(FBAShipment fbashipment) throws FBAInboundServiceMWSException {
        //TODO 测出如果数量与 plan 的不一样, Amazon 运输再创建吗?
        Monitor monitor = MonitorFactory.start("FBA.create");
        try {
            if(fbashipment.state != FBAShipment.S.PLAN) return fbashipment.state;
            String fbaTitle = String.format("%s %s", fbashipment.shipment.title(), Dates.date2DateTime());
            CreateInboundShipmentRequest create = new CreateInboundShipmentRequest();
            create.setSellerId(fbashipment.account.merchantId);
            create.setShipmentId(fbashipment.shipmentId);
            create.setInboundShipmentHeader(new InboundShipmentHeader(fbaTitle,
                    Account.address(fbashipment.account.type), fbashipment.centerId, FBAShipment.S.WORKING.name(), fbashipment.labelPrepType));
            // 设置 items
            List<InboundShipmentItem> items = FBA.shipItemsToInboundShipmentItems(fbashipment.shipItems);
            create.setInboundShipmentItems(new InboundShipmentItemList(items));

            CreateInboundShipmentResponse response = client(fbashipment.account).createInboundShipment(create);
            if(response.isSetCreateInboundShipmentResult()) {
                fbashipment.title = fbaTitle;
                fbashipment.createAt = new Date();
                return FBAShipment.S.WORKING;
            }
        } finally {
            monitor.stop();
        }
        return FBAShipment.S.PLAN;
    }


    /**
     * 更新 FBA Shipment, 包括 Amazon FBA Shipment 的状态, ShipItem 的数量 ; 指定 InboundShipmentItem
     *
     * @param fbaShipment
     * @param updateitems
     * @param state
     * @return
     * @throws FBAInboundServiceMWSException
     */
    public static FBAShipment.S update(FBAShipment fbaShipment, List<ShipItem> updateitems, FBAShipment.S state) throws FBAInboundServiceMWSException {
        Validate.notNull(state);
        // 只允许 WORKING 与 SHIPPED 状态的进行修改
        Monitor monitor = MonitorFactory.start("FBA.update");
        try {

            if(fbaShipment.state == FBAShipment.S.PLAN) return fbaShipment.state;
            UpdateInboundShipmentRequest update = new UpdateInboundShipmentRequest();
            update.setSellerId(fbaShipment.account.merchantId);
            update.setShipmentId(fbaShipment.shipmentId);
            update.setInboundShipmentHeader(new InboundShipmentHeader(fbaShipment.title,
                    Account.address(fbaShipment.account.type), fbaShipment.centerId, state.name(), fbaShipment.labelPrepType));

            List<InboundShipmentItem> items = FBA.shipItemsToInboundShipmentItems(updateitems);
            update.setInboundShipmentItems(new InboundShipmentItemList(items));

            UpdateInboundShipmentResponse response = client(fbaShipment.account).updateInboundShipment(update);
            if(response.isSetUpdateInboundShipmentResult())
                fbaShipment.state = state;
        } finally {
            monitor.stop();
        }
        return fbaShipment.state;
    }

    /**
     * 根据 FBAShipment 获取 Amazon 上的状态.
     *
     * @param shipmentIds 此账户相关的 FBA ShipmentId
     * @param account     请求的账户
     * @return shipmentId : {._1:state, ._2:centerId, ._3:shipmentName}
     */
    public static Map<String, F.T3<String, String, String>> listShipments(List<String> shipmentIds, Account account) throws FBAInboundServiceMWSException {
        Validate.notNull(shipmentIds);
        Validate.notNull(account);
        Validate.isTrue(shipmentIds.size() <= 50, "检查 Shipments 的时候, ShipmentIds 的数量必须小于 50 当前数量 " + shipmentIds.size() + ".");
        Validate.isTrue(shipmentIds.size() > 0, "需要至少一个 ShipmentId..");
        ListInboundShipmentsRequest listShipments = new ListInboundShipmentsRequest();
        listShipments.setSellerId(account.merchantId);
        listShipments.setShipmentIdList(new ShipmentIdList(shipmentIds));
        ListInboundShipmentsResponse response = client(account).listInboundShipments(listShipments);
        List<InboundShipmentInfo> inbounds = response.getListInboundShipmentsResult().getShipmentData().getMember();

        Map<String, F.T3<String, String, String>> shipmentsT3 = new HashMap<String, F.T3<String, String, String>>();
        for(InboundShipmentInfo info : inbounds) {
            // Amazon 对于重复提交的 FBA ShipmentId 不会做限制, 所以有过的信息不需要再记录
            if(shipmentsT3.containsKey(info.getShipmentId()))
                continue;
            shipmentsT3.put(info.getShipmentId(),
                    new F.T3<String, String, String>(info.getShipmentStatus(), info.getDestinationFulfillmentCenterId(), info.getShipmentName()));
        }
        return shipmentsT3;
    }

    /**
     * 返回对应 FBA Shipment 的 Items 的状态
     *
     * @param shipmentId
     * @param acc
     * @return msku: {._1: qtyReceived, ._2: qtyShiped}
     */
    public static Map<String, F.T2<Integer, Integer>> listShipmentItems(String shipmentId, Account acc) throws FBAInboundServiceMWSException {
        /**
         * item.getSellerSKU();
         * item.getQuantityShipped();
         * item.getQuantityReceived();
         */
        Map<String, F.T2<Integer, Integer>> fetchItems = new HashMap<String, F.T2<Integer, Integer>>();
        ListInboundShipmentItemsRequest request = new ListInboundShipmentItemsRequest();
        request.setShipmentId(shipmentId);
        request.setSellerId(acc.merchantId);
        ListInboundShipmentItemsResponse response = client(acc).listInboundShipmentItems(request);
        List<InboundShipmentItem> inboundItems = response.getListInboundShipmentItemsResult().getItemData().getMember();
        for(InboundShipmentItem item : inboundItems) {
            if(fetchItems.containsKey(item.getSellerSKU())) continue;
            fetchItems.put(item.getSellerSKU(), new F.T2<Integer, Integer>(item.getQuantityReceived(), item.getQuantityShipped()));
        }
        return fetchItems;
    }


    /**
     * 将 Shipment 运输的 ShipItems 转换成为 FBA 的 InboundShipmentItem
     * - 在转换的时候会检查 ShipItem 集合中是否有 msku 一样的, 如果一样, 则数量自动累计到一个 InboundShipmentItem 中
     * - 数量为 0 原来要删除, 但有存在, 那么数量也累计
     * <p/>
     * ps: 创建 FBA, 更新 FBA 时使用
     *
     * @param shipitems
     * @return
     */
    private static List<InboundShipmentItem> shipItemsToInboundShipmentItems(List<ShipItem> shipitems) {
        /**
         * 1. 根据 ShipItem 的 msku 组成 Map
         * 2. 遍历 Map 的 key 来生成 InboundShipmentItems
         */
        Map<String, AtomicInteger> shipitemsMap = shipItemMerge(shipitems);

        List<InboundShipmentItem> items = new ArrayList<InboundShipmentItem>();
        for(Map.Entry<String, AtomicInteger> entry : shipitemsMap.entrySet()) {
            // 如果 item.qty 为 0 Amazon 会自动删除这个 InboundItem
            items.add(new InboundShipmentItem(null, entry.getKey(), null, entry.getValue().get(), null));
        }
        return items;
    }

    /**
     * 将 Shipment 运输的 ShipItems 转换成为 FBA 的 InboundShipmentPlanRequestItem
     * - 在转换的时候会检查 ShipItem 集合中是否有 msku 一样的, 如果一样, 则数量自动累计到一个 InboundShipmentPlanRequestItem 中
     * - 数量为 0 原来要删除, 但有存在, 那么数量也累计
     * <p/>
     * ps: 创建计划时时候
     *
     * @param shipitems
     * @return
     */
    private static List<InboundShipmentPlanRequestItem> shipItemsToInboundShipmentPlanItems(List<ShipItem> shipitems) {
        Map<String, AtomicInteger> shipitemsMap = shipItemMerge(shipitems);

        List<InboundShipmentPlanRequestItem> items = new ArrayList<InboundShipmentPlanRequestItem>();
        for(Map.Entry<String, AtomicInteger> entry : shipitemsMap.entrySet()) {
            // 如果 item.qty 为 0 Amazon 会自动删除这个 InboundItem
            items.add(new InboundShipmentPlanRequestItem(entry.getKey(), null, null, entry.getValue().get()));
        }
        return items;
    }


    /**
     * 将 ShipItem 中由于相同 msku 的数量进行合并, Amazon 不允许分开提交.
     *
     * @param shipItems
     * @return
     */
    private static Map<String, AtomicInteger> shipItemMerge(List<ShipItem> shipItems) {
        /**
         * 1. 根据 ShipItem 的 msku 组成 Map
         * 2. 遍历 Map 的 key 来生成 InboundShipmentItems
         */
        Map<String, AtomicInteger> shipitemsMap = new HashMap<String, AtomicInteger>();
        for(ShipItem item : shipItems) {
            if(shipitemsMap.containsKey(item.unit.selling.merchantSKU))
                shipitemsMap.get(item.unit.selling.merchantSKU).addAndGet(item.qty);
            else
                shipitemsMap.put(item.unit.selling.merchantSKU, new AtomicInteger(item.qty));
        }
        return shipitemsMap;
    }


    /**
     * 获取 FBA 的 FWS 的 client
     *
     * @param acc
     * @return
     */
    private static FBAInboundServiceMWSClient client(Account acc) {
        if(!acc.isSaleAcc) throw new IllegalArgumentException("需要销售账户!");
        String key = String.format("%s_%s", acc.type, acc.id);
        FBAInboundServiceMWSClient client;
        if(CLIENT_CACHE.containsKey(key)) client = CLIENT_CACHE.get(key);
        else {
            synchronized(CLIENT_CACHE) {
                if(CLIENT_CACHE.containsKey(key)) return CLIENT_CACHE.get(key);
                FBAInboundServiceMWSConfig config = new FBAInboundServiceMWSConfig();
                // 设置服务器地址
                switch(acc.type) {
                    case AMAZON_UK:
                        config.setServiceURL(MWSEndpoint.UK.toString());
                        break;
                    case AMAZON_DE:
                        config.setServiceURL(MWSEndpoint.DE.toString());
                        break;
                    case AMAZON_US:
                        config.setServiceURL(MWSEndpoint.US.toString());
                        break;
                    default:
                        throw new UnsupportedOperationException("不支持的 FBA 地址");
                }
                client = new FBAInboundServiceMWSClient(acc.accessKey, acc.token, "elcuk2", "1.0", config);
                CLIENT_CACHE.put(key, client);
            }
        }
        return client;
    }


}
