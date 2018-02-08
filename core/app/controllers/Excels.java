package controllers;

import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import controllers.api.SystemOperation;
import helper.*;
import helper.Currency;
import models.InventoryCostUnit;
import models.OperatorConfig;
import models.RevenueAndCostDetail;
import models.User;
import models.finance.BatchReviewApply;
import models.market.BtbOrder;
import models.market.BtbOrderItem;
import models.market.M;
import models.market.OrderItem;
import models.material.MaterialPlan;
import models.material.MaterialPlanUnit;
import models.material.MaterialPurchase;
import models.material.MaterialUnit;
import models.procure.*;
import models.product.Product;
import models.view.Ret;
import models.view.dto.*;
import models.view.post.*;
import models.view.report.*;
import models.whouse.InboundUnit;
import models.whouse.Outbound;
import models.whouse.StockRecord;
import models.whouse.WhouseItem;
import org.apache.commons.lang.StringUtils;
import org.apache.commons.lang3.math.NumberUtils;
import org.apache.commons.lang3.tuple.ImmutablePair;
import org.apache.http.NameValuePair;
import org.apache.http.message.BasicNameValuePair;
import org.joda.time.DateTime;
import play.Logger;
import play.cache.Cache;
import play.data.validation.Validation;
import play.db.helper.JpqlSelect;
import play.db.helper.SqlSelect;
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
        String brandname = Objects.equals(excel.dmt.projectName, User.COR.MengTop)
                ? models.OperatorConfig.getVal("b2bbrandname") : models.OperatorConfig.getVal("brandname");
        render("Excels/deliveryment/deliveryment" + brandname.toLowerCase() + ".xls", excel, currency);
    }

    /**
     * 进出口版合同下载
     *
     * @param id
     * @param excel
     */
    public static void exportDeliveryment(String id, DeliveryExcel excel) {
        excel.dmt = Deliveryment.findById(id);
        request.format = "xls";
        renderArgs.put(RenderExcel.RA_FILENAME, id + ".xls");
        renderArgs.put(RenderExcel.RA_ASYNC, false);
        ProcureUnit unit = excel.dmt.units.get(0);
        String currency = unit.attrs.currency.symbol();
        String brandname = Objects.equals(excel.dmt.projectName, User.COR.MengTop)
                ? models.OperatorConfig.getVal("b2bbrandname") : models.OperatorConfig.getVal("brandname");
        render("Excels/exportdeliveryment" + brandname.toLowerCase() + ".xls", excel, currency);
    }


    /**
     * 进出口版合同下载
     */
    public static void exportDeliverymentByShipment(List<String> shipmentId) {
        SimpleDateFormat formatter = new SimpleDateFormat("yyyyMMdd-HHmmss");
        String sql = "SELECT c FROM ProcureUnit c, IN(c.shipItems) ci WHERE ci.shipment.id IN "
                + JpqlSelect.inlineParam(shipmentId) + "  ORDER BY ci.id";
        List<ProcureUnit> units = ProcureUnit.find(sql).fetch();
        String currency = units.get(0).attrs.currency.symbol();
        if(units.stream().noneMatch(unit -> unit.taxPoint != null && unit.taxPoint > 0)) {
            renderText("没有含税数据无法生成Excel文件！");
        } else {
            request.format = "xls";
            renderArgs.put(RenderExcel.RA_FILENAME, String.format("进出口合同%s.xls", formatter.format(new Date())));
            renderArgs.put(RenderExcel.RA_ASYNC, false);
            render(units, currency);
        }
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
            String companyName = OperatorConfig.getVal("companyname");
            render(dp, unitList, companyName);
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
        p.flag = "2";
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

            Shipment ship = dtos.get(0);
            Date date = ship.dates.planBeginDate;
            Calendar c = Calendar.getInstance();
            c.setTime(date);

            int month = c.get(Calendar.MONTH) + 1;
            int day = c.get(Calendar.DATE);
            long totalQty = 0;
            long totalUnit = 0;
            Double totalWeight = 0d;
            Double totalVolume = 0d;
            for(Shipment shipment : dtos) {
                shipment.arryParamSetUP(Shipment.FLAG.STR_TO_ARRAY);
                for(ShipItem item : shipment.items) {
                    totalQty += item.unit.shipmentQty();
                    totalUnit += item.caluTotalUnitByCheckTask();
                    totalWeight += item.caluTotalWeightByCheckTask();
                    totalVolume += item.caluTotalVolumeByCheckTask();
                }
            }

            if(ship.type.name().equals("EXPRESS")) {
                render("Excels/shipmentDetailsForExpress.xls", dtos, month, day);
            } else {
                render(dtos, month, day, totalQty, totalUnit, totalWeight, totalVolume);
            }
        }
    }

    /**
     * 利润下载
     */
    public static void profit(ProfitPost p) {
        List<Profit> profits = p.fetch();
        if(profits != null && profits.size() != 0) {
            SimpleDateFormat formatter = new SimpleDateFormat("yyyyMMdd");
            request.format = "xls";
            renderArgs.put(RenderExcel.RA_FILENAME,
                    String.format("%s-%s销售库存利润报表.xls", formatter.format(p.begin), formatter.format(p.end)));
            renderArgs.put(RenderExcel.RA_ASYNC, false);
            renderArgs.put("dateFormat", formatter);
            render(profits, p);
        } else {
            renderText(Webs.v(Validation.errors()));
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
            String cacke_key = "skuprofitmaprunning_" + sku_key + "_" + market_key + "_"
                    + new SimpleDateFormat("yyyyMMdd").format(p.begin) + "_"
                    + new SimpleDateFormat("yyyyMMdd").format(p.end);
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
                    List<NameValuePair> params = new ArrayList<>();
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
                List<Integer> months = new ArrayList<>();
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
            renderJSON(new Ret(Webs.s(e)));
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
                            renderText(Webs.s(e));
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

    /**
     * 采购应付未付报表
     *
     * @param p
     */
    public static void purchaseReport(PurchaseOrderPost p) {
        List<PurchasePaymentDTO> dtos = p.payablesReport();
        SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy/MM/dd");
        request.format = "xls";
        renderArgs.put(RenderExcel.RA_FILENAME,
                String.format("采购应付未付报表%s至%s.xls",
                        new SimpleDateFormat("yyyy/MM").format(p.from), new SimpleDateFormat("yyyy/MM").format(p.to)));
        render(dtos, dateFormat);
    }

    /**
     * 物流应付未付
     *
     * @param p
     */
    public static void shipmentReport(PurchaseOrderPost p) {
        List<PurchasePaymentDTO> dtos = p.shipmentReport();
        request.format = "xls";
        renderArgs.put(RenderExcel.RA_FILENAME,
                String.format("物流应付未付报表%s至%s.xls",
                        new SimpleDateFormat("yyyy/MM").format(p.from), new SimpleDateFormat("yyyy/MM").format(p.to)));
        render(dtos);
    }


    public static void orderReports(OrderPOST p) {
        if(p == null) p = new OrderPOST();
        //最多只允许导出 10000 个订单的数据,超过了请重新给定搜索范围
        p.perSize = 10000;
        List<OrderReportDTO> orders = p.queryForExcel();
        SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd");
        request.format = "xls";
        renderArgs.put(RenderExcel.RA_FILENAME, String.format("订单汇总报表%s.xls", dateFormat.format(p.begin)));
        renderArgs.put(RenderExcel.RA_ASYNC, false);
        render(orders, p.begin, p.end, dateFormat);
    }

    public static void orderSaleFeeReports(OrderPOST p) {
        if(p == null) p = new OrderPOST();
        p.perSize = 10000;
        List<OrderReportDTO> orders = p.queryForExcel();
        SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd");
        request.format = "xls";
        renderArgs.put(RenderExcel.RA_FILENAME, String.format("订单费用汇总报表%s.xls", dateFormat.format(p.begin)));
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
        List<StockRecord> records = p.query();
        if(records == null || records.isEmpty()) renderText("未找到任何数据!");

        SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        request.format = "xls";
        renderArgs.put(RenderExcel.RA_FILENAME, "库存异动记录.xls");
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

    /**
     * 库存管理列表导出
     *
     * @param p
     */
    public static void exportInventoryManagement(StockPost p) {
        if(p == null) p = new StockPost();
        p.pagination = false;
        List<ProcureUnit> units = p.query();
        request.format = "xls";
        renderArgs.put(RenderExcel.RA_FILENAME, "库存管理.xls");
        renderArgs.put(RenderExcel.RA_ASYNC, false);
        render(units);
    }

    public static void shipmentDetailCost(ShipmentPost p) {
        if(p == null) p = new ShipmentPost();
        p.pagination = false;
        List<Shipment> shipments = p.query();
        request.format = "xls";
        renderArgs.put(RenderExcel.RA_FILENAME, "物流费用报表.xls");
        renderArgs.put(RenderExcel.RA_ASYNC, false);
        render(shipments);
    }

    /**
     * 运输单装箱单
     *
     * @param shipmentId
     */
    public static void packingList(List<String> shipmentId) {
        if(shipmentId == null || shipmentId.size() == 0) {
            renderText("请选择需要打印的运输单！");
        } else {
            List<ShipItem> items = ShipItem.find("shipment.id IN " + JpqlSelect.inlineParam(shipmentId)).fetch();
            if(items.stream()
                    .noneMatch(item -> item.unit != null && item.unit.taxPoint != null && item.unit.taxPoint > 0)) {
                renderText("没有含税数据无法生成Excel文件！");
            }
            Shipment ship = Shipment.find("id = ? ", shipmentId.get(0)).first();
            SimpleDateFormat formatter = new SimpleDateFormat("yyyyMMdd-HHmmss");
            request.format = "xls";
            renderArgs.put(RenderExcel.RA_FILENAME,
                    String.format("装箱单表格%s.xls", formatter.format(new Date())));
            renderArgs.put(RenderExcel.RA_ASYNC, false);
            Date date = ship.dates.planBeginDate;

            Calendar begin = Calendar.getInstance();
            begin.setTime(date);
            begin.add(Calendar.DAY_OF_MONTH, -1);

            renderArgs.put("date", new SimpleDateFormat("yyyy/MM/dd").format(begin.getTime()));
            renderArgs.put("user", Login.current());
            render(items);
        }
    }


    public static void exportInboundUnitReport(InboundPost p) {
        if(p == null) p = new InboundPost();
        p.pagination = false;
        List<InboundUnit> units = p.queryDetail();
        request.format = "xls";
        renderArgs.put(RenderExcel.RA_FILENAME, "收货入库明细.xls");
        renderArgs.put(RenderExcel.RA_ASYNC, false);
        render(units);
    }

    /**
     * 库存占用资金报表
     *
     * @param year
     * @param month
     */
    public static void exportInventoryCostsReport(Integer year, Integer month) {
        Date target = new DateTime().withYear(year).withMonthOfYear(month).toDate();
        List<InventoryCostUnit> units = InventoryCostUnit.find(
                "date BETWEEN ? AND ? ORDER BY categoryId ASC, SKU ASC",
                Dates.morning(Dates.monthBegin(target)), Dates.monthEnd(target)).fetch();
        List<Map<String, Object>> summaries = InventoryCostUnit.countByCategory(target);
        ImmutablePair<List<InventoryCostUnit>, List<Map<String, Object>>> immutablePair =
                InventoryCostUnit.countPrice(units, summaries);

        units = immutablePair.getLeft();
        summaries = immutablePair.getRight();

        if(units != null && units.size() > 0) {
            SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
            request.format = "xls";
            renderArgs.put(RenderExcel.RA_FILENAME,
                    String.format("库存占用资金报表%s.xls", new SimpleDateFormat("yyyyMMdd").format(target)));
            renderArgs.put(RenderExcel.RA_ASYNC, false);
            render(units, summaries, dateFormat);
        } else {
            renderText("未找到当前日期的数据.");
        }
    }

    public static void downloadB2BPi(Long id) {
        BtbOrder order = BtbOrder.findById(id);
        int i = 1;
        for(BtbOrderItem item : order.btbOrderItemList) {
            item.index = i;
            i++;
        }
        SimpleDateFormat df = new SimpleDateFormat("dd MMMMM, yyyy", Locale.ENGLISH);
        request.format = "xls";
        renderArgs.put(RenderExcel.RA_FILENAME, String.format("%s for customs.xls", order.orderNo));
        renderArgs.put(RenderExcel.RA_ASYNC, false);
        render(order, df);
    }

    public static void exportMonthlyShipment(MonthlyShipmentPost p) {
        if(p == null) p = new MonthlyShipmentPost();
        List<MonthlyShipmentDTO> list = p.queryBySku();
        SimpleDateFormat dateFormat = new SimpleDateFormat("yyyyMMdd");
        request.format = "xls";
        renderArgs.put(RenderExcel.RA_FILENAME, String.format("单月物流发货量报表.xls"));
        renderArgs.put(RenderExcel.RA_ASYNC, false);
        render(list, dateFormat, p);
    }

    public static void exportMonthlyShipmentPrescription(ArrivalRatePost p) {
        if(p == null) p = new ArrivalRatePost();
        List<Shipment> list = p.queryMonthlyShipment();
        Map<String, F.T3<String, String, Double>> map = p.calAverageTime(list);
        request.format = "xls";
        renderArgs.put(RenderExcel.RA_FILENAME, String.format("单月运输时效统计.xls"));
        renderArgs.put(RenderExcel.RA_ASYNC, false);
        render(list, map, p);
    }

    public static void exportOutBoundReport(List<String> ids) {
        SimpleDateFormat dateFormat = new SimpleDateFormat("yyyyMMdd");
        List<Outbound> outbounds = Outbound.find("id IN " + SqlSelect.inlineParam(ids)).fetch();
        Set<Shipment.T> shipTypeSet = new HashSet<>();
        outbounds.forEach(outbound -> shipTypeSet.add(outbound.shipType));
        if(shipTypeSet.size() > 1) renderText("请勾选同一运输方式的出库单!");
        Set<String> targetIdSet = new HashSet<>();
        outbounds.forEach(outbound -> targetIdSet.add(outbound.showCompany()));
        if(targetIdSet.size() > 1) renderText("请勾选同一货代公司的出库单!");
        Set<String> createDateSet = new HashSet<>();
        outbounds.forEach(outbound -> createDateSet.add(dateFormat.format(outbound.createDate)));
        if(createDateSet.size() > 1) renderText("请勾选同一出库日期的出库单!");
        Set<String> projectNameSet = new HashSet<>();
        outbounds.forEach(outbound -> projectNameSet.add(outbound.projectName));
        if(projectNameSet.size() > 1) renderText("请勾选同一项目的出库单!");

        String createDate = dateFormat.format(outbounds.get(0).createDate);
        String targetId = outbounds.get(0).showCompany();
        String shipType = outbounds.get(0).shipType.label();
        String projectName = outbounds.get(0).projectName;
        List<ProcureUnit> procureUnits = new ArrayList<>();

        outbounds.forEach(outbound -> procureUnits.addAll(outbound.units));
        request.format = "xls";
        renderArgs.put(RenderExcel.RA_FILENAME, "出库单明细报表.xls");
        renderArgs.put(RenderExcel.RA_ASYNC, false);
        render(dateFormat, outbounds, createDate, targetId, shipType, projectName, procureUnits);
    }

    public static void exportOutBoundDetailReport(OutboundPost p) {
        if(p == null) p = new OutboundPost();
        p.pagination = false;
        List<Outbound> outbounds = p.query();
        Map<String, InventoryTurnoverDTO> map = Outbound.inventoryTurnover(outbounds);
        List<InventoryTurnoverDTO> dtos = new ArrayList<>();
        for(String categoryId : map.keySet()) {
            dtos.add(map.get(categoryId));
        }
        SimpleDateFormat dateFormat = new SimpleDateFormat("yyyyMMdd");
        request.format = "xls";
        renderArgs.put(RenderExcel.RA_FILENAME, "出库单明细报表.xls");
        renderArgs.put(RenderExcel.RA_ASYNC, false);
        render(dateFormat, p, outbounds, dtos);
    }

    /**
     * 下载物料采购单Excel表格
     *
     * @param id
     * @param excel
     */
    public static void materialPurchase(String id, MaterialPurchaseExcel excel) {
        excel.dmt = MaterialPurchase.findById(id);
        request.format = "xls";
        renderArgs.put(RenderExcel.RA_FILENAME, id + ".xls");
        renderArgs.put(RenderExcel.RA_ASYNC, false);
        MaterialUnit unit = excel.dmt.units.get(0);
        String currency = unit.planCurrency.symbol();

        String brandname = Objects.equals(excel.dmt.projectName, User.COR.MengTop)
                ? models.OperatorConfig.getVal("b2bbrandname") : models.OperatorConfig.getVal("brandname");
        render("Excels/material/materialPurchases" + brandname.toLowerCase() + ".xls", excel, currency);
    }


    /**
     * 下载物料出货单综合Excel表格
     */
    public static void materialPlan(String id) {
        MaterialPlan dp = MaterialPlan.findById(id);
        List<MaterialPlanUnit> unitList = dp.units;
        if(unitList != null && unitList.size() != 0) {
            int totalQty = unitList.stream().mapToInt(unit -> unit.qty).sum();
            SimpleDateFormat formatter = new SimpleDateFormat("yyyy-MM-dd");
            request.format = "xls";
            renderArgs.put(RenderExcel.RA_FILENAME, String.format("%s出仓单.xls", dp.id));
            renderArgs.put(RenderExcel.RA_ASYNC, false);
            renderArgs.put("dmt", formatter);
            render(dp, unitList, totalQty);
        } else {
            renderText("没有数据无法生成Excel文件！");
        }
    }


    public static void exportMaterialPlanDetail(MaterialPlanPost p) {
        if(p == null) p = new MaterialPlanPost();
        List<MaterialPlan> planList = p.query();
        if(planList != null && planList.size() != 0) {
            SimpleDateFormat formatter = new SimpleDateFormat("yyyy_MM_dd");
            request.format = "xls";
            renderArgs.put(RenderExcel.RA_FILENAME, String.format("%s-%s物料出货导出明细.xls",
                    formatter.format(p.from), formatter.format(p.to)));
            renderArgs.put(RenderExcel.RA_ASYNC, false);
            render(planList);
        } else {
            renderText("没有数据无法生成Excel文件！");
        }

    }


    public static void skuShipmentReport(LossRatePost p) {
        if(p == null) p = new LossRatePost();
        if(p.from == null || p.to == null) {
            renderText("未选择开始时间或者结束时间!");
        } else {
            String beginDate = new SimpleDateFormat("yyyyMMdd").format(p.from);
            String endDate = new SimpleDateFormat("yyyyMMdd").format(p.to);
            String redisIngKey = String.format("shipmentSkuExecuting_%s_%s", beginDate, endDate);
            String redisKey = String.format("shipmentSku_%s_%s", beginDate, endDate);
            String postvalue = Caches.get(redisKey);

            /** 验证报表结果是否在缓存中生成 **/
            if(StringUtils.isBlank(postvalue)) {
                /**  若报表结果未生成,那么验证是否已经在执行当前条件的 后台任务 **/
                String ingValue = Caches.get(redisIngKey);
                if(StringUtils.isBlank(ingValue)) {
                    //若redis没有 JRockend 正在执行任务的 标志位,那么通过 aws 发送消息队列调用任务执行
                    Map map = new HashMap();
                    map.put("jobName", "shipmentSkuJob");  //  任务ID
                    map.put("args", GTs.newMap("beginDate", beginDate).put("endDate", endDate).build());  //开始日期和结束日期
                    String message = JSONObject.toJSONString(map);
                    AmazonSQS.sendMessage(message);
                    renderText("单个sku物流费年均价报表已经在后台计算中，请于 10min 后再来查看结果~");
                } else {
                    renderText("单个sku物流费年均价报表已经在后台计算中，请于 10min 后再来查看结果~");
                }
            } else {
                List<Map> list = JSONArray.parseArray(postvalue, Map.class);
                if(list != null && list.size() != 0) {
                    SimpleDateFormat formatter = new SimpleDateFormat("yyyy-MM-dd");
                    request.format = "xls";
                    renderArgs.put(RenderExcel.RA_FILENAME,
                            String.format("单个sku物流费年均价表%s.xls", beginDate + "_" + endDate));
                    renderArgs.put(RenderExcel.RA_ASYNC, false);
                    renderArgs.put("dmt", formatter);
                    render(list);
                } else {
                    renderText("没有数据无法生成Excel文件！");
                }
            }
        }
    }

    /**
     * 下载汇签报表
     *
     * @param p
     */
    public static void exportBatchReviewApply(BatchApplyPost p) {
        if(p == null) p = new BatchApplyPost();
        List<BatchReviewApply> applies = p.query();
        if(applies != null && applies.size() != 0) {
            request.format = "xls";
            renderArgs.put(RenderExcel.RA_FILENAME, "汇签审核报表.xls");
            renderArgs.put(RenderExcel.RA_ASYNC, false);
            render(applies);
        } else {
            renderText("没有数据无法生成Excel文件！");
        }
    }

    /**
     * 导出 合作伙伴的物料与sku的Records记录与导出修改日志
     */
    public static void exportCooperItemLogs() {
        CooperatorPost post = new CooperatorPost();
        List<Map<String, Object>> logs = post.logs();
        if(logs != null && logs.size() != 0) {
            request.format = "xls";
            renderArgs.put(RenderExcel.RA_FILENAME, "物料修改日志.xls");
            renderArgs.put(RenderExcel.RA_ASYNC, false);
            renderArgs.put("dmt", new SimpleDateFormat("yyyy-MM-dd-HH-mm-ss"));
            render(logs);
        } else {
            renderText("没有数据无法生成Excel文件！");
        }
    }


    public static void downloadShipmentTemplate() {
        request.format = "xlsx";
        renderArgs.put(RenderExcel.RA_FILENAME, "运输费用明细导入.xlsx");
        renderArgs.put(RenderExcel.RA_ASYNC, false);
        render();
    }


}
