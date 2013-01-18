package controllers;

import helper.Constant;
import helper.Dates;
import helper.J;
import helper.Webs;
import models.market.*;
import models.procure.ProcureUnit;
import models.product.Category;
import models.product.Product;
import models.view.Ret;
import models.view.dto.AnalyzeDTO;
import models.view.post.AnalyzePost;
import org.apache.commons.io.FileUtils;
import org.apache.commons.lang.math.NumberUtils;
import org.joda.time.DateTime;
import org.joda.time.DateTimeZone;
import org.joda.time.format.DateTimeFormat;
import play.Logger;
import play.Play;
import play.cache.Cache;
import play.cache.CacheFor;
import play.mvc.After;
import play.mvc.Before;
import play.mvc.Controller;
import play.mvc.With;
import play.utils.FastRuntimeException;
import query.OrderItemQuery;
import query.vo.AnalyzeVO;

import java.io.File;
import java.io.IOException;
import java.util.*;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * 数据分析页面的控制器
 * TODO 备忘: 采购计划的 timeline 库 -> http://www.simile-widgets.org/timeline/
 * User: wyattpan
 * Date: 1/19/12
 * Time: 2:14 PM
 */
@With({GlobalExceptionHandler.class, Secure.class})
public class Analyzes extends Controller {

    @Check("analyzes.index")
    public static void index() {
        List<Account> accs = Account.openedSaleAcc();
        List<String> categoryIds = Category.category_ids();
        render(accs, categoryIds);
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
            Logger.info("%s past %s", request.action,
                    System.currentTimeMillis() - NumberUtils.toLong(begin.toString()));
        }
    }

    /**
     * 分析页面下方的 sku/sid table
     *
     * @param p
     */
    public static void analyzes(AnalyzePost p) {
        List<AnalyzeDTO> dtos = p.query();
        render("Analyzes/" + p.type + ".html", dtos, p);
    }

    public static void clear() {
        Cache.delete("analyze_post_sid");
        Cache.delete("analyze_post_sku");
        renderJSON(new Ret());
    }

    @Check("analyzes.allskucsv")
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
     */
    @CacheFor("30mn")
    public static void ajaxUnit(AnalyzePost p) {
        try {
            renderJSON(J.json(OrderItem.ajaxHighChartUnitOrder(p.val,
                    Account.<Account>findById(NumberUtils.toLong(p.aid)), p.type, p.from, p.to)));
        } catch(Exception e) {
            renderJSON(new Ret(Webs.S(e)));
        }
    }

    @Check("analyzes.ajaxsales")
    @CacheFor("30mn")
    public static void ajaxSales(AnalyzePost p) {
        try {
            renderJSON(J.json(OrderItem
                    .ajaxHighChartSales(p.val, Account.<Account>findById(NumberUtils.toLong(p.aid)),
                            p.type, p.from, p.to)));
        } catch(Exception e) {
            renderJSON(new Ret(Webs.S(e)));
        }
    }

    /**
     * 查看某一个 Selling 在一段时间内的 PageView & Session 数量
     */
    @CacheFor("30mn")
    public static void ajaxSellingRecord(AnalyzePost p) {
        try {
            renderJSON(J.json(SellingRecord.ajaxHighChartPVAndSS(p.val,
                    Account.<Account>findById(NumberUtils.toLong(p.aid)), p.from, p.to)));
        } catch(Exception e) {
            renderJSON(new Ret(Webs.S(e)));
        }
    }

    /**
     * 查看某一个 Selling 在一段时间内的转换率
     */
    @CacheFor("30mn")
    public static void ajaxSellingTurn(AnalyzePost p) {
        try {
            renderJSON(J.json(SellingRecord.ajaxHighChartTurnRatio(p.val,
                    Account.<Account>findById(NumberUtils.toLong(p.aid)), p.from, p.to)));
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
    @CacheFor("30mn")
// 这个方法提供缓存, 但是前台使用 POST 计算, 不用缓存, 因为此方法在 Hibernate 的二级缓存与系统缓存的支持下, 执行在 0~20 ms 左右
    public static void ajaxProcureUnitTimeline(String type, String val) {
        renderJSON(J.G(ProcureUnit.timelineEvents(type, val)));
    }

    /**
     * 给出某一天订单销量的时间区间饼图
     *
     * @param msku
     */
    public static void pie(String msku, Date date) {
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

    /**
     * 日期测试代码, 保留
     *
     * @param date
     */
    public static void test() {
        Date date = new Date();
        DateTime gmt = DateTime.parse(Dates.date2DateTime(date),
                DateTimeFormat.forPattern("yyyy-MM-dd HH:mm:ss").withZone(DateTimeZone.UTC));
        DateTime de = Dates.fromDatetime(Dates.date2DateTime(date), M.AMAZON_DE);
        DateTime uk = Dates.fromDatetime(Dates.date2DateTime(date), M.AMAZON_UK);
        DateTime us = Dates.fromDatetime(Dates.date2DateTime(date), M.AMAZON_US);
        renderText("根据当前时间的字符串,加上不同时区,最后统一的 CST(China Standard Time)时间" +
                "\nTimeZone:%s" +
                "\nCN:%s\nGMT:%tc\nDE:%s\nUK:%s\nUS:%s",
                TimeZone.getDefault(),
                date, gmt.toDate(), de.toDate(), uk.toDate(), us.toDate());
    }

    public static void test2() {
        DateTime from = DateTime.parse("2012-11-01");
        DateTime to = DateTime.parse("2012-11-02");
        List<AnalyzeVO> vos = new OrderItemQuery().analyzeVos(from.toDate(), to.toDate());
        renderJSON(vos);
    }

}
