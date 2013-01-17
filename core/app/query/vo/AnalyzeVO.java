package query.vo;

import jobs.promise.AnalyzeLineForkPromise;
import jobs.promise.AnalyzePostForkPromise;
import models.market.M;
import org.joda.time.DateTime;
import play.libs.F;
import play.utils.FastRuntimeException;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.concurrent.TimeUnit;

/**
 * --- VO 仅仅在业务层之间传递数据, 不要将 VO 传递到 Web 页面上 ---
 * <p/>
 * 值对象, 在使用 SQL 语句的时候, 一些无法满足从不同 Models 中加载出来的数据,
 * 封装为一个 VO 对象传递到前面去.
 * (数量少的时候 Tuple 可以解决, 当字段多了, Tuple 好难维护)
 * User: wyatt
 * Date: 1/17/13
 * Time: 11:47 AM
 */
public class AnalyzeVO {
    public String sku;
    public String sid;
    public String asin;

    public Integer qty;
    public Date date;

    public String aid;
    public Float usdCost;

    public M market;

    //TODO 这里很明显可以传递一个闭包进来处理, 但在这里使用一个回掉函数编写代码代价太大, 还不如重复...

    /**
     * 执行市场, 加载执行不同市场不同时间的日期;
     * Selling 排名
     *
     * @param from
     * @param to
     * @param markets
     */
    public static List<AnalyzeVO> marketsSellingRanks(DateTime from, DateTime to, M... markets) {
        List<F.Promise<List<AnalyzeVO>>> vos = new ArrayList<F.Promise<List<AnalyzeVO>>>();

        for(M m : markets) {
            vos.add(new AnalyzePostForkPromise(from, to, m).now());
        }
        // 结果汇总
        List<AnalyzeVO> marketsVos = new ArrayList<AnalyzeVO>();
        try {
            for(F.Promise<List<AnalyzeVO>> p : vos) {
                marketsVos.addAll(p.get(5, TimeUnit.SECONDS));
            }
        } catch(Exception e) {
            throw new FastRuntimeException(
                    String.format("发生错误 %s, 请稍等片刻后重试", e.getMessage()));
        }
        return marketsVos;
    }

    /**
     * 加载不同市场的曲线数据
     *
     * @param typeAndMSKU t2: _1:skuMsku, _2:type
     * @param accid
     * @param from
     * @param to
     * @param markets
     * @return
     */
    public static List<AnalyzeVO> marketsLines(F.T2<String, String> typeAndMSKU,
                                               Long accid,
                                               DateTime from,
                                               DateTime to,
                                               M... markets) {
        List<F.Promise<List<AnalyzeVO>>> vos = new ArrayList<F.Promise<List<AnalyzeVO>>>();

        for(M m : markets) {
            vos.add(new AnalyzeLineForkPromise(from, to, m, typeAndMSKU, accid).now());
        }
        List<AnalyzeVO> marketsVos = new ArrayList<AnalyzeVO>();
        try {
            // 结果汇总
            for(F.Promise<List<AnalyzeVO>> p : vos) {
                marketsVos.addAll(p.get(5, TimeUnit.SECONDS));
            }
        } catch(Exception e) {
            throw new FastRuntimeException(
                    String.format("发生错误 %s, 请稍等片刻后重试", e.getMessage()));
        }
        return marketsVos;
    }
}
