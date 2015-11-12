package controllers.api;

import helper.Caches;
import helper.HTTP;
import jobs.analyze.SellingSaleAnalyzeJob;
import models.product.Category;
import models.view.Ret;
import org.joda.time.DateTime;
import play.cache.Cache;
import play.mvc.Controller;
import play.mvc.With;

import java.util.Date;
import java.util.List;

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
        Date to = DateTime.now().toDate();
        Date from = DateTime.now().plusMonths(-1).toDate();
        String unitkey = ajaxUnitOrderKey("all", "sid", from, to);
        Cache.delete(unitkey);

        List<Category> catelist = Category.findAll();
        for(Category cat : catelist) {
            String catekey = ajaxUnitOrderKey(cat.categoryId, "sid", from, to);
            Cache.delete(catekey);
        }
        /** 重新缓存最新的数据 **/
        HTTP.get("http://"+models.OperatorConfig.getVal("rockendurl")+":4567/selling_sale_analyze");

        renderJSON(new Ret(true, "清理缓存成功!"));
    }


    public static String ajaxUnitOrderKey(String val, String type, Date from, Date to) {
        String key = Caches.Q.cacheKey("unit", val, type, from, to);
        return key;
    }

}
