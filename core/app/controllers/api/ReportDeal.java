package controllers.api;

import controllers.Secure;
import helper.Constant;
import helper.J;
import helper.OrderInvoiceFormat;
import helper.PDFs;
import jobs.analyze.SellingProfitJob;
import jobs.analyze.SellingProfitSearch;
import jobs.analyze.SkuSaleProfitJob;
import models.ReportRecord;
import models.market.OrderInvoice;
import models.market.Orderr;
import models.view.Ret;
import models.view.post.ProfitPost;
import models.view.post.SkuProfitPost;
import org.allcolor.yahp.converter.IHtmlToPdfTransformer;
import org.apache.commons.lang.StringUtils;
import org.joda.time.DateTime;
import org.joda.time.format.DateTimeFormat;
import play.Logger;
import play.libs.F;
import play.modules.pdf.PDF;
import play.mvc.Controller;
import play.mvc.With;

import java.io.File;
import java.io.IOException;
import java.math.BigDecimal;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 销量分析执行后需要清理缓存，保证数据及时
 * User: mac
 * Date: 14-3-27
 * Time: 上午10:12
 */
@With({APIChecker.class})
public class ReportDeal extends Controller {
    /**
     * 销量分析执行完后清理缓存
     */
    public static void reportClear() {
        List<ReportRecord> records = ReportRecord.find("reporttype=? and createAt<=?",
                ReportRecord.RT.ANALYZEREPORT, DateTime.now().plusDays(-14).toDate()).fetch();
        for(ReportRecord record : records) {
            File file = new File(Constant.REPORT_PATH + "/" + record.filepath);
            file.delete();
            record.delete();
        }
        renderJSON(new Ret(true, "清理销售分析文件成功!"));
    }


    public static void profitJob() {
        ProfitPost p = new ProfitPost();
        p.sku = request.params.get("sku");
        p.pmarket = request.params.get("pmarket");
        p.category = request.params.get("category");
        String begin = request.params.get("begin");
        String end = request.params.get("end");
        p.begin = DateTime.parse(begin, DateTimeFormat.forPattern("yyyy-MM-dd")).toDate();
        p.end = DateTime.parse(end, DateTimeFormat.forPattern("yyyy-MM-dd")).toDate();
        Logger.info("ProfitPost json: %s", J.json(p));

        //利润查询
        new SellingProfitSearch(p).now();
        //生成excel
        new SellingProfitJob(p).now();
        renderJSON(new Ret(true, "调用利润job成功!"));
    }

    public static void skuSaleProfitJob() {
        Logger.info("开始执行skuSaleProfitJob......");
        SkuProfitPost p = new SkuProfitPost();
        p.sku = request.params.get("sku");
        p.pmarket = request.params.get("pmarket");
        p.categories = request.params.get("categories");
        String begin = request.params.get("begin");
        String end = request.params.get("end");
        p.begin = DateTime.parse(begin, DateTimeFormat.forPattern("yyyy-MM-dd")).toDate();
        p.end = DateTime.parse(end, DateTimeFormat.forPattern("yyyy-MM-dd")).toDate();
        Logger.debug("ProfitPost json: %s", J.json(p));

        new SkuSaleProfitJob(p).now();

        renderJSON(new Ret(true, "调用利润job成功!"));
    }

    /**
     * Osticket 调用生成pdf
     */
    public static void returnInvoicePdf() {
        String orderId = request.params.get("orderId");
        String taxNumber = request.params.get("taxNumber");

        Orderr ord = Orderr.findById(orderId);
        if(ord == null) renderJSON(new Ret(false, "this order is not exist"));
        if(!(ord.state.equals(Orderr.S.SHIPPED) || ord.state.equals(Orderr.S.PAYMENT)))
            renderJSON(new Ret(true, "this order state is " + ord.state.name()));
        if(StringUtils.isNotBlank(ord.invoiceState) && ord.invoiceState.equals("yes"))
            renderJSON(new Ret(true, "this order is send before!"));

        OrderInvoiceFormat invoiceformat = OrderInvoice.invoiceformat(ord.market);

        OrderInvoice invoice = OrderInvoice.findById(orderId);
        if(invoice == null) {
            invoice = ord.createOrderInvoice();
            invoice.save();
        }
        invoice.setprice();

        final PDF.Options options = new PDF.Options();
        options.pageSize = IHtmlToPdfTransformer.A3P;

        F.T3<Float, Float, Float> amt = ord.amount();
        Float totalamount = amt._1;
        Float notaxamount = 0f;
        if(invoice.europevat == OrderInvoice.VAT.EUROPE) {
            notaxamount = -1 * totalamount;
        } else
            notaxamount = invoice.notaxamount;
        Float tax = new BigDecimal(-1 * totalamount).subtract(new BigDecimal(notaxamount)).setScale(2, 4).floatValue();
        Date returndate = ord.returndate();

        String path =  Constant.INVOICE_PATH + "/" + new DateTime(new Date()).getMonthOfYear();
        File folder = new File(path);
        if(!folder.exists()) folder.mkdir();

        String pdfName = invoiceformat.filename + orderId + ".pdf";
        String template = "Orders/invoiceTaxNumberPDF.html";
        Map<String, Object> args = new HashMap<>();
        args.put("invoiceformat", invoiceformat);
        args.put("ord", ord);
        args.put("invoice", invoice);
        args.put("totalamount", totalamount);
        args.put("notaxamount", notaxamount);
        args.put("tax", tax);
        args.put("returndate", returndate);
        args.put("taxNumber", taxNumber);
        PDFs.templateAsPDF(folder, pdfName, template, options, args);

        /**订单状态改为已发送**/
        ord.invoiceState = "yes";
        ord.save();
        if(StringUtils.isNotEmpty(taxNumber)) {
            invoice.invoiceto += "," + taxNumber;
            invoice.save();
        }
        File file = new File(folder + "/" + pdfName);
        renderBinary(file);
    }

    /**
     * 增加一个后备调用生成1W张发票的接口
     * 可以传入时间参数
     */
    public static void genreateInvoiceByTime() {
        String date = request.params.get("date");
        String num = request.params.get("num");
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
        try {
            Date time = sdf.parse(date);
            OrderInvoice.createInvoicePdf(Integer.parseInt(num), time);
            renderText("后台正在处理, 请稍后去服务器查看.");
        } catch(ParseException e) {
            e.printStackTrace();
        }
    }

}
