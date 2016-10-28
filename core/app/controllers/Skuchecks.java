package controllers;

import controllers.api.SystemOperation;
import exception.PaymentException;
import helper.J;
import helper.Webs;
import models.product.Category;
import models.product.Product;
import models.qc.SkuCheck;
import models.view.Ret;
import models.view.post.SkuCheckPost;
import play.data.validation.Validation;
import play.libs.F;
import play.mvc.Before;
import play.mvc.Controller;
import play.mvc.With;
import play.utils.FastRuntimeException;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

/**
 * sku_check列表
 * User: cary
 * Date: 5/8/14
 * Time: 3:53 PM
 */
@With({GlobalExceptionHandler.class, Secure.class,SystemOperation.class})
public class Skuchecks extends Controller {

    @Before(only = {"blank", "create", "show", "checkDelete"})
    public static void beforIndex() {
        List<String> cates = Category.categoryIds();
        F.T2<List<String>, List<String>> skusToJson = Product.fetchSkusJson();
        cates.addAll(skusToJson._2);
        renderArgs.put("cates", J.json(cates));
    }


    @Check("skuchecks.checklist")
    public static void checklist(SkuCheckPost p) {
        List<SkuCheck> skulist = null;
        if(p == null) p = new SkuCheckPost();
        skulist = p.query();
        render(skulist, p);
    }


    public static void blank() {
        SkuCheck sc = new SkuCheck();
        List<SkuCheck> sclist = new ArrayList<>();
        render(sc, sclist);
    }

    public static void show(long id) {
        SkuCheck sc = SkuCheck.findById(id);
        List<SkuCheck> sclist = sc.linelist();
        render("Skuchecks/blank.html", sc, sclist);
    }

    public static void checkDelete(long id) {
        try {
            SkuCheck sc = SkuCheck.findById(id);
            sc.delete();
            List<SkuCheck> sclist = sc.linelist();
            for(SkuCheck s : sclist) {
                s.delete();
            }
            renderJSON(new Ret(true, "成功删除"));
        } catch(FastRuntimeException e) {
            renderJSON(new Ret(Webs.E(e)));
        }
    }


    public static void create(SkuCheck sc) {
        sc.validate();
        List<SkuCheck> sclist = sc.linelist();
        if(Validation.hasErrors()) {
            render("Skuchecks/blank.html", sc, sclist);
        }
        if(sc.id == null) {
            sc.creator = Secure.Security.connected();
            sc.createdAt = new java.util.Date();
        } else {
            sc.updator = Secure.Security.connected();
            sc.updateAt = new java.util.Date();
        }

        sc.lineType = SkuCheck.LineType.HEAD;
        sc.pid = 0L;
        sc.save();
        flash.success("新建SKU_CHECK成功.");
        render("Skuchecks/blank.html", sc, sclist);
    }


    public static void createline(SkuCheck sc) {
        if(sc.id == null) {
            sc.creator = Secure.Security.connected();
            sc.createdAt = new java.util.Date();
        } else {
            sc.updator = Secure.Security.connected();
            sc.updateAt = new java.util.Date();
        }

        sc.lineType = SkuCheck.LineType.LINE;
        sc.save();
        flash.success("新建CHECKLIST成功.");

        sc = SkuCheck.findById(sc.pid);
        List<SkuCheck> sclist = sc.linelist();
        render("Skuchecks/blank.html", sc, sclist);
    }


    public static void update(Long id, String checkRequire, String checkMethod) {
        try {
            SkuCheck sc = SkuCheck.findById(id);
            if(sc == null)
                renderJSON(new Ret("不存在, 无法更新"));
            sc.checkRequire = checkRequire;
            sc.checkMethod = checkMethod;
            sc.updateAt = new Date();
            sc.updator = Secure.Security.connected();
            sc.save();
            renderJSON(new Ret(true, "CheckList更新成功"));
        } catch(Exception e) {
            renderJSON(new Ret(false, e.getMessage()));
        }
    }


    public static void remove(Long id) {
        SkuCheck sc = SkuCheck.findById(id);
        if(sc == null)
            renderJSON(new Ret("不存在, 无法删除"));
        try {
            sc.delete();
        } catch(PaymentException e) {
            renderJSON(new Ret(false, e.getMessage()));
        }
        renderJSON(new Ret(true, "删除成功."));
    }
}
