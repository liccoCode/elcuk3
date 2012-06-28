package controllers;

import helper.Webs;
import models.Ret;
import models.market.Account;
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
@Check("root")
public class Accounts extends Controller {
    public static void index() {
        List<Account> accs = Account.all().fetch();

        render(accs);
    }

    public static void update(Account a) {
        if(!a.isPersistent()) renderJSON(new Ret("Account is not persistent!"));
        try {
            a.save();
        } catch(Exception e) {
            renderJSON(new Ret(Webs.E(e)));
        }
        renderJSON(new Ret(true));
    }

    public static void create(Account a, String type) {
        if(a.isPersistent()) renderJSON(new Ret("Account is exist, can not be CREATE!"));
        try {
            a.type = Account.M.val(type);
            a.save();
        } catch(Exception e) {
            renderJSON(new Ret(Webs.E(e)));
        }
        renderJSON(new Ret(true));
    }

    public static void login(Account a) {
        if(!a.isPersistent()) renderJSON(new Ret("Account 不存在!"));
        try {
            a.loginWebSite();
            renderJSON(new Ret(true, String.format("%s login success.", a.prettyName())));
        } catch(Exception e) {
            renderJSON(new Ret(true, String.format("%s login faield. [%s]", a.prettyName(), Webs.E(e))));
        }
    }

}
