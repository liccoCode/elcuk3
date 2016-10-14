package models.view.post;

import helper.Dates;
import models.finance.Apply;
import models.finance.ProcureApply;
import org.joda.time.DateTime;
import play.db.jpa.GenericModel.JPAQuery;
import play.libs.F;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

/**
 * Created by IntelliJ IDEA.
 * User: wyatt
 * Date: 4/2/13
 * Time: 3:25 PM
 */
public class ProcreApplyPost extends Post<Apply> {

    public ProcreApplyPost() {
        DateTime now = DateTime.now(Dates.timeZone(null));
        this.from = now.minusMonths(2).toDate();
        this.to = now.toDate();
        this.dateType = DateType.CREATE;
        this.perSize = 25;
    }

    public ProcreApplyPost(int perSize) {
        this.perSize = perSize;
    }

    public Date from;
    public Date to;

    public DateType dateType;

    public Long supplierId;

    public int isneedPay;


    public enum DateType {

        CREATE {
            @Override
            public String label() {
                return "创建时间";
            }
        },
        UPDATE {
            @Override
            public String label() {
                return "更新时间";
            }
        };

        public abstract String label();
    }


    @Override
    public F.T2<String, List<Object>> params() {
        StringBuilder sql = new StringBuilder(" 1=1 ");
        List<Object> params = new ArrayList<>();

        //查询不需要付款的请款单
        if(this.isneedPay == 1) {
            sql = new StringBuilder(
                    "SELECT DISTINCT p FROM ProcureApply p LEFT JOIN p.deliveryments d "
                            + "  LEFT JOIN d.units u  WHERE 1=1 AND u.isNeedPay=false ");
        }

        if(this.dateType != null) {
            if(this.dateType == DateType.CREATE) {
                sql.append(" AND createdAt>=?  AND createdAt <=?");
            } else {
                sql.append(" AND updateAt>=? AND updateAt<=?");
            }
            params.add(Dates.morning(this.from));
            params.add(Dates.night(this.to));
        }

        if(this.supplierId != null) {
            sql.append(" AND cooperator.id=? ");
            params.add(this.supplierId);
        }

        if(this.search != null && !"" .equals(this.search.trim())) {
            sql.append(" AND serialNumber like ?");
            params.add(this.word());
        }

        return new F.T2<>(sql.toString(), params);
    }

    public List<Apply> query() {
        F.T2<String, List<Object>> params = params();
        JPAQuery query = ProcureApply.find(params._1 + "ORDER BY createdAt DESC",
                params._2.toArray());
        this.count = query.fetch().size();
        return query.fetch(this.page,this.perSize);
    }

    @Override
    public Long count(F.T2<String, List<Object>> params) {
        return ProcureApply.count(params._1, params._2.toArray());
    }

    @Override
    public Long getTotalCount() {
        return ProcureApply.count();
    }
}
