package controllers;

import helper.Webs;
import models.PageInfo;
import models.Ret;
import models.product.Brand;
import models.product.Category;
import play.mvc.Controller;

import java.util.List;

/**
 * Created by IntelliJ IDEA.
 * User: wyattpan
 * Date: 4/26/12
 * Time: 11:28 AM
 */
public class Brands extends Controller {

    public static void index(Integer p, Integer s) {
        Integer[] fixs = Webs.fixPage(p, s);
        p = fixs[0];
        s = fixs[1];
        List<Brand> brands = Brand.all().fetch(p, s);
        Long count = Category.count();

        PageInfo<Brand> pi = new PageInfo<Brand>(s, count, p, brands);
        render(brands, count, p, s, pi);
    }

    public static void detail(String bid) {
        Brand brand = Brand.findById(bid);
        List<Category> cats = Category.all().fetch();

        render(brand, cats);
    }

    /**
     * 创建 Brand
     *
     * @param b
     */
    public static void bc(Brand b) {
        if(b.isPersistent()) renderJSON(new Ret("此 Brand 已经在系统中存在."));

        b.save();
        renderJSON(new Ret());
    }
}
