package models.view.post;

import helper.Dates;
import models.material.MaterialPlan;
import models.material.MaterialPurchase;
import models.procure.DeliverPlan;
import models.procure.ProcureUnit;
import org.apache.commons.lang.StringUtils;
import org.apache.commons.lang.math.NumberUtils;
import org.joda.time.DateTime;
import play.libs.F;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Created by IntelliJ IDEA.
 * User: Even
 * Date: 17/5/31
 * Time: PM5:12
 */
public class MaterialPlanPost extends Post<MaterialPlan> {
    public MaterialPlanPost() {
        DateTime now = DateTime.now(Dates.timeZone(null));
        this.from = now.minusDays(5).toDate();
        this.to = now.toDate();
    }

    /**
     * 由于在 Action Redirect 的时候, 需要保留参数, 而 Play 并没有保留, 所以只能多写一次
     */
    public Date from;
    public Date to;

    public Long cooperId;

    public DeliverPlan.P planState;


    @Override
    public F.T2<String, List<Object>> params() {

        StringBuilder sbd = new StringBuilder(
                "SELECT DISTINCT d FROM MaterialPlan d LEFT JOIN d.units u WHERE 1=1 AND");
        List<Object> params = new ArrayList<>();

        /** 时间参数 **/
        sbd.append(" d.createDate>=? AND d.createDate<=?");
        params.add(Dates.morning(this.from));
        params.add(Dates.night(this.to));

        if(this.planState != null) {
            sbd.append(" AND d.state=?");
            params.add(this.planState);
        }

        if(this.cooperId != null && this.cooperId > 0) {
            sbd.append(" AND d.cooperator.id=?");
            params.add(this.cooperId);
        }

        /** 模糊查询参数 **/
        if(StringUtils.isNotBlank(this.search)) {
            String word = this.word();
            sbd.append(" AND (")
                    .append(" d.id LIKE ?")
                    .append(" OR u.material.code LIKE ?")
                    .append(")");
            for(int i = 0; i < 2; i++) {
                params.add(word);
            }
        }

        return new F.T2<>(sbd.toString(), params);
    }

    public List<MaterialPlan> query() {
        F.T2<String, List<Object>> params = params();
        this.count = MaterialPlan.find(params._1, params._2.toArray()).fetch().size();
        return MaterialPlan.find(params._1 + " ORDER BY d.createDate DESC", params._2.toArray())
                .fetch(this.page, this.perSize);
    }


    public Long getTotalCount() {
        return this.count;
    }

    @Override
    public Long count(F.T2<String, List<Object>> params) {
        return this.count;
    }


}