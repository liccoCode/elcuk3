package models.view.post;

import models.material.MaterialBom;
import org.apache.commons.lang.StringUtils;
import play.libs.F;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by IntelliJ IDEA.
 * User: licco
 * Date: 2017/5/17
 * Time: 下午2:47
 */
public class MaterialBomPost extends Post<MaterialBom> {

    public MaterialBom.S status;
    public int perSize = 20;


    @Override
    public F.T2<String, List<Object>> params() {
        StringBuilder sbd = new StringBuilder("SELECT m FROM MaterialBom m where 1=1 ");
        List<Object> params = new ArrayList<>();
        if(StringUtils.isNotBlank(this.search)) {
            sbd.append(" AND (m.number LIKE ? ");
            sbd.append(" OR m.name LIKE ? ").append(")");

            for(int i = 0; i < 2; i++) params.add(this.word());
        }

        return new F.T2<>(sbd.toString(), params);
    }

    @Override
    public List<MaterialBom> query() {
        F.T2<String, List<Object>> params = params();
        this.count = MaterialBom.find(params._1, params._2.toArray()).fetch().size();
        String sql = params._1 + " ";
        return MaterialBom.find(sql, params._2.toArray()).fetch(this.page, this.perSize);
    }

    public Long getTotalCount() {
        return this.count;
    }

    @Override
    public Long count(F.T2<String, List<Object>> params) {
        return this.count;
    }
}
