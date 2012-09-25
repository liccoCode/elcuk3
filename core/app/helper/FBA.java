package helper;

import com.amazonservices.mws.FulfillmentInboundShipment._2010_10_01.FBAInboundServiceMWSClient;
import com.amazonservices.mws.FulfillmentInboundShipment._2010_10_01.FBAInboundServiceMWSConfig;
import com.amazonservices.mws.FulfillmentInboundShipment._2010_10_01.FBAInboundServiceMWSException;
import com.amazonservices.mws.FulfillmentInboundShipment._2010_10_01.MWSEndpoint;
import com.amazonservices.mws.FulfillmentInboundShipment._2010_10_01.model.*;
import models.ElcukRecord;
import models.market.Account;
import models.market.Selling;
import models.procure.FBAShipment;
import models.procure.ProcureUnit;
import models.procure.ShipItem;
import models.procure.Shipment;
import org.apache.commons.lang.StringUtils;
import org.apache.commons.lang.Validate;
import play.i18n.Messages;

import java.util.*;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.regex.Matcher;
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

            // TODO 这里进行分仓的情况需要进行处理
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
            fbaShipment.postalCode = shipToAddress.getPostalCode();

            // Items
            InboundShipmentPlanItemList itemList = member.getItems();
            List<InboundShipmentPlanItem> itemMembers = itemList.getMember();
            for(ShipItem spitm : shipment.items) {
                for(InboundShipmentPlanItem item : itemMembers) {
                    if(item.getSellerSKU().equals(spitm.unit.selling.merchantSKU)) {
                        spitm.fulfillmentNetworkSKU = item.getFulfillmentNetworkSKU();
                        spitm.updateSellingFNSku();
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
                        String.format("运输单 %s 创建了多个 FBAShipment (%s) 需要去 Amazon 后台与系统中删除多余的. 让多余的重新创建运输单",
                                shipment.id, StringUtils.join(shipmentIds, ",")));
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
        String fbaTitle = String.format("%s %s", fbashipment.shipment.title(), Dates.date2DateTime());
        CreateInboundShipmentRequest create = new CreateInboundShipmentRequest();
        create.setSellerId(fbashipment.account.merchantId);
        create.setShipmentId(fbashipment.shipmentId);
        create.setInboundShipmentHeader(new InboundShipmentHeader(fbaTitle,
                Account.address(fbashipment.account.type), fbashipment.centerId, FBAShipment.S.WORKING.name(), fbashipment.labelPrepType));
        // 设置 items
        List<InboundShipmentItem> items = FBA.shipItemsToInboundShipmentItems(fbashipment.shipment.items);
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
     *
     * @param fbaShipment
     * @param state
     * @throws FBAInboundServiceMWSException
     */
    public static FBAShipment.S update(FBAShipment fbaShipment, FBAShipment.S state) throws FBAInboundServiceMWSException {
        return FBA.update(fbaShipment, fbaShipment.shipment.items, state);
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
        if(fbaShipment.state == FBAShipment.S.PLAN) return fbaShipment.state;
        UpdateInboundShipmentRequest update = new UpdateInboundShipmentRequest();
        update.setSellerId(fbaShipment.account.merchantId);
        update.setShipmentId(fbaShipment.shipmentId);
        update.setInboundShipmentHeader(new InboundShipmentHeader(fbaShipment.title,
                Account.address(fbaShipment.account.type), fbaShipment.centerId, state.name(), fbaShipment.labelPrepType));

        List<InboundShipmentItem> items = FBA.shipItemsToInboundShipmentItems(updateitems);
        update.setInboundShipmentItems(new InboundShipmentItemList(items));

        UpdateInboundShipmentResponse response = client(fbaShipment.account).updateInboundShipment(update);
        if(response.isSetUpdateInboundShipmentResult()) {
            fbaShipment.state = state;
            return fbaShipment.state;
        }
        return fbaShipment.state;
    }


    /**
     * 将 Shipment 运输的 ShipItems 转换成为 FBA 的 InboundShipmentItem
     * - 在转换的时候会检查 ShipItem 集合中是否有 msku 一样的, 如果一样, 则数量自动累计到一个 InboundShipmentItem 中
     * - 数量为 0 原来要删除, 但有存在, 那么数量也累计
     *
     * @param shipitems
     * @return
     */
    private static List<InboundShipmentItem> shipItemsToInboundShipmentItems(List<ShipItem> shipitems) {
        /**
         * 1. 根据 ShipItem 的 msku 组成 Map
         * 2. 遍历 Map 的 key 来生成 InboundShipmentItems
         */
        Map<String, AtomicInteger> shipitemsMap = new HashMap<String, AtomicInteger>();
        for(ShipItem item : shipitems) {
            if(shipitemsMap.containsKey(item.unit.selling.merchantSKU))
                shipitemsMap.get(item.unit.selling.merchantSKU).addAndGet(item.qty);
            else
                shipitemsMap.put(item.unit.selling.merchantSKU, new AtomicInteger(item.qty));
        }

        List<InboundShipmentItem> items = new ArrayList<InboundShipmentItem>();
        for(Map.Entry<String, AtomicInteger> entry : shipitemsMap.entrySet()) {
            // 如果 item.qty 为 0 Amazon 会自动删除这个 InboundItem
            items.add(new InboundShipmentItem(null, entry.getKey(), null, entry.getValue().get(), null));
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

    /**
     * 需要在 Amazon FBA 上删除的 items.
     * ps: 这里不会从 DB 加载完成的 ShipItem 数据, 构建的 ShipItem 仅仅包含删除所必须的参数.
     *
     * @param shipmentid
     * @return
     */
    public static List<ShipItem> deleteShipItem(String shipmentid) {
        List<ElcukRecord> records = ElcukRecord.find("fid=? AND action=?", shipmentid, Messages.get("shipment.cancelShip2")).fetch();
        Set<String> mskus = new HashSet<String>();
        //将[运输项目] 71ACA510-BHSPU 从[运输单] ...
        for(ElcukRecord record : records) {
            Matcher matcher = pattern.matcher(record.message);
            if(matcher.find()) {
                String mskustr = matcher.group(1);
                mskus.addAll(Arrays.asList(StringUtils.splitByWholeSeparator(mskustr, Webs.SPLIT)));
            }
        }
        List<ShipItem> shipItems = new ArrayList<ShipItem>();
        for(String msku : mskus) {
            ShipItem shipItem = new ShipItem();
            shipItem.unit = new ProcureUnit();
            shipItem.unit.selling = new Selling();
            shipItem.unit.selling.merchantSKU = msku;
            shipItem.qty = 0;
            shipItems.add(shipItem);
        }
        return shipItems;
    }
}
