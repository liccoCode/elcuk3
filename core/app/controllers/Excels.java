package controllers;

import com.alibaba.fastjson.JSON;
import controllers.api.SystemOperation;
import helper.*;
import helper.Currency;
import jobs.analyze.SellingSaleAnalyzeJob;
import models.RevenueAndCostDetail;
import models.market.BtbOrder;
import models.market.M;
import models.market.OrderItem;
import models.procure.*;
import models.product.Category;
import models.product.Product;
import models.view.Ret;
import models.view.dto.*;
import models.view.post.*;
import models.view.report.*;
import models.whouse.WhouseItem;
import org.apache.commons.lang.StringUtils;
import org.apache.commons.lang3.math.NumberUtils;
import org.apache.http.NameValuePair;
import org.apache.http.message.BasicNameValuePair;
import org.joda.time.DateTime;
import play.Logger;
import play.cache.Cache;
import play.data.validation.Validation;
import play.db.helper.JpqlSelect;
import play.jobs.Job;
import play.libs.F;
import play.modules.excel.RenderExcel;
import play.mvc.Controller;
import play.mvc.With;
import services.MetricAmazonFeeService;

import java.io.File;
import java.math.BigDecimal;
import java.text.DecimalFormat;
import java.text.SimpleDateFormat;
import java.util.*;


/**
 * Created by IntelliJ IDEA.
 * User: wyattpan
 * Date: 10/31/12
 * Time: 11:47 AM
 */
@With({GlobalExceptionHandler.class, Secure.class, SystemOperation.class})
public class Excels extends Controller {

    @Check("excels.deliveryment")
    public static void deliveryment(String id, DeliveryExcel excel) {
        excel.dmt = Deliveryment.findById(id);
        request.format = "xls";
        renderArgs.put(RenderExcel.RA_FILENAME, id + ".xls");
        renderArgs.put(RenderExcel.RA_ASYNC, false);

        ProcureUnit unit = excel.dmt.units.get(0);
        String currency = unit.attrs.currency.symbol();

        String brandname = models.OperatorConfig.getVal("brandname");
        render("Excels/deliveryment" + brandname.toLowerCase() + ".xls", excel, currency);
    }

    /**
     * 下载采购单综合Excel表格
     */
    public static void deliveryments(DeliveryPost p) {
        List<Deliveryment> deliverymentList = p.queryForExcel();
        if(deliverymentList != null && deliverymentList.size() != 0) {
            SimpleDateFormat formatter = new SimpleDateFormat("yyyyMMdd");
            request.format = "xls";
            renderArgs.put(RenderExcel.RA_FILENAME,
                    String.format("%s-%s采购单.xls", formatter.format(p.from), formatter.format(p.to)));
            renderArgs.put(RenderExcel.RA_ASYNC, false);
            renderArgs.put("dateFormat", formatter);
            render(deliverymentList);
        } else {
            renderText("没有数据无法生成Excel文件！");
        }
    }

    /**
     * 下载出货单综合Excel表格
     */
    public static void deliverplans(String id) {
        DeliverPlan dp = DeliverPlan.findById(id);

        List<ProcureUnit> unitList = dp.units;

        if(unitList != null && unitList.size() != 0) {
            SimpleDateFormat formatter = new SimpleDateFormat("yyyyMMdd");
            request.format = "xls";
            renderArgs.put(RenderExcel.RA_FILENAME,
                    String.format("%s出仓单.xls", dp.id));
            renderArgs.put(RenderExcel.RA_ASYNC, false);
            renderArgs.put("dateFormat", formatter);
            render(dp, unitList);
        } else {
            renderText("没有数据无法生成Excel文件！");
        }
    }

