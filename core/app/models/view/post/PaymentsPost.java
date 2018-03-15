package models.view.post;


import helper.Currency;
import helper.Dates;
import models.finance.Payment;
import org.joda.time.DateTime;
import play.db.helper.SqlSelect;
import play.libs.F;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;


/**
 * Created by IntelliJ IDEA.
 * User: DyLanM
 * Date: 13-8-5
 * Time: 上午10:09
 */
public class PaymentsPost extends Post<Payment> {

    public PaymentsPost() {
        DateTime now = DateTime.now(Dates.timeZone(null));
        this.from = now.minusMonths(2).toDate();
        this.to = now.toDate();
        this.dateType = DateType.CREATE;
        this.perSize = 25;
    }

    public PaymentsPost(int perSize) {
        this.perSize = perSize;
    }

    /**
     * 由于在 Action Redirect 的时候, 需要保留参数, 而 Play 并没有保留, 所以只能多写一次
     */
    public Date from;
    public Date to;

    public DateType dateType;

    public List<Payment.S> states = new ArrayList<>();

    public Long cooperId;

    public Currency actualCurrency;


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
        },
        PAYMENTDATE {
            @Override
            public String label() {
                return "付款时间";
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
            } else if(this.dateType == DateType.PAYMENTDATE) {
                sql.append(" AND paymentDate>=? AND paymentDate<=?");
            } else {
                sql.append(" AND updateAt>=? AND updateAt<=?");
            }
            params.add(Dates.morning(this.from));
            params.add(Dates.night(this.to));
        }

        if(this.states != null) {
            List<String> sts = new ArrayList<>();
            for(Payment.S state : this.states) {
                if(state == null) continue;
                sts.add(state.name());
            }
            if(sts.size() > 0) sql.append(" AND ").append(SqlSelect.whereIn("state", sts));
        }

        if(this.cooperId != null) {
            sql.append(" AND cooperator.id = ? ");
            params.add(this.cooperId);
        }

        if(this.actualCurrency != null) {
            sql.append("AND actualCurrency = ?");
            params.add(this.actualCurrency);
        }

        if(this.search != null && !"".equals(this.search.trim())) {
            sql.append(" AND target.name LIKE ?");
            params.add(this.word());
        }
        return new F.T2<>(sql.toString(), params);
    }

    public List<Payment> query() {
        F.T2<String, List<Object>> params = params();
        this.count = count(params);
        if(pagination)
            return Payment.find(params._1 + " ORDER BY createdAt DESC", params._2.toArray())
                    .fetch(this.page, this.perSize);
        else
            return Payment.find(params._1 + " ORDER BY createdAt DESC", params._2.toArray()).fetch();

    }

    @Override
    public Long count(F.T2<String, List<Object>> params) {
        return Payment.count("select count(paymentNumber) from Payment where " + params._1, params._2.toArray());
    }

    @Override
    public Long getTotalCount() {
        return this.count();
    }

}
