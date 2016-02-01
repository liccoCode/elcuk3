package controllers;

import com.google.common.collect.Lists;
import controllers.api.SystemOperation;
import helper.HTTP;
import helper.OrderInvoiceFormat;
import jobs.promise.FinanceShippedPromise;
import models.ElcukRecord;
import models.finance.SaleFee;
import models.market.Account;
import models.market.Orderr;
import models.market.OrderInvoice;

import java.math.BigDecimal;

import models.view.Ret;
import models.view.post.OrderPOST;
import org.allcolor.yahp.converter.IHtmlToPdfTransformer;
import org.apache.http.NameValuePair;
import org.apache.http.message.BasicNameValuePair;
import play.libs.F;
import play.modules.pdf.PDF;
import play.mvc.Controller;
import play.mvc.With;

import java.util.*;

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
        List<Account> accs = Account.openedSaleAcc();
        if(p == null) p = new OrderPOST();
        List<Orderr> orders = p.query();
        render(p, orders, accs);
    }

    public static void show(String id) {
        Orderr ord = Orderr.findById(id);

        if(ord.orderrate() != 0) {
            OrderInvoice invoice = OrderInvoice.findById(id);
            if(invoice != null) {
                invoice.setprice();
                boolean check = invoice.checkInvoice(ord);
                if(!check) invoice = null;
            }

            F.T3<Float, Float, Float> amt = ord.amount();
            Float totalamount = amt._1;
            Float notaxamount = amt._2;
            Float tax = amt._3;
            if(invoice != null) {
                notaxamount = invoice.notaxamount;
                tax = new BigDecimal(totalamount).subtract(new BigDecimal(notaxamount)).setScale(2, 4).floatValue();
            }
            List<ElcukRecord> records = ElcukRecord.find(" fid = '" + id + "' and "
                    + "action like '%orderinvoice.invoice%' ORDER BY "
                    + " createAt  DESC")
                    .fetch();
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
        Orderr orderr = Orderr.findById(id);
        try {
            Account account = Account.findById(orderr.account.id);
            List<SaleFee> fees = new FinanceShippedPromise(account, orderr.market, Lists.newArrayList(orderr.orderId))
                    .now().get();
            renderJSON(new Ret(true, "总共处理 " + fees.size() + " 个费用"));
        } catch(Exception e) {
            renderJSON(new Ret(e.getMessage()));
        }
    }

    public static void refreshFeeById(String id) {
        Orderr orderr = Orderr.findById(id);
        orderr.feeflag = 0;
        orderr.save();
        List<NameValuePair> params = new ArrayList<NameValuePair>();
        params.add(new BasicNameValuePair("market", orderr.market.name()));
        params.add(new BasicNameValuePair("order_id", orderr.orderId));
        HTTP.post("http://"+models.OperatorConfig.getVal("rockendurl")+":4567/amazon_finance_find_by_order_id", params);
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
}
