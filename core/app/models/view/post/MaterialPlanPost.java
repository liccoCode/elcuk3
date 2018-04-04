package models.view.post;

import helper.Dates;
import models.material.MaterialPlan;
import org.apache.commons.lang.StringUtils;
import org.joda.time.DateTime;
import play.libs.F;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

/**
 * Created by IntelliJ IDEA.
 * User: Even
 * Date: 17/5/31
 * Time: PM5:12
 */
public class MaterialPlanPost extends Post<MaterialPlan> {
    private static final long serialVersionUID = 5347073981886038520L;

    public MaterialPlanPost() {
        DateTime now = DateTime.now(Dates.timeZone(null));
        this.from = now.minusDays(5).toDate();
        this.to = now.toDate();
        this.dateType = DateType.CREATE;
    }

    /**
     * 由于在 Action Redirect 的时候, 需要保留参数, 而 Play 并没有保留, 所以只能多写一次
     */
    public Date from;
    public Date to;
    public Long cooperId;
    public Long userId;
    public MaterialPlan.P planState;
    public DateType dateType;
    public MaterialPlan.R receipt;
    public MaterialPlan.S financeState;

    public enum DateType {
        /**
         * 创建时间
         */
        CREATE {
            @Override
            public String label() {
                return "创建时间";
            }
        },
        /**
         * 交货时间
         */
        DELIVERY {
            @Override
            public String label() {
                return "交货时间";
            }
        };

        public abstract String label();
    }


    @Override
    public F.T2<String, List<Object>> params() {
        StringBuilder sbd = new StringBuilder(
                "SELECT DISTINCT d FROM MaterialPlan d LEFT JOIN d.units u WHERE 1=1 AND");
        List<Object> params = new ArrayList<>();
        /* 时间参数 **/
        if(this.dateType != null) {
            if(this.dateType == MaterialPlanPost.DateType.DELIVERY) {
                sbd.append(" d.deliveryDate>=? AND d.deliveryDate<=?");
            } else {
                sbd.append(" d.createDate>=? AND d.createDate<=?");
            }
            params.add(Dates.morning(this.from));
            params.add(Dates.night(this.to));
        }
        if(this.planState != null) {
            sbd.append(" AND d.state=?");
            params.add(this.planState);
        }
        if(this.userId != null && this.userId > 0) {
            sbd.append(" AND d.handler.id=?");
            params.add(this.userId);
        }
        if(this.cooperId != null && this.cooperId > 0) {
            sbd.append(" AND d.cooperator.id=?");
            params.add(this.cooperId);
        }
        if(this.receipt != null) {
            sbd.append(" AND d.receipt=?");
            params.add(this.receipt);
        }
        if(this.financeState != null) {
            sbd.append(" AND d.financeState=?");
            params.add(this.financeState);
        }
        /* 模糊查询参数 **/
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



