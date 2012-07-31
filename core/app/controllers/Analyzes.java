package controllers;

import helper.Constant;
import helper.J;
import helper.Webs;
import models.market.*;
import models.procure.ProcureUnit;
import models.product.Product;
import models.view.AnalyzesPager;
import models.view.Ret;
import org.apache.commons.io.FileUtils;
import org.apache.commons.lang.math.NumberUtils;
import org.joda.time.DateTime;
import play.Logger;
import play.Play;
import play.cache.CacheFor;
import play.mvc.After;
import play.mvc.Before;
import play.mvc.Controller;
import play.mvc.With;
import play.utils.FastRuntimeException;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * 数据分析页面的控制器
 * TODO 备忘: 采购计划的 timeline 库 -> http://www.simile-widgets.org/timeline/
 * User: wyattpan
 * Date: 1/19/12
 * Time: 2:14 PM
 */
@With({GlobalExceptionHandler.class, Secure.class, GzipFilter.class})
@Check("manager")
public class Analyzes extends Controller {
    public static void index() {
        List<Account> accs = Account.openedSaleAcc();
        render(accs);
    }

    // 开发用
    @Before
    public static void countTime() {
        if(Play.mode.isDev())
            request.args.put("begin", System.currentTimeMillis() + "");
    }

    //
    @After
    public static void countAfter() {
        if(Play.mode.isDev()) {
            Object begin = request.args.get("begin");
            Logger.info("%s past %s", request.action, System.currentTimeMillis() - NumberUtils.toLong(begin.toString()));
        }
    }

    /**
     * Analyze 页面下部分的 Selling 信息
     */
    public static void index_msku(AnalyzesPager<Selling> p) {
        List<Selling> sells = Selling.analyzesSKUAndSID("msku");
        p.items = AnalyzesPager.filterSellings(sells, p);
        render(p);
    }

    public static void index_sku(AnalyzesPager<Selling> p) {
        List<Selling> sells = Selling.analyzesSKUAndSID("sku");
        p.items = AnalyzesPager.filterSellings(sells, p);
        render(p);
    }

    public static void allSkuCsv(Date from, Date to) {
        String fileName = "SKU_Sales.csv";
        File file = new File(Constant.TMP, fileName);
        file.delete();
        try {
            FileUtils.writeStringToFile(file, Product.skuSales(from, to));
        } catch(IOException e) {
            // ignore
        }
        renderBinary(file, fileName);

    }

    /**
     * 加载指定 Selling 的时间段内的销量与销售额数据
     *
     * @param msku
     * @param from
     * @param to
     */
    @CacheFor("30mn")
    public static void ajaxUnit(String msku,
                                String type,
                                Account acc,
                                Date from,
                                Date to) {
        if(!acc.isPersistent()) acc = null;
        try {
            renderJSON(J.json(OrderItem.ajaxHighChartUnitOrder(msku, acc, type, from, to)));
        } catch(Exception e) {
            renderJSON(new Ret(Webs.S(e)));
        }
    }

    @Check("root")
    @CacheFor("30mn")
    public static void ajaxSales(String msku,
                                 String type,
                                 Account acc,
                                 Date from,
                                 Date to) {
        if(!acc.isPersistent()) acc = null;
        try {
            renderJSON(J.json(OrderItem.ajaxHighChartSales(msku, acc, type, from, to)));
        } catch(Exception e) {
            renderJSON(new Ret(Webs.S(e)));
        }
    }

    /**
     * 查看某一个 Selling 在一段时间内的 PageView & Session 数量
     */
    @CacheFor("30mn")
    public static void ajaxSellingRecord(String msku,
                                         Account acc,
                                         Date from,
                                         Date to) {
        try {
            renderJSON(J.json(SellingRecord.ajaxHighChartPVAndSS(msku, acc, from, to)));
        } catch(Exception e) {
            renderJSON(new Ret(Webs.E(e)));
        }
    }

    /**
     * 查看某一个 Selling 在一段时间内的转换率
     */
    @CacheFor("30mn")
    public static void ajaxSellingTurn(String msku,
                                       Account acc,
                                       Date from,
                                       Date to) {
        try {
            renderJSON(J.json(SellingRecord.ajaxHighChartTurnRatio(msku, acc, from, to)));
        } catch(Exception e) {
            renderJSON(new Ret(Webs.E(e)));
        }
    }

    /**
     * type 只允许为 sku 与 sid 两种类型
     *
     * @param type
     * @param val
     */
    @CacheFor("30mn")// 这个方法提供缓存, 但是前台使用 POST 计算, 不用缓存, 因为此方法在 Hibernate 的二级缓存与系统缓存的支持下, 执行在 0~20 ms 左右
    public static void ajaxProcureUnitTimeline(String type, String val) {
        renderJSON(J.G(ProcureUnit.timelineEvents(type, val)));
    }

    /**
     * 给出某一天订单销量的时间区间饼图
     *
     * @param msku
     */
    public static void pie(String msku,
                           Date date) {
        Map<String, AtomicInteger> dataMap = Orderr.orderPieChart(msku, date);
        List<String> datax = new ArrayList<String>();
        for(String key : dataMap.keySet()) {
            datax.add("'" + new DateTime(Long.parseLong(key)).toString("HH:mm:ss") + "'");
        }
        List<AtomicInteger> datay = new ArrayList<AtomicInteger>(dataMap.values());
        render(datax, datay, date);
    }

    public static void ps(String sid, Float ps) {
        Selling sell = Selling.findById(sid);
        if(sell == null || !sell.isPersistent()) throw new FastRuntimeException("Selling 不合法.");
        renderJSON(J.G(sell.ps(ps)));
    }
}
