package controllers;

import controllers.api.SystemOperation;
import helper.GTs;
import helper.J;
import helper.Webs;
import models.ElcukRecord;
import models.User;
import models.embedded.ERecordBuilder;
import models.procure.CooperItem;
import models.procure.Cooperator;
import models.procure.ProductMaterial;
import models.view.Ret;
import models.view.post.CooperatorPost;
import org.apache.commons.lang.StringUtils;
import play.data.validation.Validation;
import play.db.helper.SqlSelect;
import play.db.jpa.GenericModel;
import play.i18n.Messages;
import play.mvc.Controller;
import play.mvc.With;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

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
        List<CooperItem> items = coper.cooperItems.stream()
                .filter(item -> item.type.equals(CooperItem.T.SKU)).collect(Collectors.toList());
        render("/Cooperators/_items.html", items, coper);
    }

    public static void showMaterialItem(Long id) {
        Cooperator coper = Cooperator.findById(id);
        List<CooperItem> items = coper.cooperItems.stream()
                .filter(item -> item.type.equals(CooperItem.T.MATERIAL)).collect(Collectors.toList());
        render("/Cooperators/_mat_items.html", items, coper);
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

    public static void newMaterialItem(long cooperId) {
        Cooperator cop = Cooperator.findById(cooperId);
        CooperItem copItem = new CooperItem();
        renderArgs.put("materials", cop.findMaterialNotExistCooper());
        render(cop, copItem);
    }

    public static void saveMaterialItem(CooperItem copItem, Long cooperId, Long copItemId) {
        CooperItem db = null;
        if(copItemId != null) {
            db = CooperItem.findById(copItemId);
        }
        validation.valid(copItem);
        Cooperator cop = Cooperator.findById(cooperId);
        renderArgs.put("materials", cop.findMaterialNotExistCooper());
        renderArgs.put("skus", J.json(cop.frontSkuAutoPopulate()));
        if(Validation.hasErrors())
            render("Cooperators/newMaterialItem.html", copItem, cop);
        if(copItemId == null) {
            copItem.saveMaterialItem(cop);
        } else {
            copItem.updateMaterialItem(cop, db);
        }
        if(Validation.hasErrors())
            render("Cooperators/newMaterialItem.html", copItem, cop);
        flash.success("创建成功.");
        redirect("/cooperators/index#" + cooperId);
    }

    public static void editCooperItem(long cooperId) {
        CooperItem copItem = CooperItem.findById(cooperId);
        copItem.getAttributes();
        renderArgs.put("cop", copItem.cooperator);
        User user = User.findByUserName(Login.current().username.toLowerCase());
        boolean isEdit = user.privileges.stream()
                .allMatch(privilege -> Objects.equals(privilege.name, "cooperitem.price"));
        if(copItem.type.equals(CooperItem.T.SKU)) {
            renderArgs.put("mats", copItem.mats());
            renderArgs.put("skus", J.json(copItem.cooperator.frontSkuAutoPopulate()));
            render("Cooperators/newCooperItem.html", copItem, isEdit);
        } else {
            renderArgs.put("materials", copItem.cooperator.findMaterialNotExistCooper());
            render("Cooperators/newMaterialItem.html", copItem, isEdit);
        }
    }

    public static void removeCooperItemById(Long id) {
        CooperItem item = CooperItem.findById(id);
        item.delete();
        new ElcukRecord(Messages.get("cooperators.cooperitemdelete"),
                Messages.get("cooperators.cooperitemdelete.msg", id), id.toString()).save();
        flash.success("删除成功！");
        CooperatorPost p = new CooperatorPost();
        p.search = item.cooperator.fullName;
        List<Cooperator> coopers = p.query();
        render("/Cooperators/index.html", p, coopers);

    }

    public static void saveCooperItem(CooperItem copItem, long cooperId) {
        validation.valid(copItem);
        Cooperator cop = Cooperator.findById(cooperId);
        renderArgs.put("skus", J.json(cop.frontSkuAutoPopulate()));
        if(Validation.hasErrors())
            render("Cooperators/newCooperItem.html", copItem, cop);
        copItem.checkAndSave(cop);
        flash.success("创建成功.");
        new ElcukRecord(Messages.get("cooperators.savecooperitem"),
                Messages.get("cooperators.savecooperitem.msg", copItem.id), copItem.id.toString()).save();
        show(cooperId, false);
    }

    public static void agreeCooperItem(Long id) {
        CooperItem item = CooperItem.findById(id);
        item.status = CooperItem.S.Agree;
        item.save();
        flash.success("操作成功");
        CooperatorPost p = new CooperatorPost();
        p.search = item.cooperator.fullName;
        List<Cooperator> coopers = p.query();
        new ERecordBuilder("copItem.examine")
                .msgArgs(item.cooperator.name, item.sku, item.status.label()).fid(item.id).save();
        render("/Cooperators/index.html", p, coopers);
    }

    public static void disAgreeCooperItem(Long id) {
        CooperItem item = CooperItem.findById(id);
        item.status = CooperItem.S.Disagree;
        item.save();
        flash.success("操作成功");
        CooperatorPost p = new CooperatorPost();
        p.search = item.cooperator.fullName;
        List<Cooperator> coopers = p.query();
        new ERecordBuilder("copItem.examine")
                .msgArgs(item.cooperator.name, item.sku, item.status.label()).fid(item.id).save();
        render("/Cooperators/index.html", p, coopers);
    }

    public static void updateCooperItem(CooperItem copItem, Long copItemId) {
        CooperItem db = CooperItem.findById(copItemId);
        if(Validation.hasErrors()) {
            renderArgs.put("cop", copItem.cooperator);
            renderArgs.put("skus", J.json(copItem.cooperator.frontSkuAutoPopulate()));
            render("Cooperators/newCooperItem.html", copItem);
        }
        db.checkAndUpdate(copItem);
        flash.success("CooperItem %s, %s 修改成功", copItem.id, copItem.sku);
        CooperatorPost p = new CooperatorPost();
        p.search = db.cooperator.fullName;
        List<Cooperator> coopers = p.query();
        render("/Cooperators/index.html", p, coopers);
    }

    public static void removeCooperItem(CooperItem copItem) {
        copItem.checkAndRemove();
        new ElcukRecord(Messages.get("cooperators.cooperitemdelete"),
                Messages.get("cooperators.cooperitemdelete.msg", copItem.id), copItem.id.toString()).save();
        renderJSON(new Ret());
    }

    // 供应商的价格
    public static void price(long id, String sku) {
        validation.required(id);
        validation.required(sku);
        if(Validation.hasErrors())
            renderJSON(new Ret(Webs.v(Validation.errors())));

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

        if(Validation.hasErrors()) renderJSON(new Ret(false, Webs.v(Validation.errors())));

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

    /**
     * 异步请求获取供应商地址
     *
     * @param id
     */
    public static void findById(Long id) {
        Cooperator cop = Cooperator.findById(id);
        renderJSON(GTs.newMap("id", cop.id).put("address", cop.address).build());
    }

    public static void bindMaterialForSku(String[] ids, Long itemId) {
        CooperItem item = CooperItem.findById(itemId);
        List<CooperItem> items = CooperItem.find(" id IN " + SqlSelect.inlineParam(ids)).fetch();
        items.forEach(cooperItem -> {
            ProductMaterial productMaterial = new ProductMaterial();
            productMaterial.product = item;
            productMaterial.material = cooperItem;
            productMaterial.save();
        });
    }

    public static void unBindMaterialForSku(String[] ids, String itemId) {
        List<ProductMaterial> mats = ProductMaterial.find(" id IN " + SqlSelect.inlineParam(ids)).fetch();
        mats.forEach(GenericModel::delete);
    }

    public static void showMaterialForSku(Long itemId) {
        CooperItem item = CooperItem.findById(itemId);
        List<ProductMaterial> mats = item.mats();
        render(mats);
    }

    public static void findTaxPrice(String sku, Long cooperId, boolean containTax) {
        validation.required(cooperId);
        validation.required(sku);
        if(Validation.hasErrors())
            renderJSON(new Ret(Webs.v(Validation.errors())));
        CooperItem copItem = CooperItem.find("product.sku=? AND cooperator.id=?", sku, cooperId).first();
        renderJSON(GTs.newMap("price", containTax ? copItem.taxPrice : copItem.price)
                .put("currency", containTax ? copItem.taxCurrency : copItem.currency)
                .put("taxPoint", containTax ? copItem.taxPoint : 0)
                .put("flag", true)
                .put("period", copItem.period).put("boxSize", copItem.boxSize).build());
    }

}
