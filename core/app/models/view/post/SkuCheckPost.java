package models.view.post;

import models.qc.SkuCheck;
import play.libs.F;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by IntelliJ IDEA.
 * User: cary
 * Date: 5/4/14
 * Time: 3:25 PM
 */
public class SkuCheckPost extends Post<SkuCheck> {

    public SkuCheckPost() {
        this.perSize = 25;
    }

    public SkuCheckPost(int perSize) {
        this.perSize = perSize;
    }

    public SkuCheck.CheckType dateType;
    public String name;

    @Override
    public F.T2<String, List<Object>> params() {
        StringBuilder sql = new StringBuilder(" 1=1 ");
        List<Object> params = new ArrayList<>();

        if(this.dateType != null) {
            sql.append(" AND checkType=? ");
            params.add(this.dateType);
        }

        if(this.search != null && !"".equals(this.search.trim())) {
            sql.append(" AND (SkuName like ? or checkName like ?)");
            params.add(this.word());
            params.add(this.word());
        }

        sql.append(" AND lineType=? ");
        params.add(SkuCheck.LineType.HEAD);

        return new F.T2<>(sql.toString(), params);
    }

    public List<SkuCheck> query() {
        F.T2<String, List<Object>> params = params();
        this.count = this.count(params);
        return SkuCheck.find(params._1 + "ORDER BY createdAt DESC", params._2.toArray()).fetch(this.page,
                this.perSize);
    }

    @Override
    public Long count(F.T2<String, List<Object>> params) {
        return SkuCheck.count(params._1, params._2.toArray());
    }

    @Override
    public Long getTotalCount() {
        return SkuCheck.count();
    }
}