    /**
     * 下载选定的采购计划的出货单
     *
     * @param id   采购单 id
     * @param pids 选定的采购计划的 id 集合
     * @throws Exception
     */
    public static synchronized void procureunitsOrder(String id, List<Long> pids) throws Exception {
        if(pids == null || pids.size() == 0)
            Validation.addError("", "必须选择需要下载的采购计划");
        if(Validation.hasErrors()) {
            Webs.errorToFlash(flash);
            Deliveryments.show(id);
        }
        List<ProcureUnit> units = ProcureUnit.find("id IN " + JpqlSelect.inlineParam(pids)).fetch();
        StringBuffer pidstr = new StringBuffer();
        for(Long pid : pids) {
            pidstr.append(pid.toString()).append("，");
        }
        SimpleDateFormat formatter = new SimpleDateFormat("yyyyMMdd");
        request.format = "xls";
        renderArgs.put(RenderExcel.RA_FILENAME,
                String.format("%s出货计划.xls", pidstr.toString()));
        renderArgs.put(RenderExcel.RA_ASYNC, false);
        renderArgs.put("dateFormat", formatter);
        renderArgs.put("procurecompany", models.OperatorConfig.getVal("procurecompany"));
        renderArgs.put("dmt", Deliveryment.findById(id));
        render(units);
    }


    /**
     * 下载采购单综合Excel表格
     */
    public static void analyzes(AnalyzePost p) {
        List<AnalyzeDTO> dtos = p.query();
        if(dtos != null && dtos.size() != 0) {
            SimpleDateFormat formatter = new SimpleDateFormat("yyyyMMdd");
            request.format = "xls";
            renderArgs.put(RenderExcel.RA_FILENAME,
                    String.format("%s-%s销售分析.xls", formatter.format(p.from), formatter.format(p.to)));
            renderArgs.put(RenderExcel.RA_ASYNC, false);
            renderArgs.put("dateFormat", formatter);
            render(dtos);
        } else {
            renderText("没有数据无法生成Excel文件！");
        }
    }


    /**
     * 下载转化率Excel表格
     */
    public static void trafficRate(TrafficRatePost p) {
        List<TrafficRate> dtos = p.query();
        if(dtos != null && dtos.size() != 0) {
            SimpleDateFormat formatter = new SimpleDateFormat("yyyyMMdd");
            request.format = "xls";
            renderArgs.put(RenderExcel.RA_FILENAME,
                    String.format("%s-%sSelling流量转化率.xls", formatter.format(p.from), formatter.format(p.to)));
            renderArgs.put(RenderExcel.RA_ASYNC, false);
            renderArgs.put("dateFormat", formatter);
            render(dtos, p);
        } else {
            renderText("没有数据无法生成Excel文件！");
        }
    }

    /**
     * 下载运输单明细Excel表格
     */
    public static void shipmentDetails(List<String> shipmentId) {
        if(shipmentId == null || shipmentId.size() == 0) {
            renderText("请选择需要打印的运输单！");
        } else {
            List<Shipment> dtos = Shipment.find("id IN " + JpqlSelect.inlineParam(shipmentId)).fetch();
            SimpleDateFormat formatter = new SimpleDateFormat("yyyyMMdd-HHmmss");
            request.format = "xls";
            renderArgs.put(RenderExcel.RA_FILENAME,
                    String.format("运输单发货信息明细表格%s.xls", formatter.format(new Date())));
            renderArgs.put(RenderExcel.RA_ASYNC, false);
            render(dtos);
        }
    }

