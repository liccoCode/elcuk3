package controllers;

import models.Ret;
import models.product.Brand;
import models.product.Category;
import play.mvc.Controller;
import play.mvc.With;

import java.util.List;

/**
 * Created by IntelliJ IDEA.
 * User: wyattpan
 * Date: 4/26/12
 * Time: 11:28 AM
 */
@With({Secure.class, GzipFilter.class})
public class Brands extends Controller {

    public static void index() {
        List<Brand> brands = Brand.all().fetch();
        render(brands);
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
        //TODO Brand 的创建的 name 与 fullName 的参数需要验证
        if(b.isPersistent()) renderJSON(new Ret("此 Brand 已经在系统中存在."));

        b.save();
        renderJSON(new Ret());
    }
}
