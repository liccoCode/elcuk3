package controllers;

import helper.Webs;
import models.PageInfo;
import models.product.Category;
import play.data.validation.Valid;
import play.data.validation.Validation;
import play.mvc.Controller;

import java.util.List;

/**
 * Created by IntelliJ IDEA.
 * User: wyattpan
 * Date: 4/26/12
 * Time: 11:05 AM
 */
public class Categorys extends Controller {

    public static void index(Integer p, Integer s) {
        Webs.fixPage(p, s);
        List<Category> cates = Category.all().fetch(p, s);
        Long count = Category.count();

        PageInfo<Category> pi = new PageInfo<Category>(s, count, p, cates);
        render(cates, count, p, s, pi);
    }

    /**
     * Category Create
     *
     * @param c
     */
    public static void cc(@Valid Category c) {
        if(Validation.hasErrors()) renderJSON(validation.errorsMap());
        if(!c.isPersistent())
            renderJSON(new play.data.validation.Error("Category", "Category is not Persistent!", new String[]{}));
        c.save();
        c.products = null;
        renderJSON(c);
    }
}
