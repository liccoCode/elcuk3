package controllers;

import helper.Webs;
import models.Ret;
import models.product.AttrName;
import models.product.Brand;
import models.product.Category;
import models.product.Family;
import org.apache.commons.lang.StringUtils;
import play.data.validation.Valid;
import play.data.validation.Validation;
import play.mvc.Controller;
import play.mvc.With;

import java.util.List;

/**
 * Created by IntelliJ IDEA.
 * User: wyattpan
 * Date: 4/26/12
 * Time: 11:05 AM
 */
@With({Secure.class, GzipFilter.class})
public class Categorys extends Controller {

    public static void index() {
        List<Category> cates = Category.all().fetch();
        render(cates);
    }

    public static void detail(String cid) {
        Category cat = Category.findById(cid);
        List<AttrName> attrs = AttrName.all().fetch();
        List<Brand> brands = Brand.all().fetch();
        for(Brand bingB : cat.brands) brands.remove(bingB);

        render(cat, brands, attrs);
    }

    public static void bindAttrs(List<AttrName> ats, Category c) {
        if(!c.isPersistent()) renderJSON(new Ret("Category(" + c.categoryId + ") 不存在!"));

        try {
            c.bindAndUnBindAttrs(ats);
        } catch(Exception e) {
            renderJSON(new Ret(Webs.E(e)));
        }
        renderJSON(new Ret());
    }

    /**
     * Category Create
     *
     * @param c
     */
    public static void cc(@Valid Category c) {
        if(Validation.hasErrors()) renderJSON(validation.errorsMap());
        if(c.isPersistent()) renderJSON(new Ret("Category has exist!"));
        c.save();
        renderJSON(Webs.G(c));
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
            List<Family> fmys = Family.bcRelateFamily(b, c);
            if(fmys != null && fmys.size() > 0) {
                renderJSON(new Ret(String.format("Brand(%s) 与 Category(%s) 拥有 Family(%s) 不允许解除联系",
                        b.name, c.name, StringUtils.join(fmys, ","))
                ));
            } else {
                c.brands.remove(b);
                c.save();
            }
        }

        renderJSON(new Ret());
    }
}
