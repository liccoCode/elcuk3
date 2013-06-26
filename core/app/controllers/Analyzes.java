package controllers;

import helper.J;
import helper.Webs;
import models.market.*;
import models.product.Category;
import models.view.Ret;
import models.view.dto.AnalyzeDTO;
import models.view.dto.HighChart;
import models.view.post.AnalyzePost;
import org.apache.commons.lang.math.NumberUtils;
import org.joda.time.DateTime;
import play.Logger;
import play.Play;
import play.cache.Cache;
import play.cache.CacheFor;
import play.jobs.Job;
import play.mvc.After;
import play.mvc.Before;
import play.mvc.Controller;
import play.mvc.With;
import play.utils.FastRuntimeException;

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
@With({GlobalExceptionHandler.class, Secure.class})
public class Analyzes extends Controller {

    @Check("analyzes.index")
    public static void index() {
        List<Account> accs = Account.openedSaleAcc();
        List<String> categoryIds = Category.category_ids();
        AnalyzePost p = new AnalyzePost();
        render(accs, categoryIds, p);
    }

    @Before(only = {"analyzes", "ajaxUnit"})
    public static void countTime() {
        if(Play.mode.isProd()) return;
        request.args.put("begin", System.currentTimeMillis() + "");
    }

    //
    @After(only = {"analyzes", "ajaxUnit"})
    public static void countAfter() {
        if(Play.mode.isProd()) return;
        Object begin = request.args.get("begin");
        Logger.info("%s past %s ms", request.action, System.currentTimeMillis() - NumberUtils.toLong(begin.toString()));
    }


    /**
     * 分析页面下方的 sku/sid table
     *
     * @param p
     */
    public static void analyzes(AnalyzePost p) {
        try {
            final AnalyzePost copy = p.clone();
            List<AnalyzeDTO> dtos = await(new Job<List<AnalyzeDTO>>() {
                @Override
                public List<AnalyzeDTO> doJobWithResult() throws Exception {
                    return copy.query();
                }
            }.now());
            render("Analyzes/" + p.type + ".html", dtos, p);
        } catch(CloneNotSupportedException e) {
            throw new FastRuntimeException(e);
        }
    }

    public static void clear() {
        Cache.delete(AnalyzePost.AnalyzeDTO_SID_CACHE);
        Cache.delete(AnalyzePost.AnalyzeDTO_SKU_CACHE);
        renderJSON(new Ret());
    }

    /**
     * 加载指定 Selling 的时间段内的销量与销售额数据
     */
    public static void ajaxUnit(AnalyzePost p) {
        try {
            response.cacheFor("10mn");
            final AnalyzePost copy = p.clone();
            HighChart chart = await(new Job<HighChart>() {
                @Override
                public HighChart doJobWithResult() throws Exception {
                    return OrderItem.ajaxHighChartUnitOrder(copy.val, copy.type, copy.from, copy.to);
                }
            }.now());
            renderJSON(J.json(chart));
        } catch(Exception e) {
            renderJSON(new Ret(Webs.S(e)));
        }
    }

    @Check("analyzes.ajaxsales")
    public static void ajaxSales(AnalyzePost p) {
        try {
            response.cacheFor("10mn");
            final AnalyzePost copy = p.clone();
            HighChart chart = await(new Job<HighChart>() {
                @Override
                public HighChart doJobWithResult() throws Exception {
                    return OrderItem.ajaxHighChartSales(copy.val, copy.type, copy.from, copy.to);
                }
            }.now());
            renderJSON(J.json(chart));
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
            final AnalyzePost copy = p.clone();
            String json = await(new Job<String>() {
                @Override
                public String doJobWithResult() throws Exception {
                    return J.json(SellingRecord.ajaxHighChartPVAndSS(copy.val,
                            Account.<Account>findById(NumberUtils.toLong(copy.aid)), copy.from, copy.to));
                }
            }.now());
            renderJSON(json);
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
            final AnalyzePost copy = p.clone();
            String json = await(new Job<String>() {
                @Override
                public String doJobWithResult() throws Exception {
                    return J.json(SellingRecord.ajaxHighChartTurnRatio(copy.val,
                            Account.<Account>findById(NumberUtils.toLong(copy.aid)), copy.from, copy.to));
                }
            }.now());
            renderJSON(json);
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
    public static void ajaxProcureUnitTimeline(String type, String val) {
        renderJSON(J.G(AnalyzePost.timelineEvents(type, val)));
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
        response.cacheFor("10mn");
        render(datax, datay, date);
    }

    public static void ps(String sid, Float ps) {
        Selling sell = Selling.findById(sid);
        if(sell == null || !sell.isPersistent()) throw new FastRuntimeException("Selling 不合法.");
        renderJSON(J.G(sell.ps(ps)));
    }

}
