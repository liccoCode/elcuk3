package controllers;

import com.google.common.collect.Lists;
import helper.DBUtils;
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
import org.apache.commons.lang.StringUtils;
import play.db.helper.SqlSelect;
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
@With({GlobalExceptionHandler.class, Secure.class})
public class Orders extends Controller {

    public static void index(OrderPOST p) {
        List<Account> accs = Account.openedSaleAcc();
        if(p == null) p = new OrderPOST();
        List<Orderr> orders = p.query();
        render(p, orders, accs);
    }

    public static void show(String id) {
        Orderr ord = Orderr.findById(id);

        OrderInvoice invoice = OrderInvoice.findById(id);
        if(invoice != null) invoice.setprice();

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

        if(!StringUtils.isBlank(ord.address)) {
            ord.address = ord.address.replace(",,", ",");
        }
        if(!StringUtils.isBlank(ord.address1)) {
            ord.address1 = ord.address1.replace(",,", ",");
        }
        if(ord.address != null && ord.address.indexOf(",") == 0) {
            ord.address = ord.address.substring(1, ord.address.length());
        }
        if(ord.address1 != null && ord.address1.indexOf(",") == 0) {
            ord.address1 = ord.address1.substring(1, ord.address.length());
        }
        String editaddress = ord.formataddress();
        OrderInvoiceFormat invoiceformat = OrderInvoice.invoiceformat(ord.market);

        render(ord, totalamount, tax, notaxamount, invoice, records, editaddress, invoiceformat);
    }

    public static void refreshFee(String id) {
        Orderr orderr = Orderr.findById(id);
        try {
            Account account = Account.findById(orderr.account.id);
            List<SaleFee> fees = new FinanceShippedPromise(
                    account, orderr.market, Lists.newArrayList(orderr.orderId)).now().get();
            renderJSON(new Ret(true, "总共处理 " + fees.size() + " 个费用"));
        } catch(Exception e) {
            renderJSON(new Ret(e.getMessage()));
        }

    }


    /**
     * 生成订单发票
     */
    public static void invoicede(OrderInvoice invoice) {
        Orderr ord = Orderr.findById(invoice.orderid);

        if(invoice.isreturn != 2) {
            invoice.updateDate = new Date();
            invoice.updator = Secure.Security.connected();
            invoice.saveprice();
            if(invoice.europevat != null && invoice.europevat != OrderInvoice.VAT.EUROPE)
                invoice.europevat = OrderInvoice.VAT.NORMAL;
            invoice.save();
            new ElcukRecord("orderinvoice.invoice",
                    String.format("%s %s", invoice.invoiceto, invoice.europevat.label()), invoice.orderid).save();
        } else {

            if(!ord.refundmoney()) {
                flash.error("未全部退款!");
                Orders.show(ord.orderId);
            }
            new ElcukRecord("orderinvoice.invoice",
                    String.format("%s  ", "退款发票"), invoice.orderid).save();
        }


        F.T3<Float, Float, Float> amt = ord.amount();
        Float totalamount = amt._1;
        if(invoice.isreturn == 2) totalamount = totalamount * -1;


        final PDF.Options options = new PDF.Options();
        options.filename = "Rechnung de" + invoice.orderid;
        options.pageSize = IHtmlToPdfTransformer.A3P;
        Float notaxamount = 0f;
        if(invoice.europevat == OrderInvoice.VAT.EUROPE || invoice.isreturn == 2) {
            notaxamount = totalamount;
        } else
            notaxamount = invoice.notaxamount;
        Float tax = new BigDecimal(totalamount).subtract(new BigDecimal(notaxamount)).setScale(2, 4).floatValue();


        OrderInvoiceFormat invoiceformat = OrderInvoice.invoiceformat(ord.market);
        renderPDF(options, ord, totalamount, notaxamount, tax, invoice, invoiceformat);
    }
}
