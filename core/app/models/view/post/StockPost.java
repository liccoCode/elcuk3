package models.view.post;

import helper.Currency;
import helper.DBUtils;
import helper.Dates;
import models.procure.Cooperator;
import models.procure.ProcureUnit;
import models.whouse.StockRecord;
import org.apache.commons.lang.StringUtils;
import org.apache.commons.lang.math.NumberUtils;
import play.db.helper.SqlSelect;
import play.libs.F;

import java.math.BigDecimal;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

/**
 * Created by licco on 2016/12/8.
 */
public class StockPost extends Post<ProcureUnit> {

    private static final long serialVersionUID = 430478932544672708L;
    private static final Pattern ID = Pattern.compile("^-?[1-9]\\d*$");

    public Long[] whouses;
    public Cooperator cooperator;
    public String projectName;
    public List<String> categories = new ArrayList<>();

    public boolean flag = false;

    @Override
    public F.T2<String, List<Object>> params() {
        StringBuilder sbd = new StringBuilder("SELECT p FROM ProcureUnit p LEFT JOIN p.fba fba WHERE 1=1");
        List<Object> params = new ArrayList<>();
        if(this.flag) {
            sbd.append(" AND p.unqualifiedQty > 0 ");
        } else {
            sbd.append(" AND p.availableQty > 0 ");
        }

        Long unit_id = isSearchForId();
        if(unit_id != null) {
            sbd.append(" AND p.id=?");
            params.add(unit_id);
            return new F.T2<>(sbd.toString(), params);
        }

        if(this.whouses != null && this.whouses.length > 0) {
            sbd.append(" AND p.currWhouse.id IN  ").append(SqlSelect.inlineParam(whouses));
        }

        if(cooperator != null && this.cooperator.id != null) {
            sbd.append(" AND p.cooperator.id=?");
            params.add(this.cooperator.id);
        }

        if(StringUtils.isNotBlank(this.projectName)) {
            sbd.append(" AND p.projectName=?");
            params.add(this.projectName);
        }
        if(StringUtils.isNotBlank(this.search)) {
            sbd.append(" AND (p.product.sku LIKE ? OR p.fba.shipmentId LIKE ? )");
            for(int i = 0; i < 2; i++) params.add(this.word());
        }
        if(categories.size() > 0) {
            sbd.append(" AND p.product.category.id IN ").append(SqlSelect.inlineParam(categories));
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
        String sql = params._1 + " ORDER BY p.createDate DESC, p.currWhouse.id DESC";
        if(this.pagination)
            return ProcureUnit.find(sql, params._2.toArray()).fetch(this.page, this.perSize);
        else
            return ProcureUnit.find(sql, params._2.toArray()).fetch();
    }

    public Map<String, String> total() {
        Map<String, String> map = new HashMap<>();
        F.T2<String, List<Object>> params = this.params();
        List<ProcureUnit> units = ProcureUnit.find(params._1, params._2.toArray()).fetch();
        Integer totalQty = units.stream().mapToInt(unit -> unit.availableQty).sum();
        map.put("totalQty", totalQty.toString());
        Double totalCNY = units.stream().filter(unit -> Objects.equals(Currency.CNY, unit.attrs.currency))
                .mapToDouble(unit -> unit.availableQty * unit.attrs.price).sum();
        BigDecimal b = new BigDecimal(totalCNY);
        map.put("totalCNY", b.setScale(2, BigDecimal.ROUND_HALF_UP).toString());
        Double totalUSD = units.stream().filter(unit -> Objects.equals(Currency.USD, unit.attrs.currency))
                .mapToDouble(unit -> unit.availableQty * unit.attrs.price).sum();
        b = new BigDecimal(totalUSD);
        map.put("totalUSD", b.setScale(2, BigDecimal.ROUND_HALF_UP).toString());

        F.T2<String, List<Object>> stockParams = this.stockParams();

        List<StockRecord> recordList = StockRecord.find(stockParams._1, stockParams._2.toArray()).fetch();
        Integer inboundQty = recordList.stream()
                .filter(record -> Arrays.asList(StockRecord.T.Inbound, StockRecord.T.Unqualified_Transfer)
                        .contains(record.type)).mapToInt(record -> record.qty).sum();
        Integer outboundQty = recordList.stream().filter(record -> Arrays
                .asList(StockRecord.T.Outbound, StockRecord.T.OtherOutbound, StockRecord.T.Refund).contains(record.type))
                .mapToInt(record -> record.qty).sum();
        map.put("inboundQty", inboundQty.toString());
        map.put("outboundQty", outboundQty.toString());
        return map;
    }

    public F.T2<String, List<Object>> stockParams() {
        Date now = new Date();
        StringBuilder sbd = new StringBuilder("SELECT r FROM StockRecord r LEFT JOIN r.unit p LEFT JOIN p.fba fba ");
        List<Object> params = new ArrayList<>();
        sbd.append("WHERE 1=1 AND r.createDate>=? AND r.createDate<=? AND r.type IN (?,?,?,?,?)");
        params.add(Dates.morning(now));
        params.add(Dates.night(now));
        params.add(StockRecord.T.Inbound);
        params.add(StockRecord.T.Unqualified_Transfer);
        params.add(StockRecord.T.Outbound);
        params.add(StockRecord.T.OtherOutbound);
        params.add(StockRecord.T.Refund);
        if(this.flag) {
            sbd.append(" AND p.unqualifiedQty > 0 ");
        } else {
            sbd.append(" AND p.availableQty > 0 ");
        }
        Long unit_id = isSearchForId();
        if(unit_id != null) {
            sbd.append(" AND p.id=?");
            params.add(unit_id);
            return new F.T2<>(sbd.toString(), params);
        }

        if(this.whouses != null && this.whouses.length > 0) {
            sbd.append(" AND p.currWhouse.id IN  ").append(SqlSelect.inlineParam(whouses));
        }

        if(cooperator != null && this.cooperator.id != null) {
            sbd.append(" AND p.cooperator.id=?");
            params.add(this.cooperator.id);
        }

        if(StringUtils.isNotBlank(this.projectName)) {
            sbd.append(" AND p.projectName=?");
            params.add(this.projectName);
        }
        if(StringUtils.isNotBlank(this.search)) {
            sbd.append(" AND (p.product.sku LIKE ? OR p.fba.shipmentId LIKE ? )");
            for(int i = 0; i < 2; i++) params.add(this.word());
        }
        if(categories.size() > 0) {
            sbd.append(" AND p.product.category.id IN ").append(SqlSelect.inlineParam(categories));
        }
        return new F.T2<>(sbd.toString(), params);
    }


    public List<ProcureUnit> queryUnQualifiedIndex() {
        F.T2<String, List<Object>> params = this.params();
        this.count = this.count(params);
        String sql = params._1 + " ORDER BY p.id DESC";
        return ProcureUnit.find(sql, params._2.toArray()).fetch(this.page, this.perSize);
    }

    @Override
    public Long count(F.T2<String, List<Object>> params) {
        return (long) ProcureUnit.find(params._1, params._2.toArray()).fetch().size();
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
