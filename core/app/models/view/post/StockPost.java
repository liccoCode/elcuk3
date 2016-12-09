package models.view.post;

import models.procure.ProcureUnit;
import models.whouse.Whouse;
import org.apache.commons.lang.StringUtils;
import play.libs.F;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by licco on 2016/12/8.
 */
public class StockPost extends Post<ProcureUnit> {

    public Whouse whouse;
    public String projectName;

    @Override
    public F.T2<String, List<Object>> params() {
        StringBuilder sbd = new StringBuilder("1=1");
        List<Object> params = new ArrayList<>();
        if(this.whouse != null && this.whouse.id != null) {
            sbd.append(" AND currWhouse.id=?");
            params.add(this.whouse.id);
        }
        if(StringUtils.isNotBlank(this.projectName)) {
            sbd.append(" AND projectName=?");
            params.add(this.projectName);
        }
        if(StringUtils.isNotBlank(this.search)) {
            sbd.append(" AND stockObjId LIKE ?");
            params.add(this.word());
        }
        return new F.T2<>(sbd.toString(), params);
    }

    /**
     * 库存查询
     *
     * @return
     */
    public List<ProcureUnit> query() {
        F.T2<String, List<Object>> params = this.params();
        this.count = this.count(params);
        String sql = params._1 + " ORDER BY currWhouse.id, createDate DESC";
        return ProcureUnit.find(sql, params._2.toArray()).fetch(this.page, this.perSize);
    }


    @Override
    public Long count(F.T2<String, List<Object>> params) {
        return ProcureUnit.count(params._1, params._2.toArray());
    }

    @Override
    public Long getTotalCount() {
        return this.count();
    }

}
