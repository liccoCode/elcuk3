package amazon;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.serializer.SerializerFeature;
import com.amazonservices.mws.FulfillmentInboundShipment._2010_10_01.FBAInboundServiceMWSClient;
import com.amazonservices.mws.FulfillmentInboundShipment._2010_10_01.FBAInboundServiceMWSConfig;
import com.amazonservices.mws.FulfillmentInboundShipment._2010_10_01.FBAInboundServiceMWSException;
import com.amazonservices.mws.FulfillmentInboundShipment._2010_10_01.MWSEndpoint;
import com.amazonservices.mws.FulfillmentInboundShipment._2010_10_01.model.*;
import helper.Dates;
import helper.J;
import models.market.Account;
import models.market.M;
import org.junit.Before;
import org.junit.Test;
import play.test.UnitTest;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * Created by IntelliJ IDEA.
 * User: wyattpan
 * Date: 9/12/12
 * Time: 9:54 AM
 */
public class FWSTest extends UnitTest {
    FBAInboundServiceMWSClient client;
    String sellerId = "AJUR3R8UN71M4";//uk
//    String sellerId = "A3RJNQO8PQ21MT";//us

    @Before
    public void client() {
        String accessKey = "AKIAI6EBPJLG64HWDBGQ";//uk
        String secrityKey = "3e3TWsDOt6KBfubRzEIRWZuhSuxa+aRGWvnnjJuf";//uk
//        String accessKey = "AKIAIFWFCKKS5EAB3KZA";//us
//        String secrityKey = "4wruCQj0t7ZY9BPKqZg2qH5qAXdvJBvLRo0h3trD";//us
        FBAInboundServiceMWSConfig config = new FBAInboundServiceMWSConfig();

        // 设置服务器地址
        config.setServiceURL(MWSEndpoint.UK.toString());
//        config.setServiceURL(MWSEndpoint.US.toString());

        try {

            client = new FBAInboundServiceMWSClient(accessKey, secrityKey, "elcuk2", "1.0", config);
        } catch(Exception e) {
            e.printStackTrace();
            System.exit(0);
        }
    }

    /*
    * ShipmentId: [FBA5KV4JG]
    * FBA CenterId: [LTN2]
    ShipToAddress: {
    	"addressLine1":"Boundary Way",
    	"city":"Hemel Hempstead",
    	"countryCode":"GB",
    	"name":"Amazon.co.uk",
    	"postalCode":"HP27LF",
    	"setAddressLine1":true,
    	"setAddressLine2":false,
    	"setCity":true,
    	"setCountryCode":true,
    	"setDistrictOrCounty":false,
    	"setName":true,
    	"setPostalCode":true,
    	"setStateOrProvinceCode":true,
    	"stateOrProvinceCode":"Hertfordshire"
    }
    LabelPrepType: SELLER_LABEL
    Item:
    {
    	"fulfillmentNetworkSKU":"X0002RM49T",
    	"quantity":2,
    	"sellerSKU":"10HTCEVO3D-1900S",
    	"setFulfillmentNetworkSKU":true,
    	"setQuantity":true,
    	"setSellerSKU":true
    }
    {
    	"fulfillmentNetworkSKU":"X0002RM4AD",
    	"quantity":3,
    	"sellerSKU":"10HTCG14-1900S",
    	"setFulfillmentNetworkSKU":true,
    	"setQuantity":true,
    	"setSellerSKU":true
    }
    ==========================[4d7ce6f9-6cc1-40b2-9772-60db9806cf45]================================
     */

    /**
     * 向 Amazon 申请一个 Inbound Plan, 即时返回
     * 需要考虑一个问题:
     * Plan 有可能返回多个不同地址的 Amazon Shipment, 这种情况该如何解决.
     *
     * @throws FBAInboundServiceMWSException
     */
//    @Test
    public void plan() throws FBAInboundServiceMWSException {
        CreateInboundShipmentPlanRequest plan = new CreateInboundShipmentPlanRequest();

        // Merchant Id
        plan.setSellerId(sellerId);
        // 自贴标
        plan.setLabelPrepPreference("SELLER_LABEL");
        // 我们的联系地址
        plan.setShipFromAddress(address());
        // 要发送的货物
        plan.setInboundShipmentPlanRequestItems(new InboundShipmentPlanRequestItemList(planItems()));

        CreateInboundShipmentPlanResponse response = client.createInboundShipmentPlan(plan);
        CreateInboundShipmentPlanResult result = response.getCreateInboundShipmentPlanResult();
        if(result.isSetInboundShipmentPlans()) {
            InboundShipmentPlanList planList = result.getInboundShipmentPlans();
            List<InboundShipmentPlan> members = planList.getMember();
            for(InboundShipmentPlan member : members) {
                System.out.println("* ShipmentId: [" + member.getShipmentId() + "]");
                System.out.println("* FBA CenterId: [" + member.getDestinationFulfillmentCenterId() + "]");
                System.out.println("ShipToAddress: " + JSON.toJSONString(member.getShipToAddress(), SerializerFeature.PrettyFormat));
                System.out.println("LabelPrepType: " + member.getLabelPrepType());
                InboundShipmentPlanItemList itemList = member.getItems();
                List<InboundShipmentPlanItem> itemMembers = itemList.getMember();
                System.out.println("Item: ");
                for(InboundShipmentPlanItem item : itemMembers) {
                    System.out.println(JSON.toJSONString(item, SerializerFeature.PrettyFormat));
                }
            }

            System.out.println("==========================[" + response.getResponseMetadata().getRequestId() + "]================================");
        }
    }


