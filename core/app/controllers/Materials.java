package controllers;

import helper.GTs;
import helper.J;
import models.User;
import models.material.Material;
import models.material.MaterialBom;
import models.procure.Cooperator;
import models.product.Product;
import models.view.Ret;
import models.view.post.MaterialBomPost;
import models.view.post.MaterialPost;
import org.apache.commons.lang.StringUtils;
import play.db.helper.SqlSelect;
import play.mvc.Before;
import play.mvc.Controller;
import play.mvc.With;

import java.util.Date;
import java.util.List;
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
        render(p, materials);
    }


    public static void blank() {
        List<Cooperator> cooperators = Cooperator.suppliers();
        render(cooperators);
    }

    public static void create(Material m ,List<MaterialBom> boms) {
        m.projectName = Login.current().projectName;
        m.save();
        boms.forEach(unit -> {
            MaterialBom bom = MaterialBom.findById(unit.id);
            bom.materials.add(m);
            bom.save();
        });
        flash.success("新增物料【" + m.code + "】成功！");
        index(new MaterialPost());
    }

    public static void edit(Long id) {
        Material material = Material.findById(id);
        List<MaterialBom> boms = material.boms;
        render(material, boms);
    }

    public static void update(Material m ,List<MaterialBom> boms) {
        Material material = Material.findById(m.id);
        material.boms.forEach(bom -> {
            bom.materials.remove(material);
            bom.save();
        });

        boms.forEach(unit -> {
            MaterialBom bom = MaterialBom.findById(unit.id);
            bom.materials.add(m);
            bom.save();
        });
        edit(m.id);
    }

    public static void updateBom(MaterialBom bom) {
        bom.save();
        editBom(bom.id);
    }

    public static void deleteMaterial(Long id ) {
        Material material = Material.findById(id);
        material.isDel = true;
        material.updateDate = new Date();
        material.save();
        flash.success(String.format("删除 %s 成功！", material.name));
        index(new MaterialPost());
    }

    public static void deleteMaterialBom(Long id) {
        MaterialBom bom = MaterialBom.findById(id);
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
        render(p, boms, users, currUser);
    }

    public static void createBom(MaterialBom b) {
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
        List<Material> materials = Material.find("name like ? ", name).fetch();
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
        String word = String.format("%%%s%%", StringUtils.replace(search.trim(), "'", "''"));
        MaterialPost p = new MaterialPost();
        p.search = search;
        if(StringUtils.isNotBlank(type)) {
            p.type = Material.T.valueOf(type);
        }
        p.pagination = false;
        List<Material> materials = p.query();
        render("/Products/showMaterialList.html", materials);
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
        renderJSON(GTs.newMap("code", material.code).put("name", material.name)
                .put("surplusPendingQty", material.surplusPendingQty()).build());
    }

}
