package controllers.api;

import com.amazonservices.mws.finances.MWSFinancesServiceClient;
import com.amazonservices.mws.finances.model.ListFinancialEventsRequest;
import com.amazonservices.mws.finances.model.ListFinancialEventsResponse;
import helper.*;
import jobs.analyze.SellingProfitJob;
import jobs.analyze.SellingProfitSearch;
import jobs.analyze.SkuSaleProfitJob;
import models.OperatorConfig;
import models.ReportRecord;
import models.finance.SaleFee;
import models.market.OrderInvoice;
import models.market.Orderr;
import models.procure.ShipItem;
import models.procure.Shipment;
import models.procure.ShipmentMonthly;
import models.view.Ret;
import models.view.post.LossRatePost;
import models.view.post.ProfitPost;
import models.view.post.SkuProfitPost;
import models.view.report.LossRate;
import mws.MWSFinances;
import org.allcolor.yahp.converter.IHtmlToPdfTransformer;
import org.apache.commons.lang.StringUtils;
import org.apache.poi.openxml4j.exceptions.InvalidFormatException;
import org.joda.time.DateTime;
import org.joda.time.format.DateTimeFormat;
import play.Logger;
import play.db.jpa.GenericModel;
import play.libs.F;
import play.modules.excel.RenderExcel;
import play.modules.pdf.PDF;
import play.mvc.Controller;
import play.mvc.With;

import java.io.File;
import java.io.IOException;
import java.math.BigDecimal;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.*;

/**
 * 销量分析执行后需要清理缓存，保证数据及时
 * User: mac
 * Date: 14-3-27
 * Time: 上午10:12
 */
@With({APIChecker.class})
public class ReportDeal extends Controller {

    public static final String BASE_PATH = "/root/cap_elcuk2/current/core/app/views/";

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
     * Osticket调用,返回订单状态
     */
    public static void returnOrderStatus() {
        String orderId = request.params.get("orderId");
        if(StringUtils.isNotBlank(orderId)) {
            Orderr ord = Orderr.findById(orderId);
            renderJSON(new Ret(true, ord.state.name()));
        } else {
            renderJSON(new Ret(false));
        }
    }


    /**
     * Osticket 调用生成pdf
     */
    public static void returnInvoicePdf() {
        String orderId = request.params.get("orderId");
        String taxNumber = request.params.get("taxNumber");
        String flag = request.params.get("flag");
        Logger.info(String.format("Osticket 调用生成发票接口,传入参数 [orderId:%s];[flag:%s];[taxNumber:%s]",
                orderId, flag, taxNumber));
        Orderr ord = Orderr.findById(orderId);
        if(ord == null) renderJSON(new Ret(false, "this order is not exist"));
        if(ord.fees.size() == 0) {
            MWSFinancesServiceClient client = MWSFinances.client(ord.account, ord.account.type);
            ListFinancialEventsRequest request = new ListFinancialEventsRequest();
            request.setSellerId(ord.account.merchantId);
            request.setMWSAuthToken(ord.account.token);
            request.setAmazonOrderId(ord.orderId);
            ListFinancialEventsResponse response = client.listFinancialEvents(request);
            SaleFee.parseFinancesApiResult(response, ord.account);
            Orderr newOne = Orderr.findById(ord.orderId);
            ord.fees = newOne.fees;
        }

        if(flag == null || !flag.equals("1")) {
            if(!(ord.state.equals(Orderr.S.SHIPPED) || ord.state.equals(Orderr.S.PAYMENT)))
                renderJSON(new Ret(true, ord.state.name()));
            if(StringUtils.isNotBlank(ord.invoiceState) && ord.invoiceState.equals("yes"))
                renderJSON(new Ret(true, "this order is send before!"));
        }

        try {
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
            Float tax = new BigDecimal(-1 * totalamount).subtract(new BigDecimal(notaxamount)).setScale(2, 4)
                    .floatValue();
            Date returndate = ord.returndate();

            String path = Constant.INVOICE_PATH + "/" + new DateTime(new Date()).getMonthOfYear() + "sent";
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

            /*订单状态改为已发送*/
            ord.invoiceState = "yes";
            if(ord.invoiceDate == null)
                ord.invoiceDate = new Date();
            ord.totalSale = ord.totalAmount;
            ord.save();
            if(StringUtils.isNotEmpty(taxNumber)) {
                invoice.invoiceto += "," + taxNumber;
                invoice.save();
            }
            File file = new File(folder + "/" + pdfName);
            renderBinary(file);
        } catch(Exception e) {
            renderJSON(new Ret(false, e.getMessage()));
        }
    }

