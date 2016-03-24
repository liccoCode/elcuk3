package controllers;

import controllers.api.SystemOperation;
import models.product.Brand;
import models.view.Ret;
import org.apache.commons.lang.StringUtils;
import play.data.validation.Validation;
import play.mvc.Before;
import play.mvc.Controller;
import play.mvc.With;

import java.util.List;

/**
 * Created by IntelliJ IDEA.
 * User: wyattpan
 * Date: 4/26/12
 * Time: 11:28 AM
 */
@With({GlobalExceptionHandler.class, Secure.class,SystemOperation.class})
public class Brands extends Controller {

    @Before(only = {"index", "bindCategory", "unbindCategory", "update", "create"})
    public static void setUpIndexPage() {
        renderArgs.put("brands", Brand.all().<Brand>fetch());
    }

    @Check("brands.index")
    public static void index(String id) {
        List<Brand> brands = renderArgs.get("brands", List.class);
        if (brands==null || brands.size()<=0){
            Brand brand = new Brand();
            render(brand);
        }
        Brand brand = (Brand) brands.get(0);
        if(StringUtils.isNotBlank(id)) brand = Brand.findById(id);
        render(brand);
    }

    public static void bindCategory(List<String> cateIds, String id) {
        validation.valid(cateIds);
        Brand brand = Brand.findById(id);
        if(Validation.hasErrors()) render("Brands/index.html", brand);
        brand.bindCategories(cateIds);
        flash.success("绑定成功");
        redirect("/Brands/index/" + id);
    }

    public static void unbindCategory(List<String> cateIds, String id) {
        validation.valid(cateIds);
        Brand brand = Brand.findById(id);
        if(Validation.hasErrors()) render("Brands/index.html", brand);
        brand.unBindCategories(cateIds);
        if(Validation.hasErrors()) render("Brands/index.html", brand);
        flash.success("解除绑定成功");
        redirect("/Brands/index/" + id);
    }

    public static void update(Brand brand) {
        if(!Brand.exist(brand.name)) Validation.addError("", String.format("Brand %s 不存在!", brand.name));
        validation.valid(brand);
        if(Validation.hasErrors()) render("Brands/index.html", brand);
        brand.save();
        flash.success("更新成功");
        index(brand.name);
    }

    public static void create(Brand brand) {
        validation.valid(brand);
        if(Validation.hasErrors()) render("Brands/index.html", brand);
        brand.save();
        flash.success("成功添加 %s:%s", brand.name, brand.fullName);
        index(brand.name);
    }

    /**
     * 创建 Brand
     *
     * @param b
     */
    public static void bc(Brand b) {
        //TODO 创建品牌的方法需要重写
        if(b.isPersistent()) renderJSON(new Ret("此 Brand 已经在系统中存在."));

        b.save();
        renderJSON(new Ret());
    }
}
