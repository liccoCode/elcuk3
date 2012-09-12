package helper;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.serializer.SerializerFeature;
import com.amazonservices.mws.FulfillmentInboundShipment._2010_10_01.FBAInboundServiceMWSClient;
import com.amazonservices.mws.FulfillmentInboundShipment._2010_10_01.FBAInboundServiceMWSConfig;
import com.amazonservices.mws.FulfillmentInboundShipment._2010_10_01.FBAInboundServiceMWSException;
import com.amazonservices.mws.FulfillmentInboundShipment._2010_10_01.MWSEndpoint;
import com.amazonservices.mws.FulfillmentInboundShipment._2010_10_01.model.*;
import models.market.Account;
import models.procure.FBAShipment;
import models.procure.ShipItem;
import models.procure.Shipment;
import org.apache.commons.lang.Validate;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Amazon Fulfillment WebService
 * User: wyattpan
 * Date: 9/11/12
 * Time: 5:46 PM
 */
public class FWS {

    private static final Map<String, FBAInboundServiceMWSClient> CLIENT_CACHE = new HashMap<String, FBAInboundServiceMWSClient>();

    /**
     * 根据 Shipment 向 Amazon 创建 FBAShipment plan
     *
     * @param shipment
     * @return
     */
    public static FBAShipment plan(Shipment shipment) throws FBAInboundServiceMWSException {
        CreateInboundShipmentPlanRequest plan = new CreateInboundShipmentPlanRequest();

        // Merchant Id
        plan.setSellerId(shipment.whouse.account.merchantId);
        // 自贴标
        plan.setLabelPrepPreference("SELLER_LABEL");
        // 我们的联系地址
        plan.setShipFromAddress(Account.address(shipment.whouse.account.type));

        // 要发送的货物
        List<InboundShipmentPlanRequestItem> planItems = new ArrayList<InboundShipmentPlanRequestItem>();
        for(ShipItem item : shipment.items) {
            planItems.add(new InboundShipmentPlanRequestItem(item.unit.selling.merchantSKU, null, null, item.qty));
        }

        plan.setInboundShipmentPlanRequestItems(new InboundShipmentPlanRequestItemList(planItems));
        CreateInboundShipmentPlanResponse response = client(shipment.whouse.account).createInboundShipmentPlan(plan);
        CreateInboundShipmentPlanResult result = response.getCreateInboundShipmentPlanResult();

        FBAShipment fbaShipment = new FBAShipment();
        if(result.isSetInboundShipmentPlans()) {
            InboundShipmentPlanList planList = result.getInboundShipmentPlans();
            List<InboundShipmentPlan> members = planList.getMember();

            InboundShipmentPlan member = members.get(0);

            fbaShipment.account = shipment.whouse.account;
            fbaShipment.shipment = shipment;

            fbaShipment.shipmentId = member.getShipmentId();
            fbaShipment.centerId = member.getDestinationFulfillmentCenterId();
            fbaShipment.labelPrepType = member.getLabelPrepType();

            Address shipToAddress = member.getShipToAddress();
            fbaShipment.addressLine1 = shipToAddress.getAddressLine1();
            fbaShipment.addressLine2 = shipToAddress.getAddressLine2();
            fbaShipment.city = shipToAddress.getCity();
            fbaShipment.name = shipToAddress.getName();
            fbaShipment.countryCode = shipToAddress.getCountryCode();
            fbaShipment.stateOrProvinceCode = shipToAddress.getStateOrProvinceCode();

            // Items
            InboundShipmentPlanItemList itemList = member.getItems();
            List<InboundShipmentPlanItem> itemMembers = itemList.getMember();
            for(ShipItem spitm : shipment.items) {
                for(InboundShipmentPlanItem item : itemMembers) {
                    if(item.getSellerSKU().equals(spitm.unit.selling.merchantSKU)) {
                        spitm.fulfillmentNetworkSKU = item.getFulfillmentNetworkSKU();
                        // 处理 Amazon 返回的数量与实际提交的数量不一样的情况
                        if(!item.getQuantity().equals(spitm.qty)) {
                            shipment.comment(String.format("%s %s %s diff qty (%s, %s)",
                                    spitm.id, item.getSellerSKU(), item.getFulfillmentNetworkSKU(), spitm.qty, item.getQuantity()));
                            spitm.qty = item.getQuantity();
                        }
                    }
                }
            }

            // 处理本来一个 Shipment 被 Amazon 分拆成不同 Shipment 的情况
            List<String> shipmentIds = new ArrayList<String>();
            for(InboundShipmentPlan planmember : members) shipmentIds.add(planmember.getShipmentId());
            if(members.size() > 1) {
                Webs.systemMail("{WARN} Multi FBAShipment Plan " + fbaShipment.shipmentId,
                        String.format("运输单 %s 创建了多个 FBAShipment 需要去 Amazon 后台与系统中删除多余的. 让多余的重新创建运输单", shipment.id));
            }
        } else {
            Webs.systemMail("{WARN} FBAShipment Plan Error! " + Dates.date2Date(), "创建 FBAShipment 失败.");
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
    public static FBAShipment.S create(FBAShipment fbashipment) throws FBAInboundServiceMWSException {
        if(fbashipment.state != FBAShipment.S.PLAN) return fbashipment.state;
        String fbaTitle = String.format("%s %s", fbashipment.shipment.title, Dates.date2DateTime());
        CreateInboundShipmentRequest create = new CreateInboundShipmentRequest();
        create.setSellerId(fbashipment.account.merchantId);
        create.setShipmentId(fbashipment.shipmentId);
        create.setInboundShipmentHeader(new InboundShipmentHeader(fbaTitle,
                Account.address(fbashipment.account.type), fbashipment.centerId, FBAShipment.S.WORKING.name(), fbashipment.labelPrepType));
        // 设置 items
        List<InboundShipmentItem> items = FWS.shipItemsToInboundShipmentItems(fbashipment.shipment.items);
        create.setInboundShipmentItems(new InboundShipmentItemList(items));

        CreateInboundShipmentResponse response = client(fbashipment.account).createInboundShipment(create);
        if(response.isSetCreateInboundShipmentResult()) {
            fbashipment.title = fbaTitle;
            return FBAShipment.S.WORKING;
        }
        return FBAShipment.S.PLAN;
    }


    /**
     * 更新 FBA Shipment, 包括 Amazon FBA Shipment 的状态, ShipItem 的数量
     * @param fbaShipment
     * @param state
     * @throws FBAInboundServiceMWSException
     */
    public static FBAShipment.S update(FBAShipment fbaShipment, FBAShipment.S state) throws FBAInboundServiceMWSException {
        Validate.notNull(state);
        // 只允许 WORKING 与 SHIPPED 状态的进行修改
        if(fbaShipment.state == FBAShipment.S.PLAN) return fbaShipment.state;
        UpdateInboundShipmentRequest update = new UpdateInboundShipmentRequest();
        update.setSellerId(fbaShipment.account.merchantId);
        update.setShipmentId(fbaShipment.shipmentId);
        update.setInboundShipmentHeader(new InboundShipmentHeader(fbaShipment.title,
                Account.address(fbaShipment.account.type), fbaShipment.centerId, state.name(), fbaShipment.labelPrepType));

        List<InboundShipmentItem> items = FWS.shipItemsToInboundShipmentItems(fbaShipment.shipment.items);
        update.setInboundShipmentItems(new InboundShipmentItemList(items));

        UpdateInboundShipmentResponse response = client(fbaShipment.account).updateInboundShipment(update);
        if(response.isSetUpdateInboundShipmentResult()) {
            fbaShipment.state = state;
            return fbaShipment.state;
        }
        return fbaShipment.state;
    }


    private static List<InboundShipmentItem> shipItemsToInboundShipmentItems(List<ShipItem> shipitems) {
        List<InboundShipmentItem> items = new ArrayList<InboundShipmentItem>();
        for(ShipItem item : shipitems) {
            items.add(new InboundShipmentItem(null, item.unit.selling.merchantSKU, null, item.qty, null));
        }
        return items;
    }


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
