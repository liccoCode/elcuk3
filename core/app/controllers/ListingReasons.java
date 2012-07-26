package controllers;

import models.market.ListingReason;
import models.product.Category;
import play.data.validation.Validation;
import play.mvc.Controller;
import play.mvc.With;


/**
 * Created by IntelliJ IDEA.
 * User: wyattpan
 * Date: 7/26/12
 * Time: 5:05 PM
 */
@With({GlobalExceptionHandler.class, Secure.class, GzipFilter.class})
public class ListingReasons extends Controller {

    /**
     * 创建 ListingReason 需要指定原因
     *
     * @param catId
     */
    public static void blank(ListingReason lr, String catId) {
        if(lr == null) lr = new ListingReason();
        lr.category = Category.findById(catId);

        render(lr);
    }

    public static void save(ListingReason lr) {
        validation.valid(lr);
        if(Validation.hasErrors()) {
            render("ListingReasons/blank.html", lr, lr.category.categoryId);
        }
        lr.checkAndSave();
        redirect("/Categorys/index#" + lr.category.categoryId);
    }

    public static void update(ListingReason lr) {
        validation.valid(lr);
        if(Validation.hasErrors()) {
            render("ListingReasons/edit.html", lr);
        }
        lr.checkAndUpdate();
        redirect("/Categorys/index#" + lr.category.categoryId);
    }

    public static void edit(long lrid) {
        ListingReason lr = ListingReason.findById(lrid);
        render(lr);
    }

    public static void remove(long lrid) {
        ListingReason lr = ListingReason.findById(lrid);
        lr.delete();
        redirect("/Categorys/index#" + lr.category.categoryId);
    }
}
