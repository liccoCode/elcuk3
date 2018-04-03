package models.view.post;

import helper.Dates;
import models.whouse.StockRecord;
import models.whouse.Whouse;
import org.apache.commons.lang.StringUtils;
import play.db.helper.SqlSelect;
import play.libs.F;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Created by IntelliJ IDEA.
 * User: duan
 * Date: 4/5/16
 * Time: 6:00 PM
 */
public class StockRecordPost extends Post<StockRecord> {
    private static final long serialVersionUID = -4525833753528075605L;
    public Date from;
    public Date to;
    public Whouse whouse;
    public Long unitId;
    public Long cooperatorId;
    public List<String> typeList = new ArrayList<>();

    private static Pattern ID = Pattern.compile("^-?[1-9]\\d*$");


    public StockRecordPost() {
        this.page = 1;
        this.perSize = 50;
    }

    public StockRecordPost(Long unitId) {
        this.unitId = unitId;
        this.page = 1;
        this.perSize = 50;
    }

    @Override
    public F.T2<String, List<Object>> params() {
        StringBuilder sbd = new StringBuilder("SELECT s FROM StockRecord s ");
        sbd.append(" LEFT JOIN  s.unit u ");
        sbd.append(" LEFT JOIN  u.fba f ");
        sbd.append(" WHERE 1=1 ");
        List<Object> params = new ArrayList<>();

        if(this.unitId != null) {
            sbd.append(" AND s.unit.id = ?");
            params.add(this.unitId);
            return new F.T2<>(sbd.toString(), params);
        }

        if(StringUtils.isNotEmpty(this.search)) {
            Matcher matcher = ID.matcher(this.search);
            if(matcher.find()) {
                sbd.append(" AND s.unit.id = ?");
                params.add(Long.parseLong(StringUtils.trim(this.search)));
                return new F.T2<>(sbd.toString(), params);
            }
            sbd.append("AND (u.sku LIKE ? OR f.shipmentId LIKE ? )");
            params.add("%" + search + "%");
            params.add("%" + search + "%");
        }

        if(this.typeList.size() > 0) {
            sbd.append(" AND s.type IN ").append(SqlSelect.inlineParam(this.typeList));
        }

        if(this.cooperatorId != null) {
            sbd.append(" AND u.cooperator.id=?");
            params.add(this.cooperatorId);
        }

        if(this.whouse != null && this.whouse.id != null) {
            sbd.append(" AND s.whouse.id=?");
            params.add(this.whouse.id);
        }

        if(this.from != null) {
            sbd.append(" AND s.createDate>=?");
            params.add(Dates.morning(this.from));
        }

        if(this.to != null) {
            sbd.append(" AND s.createDate<=?");
            params.add(Dates.night(this.to));
        }
        return new F.T2<>(sbd.toString(), params);
    }

    @Override
    public List<StockRecord> query() {
        F.T2<String, List<Object>> params = this.params();
        this.count = this.count(params);

        String sql = params._1 + " ORDER BY s.id DESC";
        if(this.pagination) {
            return StockRecord.find(sql, params._2.toArray()).fetch(this.page, this.perSize);
        } else {
            return StockRecord.find(sql, params._2.toArray()).fetch();
        }
    }

    @Override
    public Long count(F.T2<String, List<Object>> params) {
        return (long) StockRecord.find(params._1, params._2.toArray()).fetch().size();
    }

    @Override
    public Long getTotalCount() {
        return this.count();
    }
}
