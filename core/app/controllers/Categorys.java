package controllers;

import helper.Webs;
import models.PageInfo;
import models.Ret;
import models.product.Brand;
import models.product.Category;
import play.data.validation.Error;
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
        Integer[] fixs = Webs.fixPage(p, s);
        p = fixs[0];
        s = fixs[1];
        List<Category> cates = Category.all().fetch(p, s);
        Long count = Category.count();

        PageInfo<Category> pi = new PageInfo<Category>(s, count, p, cates);
        render(cates, count, p, s, pi);
    }

    public static void detail(String cid) {
        Category cat = Category.findById(cid);
        List<Brand> brands = Brand.all().fetch();

        render(cat, brands);
    }

    /**
     * Category Create
     *
     * @param c
     */
    public static void cc(@Valid Category c) {
        if(Validation.hasErrors()) renderJSON(validation.errorsMap());
        if(c.isPersistent()) renderJSON(new Error("Product", "Product is peristent can not be save.", new String[]{}));
        c.save();
        c.products = null;
        renderJSON(c);
    }

    public static void cu(Category c) {
        if(c == null || !c.isPersistent()) renderJSON(new Ret("Category is not Exist!"));
        c.save();
        renderJSON(new Ret(true, "Category[" + c.categoryId + "] Update Success!"));
    }

    /**
     * 绑定 Brand
     */
    public static void bBrand(Category c, Brand b) {
        if(!c.isPersistent()) renderJSON(new Ret("Category 不存在."));
        if(!b.isPersistent()) renderJSON(new Ret("Brand 不存在."));
        if(c.brands.contains(b)) renderJSON(new Ret(String.format("Brand %s 已经存在了", b.name)));

        c.brands.add(b);
        c.save();

        renderJSON(new Ret());
    }

    /**
     * 解除绑定 Brand
     *
     * @param c
     * @param b
     */
    public static void uBrand(Category c, Brand b) {
        if(!c.isPersistent()) renderJSON(new Ret("Category 不存在."));
        if(!b.isPersistent()) renderJSON(new Ret("Brand 不存在."));
        if(c.brands.contains(b)) {
            c.brands.remove(b);
            c.save();
        }

        renderJSON(new Ret());
    }
}
