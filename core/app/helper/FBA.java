package helper;

import com.amazonservices.mws.FulfillmentInboundShipment._2010_10_01.FBAInboundServiceMWSClient;
import com.amazonservices.mws.FulfillmentInboundShipment._2010_10_01.FBAInboundServiceMWSConfig;
import com.amazonservices.mws.FulfillmentInboundShipment._2010_10_01.FBAInboundServiceMWSException;
import com.amazonservices.mws.FulfillmentInboundShipment._2010_10_01.MWSEndpoint;
import com.amazonservices.mws.FulfillmentInboundShipment._2010_10_01.model.*;
import models.market.Account;
import models.procure.*;
import org.apache.commons.lang.StringUtils;
import org.apache.commons.lang.Validate;
import play.libs.F;
import play.utils.FastRuntimeException;

import java.util.*;

/**
 * Amazon FBA 操作
 * <pre>
 * All operations from the Fulfillment Inbound Shipment API section, the Fulfillment Inventory API section, and
 * the Fulfillment Outbound Shipment API section together share a maximum request quota of 30 and a restore
 * rate of two requests every second.
 * </pre>
 * FBA Inbound/Inventory/Outbond API 共同享有 30 个最高请求, 每秒恢复 2 个请求
 * User: wyattpan
 * Date: 9/11/12
 * Time: 5:46 PM
 */
public class FBA {

    private static final Map<String, FBAInboundServiceMWSClient> CLIENT_CACHE = new HashMap<String, FBAInboundServiceMWSClient>();