    //        @Test
    // 向 Amazon 正式创建一个 Shipment , 即时返回
    public void create() throws FBAInboundServiceMWSException {
        CreateInboundShipmentRequest create = new CreateInboundShipmentRequest();
        create.setSellerId(sellerId);
        create.setShipmentId("FBAMHKX39");
        create.setInboundShipmentHeader(new InboundShipmentHeader("测试运输,中文" + Dates.date2DateTime(), address(), "PHX6", "WORKING", "SELLER_LABEL"));
        create.setInboundShipmentItems(new InboundShipmentItemList(items()));

        CreateInboundShipmentResponse response = client.createInboundShipment(create);
        System.out.println("* ShipmentId: [" + response.getCreateInboundShipmentResult().getShipmentId() + "]");
        System.out.println("==========================[" + response.getResponseMetadata().getRequestId() + "]================================");
    }

    //    @Test
    public void update() throws FBAInboundServiceMWSException {
        UpdateInboundShipmentRequest update = new UpdateInboundShipmentRequest();
        update.setSellerId(sellerId);
        update.setShipmentId("FBAMHKX39");
//        update.setInboundShipmentHeader(new InboundShipmentHeader("测试, 换一串文字 " + Dates.date2DateTime(), address(), "LTN2", "SHIPPED", "SELLER_LABEL"));
        update.setInboundShipmentItems(new InboundShipmentItemList(items()));
        update.setInboundShipmentHeader(new InboundShipmentHeader("通过 API 能够跳过 FBA 的分布式创建. 我 CANCELLED 了, 库存如何释放? " + Dates.date2DateTime(), address(), "PHX6", "CANCELLED", "SELLER_LABEL"));

        UpdateInboundShipmentResponse response = client.updateInboundShipment(update);
        System.out.println("* ShipmentId: [" + response.getUpdateInboundShipmentResult().getShipmentId() + "]");
        System.out.println("==========================[" + response.getResponseMetadata().getRequestId() + "]================================");
    }


    //    @Test
    public void fetchFBAShipmentItems() throws FBAInboundServiceMWSException {
        ListInboundShipmentItemsRequest request = new ListInboundShipmentItemsRequest();
        // 查看 WORKING 状态的, RECIVING 状态的和 CLOSE 状态的
        request.setShipmentId("FBA5YTXH0");
        request.setSellerId(sellerId);
        ListInboundShipmentItemsResponse response = client.listInboundShipmentItems(request);

        InboundShipmentItemList items = response.getListInboundShipmentItemsResult().getItemData();
        List<InboundShipmentItem> inboundItems = items.getMember();
        for(InboundShipmentItem item : inboundItems) {
            System.out.println(J.json(item));
        }
    }

    @Test
    public void fetchFBAShipments() throws FBAInboundServiceMWSException {
        ListInboundShipmentsRequest request = new ListInboundShipmentsRequest();
        request.setSellerId(sellerId);
        request.setShipmentIdList(new ShipmentIdList(Arrays.asList("FBA5YTXH0", "FBA5VSVJ5", "FBA5MW91C", "FBA5NTVCH", /*duplicate*/"FBA5YTXH0")));

        ListInboundShipmentsResponse response = client.listInboundShipments(request);
        InboundShipmentList inbounds = response.getListInboundShipmentsResult().getShipmentData();
        List<InboundShipmentInfo> infos = inbounds.getMember();
        for(InboundShipmentInfo info : infos) {
            System.out.println(J.json(info));
        }
    }


    private Address address() {
        return Account.address(M.AMAZON_UK);
    }

    private List<InboundShipmentItem> items() {
        List<InboundShipmentItem> items = new ArrayList<InboundShipmentItem>();
        items.add(new InboundShipmentItem(null, "71KDFHD7-BHSPU,656605389363", null, 300, null));
//        items.add(new InboundShipmentItem(null, "80DBK12000-AB", null, 3, null));
        return items;
    }

    private List<InboundShipmentPlanRequestItem> planItems() {
        List<InboundShipmentPlanRequestItem> items = new ArrayList<InboundShipmentPlanRequestItem>();
        items.add(new InboundShipmentPlanRequestItem("71KDFHD7-BHSPU,656605389363", null, null, 2));
//        items.add(new InboundShipmentPlanRequestItem("80DBK12000-AB", null, null, 3));
        return items;
    }
}
