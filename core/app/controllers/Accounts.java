package controllers;

import com.alibaba.fastjson.JSON;
import models.market.Account;
import play.data.validation.Valid;
import play.data.validation.Validation;
import play.mvc.Controller;

/**
 * 对 Accounts 的操作
 * User: Wyatt
 * Date: 12-1-7
 * Time: 上午6:20
 */
public class Accounts extends Controller {
    public static void p(Integer page) {
    }

    public static void c(@Valid Account a) {
        if(Validation.hasErrors()) {
            renderJSON(validation.errorsMap());
        }
        a.save();
        renderJSON(a);
    }

    public static void r(Long id) {
        renderJSON(JSON.toJSONString(Account.findById(id)));
    }

    public static void u(@Valid Account acc) {
        if(Validation.hasErrors()) {
            renderJSON(validation.errorsMap());
        }
        if(acc.uniqueName != null && acc.id != null) {
            acc.save();
            renderJSON(acc);
        } else {
            renderJSON("{flag: false}");
        }
    }
}
