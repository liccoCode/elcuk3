package models.view.post;

import helper.Dates;
import models.market.M;
import models.procure.Shipment;
import models.whouse.OutboundRecord;
import org.apache.commons.lang3.StringUtils;
import org.joda.time.DateTime;
import play.libs.F;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by IntelliJ IDEA.
 * User: duan
 * Date: 4/5/16
 * Time: 5:57 PM
 */
public class OutboundRecordPost extends Post<OutboundRecord> {
    public OutboundRecord.T type;
    public OutboundRecord.S state;
    public OutboundRecord.O origin;
    public Shipment.T shipType;
    public M market;

    public OutboundRecordPost() {
        DateTime now = DateTime.now().withTimeAtStartOfDay();
        this.from = now.minusMonths(1).toDate();
        this.to = now.toDate();
        this.perSize = 30;
        this.page = 1;
    }

    @Override
    public List<OutboundRecord> query() {
        this.count = this.count();
        F.T2<String, List<Object>> params = params();
        return OutboundRecord.find(params._1, params._2.toArray()).fetch(this.page, this.perSize);
    }

    @Override
    public F.T2<String, List<Object>> params() {
        StringBuilder sbd = new StringBuilder("1=1");
        List<Object> params = new ArrayList<>();

        Long recordId = isSearchForId();
        if(recordId != null) {
            sbd.append(" AND id=?");
            params.add(recordId);
            return new F.T2<>(sbd.toString(), params);
        }

        if(this.origin != null) {
            sbd.append(" AND origin=?");
            params.add(this.origin);
        }

        if(this.state != null) {
            sbd.append(" AND state=?");
            params.add(this.state);
        }

        if(this.from != null) {
            sbd.append(" AND createDate>=?");
            params.add(Dates.morning(this.from));
        }
        if(this.to != null) {
            sbd.append(" AND createDate<=?");
            params.add(Dates.night(this.to));
        }

        if(this.type != null) {
            sbd.append(" AND type=?");
            params.add(this.type);
        }

        if(this.state != null) {
            sbd.append(" AND state=?");
            params.add(this.state);
        }

        if(this.origin != null) {
            sbd.append(" AND origin=?");
            params.add(this.origin);
            ;
        }

        if(this.shipType != null) {
            sbd.append(" AND attributes LIKE ?");
            params.add("%\"shipType\":\"" + this.shipType.name() + "\"%");
        }

        if(this.market != null) {
            sbd.append(" AND attributes LIKE ?");
            params.add("%" + this.market.marketAndWhouseMapping() + "%");
        }

        if(StringUtils.isNotBlank(this.search)) {
            sbd.append(" AND (stockObjId LIKE ? OR attributes LIKE ?");
            params.add(this.word());
            params.add("%\"fba\":\"" + this.search + "\"%");
        }
        sbd.append(" ORDER BY createDate DESC");
        return new F.T2<>(sbd.toString(), params);
    }

    @Override
    public Long count(F.T2<String, List<Object>> params) {
        return OutboundRecord.count(params._1, params._2.toArray());
    }

    @Override
    public Long getTotalCount() {
        return this.count();
    }
}
