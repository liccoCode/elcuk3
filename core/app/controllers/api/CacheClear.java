package controllers.api;

import helper.Caches;
import helper.Dates;
import jobs.analyze.SellingSaleAnalyzeJob;
import models.market.M;
import models.product.Team;
import models.view.Ret;
import models.view.highchart.HighChart;
import org.joda.time.DateTime;
import play.cache.Cache;
import play.mvc.Controller;
import play.mvc.With;

import java.util.Date;
import java.util.Map;
import java.util.Iterator;

/**
 * ES执行后需要清理缓存，保证数据及时
 * User: mac
 * Date: 14-3-27
 * Time: 上午10:12
 */
@With({APIChecker.class})
public class CacheClear extends Controller {
    /**
     * es执行完后清理缓存
     */
    public static void esCacheClear() {

        /**删除与销量相关的redis信息**/
        Cache.delete(SellingSaleAnalyzeJob.AnalyzeDTO_SKU_CACHE);
        Cache.delete(SellingSaleAnalyzeJob.AnalyzeDTO_SID_CACHE);
        renderJSON(new Ret(true, "清理缓存成功!"));
    }
}
