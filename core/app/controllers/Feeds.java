package controllers;

import models.market.Feed;
import play.mvc.Controller;
import play.mvc.With;

import java.util.List;

/**
 * 对 Feed 的相关操作
 * User: mac
 * Date: 13-12-26
 * Time: 下午5:07
 */
@With({GlobalExceptionHandler.class, Secure.class})
public class Feeds extends Controller {

    /**
     * 展示所有的 Feed
     */
    @Check("products.index")
    public static void index() {
        List<Feed> feeds = Feed.find("order by id").fetch();
        render(feeds);
    }

    /**
     * 显示具体某一个 Feed 的信息
     */
    public static void show(Long id) {
        Feed feed = Feed.findById(id);
        render(feed);
    }
}
