package controllers;

import controllers.api.SystemOperation;
import models.product.Category;
import models.product.Team;
import org.apache.commons.lang.StringUtils;
import play.data.validation.Validation;
import play.mvc.Before;
import play.mvc.Controller;
import play.mvc.With;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by IntelliJ IDEA.
 * User: wyattpan
 * Date: 4/26/12
 * Time: 11:05 AM
 */
@With({GlobalExceptionHandler.class, Secure.class, SystemOperation.class})
public class Categorys extends Controller {

    @Before(only = {"show", "update", "brand", "unbrand", "delete"})
    public static void setUpShowPage() {
        List<Category> cates = Category.all().fetch();
        renderArgs.put("cates", cates);
    }

    @Check("categorys.show")
    public static void show(String id) {
        List<Category> cats = renderArgs.get("cates", List.class);
        if(cats == null || cats.size() <= 0) {
            Category cat = new Category();
            List<Team> teams = new ArrayList<>();
            render(cat, teams);
        }
        Category cat = (Category) renderArgs.get("cates", List.class).get(0);
        if(StringUtils.isNotBlank(id)) cat = Category.findById(id);
        List<Team> teams = Team.allTeams();
        render(cat, teams);
    }

    public static void update(Category cat) {
        if(!Category.exist(cat.categoryId)) {
            flash.error(String.format("Category %s 不存在!", cat.categoryId));
            redirect("/Categorys/show");
        }
        validation.valid(cat);
        if(Validation.hasErrors()) render("Categorys/show.html", cat);
        cat.save();
        redirect("/Categorys/show/" + cat.categoryId);
    }

    public static void brand(List<String> brandIds, String id) {
        validation.required(brandIds);
        Category cat = Category.findById(id);
        if(Validation.hasErrors()) render("Categorys/show.html", cat);
        cat.bindBrands(brandIds);
        flash.success("绑定成功");
        redirect("/Categorys/show/" + id);
    }

    public static void unbrand(List<String> brandIds, String id) {
        validation.required(brandIds);
        Category cat = Category.findById(id);
        if(Validation.hasErrors()) render("Categorys/show.html", cat);
        cat.unbindBrands(brandIds);
        if(Validation.hasErrors()) render("Categorys/show.html", cat);
        flash.success("解除绑定成功");
        redirect("/Categorys/show/" + id);
    }

    public static void delete(String id) {
        checkAuthenticity();
        Category cat = Category.findById(id);
        cat.deleteCategory();
        if(Validation.hasErrors()) render("Categorys/show.html", cat);
        flash.success("Category %s 删除成功", cat.categoryId);
        redirect("/Categorys/show");
    }

    public static void blank() {
        Category cat = new Category();
        List<Team> teams = Team.allTeams();
        render(cat, teams);
    }

    public static void create(Category cat) {
        if(StringUtils.isBlank(cat.categoryId)) Validation.addError("", "Category Id 必须填写");
        validation.valid(cat);
        if(Validation.hasErrors()) render("Categorys/blank.html", cat);
        cat.save();
        flash.success("创建成功.");
        redirect("/Categorys/show/" + cat.categoryId);
    }

}
