package controllers;

import helper.J;
import models.User;
import models.material.Material;
import models.material.MaterialBom;
import models.procure.Cooperator;
import models.view.Ret;
import models.view.post.MaterialBomPost;
import models.view.post.MaterialPost;
import org.apache.commons.lang.StringUtils;
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

    public static void index(MaterialPost p) {
        if(p == null) p = new MaterialPost();
        List<Material> materials = p.query();
        render(p, materials);
    }


    public static void blank() {
        List<Cooperator> cooperators = Cooperator.suppliers();
        render(cooperators);
    }

    public static void create(Material m) {
        m.save();
        flash.success("新增物料【" + m.code + "】成功！");
        index(new MaterialPost());
    }

    public static void edit(Long id) {
        Material material = Material.findById(id);
        render(material);
    }

    public static void update(Material m) {
        m.save();
        edit(m.id);
    }

    public static void updateBom(MaterialBom bom) {
        bom.save();
        editBom(bom.id);
    }

    public static void deleteMaterial(Long id) {
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
        List<String> names = materials.stream().map(m -> m.name).collect(Collectors.toList());
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

}
