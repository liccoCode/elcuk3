package controllers;

import com.alibaba.fastjson.JSON;
import controllers.api.SystemOperation;
import helper.Caches;
import helper.Currency;
import helper.Webs;
import jobs.analyze.SellingProfitJob;
import jobs.analyze.SellingSaleAnalyzeJob;
import models.market.OrderItem;
import models.procure.Deliveryment;
import models.procure.ProcureUnit;
import models.product.Category;
import models.product.Product;
import models.view.dto.AnalyzeDTO;
import models.view.dto.DeliveryExcel;
import models.view.dto.SaleReportDTO;
import models.view.dto.ShipmentWeight;
import models.view.post.*;
import models.view.report.LossRate;
import models.view.report.Profit;
import models.view.report.TrafficRate;
import org.apache.commons.lang.StringUtils;
import org.apache.commons.lang3.math.NumberUtils;
import org.joda.time.DateTime;
import play.cache.Cache;
import play.data.validation.Validation;
import play.db.helper.JpqlSelect;
import play.libs.F;
import play.modules.excel.RenderExcel;
import play.mvc.Controller;
import play.mvc.With;

import java.text.DecimalFormat;
import java.text.SimpleDateFormat;
import java.util.*;

import models.view.post.LossRatePost;

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
        render(excel);
    }

    /**
     * 下载采购单综合Excel表格
     */
    public static void deliveryments(DeliveryPost p) {
        List<Deliveryment> deliverymentList = p.query();
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
            if(p.sku != null) skukey = p.sku;
            if(p.pmarket != null) marketkey = p.pmarket;
            if(p.category != null) categorykey = p.category.toLowerCase();
            String postkey = helper.Caches.Q.cacheKey("profitpost", p.begin, p.end, categorykey, skukey, marketkey,
                    "excel");
            profits = Cache.get(postkey, List.class);
            if(profits == null) {
                new SellingProfitJob(p).now();
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


    public static void lossRateReport(LossRatePost p) {
        if(p == null) p = new LossRatePost();
        List<LossRate> lossrates = p.query();
        LossRate losstotal = p.querytotal();
        if(lossrates != null && lossrates.size() != 0) {
            SimpleDateFormat formatter = new SimpleDateFormat("yyyyMMdd");
            request.format = "xls";
            renderArgs.put(RenderExcel.RA_FILENAME,
                    String.format("%s-%s运输单丢失率报表.xls", formatter.format(p.from), formatter.format(p.to)));
            renderArgs.put(RenderExcel.RA_ASYNC, false);
            renderArgs.put("dateFormat", formatter);
            render(lossrates, losstotal, p);
        } else {
            renderText("没有数据无法生成Excel文件！");
        }
    }


    public static void skuSalesReport(Date from, Date to, String val) {
        List<F.T4<String, Long, Long, Float>> sales = OrderItem.querySalesBySkus(from, to, val);
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
}
