package controllers;

import models.product.Brand;
import models.product.Category;
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
@With({GlobalExceptionHandler.class, Secure.class, GzipFilter.class})
public class Brands extends Controller {

    @Before(only = {"index", "bindCategory", "unbindCategory"})
    public static void setUpIndexPage() {
        renderArgs.put("brands", Brand.all().<Brand>fetch());
    }

    public static void index(String id) {
        Brand brand = (Brand) renderArgs.get("brands", List.class).get(0);
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
        redirect("/Brands/index/" + brand.name);
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
