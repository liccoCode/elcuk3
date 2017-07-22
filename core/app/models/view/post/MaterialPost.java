package models.view.post;

import models.material.Material;
import org.apache.commons.lang.StringUtils;
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

    private static final long serialVersionUID = -7336544616312488827L;
    public Material.T type;
    public Long cooperId;
    public String number;


    @Override
    public F.T2<String, List<Object>> params() {
        List<Object> params = new ArrayList<>();
        StringBuilder sbd = new StringBuilder("SELECT distinct m FROM Material m "
                + "LEFT JOIN m.cooperItems ci  LEFT JOIN m.boms bs WHERE m.isDel=? ");
        params.add(false);
        if(type != null) {
            sbd.append(" AND m.type = ? ");
            params.add(type);
        }
        if(StringUtils.isNotBlank(this.search)) {
            sbd.append(" AND (m.code LIKE ? ");
            sbd.append(" OR m.name LIKE ? OR m.specification LIKE ? ");
            sbd.append(" OR m.texture LIKE ? OR m.technology LIKE ? OR m.version LIKE ? )");
            for(int i = 0; i < 6; i++) params.add(this.word());
        }
        if(cooperId != null) {
            sbd.append(" AND ci.cooperator.id = ? ");
            params.add(cooperId);
        }
        if(StringUtils.isNotBlank(this.number)) {
            sbd.append(" AND bs.number = ? ");
            params.add(this.number);
        }
        return new F.T2<>(sbd.toString(), params);
    }


    @Override
    public List<Material> query() {
        F.T2<String, List<Object>> params = params();
        this.count = Material.find(params._1, params._2.toArray()).fetch().size();
        String sql = params._1 + " ORDER BY m.id DESC";
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