    public static FBAShipment plan(Account account, ProcureUnit unit)
            throws FBAInboundServiceMWSException {
        FBAShipment fbaShipment = new FBAShipment();
        CreateInboundShipmentPlanRequest plan = new CreateInboundShipmentPlanRequest();

        // Merchant Id
        plan.setSellerId(account.merchantId);
        // 自贴标
        plan.setLabelPrepPreference("SELLER_LABEL");
        // 我们的联系地址
        plan.setShipFromAddress(Account.address(account.type));

        // 要发送的货物
        plan.setInboundShipmentPlanRequestItems(new InboundShipmentPlanRequestItemList(
                Arrays.asList(FBA.procureUnitToInboundShipmentPlanItems(unit))
        ));

        CreateInboundShipmentPlanResponse response = client(account)
                .createInboundShipmentPlan(plan);
        CreateInboundShipmentPlanResult result = response.getCreateInboundShipmentPlanResult();

        if(result.isSetInboundShipmentPlans()) {
            InboundShipmentPlanList planList = result.getInboundShipmentPlans();
            List<InboundShipmentPlan> members = planList.getMember();

            InboundShipmentPlan member = null;
            StringBuilder msg = new StringBuilder();

            /**
             * 对产生回来的 PLAN 做检查, 如果所有产生的 PLAN 中没有申请的 MSKU, 则报告异常.
             */
            for(InboundShipmentPlan planFba : members) {
                try {
                    List<InboundShipmentPlanItem> itemMember = planFba.getItems().getMember();
                    Map<String, InboundShipmentPlanItem> inboundItemMap = new HashMap<String, InboundShipmentPlanItem>();
                    for(InboundShipmentPlanItem inboundPlanItem : itemMember) {
                        inboundItemMap.put(inboundPlanItem.getSellerSKU(), inboundPlanItem);
                    }

                    if(!inboundItemMap.containsKey(fixHistoryMSKU(unit.selling.merchantSKU))) {
                        msg.append("PLAN 不存在 MSKU ").append(unit.selling.merchantSKU);
                    } else {
                        if(StringUtils.isBlank(unit.selling.fnSku)) {
                            unit.selling.fnSku = inboundItemMap
                                    .get(fixHistoryMSKU(unit.selling.merchantSKU))
                                    .getFulfillmentNetworkSKU();
                        }
                        member = planFba;
                    }

                } catch(Exception e) {
                    //ignore
                }
            }
            if(member == null)
                throw new FastRuntimeException(msg.toString());

            fbaShipment.account = account;
            fbaShipment.shipmentId = member.getShipmentId();
            fbaShipment.labelPrepType = member.getLabelPrepType();
            fbaShipment.units.add(unit);

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
        } else {
            Webs.systemMail("{WARN} FBAShipment Plan Error! " + Dates.date2Date(),
                    "创建 FBAShipment 失败.");
            throw new FBAInboundServiceMWSException("创建 FBA Plan 失败.");
        }
        return fbaShipment;
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
    public static FBAShipment.S create(FBAShipment fbashipment)
            throws FBAInboundServiceMWSException {
        if(fbashipment.state != FBAShipment.S.PLAN) return fbashipment.state;
        //TODO effects: 计算 FBA title 算法需要调整
        StringBuilder fbaTitle = new StringBuilder();
        Set<Shipment> shipments = new HashSet<Shipment>();
        int qty = 0;
        for(ProcureUnit unit : fbashipment.units) {
            for(ShipItem item : unit.shipItems) {
                shipments.add(item.shipment);
            }
            qty += unit.qty();
        }
        fbaTitle.append("总共运输数量为 ").append(qty).append(" 并关联 ");
        for(Shipment shipment : shipments) {
            fbaTitle.append(shipment.id).append(",");
        }
        fbaTitle.append(" 运输单");

        CreateInboundShipmentRequest create = new CreateInboundShipmentRequest();
        create.setSellerId(fbashipment.account.merchantId);
        create.setShipmentId(fbashipment.shipmentId);
        create.setInboundShipmentHeader(new InboundShipmentHeader(fbaTitle.toString(),
                Account.address(fbashipment.account.type), fbashipment.centerId,
                FBAShipment.S.WORKING.name(), fbashipment.labelPrepType));
        // 设置 items
        //TODO effect: 创建 FBA 的算法需要调整
        List<InboundShipmentItem> items = FBA.procureUnitsToInboundShipmentItems(fbashipment.units);
        create.setInboundShipmentItems(new InboundShipmentItemList(items));

        CreateInboundShipmentResponse response = client(fbashipment.account)
                .createInboundShipment(create);
        if(response.isSetCreateInboundShipmentResult()) {
            fbashipment.title = fbaTitle.toString();
            fbashipment.createAt = new Date();
            return FBAShipment.S.WORKING;
        }
        return FBAShipment.S.PLAN;
    }


    /**
     * 更新 FBA Shipment, 包括 Amazon FBA Shipment 的状态, ShipItem 的数量 ; 指定 InboundShipmentItem
     *
     * @param fbaShipment
     * @param state
     * @return
     * @throws FBAInboundServiceMWSException
     */
    public static FBAShipment.S update(FBAShipment fbaShipment, FBAShipment.S state) throws
            FBAInboundServiceMWSException {
        Validate.notNull(state);
        // 只允许 WORKING 与 SHIPPED 状态的进行修改
        if(Arrays.asList(FBAShipment.S.PLAN, state).contains(fbaShipment.state))
            return fbaShipment.state;

        UpdateInboundShipmentRequest update = new UpdateInboundShipmentRequest();
        update.setSellerId(fbaShipment.account.merchantId);
        update.setShipmentId(fbaShipment.shipmentId);
        update.setInboundShipmentHeader(new InboundShipmentHeader(fbaShipment.title,
                Account.address(fbaShipment.account.type), fbaShipment.centerId, state.name(),
                fbaShipment.labelPrepType));

        List<InboundShipmentItem> items = FBA.procureUnitsToInboundShipmentItems(fbaShipment.units);
        update.setInboundShipmentItems(new InboundShipmentItemList(items));

        UpdateInboundShipmentResponse response = client(fbaShipment.account)
                .updateInboundShipment(update);
        if(response.isSetUpdateInboundShipmentResult())
            fbaShipment.state = state;
        return fbaShipment.state;
    }

    /**
     * 根据 FBAShipment 获取 Amazon 上的状态.
     *
     * @param shipmentIds 此账户相关的 FBA ShipmentId
     * @param account     请求的账户
     * @return shipmentId : {._1:state, ._2:centerId, ._3:shipmentName}
     */
    public static Map<String, F.T3<String, String, String>> listShipments(List<String> shipmentIds,
                                                                          Account account)
            throws FBAInboundServiceMWSException {
        Validate.notNull(shipmentIds);
        Validate.notNull(account);
        // Amazon 提供的这个 API 最多一次查看 50 个 FBA 的状态, 但最多进行 20 个, 避免翻页
        Validate.isTrue(shipmentIds.size() <= 20,
                "检查 Shipments 的时候, ShipmentIds 的数量必须小于 20 当前数量 " + shipmentIds.size() + ".");
        Validate.isTrue(shipmentIds.size() > 0, "需要至少一个 ShipmentId..");
        ListInboundShipmentsRequest listShipments = new ListInboundShipmentsRequest();
        listShipments.setSellerId(account.merchantId);
        listShipments.setShipmentIdList(new ShipmentIdList(shipmentIds));
        ListInboundShipmentsResponse response = client(account).listInboundShipments(listShipments);
        List<InboundShipmentInfo> inbounds = response.getListInboundShipmentsResult()
                .getShipmentData().getMember();

        Map<String, F.T3<String, String, String>> shipmentsT3 = new HashMap<String, F.T3<String, String, String>>();
        for(InboundShipmentInfo info : inbounds) {
            // Amazon 对于重复提交的 FBA ShipmentId 不会做限制, 所以有过的信息不需要再记录
            if(shipmentsT3.containsKey(info.getShipmentId()))
                continue;
            shipmentsT3.put(info.getShipmentId(),
                    new F.T3<String, String, String>(info.getShipmentStatus(),
                            info.getDestinationFulfillmentCenterId(), info.getShipmentName()));
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
    public static Map<String, F.T2<Integer, Integer>> listShipmentItems(String shipmentId,
                                                                        Account acc)
            throws FBAInboundServiceMWSException {
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
        List<InboundShipmentItem> inboundItems = response.getListInboundShipmentItemsResult()
                .getItemData().getMember();
        for(InboundShipmentItem item : inboundItems) {
            // 进入系统内 msku 全变成大写
            if(fetchItems.containsKey(item.getSellerSKU().toUpperCase())) continue;
            fetchItems.put(item.getSellerSKU().toUpperCase(),
                    new F.T2<Integer, Integer>(item.getQuantityReceived(),
                            item.getQuantityShipped()));
        }
        return fetchItems;
    }

    /**
     * 将 采购计划 转换为 FBA 的提交 Request Plan Item
     *
     * @param unit
     * @return
     */
    public static InboundShipmentPlanRequestItem procureUnitToInboundShipmentPlanItems(
            ProcureUnit unit) {
        return new InboundShipmentPlanRequestItem(
                fixHistoryMSKU(unit.selling.merchantSKU),
                null,
                null,
                unit.qty());
    }

    /**
     * 将 List 采购计划 转换为 FBA 的提交 Request Create Item
     *
     * @param units
     * @return
     */
    private static List<InboundShipmentItem> procureUnitsToInboundShipmentItems
    (List<ProcureUnit> units) {

        List<InboundShipmentItem> items = new ArrayList<InboundShipmentItem>();
        for(ProcureUnit unit : units) {
            items.add(new InboundShipmentItem(null,
                    fixHistoryMSKU(unit.selling.merchantSKU),
                    null,
                    unit.qty(),
                    null));
        }
        return items;
    }


    /**
     * 历史的 msku 的问题兼容
     *
     * @return
     */
    private static String fixHistoryMSKU(String msku) {
        if(StringUtils.equals(msku, "80-QW1A56-BE")) {
            return "80-qw1a56-be";
        }
        return msku;
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
                client = new FBAInboundServiceMWSClient(acc.accessKey, acc.token, "elcuk2", "1.0",
                        config);
                CLIENT_CACHE.put(key, client);
            }
        }
        return client;
    }


}