    /**
     * 利润下载
     */
    public static void profit(ProfitPost p) {
        List<Profit> profits = new ArrayList<Profit>();
        String cacke_key = SellingSaleAnalyzeJob.AnalyzeDTO_SID_CACHE;
        // 这个地方有缓存, 但还是需要一个全局锁, 控制并发, 如果需要写缓存则锁住
        List<AnalyzeDTO> dtos = null;
        String cache_str = Caches.get(cacke_key);
        if(!StringUtils.isBlank(cache_str)) {
            dtos = JSON.parseArray(cache_str, AnalyzeDTO.class);
        }
        if(dtos == null) {
            renderText("Analyze后台事务正在执行中,请稍候...");
        }

        if(StringUtils.isBlank(p.category) && StringUtils.isBlank(p.sku)) {
            renderText("未选择category或者sku!");
        } else {
            if(!StringUtils.isBlank(p.sku)) {
                if(!Product.exist(p.sku)) {
                    renderText("系统不存在sku:" + p.sku);
                }
            }
            if(!StringUtils.isBlank(p.category)) {
                if(!Category.exist(p.category)) {
                    renderText("系统不存在category:" + p.category);
                }
            }
            String skukey = "";
            String marketkey = "";
            String categorykey = "";
            if(p.pmarket != null) marketkey = p.pmarket;
            if(p.category != null) categorykey = p.category.toLowerCase();

            String postkey = helper.Caches.Q.cacheKey("profitpost", p.begin, p.end, categorykey, skukey, marketkey,
                    "excel");
            profits = Cache.get(postkey, List.class);
            if(profits == null) {
                if(StringUtils.isNotBlank(p.sku)) {
                    categorykey = p.sku;
                }
                SimpleDateFormat formater = new SimpleDateFormat("yyyyMMdd");
                postkey = "profitpost_" + categorykey + "_" + marketkey + "_"
                        + formater.format(p.begin) + "_"
                        + formater.format(p.end);
                String postvalue = Caches.get(postkey);
                if(StringUtils.isBlank(postvalue)) {
                    String categoryname = "";
                    int is_sku = 0;
                    if(StringUtils.isNotBlank(p.sku)) {
                        categoryname = p.sku;
                        is_sku = 1;
                    } else {
                        categoryname = p.category.toLowerCase();
                    }
                    String url = String.format("%s/profit_batch_work?category=%s&&market=%s&from=%s&to=%s&is_sku=%s",
                            System.getenv(Constant.ROCKEND_HOST),
                            categoryname,
                            marketkey,
                            formater.format(p.begin),
                            formater.format(p.end),
                            is_sku);
                    HTTP.get(url);
                }
            }
        }

        if(profits != null && profits.size() != 0) {
            SimpleDateFormat formatter = new SimpleDateFormat("yyyyMMdd");
            request.format = "xls";
            renderArgs.put(RenderExcel.RA_FILENAME,
                    String.format("%s-%s销售库存利润报表.xls", formatter.format(p.begin), formatter.format(p.end)));
            renderArgs.put(RenderExcel.RA_ASYNC, false);
            renderArgs.put("dateFormat", formatter);
            render(profits, p);
        } else {
            renderText("正在后台计算生成Excel文件！");
        }
    }

    public static void skuProfit(SkuProfitPost p) {
        if(p == null) p = new SkuProfitPost();
        if(StringUtils.isBlank(p.categories) && StringUtils.isBlank(p.sku)) {
            renderText("未选择category或者sku!");
        } else {
            String sku_key = "";
            String market_key = p.pmarket;
            String categories_key = "";
            if(StringUtils.isBlank(p.sku)) {
                sku_key = p.categories;
            } else {
                sku_key = p.sku;
            }
            String cacke_key = "skuprofitmaprunning_" + sku_key + "_" + market_key + "_" +
                    new SimpleDateFormat("yyyyMMdd").format(p.begin) + "_" +
                    new SimpleDateFormat("yyyyMMdd").format(p.end);
            String cache_str = Caches.get(cacke_key);

            if(!StringUtils.isBlank(cache_str)) {
                renderText("Analyze后台事务正在执行中,请稍候...");
            } else {
                if(p.sku != null) sku_key = p.sku;
                if(p.pmarket != null) market_key = p.pmarket;
                if(p.categories != null) categories_key = p.categories.replace(" ", "").toLowerCase();
                String post_key = Caches.Q
                        .cacheKey("skuprofitpost", p.begin, p.end, categories_key, sku_key, market_key);
                Logger.info("skuprofitpost KEY: " + post_key);
                List<SkuProfit> dtos = Cache.get(post_key, List.class);
                if(dtos == null) {
                    String category_names = "";
                    int is_sku = 0;
                    if(StringUtils.isNotBlank(p.sku)) {
                        category_names = p.sku;
                        is_sku = 1;
                    } else {
                        category_names = p.categories.replace(" ", "").toLowerCase();
                    }
                    List<NameValuePair> params = new ArrayList<NameValuePair>();
                    params.add(new BasicNameValuePair("categories", category_names));
                    params.add(new BasicNameValuePair("market", market_key));
                    params.add(new BasicNameValuePair("from", new SimpleDateFormat("yyyy-MM-dd").format(p.begin)));
                    params.add(new BasicNameValuePair("to", new SimpleDateFormat("yyyy-MM-dd").format(p.end)));
                    params.add(new BasicNameValuePair("is_sku", String.valueOf(is_sku)));
                    HTTP.post(System.getenv(Constant.ROCKEND_HOST) + "/sku_profit_batch_work", params);
                    renderText("后台事务正在计算中,请稍候...");
                    renderText("后台事务正在计算中,请稍候...");
                } else {
                    SkuProfit total = SkuProfit.handleSkuProfit(dtos);
                    SimpleDateFormat formatter = new SimpleDateFormat("yyyyMMdd");
                    request.format = "xls";
                    renderArgs.put(RenderExcel.RA_FILENAME,
                            String.format("SKU销售库存利润报表%s.xls", formatter.format(new Date())));
                    renderArgs.put(RenderExcel.RA_ASYNC, false);
                    renderArgs.put("dateFormat", formatter);
                    render(total, p, dtos);
                }
            }
        }
    }

