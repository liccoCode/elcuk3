package controllers.api;

import models.view.Ret;
import play.cache.Cache;
import play.mvc.Controller;

/**
 * ES执行后需要清理缓存，保证数据及时
 * User: mac
 * Date: 14-3-27
 * Time: 上午10:12
 */
public class APICache extends Controller {
    /**
     * es执行完后清理缓存
     */
    public static void esCacheClear() {
        Cache.clear();
        renderJSON(new Ret(true, "清理缓存成功!"));
    }
}
