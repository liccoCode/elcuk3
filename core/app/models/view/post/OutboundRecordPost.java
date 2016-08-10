package models.view.post;

import helper.Dates;
import models.market.M;
import models.procure.Shipment;
import models.whouse.OutboundRecord;
import org.apache.commons.lang3.StringUtils;
import org.joda.time.DateTime;
import play.libs.F;
import play.utils.FastRuntimeException;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Pattern;

/**
 * Created by IntelliJ IDEA.
 * User: duan
 * Date: 4/5/16
 * Time: 5:57 PM
 */
public class OutboundRecordPost extends Post<OutboundRecord> {
    private static final Pattern ID = Pattern.compile("^id:(\\d*)$");
    public static final List<F.T2<String, String>> DATE_TYPES;

    static {
        DATE_TYPES = new ArrayList<>();
        DATE_TYPES.add(new F.T2<>("createDate", "创建时间"));
        DATE_TYPES.add(new F.T2<>("planBeginDate", "预计运输时间"));
    }

    public String dateType;
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
        this.dateType = "planBeginDate";
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
            sbd.append(" AND (stockObjId LIKE ? OR attributes LIKE ?)");
            params.add(this.word());
            params.add("%\"fba\":\"" + this.search + "\"%");
        }

        this.setupDateRange(sbd, params);

        if(StringUtils.equalsIgnoreCase(this.dateType, "createDate")) {
            sbd.append(" ORDER BY createDate DESC");
        } else if(StringUtils.equalsIgnoreCase(this.dateType, "planBeginDate")) {
            sbd.append(" ORDER BY substring_index(substring_index(attributes, 'planBeginDate\":\"', -1), '\"', 1) DESC");
        }
        return new F.T2<>(sbd.toString(), params);
    }

    /**
     * 设置时间区间过滤条件
     *
     * @param sql
     * @param params
     */
    public void setupDateRange(StringBuilder sql, List<Object> params) {
        String dateRangeSql = null;
        if(StringUtils.equalsIgnoreCase(this.dateType, "createDate")) {
            dateRangeSql = " AND createDate";
        } else if(StringUtils.equalsIgnoreCase(this.dateType, "planBeginDate")) {
            dateRangeSql = " AND STR_TO_DATE(" +
                    "substring_index(" +
                    "substring_index(attributes, 'planBeginDate\":\"', -1)," +
                    "'\"', 1)," +
                    "'%Y-%m-%d %H:%i:%s'" +
                    ")";
        } else {
            throw new FastRuntimeException("非法的 dateType!");
        }
        if(this.from != null) {
            sql.append(dateRangeSql).append(">=?");
            params.add(Dates.morning(this.from));
        }
        if(this.to != null) {
            sql.append(dateRangeSql).append("<=?");
            params.add(Dates.night(this.to));
        }
    }

    @Override
    public Long count(F.T2<String, List<Object>> params) {
        return OutboundRecord.count(params._1, params._2.toArray());
    }

    @Override
    public Long getTotalCount() {
        return this.count();
    }

    public List<OutboundRecord> queryForExcel() {
        F.T2<String, List<Object>> params = params();
        return OutboundRecord.find(params._1, params._2.toArray()).fetch();
    }
}
