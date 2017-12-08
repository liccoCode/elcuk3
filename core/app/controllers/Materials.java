package controllers;

import helper.GTs;
import helper.J;
import helper.Webs;
import models.ElcukRecord;
import models.User;
import models.material.Material;
import models.material.MaterialBom;
import models.procure.CooperItem;
import models.procure.Cooperator;
import models.product.Product;
import models.view.Ret;
import models.view.post.CooperItemPost;
import models.view.post.MaterialBomPost;
import models.view.post.MaterialPost;
import org.apache.commons.lang.StringUtils;
import org.jsoup.helper.StringUtil;
import play.data.validation.Validation;
import play.db.helper.SqlSelect;
import play.i18n.Messages;
import play.mvc.Before;
import play.mvc.Controller;
import play.mvc.With;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.List;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

/**
 * Created by IntelliJ IDEA.
 * User: licco
 * Date: 2017/5/16
 * Time: 下午4:05
 */
@With({GlobalExceptionHandler.class, Secure.class})
public class Materials extends Controller {


    @Before(only = {"blank", "edit"})
    public static void showPageSetUp() {
        List<MaterialBom> materialBoms = MaterialBom.findAll();
        renderArgs.put("materialBoms", materialBoms);
    }

    public static void index(MaterialPost p) {
        if(p == null) p = new MaterialPost();
        List<Material> materials = p.query();
        renderArgs.put("logs",
                ElcukRecord.records(Arrays.asList("materials.create", "materials.update", "materials.delete"), 50));
        render(p, materials);
    }


    public static void blank() {
        List<Cooperator> cooperators = Cooperator.suppliers();
        render(cooperators);
    }

    public static void create(Material m, List<MaterialBom> boms) {
        m.projectName = Login.current().projectName;
        if(Material.find("code =? and isDel =0", m.code).first() != null) {
            Validation.addError("", "物料编码" + m.code + "已经存在");
        }
        if(Validation.hasErrors()) {
            render("/Materials/blank.html", m);
        }
        m.save();
        boms.forEach(unit -> {
            if(unit.id != null) {
                MaterialBom bom = MaterialBom.findById(unit.id);
                bom.materials.add(m);
                bom.save();
            }
        });
        flash.success("新增物料【" + m.code + "】成功！");
        new ElcukRecord(Messages.get("materials.create"),
                Messages.get("materials.create.msg", m.id), m.id.toString()).save();
        index(new MaterialPost());
    }

    public static void edit(Long id) {
        Material material = Material.findById(id);
        List<MaterialBom> boms = material.boms;
        render(material, boms);
    }

    public static void update(Material m, List<MaterialBom> boms) {
        m.save();
        Material material = Material.findById(m.id);
        material.boms.forEach(bom -> {
            bom.materials.remove(material);
            bom.save();
        });
        boms.forEach(unit -> {
            if(unit.id != null) {
                MaterialBom bom = MaterialBom.findById(unit.id);
                bom.materials.add(m);
                bom.save();
            }
        });
        flash.success("保存成功！");
        new ElcukRecord(Messages.get("materials.update"),
                Messages.get("materials.update.msg", m.id), m.id.toString()).save();
        edit(m.id);
    }

    public static void updateBom(MaterialBom bom) {
        bom.save();
        editBom(bom.id);
    }

    public static void deleteMaterial(Long id) {
        Material material = Material.findById(id);
        if(material.isDel) {
            material.isDel = false;
            material.updateDate = new Date();
            material.save();
            flash.success(String.format(" %s 上架成功！", material.name));
            new ElcukRecord(Messages.get("materials.up"),
                    Messages.get("materials.up.msg", material.id), material.id.toString()).save();
        } else {
            material.isDel = true;
            material.updateDate = new Date();
            material.save();
            flash.success(String.format(" %s 下架成功！", material.name));
            new ElcukRecord(Messages.get("materials.down"),
                    Messages.get("materials.down.msg", material.id), material.id.toString()).save();
        }
        index(new MaterialPost());
    }

    public static void deleteMaterialBom(Long id) {
        MaterialBom bom = MaterialBom.findById(id);
        if(bom.materials != null && bom.materials.size() > 0) {
            Validation.addError("", "B0M—ID[" + bom.number + "]已绑定物料,不允许删除!");
            Webs.errorToFlash(flash);
            Materials.indexBom(new MaterialBomPost());
        }
        bom.isDel = true;
        bom.updateDate = new Date();
        bom.save();
        flash.success(String.format("删除 %s 成功！", bom.name));
        indexBom(new MaterialBomPost());
    }

