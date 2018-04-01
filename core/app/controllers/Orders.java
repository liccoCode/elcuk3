package controllers;

import com.amazonservices.mws.finances.MWSFinancesServiceClient;
import com.amazonservices.mws.finances.model.ListFinancialEventsRequest;
import com.amazonservices.mws.finances.model.ListFinancialEventsResponse;
import controllers.api.SystemOperation;
import helper.Constant;
import helper.HTTP;
import helper.OrderInvoiceFormat;
import helper.Webs;
import models.ElcukRecord;
import models.User;
import models.finance.EbayFee;
import models.market.Account;
import models.market.EbayOrder;
import models.market.OrderInvoice;
import models.market.Orderr;
import models.product.Category;
import models.view.Ret;
import models.view.post.EbayOrderPost;
import models.view.post.OrderPOST;
import mws.MWSFinances;
import mws.MWSOrders;
import org.allcolor.yahp.converter.IHtmlToPdfTransformer;
import org.apache.http.NameValuePair;
import org.apache.http.message.BasicNameValuePair;
import play.db.jpa.GenericModel;
import play.libs.F;
import play.modules.pdf.PDF;
import play.mvc.Before;
import play.mvc.Controller;
import play.mvc.With;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import static play.modules.pdf.PDF.renderPDF;

/**
 * Created by IntelliJ IDEA.
 * User: Wyatt
 * Date: 12-1-6
 * Time: 下午4:02
 */
@With({GlobalExceptionHandler.class, Secure.class, SystemOperation.class})
public class Orders extends Controller {

    public static void index(OrderPOST p) {
        List<Account> accounts = Account.openedSaleAcc();
        User user = User.findById(Login.current().id);
        renderArgs.put("categories", user.categories);
        if(p == null) p = new OrderPOST();
        List<Orderr> orders = p.query();
        render(p, orders, accounts);
    }

    public static void indexEbay(EbayOrderPost p) {
        if(p == null) p = new EbayOrderPost();
        List<EbayOrder> orders = p.query();
        List<String> categoryIds = Category.categoryIds();
        render(p, orders, categoryIds);
    }

    public static void show(String id) {
        Orderr ord = Orderr.findById(id);
        notFoundIfNull(ord, "未找到相关订单,请稍后再来查看 : )");
        if(ord.orderrate() != 0) {
            OrderInvoice invoice = OrderInvoice.findById(id);
            if(invoice != null) {
                invoice.setprice();
                if(!invoice.checkInvoice(ord)) invoice = null;
            }
            F.T3<Float, Float, Float> amt = ord.amount();
            Float totalamount = amt._1;
            Float notaxamount = amt._2;
            Float tax = amt._3;
            if(invoice != null) {
                notaxamount = invoice.notaxamount;
                tax = new BigDecimal(totalamount)
                        .subtract(new BigDecimal(notaxamount))
                        .setScale(2, 4)
                        .floatValue();
            }
            List<ElcukRecord> records = ElcukRecord.find(" fid =? and "
                    + "action like '%orderinvoice.invoice%' ORDER BY createAt DESC ", id).fetch();
            OrderInvoiceFormat invoiceformat = OrderInvoice.invoiceformat(ord.market);
            String editaddress = ord.formataddress(invoiceformat.country);
            Date returndate = ord.returndate();
            //判断是否存在退款
            int isreturn = ord.isreturn();
            render(ord, totalamount, tax, notaxamount, invoice, records, editaddress, invoiceformat, returndate,
                    isreturn);
        } else
            render(ord);
    }

    public static void refreshFee(String id) {
        try {
            Orderr orderr = Orderr.findById(id);
            boolean flag = orderr.refreshFee();
            orderr.refresh();
            orderr.refreshESFee();
            renderJSON(new Ret(flag, "重新抓取费用成功！"));
        } catch(Exception e) {
            renderJSON(new Ret(false, e.getMessage()));
        }
    }

    public static void refreshFeeByEbay(String id) {
        EbayOrder orderr = EbayOrder.findById(id);
        orderr.fees.forEach(GenericModel::delete);
        try {
            Account account = Account.findById(orderr.account.id);
            MWSFinancesServiceClient client = MWSFinances.client(account, orderr.market);
            ListFinancialEventsRequest request = new ListFinancialEventsRequest();
            request.setSellerId(account.merchantId);
            request.setMWSAuthToken(account.token);
            request.setAmazonOrderId(id);
            ListFinancialEventsResponse response = client.listFinancialEvents(request);
            EbayFee.parseFinancesApiResult(response, orderr);
            flash.success("重新抓取费用成功");
            indexEbay(new EbayOrderPost());
        } catch(Exception e) {
            Webs.e(e);
            renderJSON(new Ret(false, e.getMessage()));
        }
    }

