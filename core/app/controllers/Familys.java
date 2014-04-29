package controllers;

import helper.Webs;
import models.product.Brand;
import models.product.Category;
import models.product.Family;
import models.product.Product;
import models.view.Ret;
import play.mvc.Controller;
import play.mvc.With;
import play.utils.FastRuntimeException;

import java.util.ArrayList;
import java.util.List;

/**
 * Family 的控制
 * User: wyattpan
 * Date: 4/27/12
 * Time: 11:36 AM
 */
@With({GlobalExceptionHandler.class, Secure.class})
public class Familys extends Controller {
    @Check("familys.index")
    public static void index() {
        List<Category> cats = Category.find("ORDER BY categoryId").fetch();
        render(cats);
    }

    public static void create(Family f) {
        try {
            if(f.isExist()) renderJSON(new Ret("Family 已经存在, 不需要添加."));
            f.checkAndCreate();
            renderJSON(new Ret(true, "创建成功."));
        } catch(FastRuntimeException e) {
            renderJSON(new Ret(Webs.E(e)));

        }
    }

    public static void destroy(String family) {
        try {
            Family fa = Family.findById(family);
            fa.safeDestroy();
            render(new Ret(true, "成功删除"));
        } catch(FastRuntimeException e) {
            renderJSON(new Ret(Webs.E(e)));
        }
    }

    public static void pro_div(Family f) {
        List<Product> prods = new ArrayList<Product>();

        // 查找 Family 相关的 Product
        if(f != null && f.isPersistent()) prods = f.products;
        render("Products/_products.html", prods);
    }

    public static void fam_div(Brand b, Category c) {
        List<Family> fmys = new ArrayList<Family>();
        if(b != null && b.isPersistent() && c != null && c.isPersistent())
            fmys = Family.bcRelateFamily(b, c);
        render(fmys, b, c);
    }
}
