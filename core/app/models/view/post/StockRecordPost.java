package models.view.post;

import helper.DBUtils;
import helper.Dates;
import models.whouse.StockObj;
import models.whouse.StockRecord;
import models.whouse.Whouse;
import org.apache.commons.lang3.StringUtils;
import play.libs.F;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Map;

/**
 * Created by IntelliJ IDEA.
 * User: duan
 * Date: 4/5/16
 * Time: 6:00 PM
 */
public class StockRecordPost extends Post<StockRecord> {
    public Date from;
    public Date to;
    public Whouse whouse;
    public Long unitId;


    public StockRecordPost() {
        this.page = 1;
        this.perSize = 20;
    }

    public StockRecordPost(Long unitId) {
        this.unitId = unitId;
        this.page = 1;
        this.perSize = 20;
    }

    @Override
    public F.T2<String, List<Object>> params() {
        StringBuilder sbd = new StringBuilder("1=1");
        List<Object> params = new ArrayList<>();

        if(this.whouse != null && this.whouse.id != null) {
            sbd.append(" AND whouse_id=?");
            params.add(this.whouse.id);
        }

        if(unitId != null) {
            sbd.append(" AND unit.id = ? ");
            params.add(unitId);
        }

        if(this.from != null) {
            sbd.append(" AND createDate>=?");
            params.add(Dates.morning(this.from));
        }

        if(this.to != null) {
            sbd.append(" AND createDate<=?");
            params.add(Dates.night(this.to));
        }
        return new F.T2<>(sbd.toString(), params);
    }

    @Override
    public List<StockRecord> query() {
        F.T2<String, List<Object>> params = this.params();
        this.count = this.count(params);

        String sql = params._1 + " ORDER BY whouse.id, createDate DESC";
        if(this.pagination) {
            return StockRecord.find(sql, params._2.toArray()).fetch(this.page, this.perSize);
        } else {
            return StockRecord.find(sql, params._2.toArray()).fetch();
        }
    }

    @Override
    public Long count(F.T2<String, List<Object>> params) {
        return StockRecord.count(params._1, params._2.toArray());
    }

    @Override
    public Long getTotalCount() {
        return this.count();
    }

    /**
     * 盘点
     *
     * @return
     */
    public List<Map<String, Object>> checkRecords() {
        F.T2<String, List<Object>> params = this.params();

        String sql = String.format(
                "SELECT SUM(s.qty) as qty,s.stockObjId,s.stockObjType,w.name as whouse_name,s.attributes" +
                        " FROM StockRecord s " +
                        " LEFT JOIN Whouse w ON s.whouse_id=w.id" +
                        " WHERE %s AND s.qty!=0" +
                        " GROUP BY s.whouse_id,s.stockObjType,s.stockObjId,s.attributes", //whouseName fba shipType
                //如何处理多种类型的 StockObj(SKU 物料)
                params._1);

        List<Map<String, Object>> records = DBUtils.rows(sql, params._2.toArray());
        for(Map<String, Object> record : records) record.put("stockObj", new StockObj(record));
        return records;
    }
}
