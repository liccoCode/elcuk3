package models.view.post;

import models.procure.ReceiveRecord;
import models.procure.Shipment;
import org.apache.commons.lang.StringUtils;
import org.apache.commons.lang.math.NumberUtils;
import org.joda.time.DateTime;
import play.libs.F;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

/**
 * Created by IntelliJ IDEA.
 * User: duan
 * Date: 5/23/16
 * Time: 3:59 PM
 */
public class ReceiveRecordPost extends Post<ReceiveRecord> {
    public static final List<F.T2<String, String>> DATE_TYPES;

    static {
        DATE_TYPES = new ArrayList<>();
        DATE_TYPES.add(new F.T2<>("createDate", "创建时间"));
        DATE_TYPES.add(new F.T2<>("confirmDate", "确认时间"));
    }

    public Date from;
    public Date to;

    public String dateType;
    public ReceiveRecord.S state;

    public Long whouseId;
    public Shipment.T shipType;
    public Long cooperatorId;

    public ReceiveRecordPost() {
        DateTime now = DateTime.now().withTimeAtStartOfDay();
        this.from = now.minusMonths(1).toDate();
        this.to = now.toDate();
        this.dateType = "createDate";
        this.state = ReceiveRecord.S.Pending;
        this.perSize = 25;
        this.page = 1;
    }

    @Override
    public F.T2<String, List<Object>> params() {
        StringBuilder sbd = new StringBuilder("SELECT DISTINCT r FROM ReceiveRecord r")
                .append(" LEFT JOIN r.procureUnit p")
                .append(" LEFT JOIN p.product pd")
                .append(" LEFT JOIN p.selling s")
                .append(" WHERE 1=1");
        List<Object> params = new ArrayList<>();

        String recordId = isSearchId();
        if(recordId != null) {
            sbd.append(" AND r.id=?");
            params.add(recordId);
            return new F.T2<>(sbd.toString(), params);
        }
        if(this.state != null) {
            sbd.append(" AND r.state=?");
            params.add(this.state);
        }
        if(this.whouseId != null) {
            sbd.append(" AND p.whouse.id=?");
            params.add(this.whouseId);
        }
        if(this.shipType != null) {
            sbd.append(" AND p.shipType=?");
            params.add(this.shipType);
        }
        if(this.cooperatorId != null) {
            sbd.append(" AND p.cooperator.id=?");
            params.add(this.cooperatorId);
        }
        if(StringUtils.isNotBlank(this.search)) {
            sbd.append(" AND (");

            if(NumberUtils.isNumber(this.search)) {
                sbd.append(" p.id=? OR");
                params.add(NumberUtils.toLong(this.search));
            }

            String word = this.word();
            sbd.append(" p.sku LIKE ?")
                    .append(" OR pd.sku LIKE ?")
                    .append(" OR pd.abbreviation LIKE ?")
                    .append(" OR s.fnSku LIKE ?")
                    .append(")");
            for(int i = 0; i < 4; i++) params.add(word);
        }
        if(StringUtils.equalsIgnoreCase(this.dateType, "createDate")) {
            sbd.append(" ORDER BY r.createDate DESC");
        } else if(StringUtils.equalsIgnoreCase(this.dateType, "confirmDate")) {
            sbd.append(" ORDER BY r.confirmDate DESC");
        }

        return new F.T2<>(sbd.toString(), params);
    }

    @Override
    public Long getTotalCount() {
        return this.count();
    }

    @Override
    public List<ReceiveRecord> query() {
        this.count = this.count();
        F.T2<String, List<Object>> params = params();
        return ReceiveRecord.find(params._1, params._2.toArray()).fetch(this.page, this.perSize);
    }

    @Override
    public Long count(F.T2<String, List<Object>> params) {
        return (long) ReceiveRecord.find(params._1, params._2.toArray()).fetch().size();
    }

    public String isSearchId() {
        if(StringUtils.containsIgnoreCase(this.search, "id:")) {
            String[] match = StringUtils.split(this.search, "id:");
            if(match != null && match.length > 0) return match[0];
        }
        return null;
    }
}