    public static void editBom(Long id) {
        MaterialBom bom = MaterialBom.findById(id);
        List<User> users = User.find("closed=?", false).fetch();
        render(bom, users);
    }

    public static void indexBom(MaterialBomPost p) {
        if(p == null) p = new MaterialBomPost();
        List<MaterialBom> boms = p.query();
        List<User> users = User.find("closed=?", false).fetch();
        User currUser = Login.current();
        boms.forEach(bom -> {
            List<Material> materialList = new ArrayList<>();
            bom.materials.forEach(material -> {
                if(!material.isDel)
                    materialList.add(material);
            });
            bom.materials = materialList;
        });
        render(p, boms, users, currUser);
    }

    public static void createBom(MaterialBom b) {
        if(StringUtil.isBlank(b.number) || Pattern.compile("[\u4e00-\u9fa5]").matcher(b.number).find()) {
            Validation.addError("", "B0M—ID[ " + b.number + " ] 不合法!");
            Webs.errorToFlash(flash);
            Materials.indexBom(new MaterialBomPost());
        }

        List<MaterialBom> materialBoms = MaterialBom.find("number = ? and isDel = false", b.number).fetch();
        if(materialBoms != null && materialBoms.size() > 0) {
            Validation.addError("", "B0M—ID[" + b.number + "]已存在,不允许二次创建!");
            Webs.errorToFlash(flash);
            Materials.indexBom(new MaterialBomPost());
        }

        b.creator = Login.current();
        b.createDate = new Date();
        b.updateDate = new Date();
        b.save();
        indexBom(new MaterialBomPost());
    }

    public static void sameMaterial(String search) {
        String word = String.format("%%%s%%", StringUtils.replace(search.trim(), "'", "''"));
        List<Material> materials = Material.find("code like ? or name like ? ", word, word).fetch();
        List<String> names = materials.stream().map(m -> m.code).collect(Collectors.toList());
        renderJSON(J.json(names));
    }

    public static void quickAddByMaterialName(String name, Long id) {
        List<Material> materials = Material.find("code like ? ", name).fetch();
        MaterialBom bom = MaterialBom.findById(id);
        materials.forEach(m -> bom.materials.add(m));
        bom.save();
        renderJSON(new Ret(true));
    }

    /**
     * 解除BOM和物料关系
     *
     * @param bomId
     * @param id
     */
    public static void deleteRelationForBom(Long bomId, Long id) {
        MaterialBom bom = MaterialBom.findById(bomId);
        bom.materials.remove(Material.findById(id));
        bom.save();
        flash.success("解除关系成功！");
        editBom(bomId);
    }

    public static void showMaterialList(String search, String type) {
        MaterialPost p = new MaterialPost();
        p.search = search;
        if(StringUtils.isNotBlank(type)) {
            p.type = Material.T.valueOf(type);
        }
        p.pagination = false;
        List<Material> materials = p.query();
        render("/Products/showMaterialList.html", materials);
    }

    public static void showMaterialListForCopItem(String search, String type) {
        CooperItemPost p = new CooperItemPost();
        p.search = search;
        p.type = CooperItem.T.MATERIAL;
        if(StringUtils.isNotBlank(type)) {
            p.matType = Material.T.valueOf(type);
        }
        p.pagination = false;
        List<CooperItem> items = p.query();
        render("/Cooperators/showMaterialListForCopItem.html", items);
    }

    public static void bindMaterialForSku(String[] ids, String sku) {
        Product product = Product.findById(sku);
        List<Material> materials = Material.find(" id IN " + SqlSelect.inlineParam(ids)).fetch();
        materials.forEach(m -> {
            m.products.add(product);
            m.save();
        });
        render("/Products/showMaterialList.html", materials);
    }

    public static void unBindMaterialForSku(String[] ids, String sku) {
        Product product = Product.findById(sku);
        List<Material> materials = Material.find(" id IN " + SqlSelect.inlineParam(ids)).fetch();
        materials.forEach(m -> {
            m.products.remove(product);
            m.save();
        });
        render("/Products/showMaterialList.html", materials);

    }

    /**
     * 根据ID返回单个物料信息
     */
    public static void findMaterial(long id) {
        Material material = Material.findById(id);
        renderJSON(GTs.newMap("code", material.code).put("name", material.name).build());
    }

    /**
     * 根据BomID查询物料集合
     *
     * @param id
     */
    public static void showMaterialListByBom(Long id) {
        MaterialBom bom = MaterialBom.findById(id);
        List<Material> materials = bom.materials.stream().
                filter(item -> !item.isDel).collect(Collectors.toList());
        render("/Materials/_unit_list.html", materials);
    }
}
