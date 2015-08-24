package controllers;

import controllers.api.SystemOperation;
import play.mvc.Controller;
import play.mvc.With;

/**
 * 市场分析控制器
 * Created by IntelliJ IDEA.
 * User: mac
 * Date: 15-1-5
 * Time: AM9:57
 */
@With({GlobalExceptionHandler.class, Secure.class, SystemOperation.class})
public class MarketAnalysis extends Controller {
    /**
     * 渠道(数据抓取任务)
     */
    public static void channels() {
        render();
    }

    /**
     * Listing 属性标签
     */
    public static void tags() {
        render();
    }

    /**
     * 卖家分析
     */
    public static void reports() {
        render();
    }
}
