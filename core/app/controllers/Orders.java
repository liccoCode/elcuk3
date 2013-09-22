package controllers;

import models.market.Account;
import models.market.Orderr;
import models.view.post.OrderPOST;
import play.mvc.Controller;
import play.mvc.With;

import java.util.List;

/**
 * Created by IntelliJ IDEA.
 * User: Wyatt
 * Date: 12-1-6
 * Time: 下午4:02
 */
@With({GlobalExceptionHandler.class, Secure.class})
public class Orders extends Controller {

    public static void index(OrderPOST p) {
        List<Account> accs = Account.openedSaleAcc();
        if(p == null) p = new OrderPOST();
        List<Orderr> orders = p.query();
        render(p, orders, accs);
    }

    public static void show(String id) {
        Orderr ord = Orderr.findById(id);
        render(ord);
    }
}
