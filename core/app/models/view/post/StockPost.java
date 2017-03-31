package models.view.post;

import helper.DBUtils;
import helper.Dates;
import models.procure.Cooperator;
import models.procure.ProcureUnit;
import org.apache.commons.lang.StringUtils;
import org.apache.commons.lang.math.NumberUtils;
import play.db.helper.SqlSelect;
import play.libs.F;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

/**
 * Created by licco on 2016/12/8.
 */
public class StockPost extends Post<ProcureUnit> {

    private static final Pattern ID = Pattern.compile("^-?[1-9]\\d*$");

    public Long[] whouses;
    public Cooperator cooperator;
    public String projectName;

    public boolean flag = false;

    @Override
    public F.T2<String, List<Object>> params() {
        StringBuilder sbd = new StringBuilder("1=1");
        List<Object> params = new ArrayList<>();
        if(this.flag) {
            sbd.append(" AND unqualifiedQty > 0 ");
        } else {
            sbd.append(" AND availableQty > 0 ");
        }

        Long unit_id = isSearchForId();
        if(unit_id != null) {
            sbd.append(" AND id=?");
            params.add(unit_id);
            return new F.T2<>(sbd.toString(), params);
        }

        if(this.whouses != null && this.whouses.length > 0) {
            sbd.append(" AND currWhouse.id IN  " + SqlSelect.inlineParam(whouses));
        }

        if(cooperator != null && this.cooperator.id != null) {
            sbd.append(" AND cooperator.id=?");
            params.add(this.cooperator.id);
        }

        if(StringUtils.isNotBlank(this.projectName)) {
            sbd.append(" AND projectName=?");
            params.add(this.projectName);
        }
        if(StringUtils.isNotBlank(this.search)) {
            sbd.append(" AND (product.sku LIKE ? OR fba.shipmentId LIKE ? )");
            for(int i = 0; i < 2; i++) params.add(this.word());
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
        String sql = params._1 + " ORDER BY currWhouse.id DESC, createDate DESC";
        if(this.pagination)
            return ProcureUnit.find(sql, params._2.toArray()).fetch(this.page, this.perSize);
        else
            return ProcureUnit.find(sql, params._2.toArray()).fetch();
    }


    public List<ProcureUnit> queryUnQualifiedIndex() {
        F.T2<String, List<Object>> params = this.params();
        this.count = this.count(params);
        String sql = params._1 + " ORDER BY id DESC";
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


    /**
     * 根据正则表达式搜索是否有类似 id:123 这样的搜索如果有则直接进行 id 搜索
     *
     * @return
     */
    private Long isSearchForId() {
        if(StringUtils.isNotBlank(this.search)) {
            Matcher matcher = ID.matcher(this.search);
            if(matcher.find()) return NumberUtils.toLong(matcher.group(0));
        }
        return null;
    }

    public List<ProcureUnit> queryHistoryStock() {
        List<Object> params = new ArrayList<>();
        StringBuilder sql = new StringBuilder("SELECT p.id, p.sku, d.abbreviation, ");
        sql.append(" IFNULL((SELECT s.currQty FROM StockRecord s WHERE s.unit_id = p.id AND s.createDate <= ? ");
        params.add(Dates.night(this.to));
        sql.append(" ORDER BY s.id DESC LIMIT 1), 0) AS currQty");
        sql.append(" FROM ProcureUnit p LEFT JOIN Product d ON d.sku = p.product_sku ");
        sql.append(" WHERE  p.createDate >= ? AND p.createDate <= ? ");
        params.add(Dates.morning(Dates.aMonthAgo()));
        params.add(Dates.night(this.to));
        Long unit_id = isSearchForId();
        if(unit_id != null) {
            sql.append(" AND p.id=?");
            params.add(unit_id);
        }
        if(this.whouses != null && this.whouses.length > 0) {
            sql.append(" AND p.currWhouse_id IN  " + SqlSelect.inlineParam(whouses));
        }
        sql.append(" ORDER BY p.id DESC ");
        List<Map<String, Object>> rows = DBUtils.rows(sql.toString(), params.toArray());
        List<ProcureUnit> units = rows.stream().filter(row -> Integer.parseInt(row.get("currQty").toString()) > 0)
                .map(row -> {
                    ProcureUnit unit = ProcureUnit.findById(Long.parseLong(row.get("id").toString()));
                    unit.currQty = Integer.parseInt(row.get("currQty").toString());
                    return unit;
                }).collect(Collectors.toList());

        return units;
    }
}
