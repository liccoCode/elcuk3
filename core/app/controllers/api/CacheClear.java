package controllers.api;

import models.view.Ret;
import play.cache.Cache;
import play.mvc.Controller;
import play.mvc.With;

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
        Cache.clear();
        renderJSON(new Ret(true,"清理缓存成功!"));
    }
}