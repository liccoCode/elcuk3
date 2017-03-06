package models.view.post;


import helper.Dates;
import models.finance.TransportApply;
import models.procure.ShipItem;
import org.joda.time.DateTime;
import play.libs.F;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;


/**
 * 物流请款
 * <p/>
 * Created by IntelliJ IDEA.
 * User: DyLanM
 * Date: 13-8-5
 * Time: 上午10:09
 */
public class TransportApplyPost extends Post<TransportApply> {

    public TransportApplyPost() {
        DateTime now = DateTime.now(Dates.timeZone(null));
        this.from = now.minusMonths(2).toDate();
        this.to = now.toDate();
        this.dateType = DateType.CREATE;
        this.perSize = 25;
    }

    public TransportApplyPost(int perSize) {
        this.perSize = perSize;
    }

    public Date from;

    public Date to;

    public DateType dateType;

    public Long cooperId;

    /**
     * 请款人 ID
     */
    public Long userId;

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

        StringBuffer sql = new StringBuffer(" 1=1 ");
        List<Object> params = new ArrayList<>();

        if(this.dateType != null) {
            if(this.dateType == DateType.CREATE) {
                sql.append(" AND createdAt>=? AND createdAt<=?");
            } else {
                sql.append(" AND updateAt>=? AND updateAt<=?");
            }
            params.add(Dates.morning(this.from));
            params.add(Dates.night(this.to));
        }

        if(this.userId != null) {
            sql.append(" AND cooperator.id = ? ");
            params.add(this.cooperId);
        }

        if(this.userId != null) {
            sql.append(" AND applier.id = ? ");
            params.add(this.userId);
        }

        if(this.search != null && !"".equals(this.search)) {
            sql.append(" AND serialNumber like ?");
            params.add(this.word());
        }

        return new F.T2<>(sql.toString(), params);
    }

    public List<TransportApply> query() {
        F.T2<String, List<Object>> params = params();
        this.count = count(params);
        if(this.pagination)
            return TransportApply.find(params._1 + " ORDER BY createdAt DESC", params._2.toArray())
                    .fetch(this.page, this.perSize);
        else
            return TransportApply.find(params._1 + " ORDER BY createdAt DESC", params._2.toArray()).fetch();
    }

    @Override
    public Long count(F.T2<String, List<Object>> params) {
        return TransportApply.count(params._1, params._2.toArray());
    }

    @Override
    public Long getTotalCount() {
        return this.count();
    }

}
