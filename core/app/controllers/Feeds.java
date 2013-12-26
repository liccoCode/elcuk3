package controllers;

import controllers.Check;
import controllers.GlobalExceptionHandler;
import controllers.Secure;
import models.market.Feed;
import models.product.Product;
import models.view.post.FeedPost;
import models.view.post.ProductPost;
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
    public static void index(FeedPost p) {
        if(p == null) p = new FeedPost();
        List<Feed> prods = p.query();
        render(prods, p);
    }
}