    public static void saleReport(SaleReportPost p) {
        List<SaleReportDTO> dtos = p.query();
        if(dtos != null && dtos.size() != 0) {
            SimpleDateFormat formatter = new SimpleDateFormat("yyyyMMdd");
            request.format = "xls";
            renderArgs.put(RenderExcel.RA_FILENAME,
                    String.format("%s-%s产品销售统计报表.xls", formatter.format(p.from), formatter.format(p.to)));
            renderArgs.put(RenderExcel.RA_ASYNC, false);
            renderArgs.put("dateFormat", formatter);
            render(dtos, p);

        } else {
            renderText("没有数据无法生成Excel文件！");
        }
    }

    /**
     * 请款单页面导出采购单中所有采购计划列表数据
     *
     * @param id 采购单ID
     */
    public static void exportDeliverymentDetailToExcel(String id) {
        Deliveryment deliveryment = Deliveryment.findById(id);
        List<ProcureUnit> units = deliveryment.units;
        if(units != null && units.size() > 0) {
            request.format = "xls";
            renderArgs.put(RenderExcel.RA_FILENAME,
                    String.format("采购单%s采购计划列表数据.xls", deliveryment.id));
            renderArgs.put(RenderExcel.RA_ASYNC, false);
            float cny_summery = 0f;
            float usd_summery = 0f;
            float unkown_summery = 0f;
            for(ProcureUnit unit : units) {
                if(unit.attrs.currency == Currency.CNY) {
                    cny_summery += unit.totalAmount();
                } else if(unit.attrs.currency == Currency.USD) {
                    usd_summery += unit.totalAmount();
                } else {
                    unkown_summery += unit.totalAmount();
                }
            }
            render(units, cny_summery, usd_summery, unkown_summery);
        } else {
            renderText("没有数据无法生成Excel文件!");
        }

    }

    /**
     * 导出产品SKU基本信息
     */
    public static void exportProductDetailToExcel(ProductPost p) {
        p.perSize = NumberUtils.toInt(Product.count() + "");
        List<Product> prods = p.query();
        if(prods != null && prods.size() != 0) {
            DecimalFormat df = new DecimalFormat("0.00");
            request.format = "xls";
            renderArgs.put(RenderExcel.RA_FILENAME,
                    String.format("%s产品SKU基本信息.xls", StringUtils.isEmpty(p.search) ? "ALL" : p.search));
            renderArgs.put(RenderExcel.RA_ASYNC, false);
            render(prods, df);
        } else {
            renderText("没有数据无法生成Excel文件！");
        }
    }

