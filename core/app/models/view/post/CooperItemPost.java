package models.view.post;

import models.material.Material;
import models.procure.CooperItem;
import org.apache.commons.lang.StringUtils;
import play.libs.F;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

/**
 * Created by IntelliJ IDEA.
 * User: licco
 * Date: 2017/10/18
 * Time: 下午5:57
 */
public class CooperItemPost extends Post<CooperItem> {

    private static final long serialVersionUID = 7859312105262648769L;

    public CooperItem.T type;
    public Material.T matType;

    public Long cooperId;
    public String number;

    @Override
    public F.T2<String, List<Object>> params() {
        List<Object> params = new ArrayList<>();
        StringBuilder sql = new StringBuilder("SELECT DISTINCT i FROM CooperItem i ");
        if(Objects.equals(type, CooperItem.T.MATERIAL)) {
            sql.append(" LEFT JOIN i.material m LEFT JOIN m.boms bs WHERE m.isDel='false' ");
            if(matType != null) {
                sql.append(" AND m.type = ? ");
                params.add(matType);
            }
            if(StringUtils.isNotBlank(this.search)) {
                sql.append(" AND (m.code LIKE ? ");
                sql.append(" OR m.name LIKE ? OR m.specification LIKE ? ");
                sql.append(" OR m.texture LIKE ? OR m.technology LIKE ? OR m.version LIKE ? )");
                for(int i = 0; i < 6; i++) params.add(this.word());
            }
            if(cooperId != null) {
                sql.append(" AND i.cooperator.id = ? ");
                params.add(cooperId);
            }
            if(StringUtils.isNotBlank(this.number)) {
                sql.append(" AND bs.number = ? ");
                params.add(this.number);
            }
        } else {
            sql.append(" LEFT JOIN i.product p WHERE  1 = 1 ");
            if(StringUtils.isNotEmpty(search)) {
                sql.append(" AND (i.product.sku like ? OR i.cooperator.fullName like ? OR i.cooperator.name like ? )");
                params.add("%" + search + "%");
                params.add("%" + search + "%");
                params.add("%" + search + "%");
            }
        }
        sql.append(" ORDER BY i.id DESC");
        return new F.T2<>(sql.toString(), params);
    }

    @Override
    public List<CooperItem> query() {
        F.T2<String, List<Object>> params = params();
        this.count = CooperItem.find(params._1, params._2.toArray()).fetch().size();
        if(this.pagination) {
            return CooperItem.find(params._1, params._2.toArray()).fetch(this.page, this.perSize);
        } else {
            return CooperItem.find(params._1, params._2.toArray()).fetch();
        }
    }

}
