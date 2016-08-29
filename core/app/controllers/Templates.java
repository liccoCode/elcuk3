package controllers;

import controllers.api.SystemOperation;
import models.User;
import models.product.Template;
import models.product.TemplateAttribute;
import play.data.validation.Validation;
import play.mvc.Before;
import play.mvc.Controller;
import play.mvc.With;

import java.util.List;

/**
 * Created by IntelliJ IDEA.
 * User: mac
 * Date: 14-4-14
 * Time: PM2:50
 */
@With({GlobalExceptionHandler.class, Secure.class, SystemOperation.class})
public class Templates extends Controller {
    @Before(only = {"show", "update", "delete", "attribute", "attribute", "unattribute", "category", "uncategory"})
    public static void beforeShow() {
        List<Template> temps = Template.findAll();
        renderArgs.put("temps", temps);
    }

    public static void show(Long id) {
        List<Template> temps = renderArgs.get("temps", List.class);
        Template temp = null;
        if(id != null) {
            temp = Template.findById(id);
        } else {
            if(temps.size() > 0) temp = temps.get(0);
        }
        render(temps, temp);
    }

    public static void blank() {
        Template temp = new Template();
        render(temp);
    }

    public static void create(Template temp) {
        validation.valid(temp);
        if(Validation.hasErrors()) render("Templates/blank.html", temp);
        User user = User.findByUserName(Secure.Security.connected());
        temp.createUser = user;
        temp.save();
        flash.success("创建成功.");
        redirect("/templates/show/" + temp.id);
    }


    public static void update(Template temp) {
        if(!temp.exist()) {
            flash.error(String.format("Template %s 不存在", temp.name));
            redirect("/Templates/show");
        }
        validation.valid(temp);
        if(Validation.hasErrors()) render("Templates/show.html", temp);
        temp.save();
        redirect("/templates/show/" + temp.id);
    }

    public static void delete(Long id) {
        checkAuthenticity();
        Template temp = Template.findById(id);
        temp.deleteAttribute();
        if(Validation.hasErrors()) render("Templates/show.html", temp);
        flash.success("Template %s 删除成功", temp.id);
        redirect("/templates/show");
    }

    /**
     * 绑定属性
     *
     * @param id
     * @param attributeIds
     */
    public static void attribute(Long id, List<Long> attributeIds) {
        validation.required(attributeIds);
        Template temp = Template.findById(id);
        if(Validation.hasErrors()) render("Templates/show.html", temp);
        temp.bindAttributes(attributeIds);
        flash.success("绑定成功");
        redirect("/templates/show/" + id);
    }

    /**
     * 解绑属性
     *
     * @param id
     * @param attributeIds
     */
    public static void unattribute(Long id, List<Long> attributeIds) {
        validation.required(attributeIds);
        Template temp = Template.findById(id);
        if(Validation.hasErrors()) render("Templates/show.html", temp);
        temp.unBindAttributes(attributeIds);
        flash.success("解除绑定成功");
        redirect("/templates/show/" + id);
    }

    public static void saveDeclare(Long id, List<TemplateAttribute> templateAttributes) {
        for(TemplateAttribute templateAttribute : templateAttributes) {
            TemplateAttribute manager = TemplateAttribute.findById(templateAttribute.id);
            manager.updateDeclare(templateAttribute.isDeclare);
        }
        flash.success("更新成功!");
        redirect("/templates/show/" + id);
    }

    /**
     * 绑定 Category
     *
     * @param id
     * @param categoryIds
     */
    public static void category(Long id, List<String> categoryIds) {
        validation.required(categoryIds);
        Template temp = Template.findById(id);
        if(Validation.hasErrors()) render("Templates/show.html", temp);
        temp.bindCategorys(categoryIds);
        flash.success("绑定成功");
        redirect("/templates/show/" + id);
    }

    /**
     * 解绑 Category
     *
     * @param id
     * @param categoryIds
     */
    public static void uncategory(Long id, List<String> categoryIds) {
        validation.required(categoryIds);
        Template temp = Template.findById(id);
        if(Validation.hasErrors()) render("Templates/show.html", temp);
        temp.unBindCategorys(categoryIds);
        flash.success("解除绑定成功");
        redirect("/templates/show/" + id);
    }
}
