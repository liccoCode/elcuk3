package controllers;

import models.Ret;
import models.product.Brand;
import models.product.Category;
import models.product.Family;
import play.mvc.Controller;

import java.util.ArrayList;
import java.util.List;

/**
 * Family 的控制
 * User: wyattpan
 * Date: 4/27/12
 * Time: 11:36 AM
 */
public class Familys extends Controller {
    public static void index() {
        render();
    }

    public static void create(Family f) {
        if(f != null && f.isPersistent()) renderJSON(new Ret("Family 已经存在, 不需要添加."));
        f.save();
        renderJSON(new Ret());
    }

    public static void cat_div(Brand b) {
        List<Category> cats = null;

        // 查找 Brand 相关的 Category
        if(b != null && b.isPersistent()) cats = b.categories;
        else cats = Category.all().fetch();

        render(cats);
    }

    public static void brand_div(Category c) {
        List<Brand> brands = null;

        if(c != null && c.isPersistent()) brands = c.brands;
        else brands = Brand.all().fetch();

        render(brands);
    }

    public static void fam_div(Brand b, Category c) {
        List<Family> fmys = new ArrayList<Family>();
        if(b != null && b.isPersistent() && c != null && c.isPersistent())
            fmys = Family.bcRelateFamily(b, c);
        render(fmys);
    }
}
