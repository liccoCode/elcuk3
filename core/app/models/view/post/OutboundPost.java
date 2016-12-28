package models.view.post;

import helper.Dates;
import models.whouse.Outbound;
import org.apache.commons.lang.StringUtils;
import org.joda.time.DateTime;
import play.libs.F;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Created by licco on 2016/11/30.
 */
public class OutboundPost extends Post<Outbound> {

    private static final Pattern ID = Pattern.compile("^PTC(\\|\\d{6}\\|\\d+)$");
    private static final Pattern NUM = Pattern.compile("^[0-9]*$");
    public Outbound.S status;
    public Outbound.T type;

    public String search;

    public OutboundPost() {
        DateTime now = DateTime.now(Dates.timeZone(null));
        this.from = now.minusDays(5).toDate();
        this.to = now.toDate();
        this.status = Outbound.S.Create;
        this.perSize = 25;
        this.page = 1;
    }

    @Override
    public F.T2<String, List<Object>> params() {
        StringBuilder sbd = new StringBuilder("SELECT DISTINCT o FROM Outbound o LEFT JOIN o.units u WHERE 1=1");
        List<Object> params = new ArrayList<>();
        sbd.append(" AND o.createDate >= ? AND o.createDate <= ? ");
        params.add(Dates.morning(this.from));
        params.add(Dates.night(this.to));

        if(StringUtils.isNotEmpty(this.search)) {
            if(StringUtils.isNotEmpty(isSearchForId())) {
                sbd.append(" AND o.id = ? ");
                params.add(isSearchForId());
                return new F.T2<>(sbd.toString(), params);
            }
            if(isNumForSearch() != null) {
                sbd.append(" AND o.unit.id = ? ");
                params.add(isNumForSearch());
                return new F.T2<>(sbd.toString(), params);
            }
            sbd.append(" AND o.unit.sku LIKE ? ");
            params.add("%" + this.search + "%");
        }
        if(status != null) {
            sbd.append(" AND o.status = ? ");
            params.add(this.status);
        }
        if(type != null) {
            sbd.append(" AND o.type = ? ");
            params.add(this.type);
        }
        sbd.append(" ORDER BY o.createDate DESC");
        return new F.T2<>(sbd.toString(), params);
    }

    @Override
    public List<Outbound> query() {
        this.count = this.count();
        F.T2<String, List<Object>> params = params();
        return Outbound.find(params._1, params._2.toArray()).fetch(this.page, this.perSize);
    }

    @Override
    public Long getTotalCount() {
        return this.count();
    }


    @Override
    public Long count(F.T2<String, List<Object>> params) {
        return (long) Outbound.find(params._1, params._2.toArray()).fetch().size();
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
