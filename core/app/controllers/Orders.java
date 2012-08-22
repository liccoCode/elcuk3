package controllers;

import models.market.Account;
import models.market.Feedback;
import models.market.Orderr;
import models.view.OrderPOST;
import models.view.Pager;
import play.cache.CacheFor;
import play.mvc.Controller;
import play.mvc.With;

import java.util.List;

/**
 * Created by IntelliJ IDEA.
 * User: Wyatt
 * Date: 12-1-6
 * Time: 下午4:02
 */
@With({GlobalExceptionHandler.class, Secure.class, GzipFilter.class})
public class Orders extends Controller {

    public static void index(Integer p) {
        List<Orderr> orders = Orderr.find("ORDER BY createDate DESC").fetch(p, 100);
        Long count = Orderr.count();
        Pager<Orderr> pi = new Pager<Orderr>(count, p, orders);
        List<Account> accs = Account.openedSaleAcc();


        render(orders, count, p, pi, accs);
    }

    public static void show(String oid) {
        Feedback f = Feedback.findById(oid);
        if(f != null)
            redirect("Feedbacks.show", oid);
        else {
            Orderr ord = Orderr.findById(oid);
            render(ord);
        }
    }

    /**
     * Orders 页面的搜索方法
     *
     * @param p
     */
    public static void search(OrderPOST p) {
        List<Orderr> orders = p.query();
        Long count = p.count();
        Pager<Orderr> pi = new Pager<Orderr>(p.size, count, p.page, orders);

        render(pi);
    }
}
