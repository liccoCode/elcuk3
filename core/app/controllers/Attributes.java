package controllers;

import controllers.api.SystemOperation;
import helper.J;
import models.User;
import models.product.Attribute;
import org.apache.commons.lang.StringUtils;
import play.data.validation.Validation;
import play.mvc.Before;
import play.mvc.Controller;
import play.mvc.With;

import java.util.List;
import java.util.stream.Collectors;

/**
 * Created by IntelliJ IDEA.
 * User: mac
 * Date: 14-4-14
 * Time: PM3:17
 */
@With({GlobalExceptionHandler.class, Secure.class,SystemOperation.class})
public class Attributes extends Controller {

    @Before(only = {"index", "update", "delete"})
    public static void beforeShow() {
        List<Attribute> atts = Attribute.find("ORDER BY sort ASC").fetch();
        Attribute newAt = new Attribute();
        renderArgs.put("atts", atts);
        renderArgs.put("newAt", newAt);
    }

    public static void index() {
        Attribute newAt = new Attribute();
        render(newAt);
    }

    public static void create(Attribute newAt) {
        validation.valid(newAt);
        if(newAt.exist()) Validation.addError("", "属性名称重复");
        List<Attribute> atts = Attribute.findAll();
        if(Validation.hasErrors()) render("Attributes/index.html", newAt, atts);
        User user = User.findByUserName(Secure.Security.connected());
        newAt.createUser = user;
        newAt.save();
        flash.success("附加属性 %s 添加成功", newAt.name);
        redirect("/attributes/index");
    }

    public static void update(Long id, Attribute at) {
        Attribute manageAtt = Attribute.findById(id);
        if(!StringUtils.equalsIgnoreCase(at.name, manageAtt.name) && at.exist()) {
            Validation.addError("", "属性名称重复");
        }
        manageAtt.update(at);
        validation.valid(manageAtt);
        if(Validation.hasErrors()) render("Attributes/index.html");
        manageAtt.save();
        flash.success("附加属性 %s 修改成功", at.name);
        redirect("/attributes/index");
    }

    public static void delete(Long id) {
        Attribute at = Attribute.findById(id);
        at.safeDelete();
        if(Validation.hasErrors()) render("Attributes/index.html");
        flash.success("Attribute %s 删除成功", at.name);
        redirect("/attributes/index");
    }

    public static void sameAttr(String name) {
        List<Attribute> attributes = Attribute.find("name like '%" + name + "%'").fetch();
        List<String> names = attributes.stream().map(attr -> attr.name).collect(Collectors.toList());
        renderJSON(J.json(names));
    }



}


