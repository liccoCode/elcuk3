package controllers;

import models.market.Account;
import play.data.validation.Error;
import play.mvc.Controller;
import play.mvc.With;

import java.util.List;

/**
 * 对 Accounts 的操作
 * User: Wyatt
 * Date: 12-1-7
 * Time: 上午6:20
 */
@With({Secure.class, GzipFilter.class})
public class Accounts extends Controller {
    public static void index() {
        List<Account> accs = Account.all().fetch();

        render(accs);
    }

    public static void update(Account a) {
        if(!a.isPersistent()) renderJSON(new Error("Account", "Account is not persistent!", new String[]{}));
        try {
            a.save();
        } catch(Exception e) {
            renderJSON(new Error("Exception", e.getClass().getSimpleName() + "|" + e.getMessage(), new String[]{}));
        }
        renderJSON("{\"flag\":\"true\"}");
    }

    public static void create(Account a, String type) {
        if(a.isPersistent()) renderJSON(new Error("Account", "Account is exist, can not be CREATE!", new String[]{}));
        try {
            a.type = Account.M.val(type);
            a.save();
        } catch(Exception e) {
            renderJSON(new Error("Exception", e.getClass().getSimpleName() + "|" + e.getMessage(), new String[]{}));
        }
        renderJSON("{\"flag\":\"true\"}");
    }

}