    /**
     * 增加一个后备调用生成1W张发票的接口
     * 可以传入时间参数
     */
    public static void genreateInvoiceByTime() {
        String date = request.params.get("date");
        String num = request.params.get("num");
        String market = request.params.get("market");
        String regex = request.params.get("regex");

        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
        try {
            Date time = sdf.parse(date);
            OrderInvoice.createInvoicePdf(Integer.parseInt(num), time, market, regex);
            renderText("后台正在处理, 请稍后去服务器查看.");
        } catch(ParseException e) {
            Logger.error(Webs.s(e));
        }
    }

    public static void generateShipmentMonthlyReport() {
        List<Shipment> list = Shipment.shipmentMonthly();
        int year = DateTime.now().minusMonths(1).getYear();
        int month = DateTime.now().minusMonths(1).getMonthOfYear();
        List<ShipmentMonthly> monthlyList = ShipmentMonthly.find("year=? AND month=?", year, month).fetch();
        List<ReportRecord> records = ReportRecord.find("year=? AND month=? AND reporttype=?",
                year, month, ReportRecord.RT.SHIPMENTMONTHLY).fetch();
        monthlyList.forEach(GenericModel::delete);
        records.forEach(GenericModel::delete);
        list.stream().filter(shipment -> shipment.items.size() > 0).forEach(shipment -> shipment.items.forEach(item -> {
            ShipmentMonthly monthly = new ShipmentMonthly();
            monthly.unit = item.unit;
            monthly.shipItem = item;
            monthly.year = year;
            monthly.month = month;
            monthly.type = item.shipment.type;
            if(Arrays.asList(Shipment.T.EXPRESS, Shipment.T.DEDICATED).contains(item.shipment.type)) {
                item.crawlWeight(monthly);
            }
            monthly.save();
        }));
        ReportRecord record = new ReportRecord();
        record.reporttype = ReportRecord.RT.SHIPMENTMONTHLY;
        record.year = DateTime.now().minusMonths(1).getYear();
        record.month = DateTime.now().minusMonths(1).getMonthOfYear();
        record.filename = String.format("月度物流报表_%s年_%s月", record.year, record.month);
        record.save();
    }

    /**
     * 生成未完全入库的报表
     */
    public static void generateLossRateReport() throws IOException, InvalidFormatException {
        LossRatePost p = new LossRatePost();
        Map<String, Object> map = p.queryDate();
        List<LossRate> lossrates = (List<LossRate>) map.get("lossRateList");
        List<ShipItem> dtos = (List<ShipItem>) map.get("shipItems");
        LossRate losstotal = p.buildTotalLossRate(lossrates);
        SimpleDateFormat formatter = new SimpleDateFormat("yyyyMMdd");
        SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd");
        renderArgs.put(RenderExcel.RA_FILENAME,
                String.format("%s-%s运输单丢失率报表.xls", formatter.format(p.from), formatter.format(p.to)));
        renderArgs.put("dmt", formatter);
        renderArgs.put("dft", dateFormat);
        Map<String, Object> beanParams = new HashMap<>();
        beanParams.put("dmt", formatter);
        beanParams.put("dft", dateFormat);
        beanParams.put("dtos", dtos);
        beanParams.put("p", p);
        beanParams.put("lossrates", lossrates);
        beanParams.put("losstotal", losstotal);
        String filePath = Constant.TMP + String.format("%s-%s运输单丢失率报表.xls",
                formatter.format(p.from), formatter.format(p.to));
        new ExcelUtils().createExcel(BASE_PATH + "/Excels/lossRateReport.xls", beanParams, filePath);
        File excel = new File(filePath);
        String config = OperatorConfig.getVal("shipmentlossreport");
        if(StringUtils.isNotBlank(config)) {
            String[] emailData = config.split(",");
            List<String> emailAddress = new ArrayList<>(Arrays.asList(emailData));
            Webs.sendEmailWithAttach("运输单丢失率报表", "FYI", emailAddress, excel);
        }
        excel.deleteOnExit();
    }
}
