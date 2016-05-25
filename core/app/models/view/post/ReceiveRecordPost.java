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
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Created by IntelliJ IDEA.
 * User: duan
 * Date: 5/23/16
 * Time: 3:59 PM
 */
public class ReceiveRecordPost extends Post<ReceiveRecord> {
    public static Pattern NUMBER_PATTEN = Pattern.compile("^\\d+$");
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
                .append(" WHERE 1=1");
        List<Object> params = new ArrayList<>();

        Long recordId = isSearchForId();
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
            params.add(this.shipType.label());
        }
        if(this.cooperatorId != null) {
            sbd.append(" AND p.cooperator.id=?");
            params.add(this.cooperatorId);
        }
        if(StringUtils.isNotBlank(this.search)) {
            sbd.append(" AND (");
            Matcher matcher = NUMBER_PATTEN.matcher(this.search);
            if(matcher.find()) {
                sbd.append(" r.deliverPlan.id=?").append(" OR p.id=?");
                long searchId = NumberUtils.toLong(matcher.group(1));
                for(int i = 0; i < 2; i++) params.add(searchId);
            }

            String word = this.word();
            sbd.append(" OR p.sku LIKE ?")
                    .append(" OR p.product.sku LIKE ?")
                    .append(" OR p.product.abbreviation LIKE ?")
                    .append(" OR p.selling.fnSku LIKE ?")
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
}
