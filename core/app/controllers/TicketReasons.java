package controllers;

import models.product.Category;
import models.support.TicketReason;
import play.data.validation.Validation;
import play.mvc.Controller;
import play.mvc.With;
import play.utils.FastRuntimeException;


/**
 * Created by IntelliJ IDEA.
 * User: wyattpan
 * Date: 7/26/12
 * Time: 5:05 PM
 */
@With({GlobalExceptionHandler.class, Secure.class, GzipFilter.class})
public class TicketReasons extends Controller {

    /**
     * 创建 TicketReason 需要指定原因
     *
     * @param catId
     */
    public static void blank(TicketReason lr, String catId) {
        if(lr == null) lr = new TicketReason();
        lr.category = Category.findById(catId);

        render(lr);
    }

    public static void save(TicketReason lr) {
        validation.valid(lr);
        if(Validation.hasErrors()) {
            render("TicketReasons/blank.html", lr, lr.category.categoryId);
        }
        lr.checkAndSave();
        redirect("/Categorys/index#" + lr.category.categoryId);
    }

    public static void update(TicketReason lr) {
        validation.valid(lr);
        if(Validation.hasErrors()) {
            render("TicketReasons/edit.html", lr);
        }
        lr.checkAndUpdate();
        redirect("/Categorys/index#" + lr.category.categoryId);
    }

    public static void edit(long lrid) {
        TicketReason lr = TicketReason.findById(lrid);
        render(lr);
    }

    public static void remove(long lrid) {
        TicketReason lr = TicketReason.findById(lrid);
        try {
            lr.checkAndRemove();
        } catch(FastRuntimeException e) {
            flash.error(e.getMessage());
        }
        redirect("/Categorys/index#" + lr.category.categoryId);
    }
}
