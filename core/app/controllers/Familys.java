package controllers;

import controllers.api.SystemOperation;
import helper.J;
import helper.Webs;
import models.product.Brand;
import models.product.Category;
import models.product.Family;
import models.product.Product;
import models.view.Ret;
import models.view.post.FamilyPost;
import org.apache.commons.lang.StringUtils;
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
@With({GlobalExceptionHandler.class, Secure.class, SystemOperation.class})
public class Familys extends Controller {

    @Check("familys.index")
    public static void index(FamilyPost p) {
        List<Category> cats = Category.find("ORDER BY categoryId").fetch();
        if(p == null) p = new FamilyPost();
        List<Family> families = p.query();
        render(p, families, cats);
    }

    public static void reloadFamily(String categoryId, String brand) {
        List<Family> families = Family.find("category.categoryId=? AND brand.name=?", categoryId, brand).fetch();
        render("Familys/familyIndex.html", families, categoryId, brand);
    }

    public static void create(Family f) {
        try {
            if(f.isExist()) renderJSON(new Ret("Family 已经存在, 不需要添加."));
            f.checkAndCreate();
            flash.success("创建成功");
            index(new FamilyPost());
        } catch(FastRuntimeException e) {
            renderJSON(new Ret(Webs.e(e)));
        }
    }

    @Check("familys.delete")
    public static void destroy(String family) {
        try {
            Family fa = Family.findById(family);
            fa.safeDestroy();
            flash.success("成功删除");
            index(new FamilyPost());
        } catch(FastRuntimeException e) {
            renderJSON(new Ret(Webs.e(e)));
        }
    }

    public static void proDiv(String name) {
        if(StringUtils.isNotBlank(name)) {
            Family family = Family.findById(name);
            List<Product> prods = family.productList();
            render("Familys/_products.html", prods);
        }
    }

    public static void famDiv(Brand b, Category c) {
        List<Family> fmys = new ArrayList<>();
        if(b != null && b.isPersistent() && c != null && c.isPersistent())
            fmys = Family.bcRelateFamily(b, c);
        render(fmys, b, c);
    }

    /**
     * 根据 CategoryId 获取 familyId 集合
     *
     * @param categoryId
     * @return
     */
    public static void categoryRelateFamily(String categoryId) {
        List<Family> families = Family.find("category_categoryId =?", categoryId).fetch();
        List<String> familieIds = new ArrayList<>();
        for(Family f : families) familieIds.add(f.family);
        renderJSON(J.json(familieIds));
    }
}