    public static void exportShipmentWeighReport(ShipmentWeight excel) {
        Map<String, ShipmentWeight> sws = excel.query();
        if(sws != null && sws.size() > 0) {
            SimpleDateFormat formatter = new SimpleDateFormat("yyyyMMdd");
            request.format = "xls";
            renderArgs.put("dateFormat", formatter);
            renderArgs.put(RenderExcel.RA_FILENAME,
                    String.format("%s-%s运输重量报表.xls", formatter.format(excel.from), formatter.format(excel.to)));
            renderArgs.put(RenderExcel.RA_ASYNC, false);
            render(sws, excel);
        } else {
            renderText("没有数据无法生成Excel文件！");
        }
    }

    public static void exportShipmentCostAndWeightReport(ShipmentWeight excel) {
        List<Shipment> dtos = excel.queryShipmentCostAndWeight();
        if(dtos != null && dtos.size() > 0) {
            SimpleDateFormat formatter = new SimpleDateFormat("yyyyMMdd");
            request.format = "xls";
            renderArgs.put("dateFormat", formatter);
            renderArgs.put(RenderExcel.RA_FILENAME,
                    String.format("运输单费用及重量统计报表%s-%s.xls", formatter.format(excel.from), formatter.format(excel.to)));
            renderArgs.put(RenderExcel.RA_ASYNC, false);
            renderArgs.put("symbol", Currency.CNY.symbol());
            render(dtos, excel);
        } else {
            renderText("没有数据无法生成Excel文件！");
        }
    }

    public static void exportProcureUnitsLogs(ProcurePost p) {
        List<HashMap<String, Object>> logs = p.queryLogs();
        if(logs != null && logs.size() > 0) {
            SimpleDateFormat formatter = new SimpleDateFormat("yyyyMMdd");
            request.format = "xls";
            renderArgs.put("dateFormat", new SimpleDateFormat("yyyy-MM-dd HH:MM:SS"));
            renderArgs.put(RenderExcel.RA_FILENAME,
                    String.format("%s-%s采购计划log记录.xls", formatter.format(p.from), formatter.format(p.to)));
            renderArgs.put(RenderExcel.RA_ASYNC, false);
            render(logs);
        } else {
            renderText("没有数据无法生成Excel文件!");
        }
    }

    public static void arrivalRateReport(ArrivalRatePost p) {
        if(p == null) p = new ArrivalRatePost();
        List<ArrivalRate> dtos = p.query();
        if(dtos != null && dtos.size() > 1) {
            List<Shipment> shipments = p.queryOverTimeShipment();
            SimpleDateFormat formatter = new SimpleDateFormat("yyyyMMdd");
            request.format = "xls";
            renderArgs.put("dmt", new SimpleDateFormat("yyyy-MM-dd"));
            renderArgs.put("dateFormat", new SimpleDateFormat("yyyy-MM-dd HH:MM:SS"));
            renderArgs.put(RenderExcel.RA_FILENAME,
                    String.format("%s-%s运输准时到货统计报表.xls", formatter.format(p.from), formatter.format(p.to)));
            renderArgs.put(RenderExcel.RA_ASYNC, false);
            render(dtos, shipments, p);
        } else {
            renderText("没有数据无法生成Excel文件!");
        }
    }

