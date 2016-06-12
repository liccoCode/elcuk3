package models.view.post;

import helper.Dates;
import models.whouse.InboundRecord;
import org.apache.commons.lang.math.NumberUtils;
import org.apache.commons.lang3.StringUtils;
import org.joda.time.DateTime;
import play.libs.F;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

/**
 * Created by IntelliJ IDEA.
 * User: duan
 * Date: 4/5/16
 * Time: 5:57 PM
 */
public class InboundRecordPost extends Post<InboundRecord> {
    public static final List<F.T2<String, String>> DATE_TYPES;

    static {
        DATE_TYPES = new ArrayList<>();
        DATE_TYPES.add(new F.T2<>("createDate", "提交入库时间"));
        DATE_TYPES.add(new F.T2<>("completeDate", "实际入库时间"));
    }

    public String dateType;

    public Date from;
    public Date to;

    public InboundRecord.O origin;
    public InboundRecord.S state;

    public Long cooperatorId;
    public String confirmer;

    public InboundRecordPost() {
        DateTime now = DateTime.now().withTimeAtStartOfDay();
        this.from = now.minusMonths(1).toDate();
        this.to = now.toDate();
        this.state = InboundRecord.S.Pending;
        this.dateType = "createDate";
        this.perSize = 25;
    }

    @Override
    public F.T2<String, List<Object>> params() {
        StringBuilder sbd = new StringBuilder("SELECT DISTINCT i FROM InboundRecord i")
                .append(" LEFT JOIN i.confirmer c")
                .append(" WHERE 1=1");
        List<Object> params = new ArrayList<>();

        Long recordId = isSearchForId();
        if(recordId != null) {
            sbd.append(" AND i.id=?");
            params.add(recordId);
            return new F.T2<>(sbd.toString(), params);
        }
        if(this.origin != null) {
            sbd.append(" AND i.origin=?");
            params.add(this.origin);
        }
        if(this.state != null) {
            sbd.append(" AND i.state=?");
            params.add(this.state);
        }
        if(this.from != null) {
            sbd.append(" AND i.createDate>=?");
            params.add(Dates.morning(this.from));
        }
        if(this.to != null) {
            sbd.append(" AND i.createDate<=?");
            params.add(Dates.night(this.to));
        }
        if(this.cooperatorId != null) {
            sbd.append(" AND i.attributes LIKE ?");
            params.add("%\"cooperatorId\":" + this.cooperatorId + "%");
        }
        if(StringUtils.isNotBlank(this.confirmer)) {
            sbd.append(" AND c.username=?");
            params.add(this.confirmer);
        }
        if(StringUtils.isNotBlank(this.search)) {
            sbd.append(" AND(");
            if(NumberUtils.isNumber(this.search)) {
                sbd.append("i.checkTask.id=? OR");
                params.add(NumberUtils.toLong(this.search));//质检任务
            }
            sbd.append(" i.stockObj.stockObjId LIKE ? ")
                    .append(" OR i.stockObj.attributes LIKE ?")
                    .append(" OR i.stockObj.attributes LIKE ?)");
            params.add(this.word());//物料(sku)
            params.add("%\"fba\":\"" + this.search + "\"%");//FBA
            params.add("%\"procureunitId\":" + this.search + "%");//采购计划 ID
        }
        sbd.append(String.format(" ORDER BY i.%s DESC", this.dateType));
        return new F.T2<>(sbd.toString(), params);
    }

    @Override
    public Long getTotalCount() {
        return this.count();
    }

    @Override
    public List<InboundRecord> query() {
        this.count = this.count();
        F.T2<String, List<Object>> params = params();
        return InboundRecord.find(params._1, params._2.toArray()).fetch(this.page, this.perSize);
    }

    @Override
    public Long count(F.T2<String, List<Object>> params) {
        return (long) InboundRecord.find(params._1, params._2.toArray()).fetch().size();
    }
}
