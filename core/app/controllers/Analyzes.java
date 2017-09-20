package controllers;

import controllers.api.SystemOperation;
import helper.J;
import helper.Webs;
import models.OperatorConfig;
import models.User;

import models.market.*;
import models.product.Category;
import models.view.Ret;
import models.view.dto.AnalyzeDTO;
import models.view.highchart.HighChart;
import models.view.post.AnalyzePost;
import models.view.post.TrafficRatePost;
import models.view.report.TrafficRate;
import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang.math.NumberUtils;
import play.Logger;
import play.Play;
import play.cache.CacheFor;
import play.jobs.Job;
import play.mvc.After;
import play.mvc.Before;
import play.mvc.Controller;
import play.mvc.With;
import play.utils.FastRuntimeException;

import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;


/**
 * 数据分析页面的控制器
 * 备忘: 采购计划的 timeline 库 -> http://www.simile-widgets.org/timeline/
 * User: wyattpan
 * Date: 1/19/12
 * Time: 2:14 PM
 */
@With({GlobalExceptionHandler.class, Secure.class, SystemOperation.class})
public class Analyzes extends Controller {

    @Check("analyzes.index")
    public static void index() {
        List<Account> accs = Account.openedSaleAcc();
        List<String> categoryIds = Category.categoryIds();
        AnalyzePost p = new AnalyzePost();
        render(accs, categoryIds, p);
    }

    @Before(only = {"analyzes", "ajaxUnit"})
    public static void countTime() {
        if(Play.mode.isProd()) return;
        request.args.put("begin", System.currentTimeMillis() + "");
    }

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
    public static void analyzes(final AnalyzePost p) {
        try {
            List<AnalyzeDTO> dtos = p.query();
            User user = User.findById(Login.current().id);
            List<String> categories =
                    user.categories.stream().map(category -> category.categoryId).collect(Collectors.toList());
            dtos = p.queryByPrivate(dtos, categories);
            render("Analyzes/" + p.type + ".html", dtos, p);
        } catch(FastRuntimeException e) {
            renderHtml("<h3>" + e.getMessage() + "</h3>");
        }
    }


    /**
     * 流量转化率统计报表
     *
     * @param p
     */
    public static void trafficRate(TrafficRatePost p) {
        try {
            if(p == null) p = new TrafficRatePost();
            List<TrafficRate> trs = p.query();
            render(trs, p);
        } catch(FastRuntimeException e) {
            renderHtml("<h3>" + e.getMessage() + "</h3>");
        }
    }


    /**
     * 加载指定 Selling 的时间段内的销量与销售额数据
     */
    public static void ajaxUnit(final AnalyzePost p) {
        try {
            HighChart chart = await(new Job<HighChart>() {
                @Override
                public HighChart doJobWithResult() throws Exception {
                    return OrderItem.ajaxHighChartUnitOrder(p.val, p.type, p.from, p.to);
                }
            }.now());
            String countryName = p.countryName(false);
            chart.series.forEach(se -> se.visible = se.name.contains(countryName));
            renderJSON(J.json(chart));
        } catch(Exception e) {
            renderJSON(new Ret(Webs.e(e)));
        }
    }

    public static void ajaxMovingAve(final AnalyzePost p) {
        try {
            final M m = M.val(p.market);
            HighChart chart = await(new Job<HighChart>() {
                @Override
                public HighChart doJobWithResult() throws Exception {
                    return OrderItem.ajaxHighChartMovinAvg(p.val, p.type, m, p.from, p.to);
                }
            }.now());
            renderJSON(J.json(chart));
        } catch(Exception e) {
            renderJSON(new Ret(Webs.e(e)));
        }
    }

    /**
     * 查看某一个 Selling 在一段时间内的 PageView & Session 数量
     */
    @CacheFor("30mn")
    public static void ajaxSellingRecord(final AnalyzePost p) {
        try {
            String brandname = OperatorConfig.getVal("brandname");
            if(Objects.equals(brandname, "EASYACC")) {
                String json = await(new Job<String>() {
                    @Override
                    public String doJobWithResult() throws Exception {
                        HighChart chart = SellingRecord
                                .ajaxHighChartPVAndSS(p.val, Account.findById(NumberUtils.toLong(p.aid)), p.from, p.to);
                        String sortName = p.countryName(true);
                        chart.series.forEach(se -> se.visible = se.name.contains(sortName));
                        return J.json(chart);
                    }
                }.now());
                renderJSON(json);
            } else {
                renderJSON(J.json(new HighChart()));
            }
        } catch(Exception e) {
            renderJSON(new Ret(Webs.s(e)));
        }
    }

    /**
     * 查看某一个 Selling 在一段时间内的转换率
     */
    @CacheFor("30mn")
    public static void ajaxSellingTurn(final AnalyzePost p) {
        try {
            String brandname = OperatorConfig.getVal("brandname");
            if(Objects.equals(brandname, "EASYACC")) {
                String json = await(new Job<String>() {
                    @Override
                    public String doJobWithResult() throws Exception {
                        HighChart chart = SellingRecord
                                .ajaxHighChartTurnRatio(p.val, Account.findById(NumberUtils.toLong(p.aid)), p.from,
                                        p.to);
                        String sortName = p.countryName(true);
                        chart.series.forEach(se -> se.visible = se.name.contains(sortName));
                        return J.json(chart);
                    }
                }.now());
                renderJSON(json);
            } else {
                renderJSON(J.json(new HighChart()));
            }
        } catch(Exception e) {
            renderJSON(new Ret(Webs.e(e)));
        }
    }

    /**
     * type 只允许为 sku 与 sid 两种类型
     *
     * @param type
     * @param val
     */
    public static void ajaxProcureUnitTimeline(String type, String val) {
        renderJSON(J.g(AnalyzePost.timelineEvents(type, val)));
    }

    public static void ps(String sid, Float ps) {
        Selling sell = Selling.findById(sid);
        if(sell == null || !sell.isPersistent()) throw new FastRuntimeException("Selling 不合法.");
        renderJSON(J.g(sell.ps(ps)));
    }

}