    public static void lossRateReport(LossRatePost p, String type) {
        if(p == null) p = new LossRatePost();
        SimpleDateFormat formatter = new SimpleDateFormat("yyyyMMdd");
        if(type != null && type.equals("pay")) {
            Map<String, Object> map = p.queryDate();
            List<LossRate> lossrates = (List<LossRate>) map.get("lossrate");
            LossRate losstotal = p.buildTotalLossRate(lossrates);
            if(lossrates != null && lossrates.size() != 0) {
                request.format = "xls";
                renderArgs.put(RenderExcel.RA_ASYNC, false);
                renderArgs.put(RenderExcel.RA_FILENAME,
                        String.format("%s-%s运输单丢失率报表.xls", formatter.format(p.from), formatter.format(p.to)));
                renderArgs.put("dmt", formatter);
                render(lossrates, losstotal, p);
            } else {
                renderText("没有数据无法生成Excel文件！");
            }
        } else {
            Map<String, Object> map = p.queryDate();
            List<ShipItem> dtos = (List<ShipItem>) map.get("shipItems");
            if(dtos != null && dtos.size() > 0) {
                SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd");
                request.format = "xls";
                renderArgs.put(RenderExcel.RA_ASYNC, false);
                renderArgs.put(RenderExcel.RA_FILENAME,
                        String.format("%s-%s未完全入库统计报表.xls", formatter.format(p.from), formatter.format(p.to)));
                renderArgs.put("dmt", formatter);
                renderArgs.put("dft", dateFormat);
                render("Excels/notFullyStorageReport.xls", dtos, p);
            } else {
                renderText("没有数据无法生成Excel文件！");
            }
        }
    }


    public static void skuSalesReport(Date from, Date to, String val) {
        List<F.T4<String, Long, Long, Double>> sales = OrderItem.querySalesBySkus(from, to, val);
        if(sales != null && sales.size() != 0) {
            SimpleDateFormat formatter = new SimpleDateFormat("yyyyMMdd");
            request.format = "xls";
            renderArgs.put(RenderExcel.RA_FILENAME,
                    String.format("自定义销售报表%s.xls", formatter.format(DateTime.now().toDate())));
            renderArgs.put(RenderExcel.RA_ASYNC, false);
            renderArgs.put("dateFormat", formatter);
            render(sales, from, to);
        } else {
            renderText("没有数据无法生成Excel文件！");
        }
    }

    public static void skuMonthlyDailySalesReports(final Date from, final Date to, final M market,
                                                   final String category, final String val) {
        try {
            final int begin = new DateTime(from).getMonthOfYear();
            final int end = new DateTime(to).getMonthOfYear();
            if(from.getTime() > to.getTime() || begin > end) renderJSON(new Ret("开始时间必须小于结束时间且必须在同一年份内!"));
            String cacheKey = Caches.Q.cacheKey("SkuMonthlyDailySales", from, to, category, market, val);
            List<DailySalesReportsDTO> dtos = Cache.get(cacheKey, List.class);
            if(dtos == null || dtos.size() == 0) {
                new Job() {
                    @Override
                    public void doJob() throws Exception {
                        OrderItem.skuMonthlyDailySales(from, to, market, category, val);
                    }
                }.now();
                renderText("正在处理中...请稍后几分钟再来查看...");
            } else {
                List<Integer> months = new ArrayList<Integer>();
                for(int i = begin; i <= end; i++) months.add(i);
                SimpleDateFormat formatter = new SimpleDateFormat("yyyyMMdd");
                request.format = "xls";
                renderArgs.put(RenderExcel.RA_FILENAME,
                        String.format("SKU月度日均销量报表%s.xls", formatter.format(DateTime.now().toDate())));
                renderArgs.put(RenderExcel.RA_ASYNC, false);
                File fileOut = OrderItem.createWorkBook(dtos, months, cacheKey);
                renderBinary(fileOut);
            }
        } catch(Exception e) {
            renderJSON(new Ret(Webs.S(e)));
        }
    }

    /**
     * 主营业务收入与成本报表(Amazon)
     */
    public static void revenueAndCostReport(Integer year, Integer month) {
        Date target = new DateTime().withYear(year).withMonthOfYear(month).toDate();
        List<RevenueAndCostDetail> dtos = RevenueAndCostDetail
                .find("create_at BETWEEN ? AND ?", Dates.monthBegin(target), Dates.monthEnd(target)).fetch();
        if(dtos != null && dtos.size() > 0) {
            SimpleDateFormat formatter = new SimpleDateFormat("yyyy年MM月");
            request.format = "xls";
            renderArgs.put(RenderExcel.RA_FILENAME, "主营业务收入与成本报表(Amazon).xls");
            renderArgs.put(RenderExcel.RA_ASYNC, false);
            render(dtos, target, formatter);
        } else {
            HTTP.get(String.format("%s/revenue_and_cost_calculator?year=%s&month=%s",
                    System.getenv(Constant.ROCKEND_HOST), year, month));
            renderText("正在计算中...请稍后再来查看.");
        }
    }

