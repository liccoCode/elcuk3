package models.view.post;

import models.material.Material;
import play.libs.F;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by IntelliJ IDEA.
 * User: licco
 * Date: 2017/5/16
 * Time: 下午4:16
 */
public class MaterialPost extends Post<Material> {


    public Material.T type;


    @Override
    public F.T2<String, List<Object>> params() {
        StringBuilder sbd = new StringBuilder("SELECT m FROM Material m ");
        List<Object> params = new ArrayList<>();

        return new F.T2<>(sbd.toString(), params);
    }


    @Override
    public List<Material> query() {
        F.T2<String, List<Object>> params = params();
        this.count = Material.find(params._1, params._2.toArray()).fetch().size();
        String sql = params._1 + " ";
        return Material.find(sql, params._2.toArray()).fetch(this.page, this.perSize);
    }

    public Long getTotalCount() {
        return this.count;
    }

    @Override
    public Long count(F.T2<String, List<Object>> params) {
        return this.count;
    }

}
