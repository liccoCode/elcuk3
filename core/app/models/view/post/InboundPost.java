package models.view.post;

import helper.Dates;
import models.whouse.Inbound;
import models.whouse.InboundUnit;
import org.apache.commons.lang.StringUtils;
import org.joda.time.DateTime;
import play.libs.F;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Created by licco on 2016/11/11.
 */
public class InboundPost extends Post<Inbound> {
    private static final Pattern ID = Pattern.compile("^SR(\\|\\d{6}\\|\\d+)$");
    private static final Pattern NUM = Pattern.compile("^[0-9]*$");

    public Inbound.S status;
    public Long cooperatorId;
    public Inbound.T type;
    public InboundUnit.R result;
    public String search;

    public InboundPost() {
        DateTime now = DateTime.now(Dates.timeZone(null));
        this.from = now.minusDays(5).toDate();
        this.to = now.toDate();
        this.perSize = 25;
        this.page = 1;
    }


    @Override
    public F.T2<String, List<Object>> params() {
        StringBuilder sbd = new StringBuilder("SELECT DISTINCT i FROM Inbound i LEFT JOIN i.units u WHERE 1=1 ");
        List<Object> params = new ArrayList<>();
        if(StringUtils.isNotEmpty(this.search)) {
            if(StringUtils.isNotEmpty(isSearchForId())) {
                sbd.append(" AND i.id = ? ");
                params.add(isSearchForId());
                return new F.T2<>(sbd.toString(), params);
            }
            if(isNumForSearch() != null) {
                sbd.append(" AND u.unit.id = ? ");
                params.add(isNumForSearch());
                return new F.T2<>(sbd.toString(), params);
            }
            sbd.append(" AND u.unit.sku LIKE ? ");
            params.add("%" + this.search + "%");
        }
        sbd.append(" AND i.createDate >= ? AND i.createDate <= ? ");
        params.add(Dates.morning(this.from));
        params.add(Dates.night(this.to));
        if(status != null) {
            sbd.append(" AND i.status = ? ");
            params.add(this.status);
        }
        if(cooperatorId != null) {
            sbd.append(" AND i.cooperator.id = ? ");
            params.add(cooperatorId);
        }
        if(type != null) {
            sbd.append(" AND i.type = ? ");
            params.add(type);
        }
        return new F.T2<>(sbd.toString(), params);
    }

    private F.T2<String, List<Object>> detailParams() {
        StringBuilder sbd = new StringBuilder("SELECT DISTINCT i FROM InboundUnit i LEFT JOIN i.unit u WHERE 1=1 ");
        List<Object> params = new ArrayList<>();
        if(StringUtils.isNotEmpty(this.search)) {
            if(StringUtils.isNotEmpty(isSearchForId())) {
                sbd.append(" AND i.inbound.id = ? ");
                params.add(isSearchForId());
                return new F.T2<>(sbd.toString(), params);
            }
            if(isNumForSearch() != null) {
                sbd.append(" AND u.id = ? ");
                params.add(isNumForSearch());
                return new F.T2<>(sbd.toString(), params);
            }
            sbd.append(" AND u.sku LIKE ? ");
            params.add("%" + this.search + "%");
        }
        sbd.append(" AND i.inbound.createDate >= ? AND i.inbound.createDate <= ? ");
        params.add(Dates.morning(this.from));
        params.add(Dates.night(this.to));
        if(status != null) {
            sbd.append(" AND i.inbound.status = ? ");
            params.add(this.status);
        }
        if(cooperatorId != null) {
            sbd.append(" AND u.cooperator.id = ? ");
            params.add(cooperatorId);
        }
        if(type != null) {
            sbd.append(" AND i.inbound.type = ? ");
            params.add(type);
        }
        if(result != null) {
            sbd.append(" AND i.result = ? ");
            params.add(result);
        }
        sbd.append(" ORDER BY u.id DESC ");
        return new F.T2<>(sbd.toString(), params);
    }


    @Override
    public List<Inbound> query() {
        this.count = this.count();
        F.T2<String, List<Object>> params = params();
        String sql = params._1 + " ";
        return Inbound.find(sql, params._2.toArray()).fetch(this.page, this.perSize);
    }

    public List<InboundUnit> queryDetail() {
        this.count = this.count();
        F.T2<String, List<Object>> params = detailParams();
        if(this.pagination)
            return InboundUnit.find(params._1, params._2.toArray()).fetch(this.page, this.perSize);
        else
            return InboundUnit.find(params._1, params._2.toArray()).fetch();
    }

    @Override
    public Long getTotalCount() {
        return this.count();
    }


    @Override
    public Long count(F.T2<String, List<Object>> params) {
        return (long) Inbound.find(params._1, params._2.toArray()).fetch().size();
    }

    private String isSearchForId() {
        if(StringUtils.isNotBlank(this.search)) {
            Matcher matcher = ID.matcher(this.search);
            if(matcher.find()) return matcher.group(0);
        }
        return null;
    }

    private Long isNumForSearch() {
        if(StringUtils.isNotBlank(this.search)) {
            Matcher matcher = NUM.matcher(this.search);
            if(matcher.find()) return Long.parseLong(matcher.group(0));
        }
        return null;
    }
}
