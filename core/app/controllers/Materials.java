package controllers;

import models.material.Material;
import models.material.MaterialBom;
import models.procure.Cooperator;
import models.view.post.MaterialBomPost;
import models.view.post.MaterialPost;
import play.mvc.Controller;
import play.mvc.With;

import java.util.List;

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

    public static void indexBom(MaterialBomPost p) {
        if(p == null) p = new MaterialBomPost();
        List<MaterialBom> boms = p.query();
        render(p, boms);
    }

    public static void blankBom() {

       render();
    }


}
