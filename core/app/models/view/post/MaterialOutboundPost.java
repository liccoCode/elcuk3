package models.view.post;

import helper.Dates;
import models.material.Material;
import models.material.MaterialOutbound;
import models.whouse.Outbound;
import org.apache.commons.lang.StringUtils;
import play.libs.F;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by IntelliJ IDEA.
 * User: Even
 * Date: 17/6/16
 * Time: PM5:11
 */
public class MaterialOutboundPost extends Post<MaterialOutbound> {

    public Outbound.S status;
    public MaterialOutbound.C type;
    public String projectName;
    public Long cooperId;

    @Override
    public F.T2<String, List<Object>> params() {
        StringBuilder sbd = new StringBuilder("SELECT DISTINCT o FROM MaterialOutbound o LEFT JOIN o.units u WHERE "
                + "1=1");

        List<Object> params = new ArrayList<>();

        /** 时间参数 **/
        sbd.append(" AND o.createDate >= ? AND o.createDate <= ? ");
        params.add(Dates.morning(this.from));
        params.add(Dates.night(this.to));

        /** 状态参数 **/
        if(status != null) {
            sbd.append(" AND o.status = ? ");
            params.add(this.status);
        }
        /** 项目参数 **/
        if(StringUtils.isNotEmpty(this.projectName)) {
            sbd.append(" AND o.projectName=? ");
            params.add(this.projectName);
        }
        /** 出库类型参数 **/
        if(type != null) {
            sbd.append(" AND o.type = ? ");
            params.add(this.type);
        }

        /** 供应商参数 **/
        if(this.cooperId != null && this.cooperId > 0) {
            sbd.append(" AND o.cooperator.id=?");
            params.add(this.cooperId);
        }

        /** 模糊查询参数 **/
        if(StringUtils.isNotBlank(this.search)) {
            String word = this.word();
            sbd.append(" AND (")
                    .append(" o.id LIKE ?")
                    .append(" OR u.material.code LIKE ?")
                    .append(")");
            for(int i = 0; i < 2; i++) {
                params.add(word);
            }
        }


        sbd.append(" ORDER BY o.createDate DESC");
        return new F.T2<>(sbd.toString(), params);
    }


    @Override
    public List<MaterialOutbound> query() {
        F.T2<String, List<Object>> params = params();
        this.count = MaterialOutbound.find(params._1, params._2.toArray()).fetch().size();
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
