package controllers;

import controllers.api.SystemOperation;
import helper.GTs;
import helper.J;
import helper.Webs;
import models.market.BtbOrder;
import models.procure.BtbCustom;
import models.procure.CooperItem;
import models.procure.Cooperator;
import models.view.Ret;
import models.view.post.CooperatorPost;
import org.apache.commons.lang.StringUtils;
import play.data.validation.Validation;
import play.mvc.Controller;
import play.mvc.With;

import java.util.ArrayList;
import java.util.List;

/**
 * 控制器
 * User: wyattpan
 * Date: 7/16/12
 * Time: 12:12 PM
 */
@With({GlobalExceptionHandler.class, Secure.class, SystemOperation.class})
public class Cooperators extends Controller {

    @Check("cooperators.index")
    public static void index(CooperatorPost p) {
        if(p == null) p = new CooperatorPost();
        List<Cooperator> coopers = p.query();
        render(p, coopers);
    }

    public static void show(long id, Boolean full) {
        Cooperator coper = Cooperator.findById(id);
        if(coper == null || !coper.isPersistent()) notFound();
        if(full == null) {
            redirect("/cooperators/index#" + id);
        } else {
            render(coper, full);
        }
    }

    public static void showCooperItem(Long id) {
        Cooperator coper = Cooperator.findById(id);
        List<CooperItem> items = coper.cooperItems;
        render("/Cooperators/_items.html", items, coper);
    }

    /**
     * 修改 Cooperator 对象
     *
     * @param cop
     */
    public static void edit(Cooperator cop) {
        validation.valid(cop);
        cop.checkAndUpdate();
        if(Validation.hasErrors())
            renderJSON(new Ret(J.json(Validation.errors())));
        renderJSON(new Ret());
    }

    public static void itemEdit(CooperItem copItem) {
        validation.valid(copItem);
        if(Validation.hasErrors())
            renderJSON(new Ret(validation.errorsMap()));
        copItem.checkAndUpdate();
        renderJSON(new Ret());
    }

    /**
     * 创建新的 Cooperator
     *
     * @param cop
     */
    public static void newCooper(Cooperator cop) {
        if(cop == null) cop = new Cooperator();
        render(cop);
    }

    public static void saveCooper(Cooperator cop) {
        validation.valid(cop);
        if(Validation.hasErrors())
            render("Cooperators/newCooper.html", cop);
        cop.checkAndUpdate();
        // 这样编写, 是因为前台这个页面有自动处理 hash 值
        redirect("/cooperators/index#" + cop.id);
    }

    /**
     * 添加新的 CooperItem
     *
     * @param copItem
     */
    public static void newCooperItem(CooperItem copItem, long cooperId) {
        if(copItem == null) copItem = new CooperItem();
        Cooperator cop = Cooperator.findById(cooperId);
        if(cop == null || !cop.isPersistent()) error("不正确的合作者参数");
        renderArgs.put("skus", J.json(cop.frontSkuAutoPopulate()));
        render(copItem, cop);
    }

    public static void editCooperItem(long cooperId) {
        CooperItem copItem = CooperItem.findById(cooperId);
        copItem.getAttributes();
        renderArgs.put("cop", copItem.cooperator);
        renderArgs.put("skus", J.json(copItem.cooperator.frontSkuAutoPopulate()));
        render("Cooperators/newCooperItem.html", copItem);
    }

    public static void saveCooperItem(CooperItem copItem, long cooperId) {
        checkAuthenticity();
        validation.valid(copItem);
        Cooperator cop = Cooperator.findById(cooperId);
        renderArgs.put("skus", J.json(cop.frontSkuAutoPopulate()));
        if(Validation.hasErrors())
            render("Cooperators/newCooperItem.html", copItem, cop);
        copItem.checkAndSave(cop);
        flash.success("创建成功.");
        redirect("/cooperators/index#" + copItem.cooperator.id);
    }

    public static void updateCooperItem(CooperItem copItem) {
        checkAuthenticity();
        validation.valid(copItem);
        if(Validation.hasErrors()) {
            renderArgs.put("cop", copItem.cooperator);
            renderArgs.put("skus", J.json(copItem.cooperator.frontSkuAutoPopulate()));
            render("Cooperators/newCooperItem.html", copItem);
        }
        copItem.checkAndUpdate();
        flash.success("CooperItem %s, %s 修改成功", copItem.id, copItem.sku);
        redirect("/cooperators/index#" + copItem.cooperator.id);
    }

    public static void removeCooperItem(CooperItem copItem) {
        copItem.checkAndRemove();
        renderJSON(new Ret());
    }

    // 供应商的价格
    public static void price(long id, String sku) {
        validation.required(id);
        validation.required(sku);
        if(Validation.hasErrors())
            renderJSON(new Ret(Webs.V(Validation.errors())));

        CooperItem copItem = CooperItem.find("sku=? AND cooperator.id=?", sku, id).first();
        renderJSON(GTs.newMap("price", copItem.price).put("currency", copItem.currency).put("flag", true)
                .put("period", copItem.period).put("boxSize", copItem.boxSize).build());
    }

    /**
     * 选择供应商
     *
     * @param coperId
     * @param sku
     * @param size
     */
    public static void boxSize(long coperId, String sku, int size) {
        validation.required(coperId);
        validation.required(sku);
        validation.required(size);

        if(Validation.hasErrors()) renderJSON(new Ret(false, Webs.V(Validation.errors())));

        CooperItem copi = CooperItem.find("cooperator.id=? AND product.sku=?", coperId, sku).first();
        renderJSON(new Ret(true, copi.boxToSize(size) + ""));
    }

    /**
     * 模糊匹配合作伙伴
     *
     * @param name
     */
    public static void findSameCooperator(String name, Cooperator.T type) {
        StringBuilder sql = new StringBuilder();
        List<Object> params = new ArrayList<>();
        if(StringUtils.isNotBlank(name)) {
            sql.append("name LIKE ?");
            params.add("%" + name + "%");
        }
        if(type != null) {
            sql.append(" AND type=?");
            params.add(type);
        }
        List<Cooperator> list = Cooperator.find(sql.toString(), params.toArray()).fetch();
        List<String> names = new ArrayList<>();
        for(Cooperator coop : list) {
            names.add(coop.name + "-" + coop.id);
        }
        renderJSON(J.json(names));
    }
}