    public static void procureUnitSearchExcel(ProcurePost p) {
        List<ProcureUnit> dtos = p.queryForExcel();
        if(dtos == null || dtos.size() == 0) {
            renderText("没有数据无法生成Excel文件!");
        } else {
            SimpleDateFormat formatter = new SimpleDateFormat("yyyyMMdd");
            request.format = "xls";
            renderArgs.put(RenderExcel.RA_FILENAME,
                    String.format("采购计划明细表%s-%s.xls", formatter.format(p.from), formatter.format(p.to)));
            renderArgs.put(RenderExcel.RA_ASYNC, false);
            renderArgs.put("dateFormat", formatter);
            render(dtos, p);
        }
    }

    public static void areaGoodsAnalyzeExcel(AreaGoodsAnalyze a) {
        if(a == null) {
            a = new AreaGoodsAnalyze();
            a.from = DateTime.now().minusMonths(1).plusDays(1).toDate();
            a.to = DateTime.now().toDate();
        }
        List<AreaGoodsAnalyze> dtos = a.query();
        if(dtos != null && dtos.size() > 0) {
            a.queryTotalShipmentAnalyze();
            SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd");
            request.format = "xls";
            renderArgs.put(RenderExcel.RA_FILENAME, String.format("物流区域货量分析报表%s-%s.xls", dateFormat.format(a.from),
                    dateFormat.format(a.to)));
            renderArgs.put(RenderExcel.RA_ASYNC, false);

            render(dtos, a, dateFormat);
        } else {
            renderText("没有数据无法生成Excel文件！");
        }
    }

    /**
     * 报关要素下载
     *
     * @param id
     */
    public static void declare(String id) {
        Shipment ship = Shipment.findById(id);
        String invoiceNo = ship.buildInvoiceNO();//生成 InvoiceNO
        String countryCode = ship.items.get(0).unit.fba.fbaCenter.countryCode;
        DeclareDTO dto = DeclareDTO.changeCounty(countryCode);
        SimpleDateFormat dateFormat = new SimpleDateFormat("yyyyMMdd");
        String issueDate = Dates.date2Date();
        request.format = "xls";
        renderArgs.put(RenderExcel.RA_FILENAME, String.format("%s%s%s%s.xls",
                dateFormat.format(new Date()), ship.items.get(0).unit.fba.centerId, ship.type.label(), "报关要素"));
        renderArgs.put(RenderExcel.RA_ASYNC, false);
        render(invoiceNo, ship, dto, issueDate);
    }

    /**
     * 运费与重量月报表报表
     *
     * @param from
     * @param to
     */
    public static void downloadFreightReport(Date from, Date to) {
        List<CostReportDTO> dtos = CostReportDTO.setReportData(from, to);
        SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd");
        request.format = "xls";
        renderArgs.put(RenderExcel.RA_FILENAME, String.format("%s至%s运费与重量报表报表.xls", dateFormat.format(from),
                dateFormat.format(to)));
        renderArgs.put(RenderExcel.RA_ASYNC, false);
        render(dtos, from, to, dateFormat);
    }

    /***
     * 税金与重量报表
     *
     * @param from
     * @param to
     */
    public static void downloadVATReport(Date from, Date to) {
        List<CostReportDTO> dtos = CostReportDTO.setReportData(from, to);
        SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd");
        request.format = "xls";
        renderArgs.put(RenderExcel.RA_FILENAME, String.format("%s至%s运费与重量报表报表.xls", dateFormat.format(from),
                dateFormat.format(to)));
        renderArgs.put(RenderExcel.RA_ASYNC, false);
        render(dtos, from, to, dateFormat);
    }