    public static void refreshOrder(String orderId) {
        Orderr orderr = Orderr.findById(orderId);
        try {
            MWSOrders.invokeListOrderItems(orderr);
            renderJSON(new Ret(true, "重新抓取订单明细成功！"));
        } catch(Exception e) {
            e.printStackTrace();
            renderJSON(new Ret(false, e.getMessage()));
        }
    }

    public static void refreshFeeById(String id) {
        Orderr orderr = Orderr.findById(id);
        orderr.feeflag = 0;
        orderr.save();
        List<NameValuePair> params = new ArrayList<>();
        params.add(new BasicNameValuePair("market", orderr.market.name()));
        params.add(new BasicNameValuePair("order_id", orderr.orderId));
        HTTP.post(System.getenv(Constant.ROCKEND_HOST) + "/amazon_finance_find_by_order_id", params);
        renderJSON(new Ret(true, "后台正在处理，请隔1分钟刷新此页面！"));
    }


    /**
     * 生成订单发票
     */
    public static void invoicepdf(OrderInvoice invoice) {
        Orderr ord = Orderr.findById(invoice.orderid);

        invoice.updateDate = new Date();
        invoice.updator = Secure.Security.connected();
        invoice.saveprice();
        if(invoice.europevat != null && invoice.europevat != OrderInvoice.VAT.EUROPE)
            invoice.europevat = OrderInvoice.VAT.NORMAL;
        invoice.save();
        new ElcukRecord("orderinvoice.invoice",
                String.format("销售发票 %s", invoice.europevat.label()), invoice.orderid).save();

        F.T3<Float, Float, Float> amt = ord.amount();
        Float totalamount = amt._1;

        OrderInvoiceFormat invoiceformat = OrderInvoice.invoiceformat(ord.market);
        final PDF.Options options = new PDF.Options();
        options.filename = invoiceformat.filename + invoice.orderid + ".pdf";
        options.pageSize = IHtmlToPdfTransformer.A3P;
        Float notaxamount = 0f;
        if(invoice.europevat == OrderInvoice.VAT.EUROPE) {
            notaxamount = totalamount;
        } else
            notaxamount = invoice.notaxamount;
        Float tax = new BigDecimal(totalamount).subtract(new BigDecimal(notaxamount)).setScale(2, 4).floatValue();


        renderPDF(options, ord, totalamount, notaxamount, tax, invoice, invoiceformat);
    }

    /**
     * 生成退货发票
     */
    public static void invoicereturnpdf(OrderInvoice invoice) {
        Orderr ord = Orderr.findById(invoice.orderid);

        if(!ord.refundmoney()) {
            flash.error("未全部退款!");
            Orders.show(ord.orderId);
        }
        new ElcukRecord("orderinvoice.invoice",
                String.format("%s %s", "退款发票", invoice.europevat.label()), invoice.orderid).save();

        F.T3<Float, Float, Float> amt = ord.amount();
        Float totalamount = amt._1;

        OrderInvoiceFormat invoiceformat = OrderInvoice.invoiceformat(ord.market);
        final PDF.Options options = new PDF.Options();
        options.filename = invoiceformat.filename + invoice.orderid;
        options.pageSize = IHtmlToPdfTransformer.A3P;
        Float notaxamount = 0f;
        if(invoice.europevat == OrderInvoice.VAT.EUROPE) {
            notaxamount = -1 * totalamount;
        } else
            notaxamount = invoice.notaxamount;
        Float tax = new BigDecimal(-1 * totalamount).subtract(new BigDecimal(notaxamount)).setScale(2,
                4).floatValue();
        Date returndate = ord.returndate();
        renderPDF(options, ord, totalamount, notaxamount, tax, invoice, invoiceformat, returndate);
    }

    @Before(only = {"btbOrderIndex", "createBtbOrderPage", "createBtbOrder", "updateBtbOrder"})
    public static void setUpShowPage() {
        List<String> categoryIds = Category.categoryIds();
        renderArgs.put("categorys", categoryIds);
    }

}
