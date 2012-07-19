package controllers;

import helper.J;
import helper.Webs;
import models.market.Account;
import models.view.Ret;
import play.data.validation.Validation;
import play.mvc.Controller;
import play.mvc.With;

import java.util.List;

/**
 * 对 Accounts 的操作
 * User: Wyatt
 * Date: 12-1-7
 * Time: 上午6:20
 */
@With({GlobalExceptionHandler.class, Secure.class, GzipFilter.class})
@Check("root")
public class Accounts extends Controller {
    public static void index() {
        List<Account> accs = Account.findAll();
        render(accs);
    }

    public static void update(Account a) {
        validation.valid(a);
        if(!a.isPersistent()) renderJSON(new Ret("Account is not persistent!"));
        if(Validation.hasErrors())
            renderJSON(new Ret(J.json(Validation.errors())));
        a.save();
        renderJSON(new Ret());
    }

    public static void create(Account a) {
        if(a.isPersistent()) renderJSON(new Ret("Account is exist, can not be CREATE!"));
        validation.valid(a);
        if(Validation.hasErrors())
            renderJSON(new Ret(J.json(Validation.errors())));
        a.save();
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