    public static void btbOrderDetailReport(BtbOrderPost p) {
        if(p == null) p = new BtbOrderPost();
        List<BtbOrder> dtos = p.query();
        p.totalCost(dtos);
        SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd");
        request.format = "xls";
        renderArgs.put(RenderExcel.RA_FILENAME, String.format("B2B销售订单明细%s.xls", dateFormat.format(new Date())));
        renderArgs.put(RenderExcel.RA_ASYNC, false);
        render(dtos, dateFormat, p);
    }

    /**
     * 订单费用汇总报表
     *
     * @param from
     * @param to
     * @param market
     */
    public static void orderFeesCostReport(final Date from, final Date to, final M market) {
        Map<String, Map<String, BigDecimal>> feesCost = await(
                new Job<Map<String, Map<String, BigDecimal>>>() {
                    @Override
                    public Map<String, Map<String, BigDecimal>> doJobWithResult() throws Exception {
                        MetricAmazonFeeService service = new MetricAmazonFeeService(from, to, market);
                        try {
                            return service.orderFeesCost();
                        } catch(Exception e) {
                            renderText(Webs.S(e));
                        }
                        return null;
                    }
                }.now());

        SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy/MM/dd");
        request.format = "xls";
        renderArgs.put(RenderExcel.RA_FILENAME,
                String.format("订单费用汇总报表%s.xls", new SimpleDateFormat("yyyy-MM-dd-HH-mm-ss").format(
                        DateTime.now().toDate())));
        renderArgs.put(RenderExcel.RA_ASYNC, false);
        render(feesCost, from, to, dateFormat);
    }

    /**
     * 采购付款明细报表
     */
    public static void purchasePaymentReport(PurchaseOrderPost p) {
        List<PurchasePaymentDTO> dtos = p.downloadReport();
        SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy/MM/dd");
        request.format = "xls";
        renderArgs.put(RenderExcel.RA_FILENAME,
                String.format("采购付款明细报表%s.xls", new SimpleDateFormat("yyyy-MM-dd-HH-mm-ss").format(
                        DateTime.now().toDate())));
        render(dtos, dateFormat);
    }

    public static void orderReports(OrderPOST p) {
        if(p == null) p = new OrderPOST();
        List<OrderReportDTO> orders = p.queryForExcel();
        SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd");
        request.format = "xls";
        renderArgs.put(RenderExcel.RA_FILENAME, String.format("订单汇总报表%s.xls", dateFormat.format(p.begin)));
        renderArgs.put(RenderExcel.RA_ASYNC, false);
        render(orders, p.begin, p.end, dateFormat);
    }

    /**
     * 采购订单明细报表
     */
    public static void purchaseOrderDetailReport(PurchaseOrderPost p) {
        if(p == null) p = new PurchaseOrderPost();
        List<ProcureUnit> dtos = p.query();
        SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd");
        SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy/MM/dd");
        DecimalFormat df = new DecimalFormat("#.00");
        request.format = "xls";
        renderArgs.put(RenderExcel.RA_FILENAME, String.format("采购订单明细表%s.xls", format.format(new Date())));
        renderArgs.put(RenderExcel.RA_ASYNC, false);
        render(dtos, dateFormat, p, df);
    }

    public static void stockRecords(StockRecordPost p) {
        if(p == null) p = new StockRecordPost();
        if(p.dateRange() > 90) renderText("时间区间过大!");

        p.pagination = false;
        List<Map<String, Object>> records = p.checkRecords();
        if(records == null || records.isEmpty()) renderText("未找到任何数据!");

        SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        request.format = "xls";
        renderArgs.put(RenderExcel.RA_FILENAME, "库存异动盘点.xls");
        renderArgs.put(RenderExcel.RA_ASYNC, false);
        render(records, dateFormat, p);
    }

    public static void whouseItems(WhouseItemPost p) {
        if(p == null) p = new WhouseItemPost();
        p.pagination = false;
        List<WhouseItem> items = p.query();
        request.format = "xls";
        renderArgs.put(RenderExcel.RA_FILENAME, "库存报表.xls");
        renderArgs.put(RenderExcel.RA_ASYNC, false);
        render(items);
    }
}