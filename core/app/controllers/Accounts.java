package controllers;

import controllers.api.SystemOperation;
import helper.J;
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
@With({GlobalExceptionHandler.class, Secure.class, SystemOperation.class})
public class Accounts extends Controller {

    @Check("accounts.index")
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

    public static void blank(Account a) {
        if(a == null) a = new Account();
        render(a);
    }

    public static void create(Account a) {
        if(a.isPersistent()) renderJSON(new Ret("Account is exist, can not be CREATE!"));
        validation.valid(a);
        if(Validation.hasErrors()) {
            render("Accounts/blank.html", a);
        }
        a.save();
        redirect("/Accounts/index#" + a.id);
    }
}
