package mws;

import com.amazonservices.mws.FulfillmentInboundShipment.FBAInboundServiceMWSClient;
import com.amazonservices.mws.FulfillmentInboundShipment.FBAInboundServiceMWSConfig;
import com.amazonservices.mws.FulfillmentInboundShipment.FBAInboundServiceMWSException;
import com.amazonservices.mws.FulfillmentInboundShipment.MWSEndpoint;
import com.amazonservices.mws.FulfillmentInboundShipment.model.*;
import helper.Webs;
import models.OperatorConfig;
import models.market.Account;
import models.procure.*;
import org.apache.commons.lang.StringUtils;
import org.apache.commons.lang.Validate;
import play.libs.F;
import play.utils.FastRuntimeException;

import java.util.*;
import java.util.stream.Collectors;

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

    private FBA() {
    }

    private static final Map<String, FBAInboundServiceMWSClient> CLIENT_CACHE = new HashMap<>();

    public static FBAShipment plan(Account account, ProcureUnit unit) throws FBAInboundServiceMWSException {
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
                Collections.singletonList(FBA.procureUnitToInboundShipmentPlanItems(unit))
        ));
        //单账户跨市场相关的处理
        plan.setMarketplace(unit.selling.market.amid().name());
        plan.setShipToCountryCode(unit.selling.market.country());

        CreateInboundShipmentPlanResponse response = client(account).createInboundShipmentPlan(plan);
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
                    Map<String, InboundShipmentPlanItem> inboundItemMap = new HashMap<>();
                    for(InboundShipmentPlanItem inboundPlanItem : itemMember) {
                        inboundItemMap.put(inboundPlanItem.getSellerSKU(), inboundPlanItem);
                    }

                    if(!inboundItemMap.containsKey(fixHistoryMSKU(unit.selling.merchantSKU))) {
                        msg.append("PLAN 不存在 MSKU ").append(unit.selling.merchantSKU);
                    } else {
                        if(StringUtils.isBlank(unit.selling.fnSku)) {
                            unit.selling.fnSku = inboundItemMap.get(fixHistoryMSKU(unit.selling.merchantSKU))
                                    .getFulfillmentNetworkSKU();
                        }
                        member = planFba;
                    }
                } catch(Exception e) {
                    //ignore
                }
            }
            if(member == null) {
                throw new FastRuntimeException(msg.toString());
            }

            fbaShipment.account = account;
            fbaShipment.shipmentId = member.getShipmentId();
            fbaShipment.labelPrepType = member.getLabelPrepType();
            fbaShipment.units.add(unit);

            fbaShipment.centerId = member.getDestinationFulfillmentCenterId();

            // FBA 仓库自适应
            Address fbaAddress = member.getShipToAddress();
            fbaShipment.fbaCenter = new FBACenter(fbaShipment.centerId, fbaAddress.getAddressLine1(),
                    fbaAddress.getAddressLine2(), fbaAddress.getCity(),
                    fbaAddress.getName(), fbaAddress.getCountryCode(),
                    fbaAddress.getStateOrProvinceCode(), fbaAddress.getPostalCode()
            ).createOrUpdate();
        } else {
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
    public static FBAShipment.S create(FBAShipment fbashipment, List<ProcureUnit> units)
            throws FBAInboundServiceMWSException {
        if(fbashipment.state != FBAShipment.S.PLAN) return fbashipment.state;
        //TODO effects: 计算 FBA title 算法需要调整
        StringBuilder fbaTitle = new StringBuilder();
        Set<Shipment> shipments = new HashSet<>();
        int qty = 0;
        for(ProcureUnit unit : units) {
            for(ShipItem item : unit.shipItems) {
                if(item.shipment == null) continue;
                shipments.add(item.shipment);
            }
            qty += unit.qtyForFba();
        }
        fbaTitle.append("总共运输数量为 ").append(qty);
        if(shipments.size() > 0) {
            fbaTitle.append(" 并关联 ");
            for(Shipment shipment : shipments) {
                fbaTitle.append(shipment.id).append(",");
            }
            fbaTitle.append(" 运输单");
        }

        CreateInboundShipmentRequest create = new CreateInboundShipmentRequest();
        create.setSellerId(fbashipment.account.merchantId);
        create.setShipmentId(fbashipment.shipmentId);
        create.setMarketplace(fbashipment.marketplace());
        InboundShipmentHeader header = new InboundShipmentHeader(fbaTitle.toString(),
                Account.address(fbashipment.account.type), fbashipment.centerId, false,
                FBAShipment.S.WORKING.name(), fbashipment.labelPrepType);
        //设置 IntendedBoxContentsSource(FBA 箱内包装数据) 为 FEED(only US)
        header.setIntendedBoxContentsSource("FEED");
        create.setInboundShipmentHeader(header);

        // 设置 items
        //TODO effect: 创建 FBA 的算法需要调整
        List<InboundShipmentItem> items = FBA.procureUnitsToInboundShipmentItems(units);
        create.setInboundShipmentItems(new InboundShipmentItemList(items));

        CreateInboundShipmentResponse response = client(fbashipment.account).createInboundShipment(create);
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
        if(Arrays.asList(FBAShipment.S.PLAN, state).contains(fbaShipment.state)) {
            return fbaShipment.state;
        }
        UpdateInboundShipmentResponse response = updateFbaInboundCartonContents(fbaShipment, state);
        if(response.isSetUpdateInboundShipmentResult())
            fbaShipment.state = state;
        return fbaShipment.state;
    }

    /**
     * 更新 FBA Shipment
     *
     * @param fbaShipment
     */
    public static UpdateInboundShipmentResponse updateFbaInboundCartonContents(FBAShipment fbaShipment,
                                                                               FBAShipment.S state) {
        UpdateInboundShipmentRequest update = new UpdateInboundShipmentRequest();
        update.setSellerId(fbaShipment.account.merchantId);
        update.setShipmentId(fbaShipment.shipmentId);
        update.setMarketplace(fbaShipment.marketplace());
        InboundShipmentHeader header = new InboundShipmentHeader(fbaShipment.title,
                Account.address(fbaShipment.account.type), fbaShipment.centerId, false, state.name(),
                fbaShipment.labelPrepType);
        //设置 IntendedBoxContentsSource(FBA 箱内包装数据) 为 FEED(only US)
        header.setIntendedBoxContentsSource("FEED");
        update.setInboundShipmentHeader(header);

        List<InboundShipmentItem> items = FBA.procureUnitsToInboundShipmentItems(fbaShipment.units);
        update.setInboundShipmentItems(new InboundShipmentItemList(items));
        return client(fbaShipment.account).updateInboundShipment(update);
    }

    /**
     * 提交运输信息给 Amazon
     * <p>
     * Carrier 和 Tracking numbers 和 ShipmentType
     *
     * @return
     */
    public static void putTransportContent(FBAShipment fbaShipment, Shipment shipment) throws
            FBAInboundServiceMWSException {
        PutTransportContentRequest request = new PutTransportContentRequest();
        request.setSellerId(fbaShipment.account.merchantId);
        request.setShipmentId(fbaShipment.shipmentId);
        request.setIsPartnered(false);
        request.setShipmentType(shipmentType(shipment.type));
        request.setTransportDetails(fbaShipment.transportDetails(shipment));
        PutTransportContentResponse response = client(fbaShipment.account).putTransportContent(request);
    }

    /**
     * 提交运输信息时用到的 ShipmentType
     * <p>
     * ShipmentType values:
     * 1. SP – Small Parcel
     * 2. LTL – Less Than Truckload/Full Truckload (LTL/FTL)
     *
     * @param shipType
     * @return
     */
    public static String shipmentType(Shipment.T shipType) {
        if(shipType == null) return null;
        switch(shipType) {
            case EXPRESS:
                return "SP";
            case AIR:
            case SEA:
                return "LTL";
            default:
                return "SP";
        }
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

        Map<String, F.T3<String, String, String>> shipmentsT3 = new HashMap<>();
        for(InboundShipmentInfo info : inbounds) {
            // Amazon 对于重复提交的 FBA ShipmentId 不会做限制, 所以有过的信息不需要再记录
            if(shipmentsT3.containsKey(info.getShipmentId()))
                continue;
            shipmentsT3.put(info.getShipmentId(), new F.T3<>(
                    info.getShipmentStatus(), info.getDestinationFulfillmentCenterId(), info.getShipmentName())
            );
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
    public static Map<String, F.T2<Integer, Integer>> listShipmentItems(String shipmentId, Account acc)
            throws FBAInboundServiceMWSException {
        /**
         * item.getSellerSKU();
         * item.getQuantityShipped();
         * item.getQuantityReceived();
         */
        Map<String, F.T2<Integer, Integer>> fetchItems = new HashMap<>();
        ListInboundShipmentItemsRequest request = new ListInboundShipmentItemsRequest();
        request.setShipmentId(shipmentId);
        request.setSellerId(acc.merchantId);
        ListInboundShipmentItemsResponse response = client(acc).listInboundShipmentItems(request);
        List<InboundShipmentItem> inboundItems = response.getListInboundShipmentItemsResult().getItemData().getMember();
        for(InboundShipmentItem item : inboundItems) {
            // 进入系统内 msku 全变成大写
            if(fetchItems.containsKey(item.getSellerSKU().toUpperCase())) continue;
            fetchItems.put(item.getSellerSKU().toUpperCase(),
                    new F.T2<>(item.getQuantityReceived(), item.getQuantityShipped())
            );
        }
        return fetchItems;
    }

    /**
     * 将 采购计划 转换为 FBA 的提交 Request Plan Item
     *
     * @param unit
     * @return
     */
    public static InboundShipmentPlanRequestItem procureUnitToInboundShipmentPlanItems(ProcureUnit unit) {
        return new InboundShipmentPlanRequestItem(
                fixHistoryMSKU(unit.selling.merchantSKU),
                null,
                null,
                unit.qtyForFba(),
                null
        );//.withPrepDetailsList(new PrepDetailsList(Collections.singletonList(new PrepDetails("Labeling", "SELLER")))
    }

    /**
     * 将 List 采购计划 转换为 FBA 的提交 Request Create Item
     *
     * @param units
     * @return
     */
    private static List<InboundShipmentItem> procureUnitsToInboundShipmentItems(List<ProcureUnit> units) {
        return units.stream().map(unit -> new InboundShipmentItem(
                null,
                fixHistoryMSKU(unit.selling.merchantSKU),
                null,
                unit.qtyForFba(),
                null,
                null,
                null)).collect(Collectors.toList());
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
        if(CLIENT_CACHE.containsKey(key)) {
            client = CLIENT_CACHE.get(key);
        } else {
            synchronized(CLIENT_CACHE) {
                if(CLIENT_CACHE.containsKey(key)) return CLIENT_CACHE.get(key);
                FBAInboundServiceMWSConfig config = new FBAInboundServiceMWSConfig();
                // 设置服务器地址
                switch(acc.type) {
                    case AMAZON_CA:
                    case AMAZON_MX:
                    case AMAZON_US:
                        config.setServiceURL("https://mws.amazonservices.com");
                        break;
                    case AMAZON_UK:
                    case AMAZON_IT:
                    case AMAZON_FR:
                    case AMAZON_ES:
                    case AMAZON_DE:
                        config.setServiceURL("https://mws-eu.amazonservices.com");
                        break;
                    case AMAZON_JP:
                        config.setServiceURL("https://mws.amazonservices.jp");
                        break;
                    case AMAZON_AU:
                        config.setServiceURL("https://mws.amazonservices.com.au");
                        break;
                    case AMAZON_IN:
                        config.setServiceURL("https://mws.amazonservices.in");
                        break;
                    default:
                        throw new UnsupportedOperationException("不支持的 FBA 地址");
                }
                client = new FBAInboundServiceMWSClient(acc.accessKey, acc.token, OperatorConfig.getVal("brandname"),
                        "1.0",
                        config);
                CLIENT_CACHE.put(key, client);
            }
        }
        return client;
    }

    //BEGIN GENERATED CODE
    public enum FBA_ERROR_TYPE {
        LOCKED {
            @Override
            public String message() {
                return "FBA 已经被锁定！";
            }
        },
        IN_TRANSIT {
            @Override
            public String message() {
                return "运输商已经向 Amazon fulfillment center 报告接收到了运输信息！";
            }
        },
        INVALID_STATUS_CHANGE {
            @Override
            public String message() {
                return "物流人员没有通过系统进行开始运输而手动在 Amazon 后台操作了 FBA.";
            }
        },
        UNKNOWN_SKU {
            @Override
            public String message() {
                return "请检查 Selling 的 Merchant SKU 属性: "
                        + " 1. 格式是否正确？(正确的格式应该为 \"SKU,UPC\")"
                        + " 2. 是否能够在 Amazon sellercentral 上找到对应的产品?";
            }
        },
        MISSING_DIMENSIONS {
            @Override
            public String message() {
                return "Selling 在 Amazon sellercentral 中对应的产品的尺寸或单位错误.";
            }
        },
        ANDON_PULL_STRIKE_ONE {
            @Override
            public String message() {
                return "Amazon fulfillment center 中有其他 FBA 在入库时出现了错误.";
            }
        },
        NON_SORTABLE {
            @Override
            public String message() {
                return "请检查 Amazon sellercentral: "
                        + " 1. FBA 仓库剩余可用容量是否不足?"
                        + " 2. Selling 在 Amazon sellercentral 中对应的产品的尺寸或单位错误导致匹配的仓库类型错误.";
            }
        },
        NOT_ELIGIBLE_FC_FOR_ITEM {
            @Override
            public String message() {
                return "当前 FBA 的 FBACenter 暂时不可用，请等待或更换 FBA.";
            }
        },
        UNKNOWN_ERROR {
            @Override
            public String message() {
                return "出现了一个未知错误，请向开发部报告.";
            }
        },
        QUANTITIES {
            @Override
            public String message() {
                return "收货数量和出货数量不一致，请联系开发部";
            }
        };

        public abstract String message();
    }
    //END GENERATED CODE

    /**
     * 格式化 Amazon 报告的 FBA 相关的错误
     *
     * @param e
     * @return
     */
    public static FBA_ERROR_TYPE fbaErrorFormat(FBAInboundServiceMWSException e) {
        String errMsg = e.getMessage();
        if(errMsg.contains("Shipment is locked. No updates allowed")
                || errMsg.contains("Shipment is in locked status")) {
            return FBA_ERROR_TYPE.LOCKED;
        } else if(errMsg.contains("items and quantities that have been previously planned ")) {
            return FBA_ERROR_TYPE.QUANTITIES;
        } else if(errMsg.contains("FBA31004")) {
            return FBA_ERROR_TYPE.IN_TRANSIT;
        } else if(errMsg.contains("Invalid Status change")) {
            return FBA_ERROR_TYPE.INVALID_STATUS_CHANGE;
        } else if(errMsg.contains("NOT_IN_PRODUCT_CATALOG") || errMsg.contains("UNKNOWN_SKU")) {
            return FBA_ERROR_TYPE.UNKNOWN_SKU;
        } else if(errMsg.contains("MISSING_DIMENSIONS") || errMsg.contains("UNFULFILLABLE_IN_DESTINATION_MP")) {
            return FBA_ERROR_TYPE.MISSING_DIMENSIONS;
        } else if(errMsg.contains("ANDON_PULL_STRIKE_ONE")) {
            return FBA_ERROR_TYPE.ANDON_PULL_STRIKE_ONE;
        } else if(errMsg.contains("NON_SORTABLE") || errMsg.contains("SORTABLE")) {
            return FBA_ERROR_TYPE.NON_SORTABLE;
        } else if(errMsg.contains("NOT_ELIGIBLE_FC_FOR_ITEM")) {
            return FBA_ERROR_TYPE.NOT_ELIGIBLE_FC_FOR_ITEM;
        } else {
            Webs.systemMail("FBA 相关操作出现未知错误", Webs.s(e), Arrays.asList("even@easya.cc", "licco@easya.cc"));
            return FBA_ERROR_TYPE.UNKNOWN_ERROR;
        }
    }
}
