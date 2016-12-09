package models.view.post;

import helper.Dates;
import models.whouse.Inbound;
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

    public Inbound.S status;
    public Long cooperatorId;
    public Inbound.T type;
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
        StringBuilder sbd = new StringBuilder("1=1");
        List<Object> params = new ArrayList<>();
        String id = isSearchForId();
        if(StringUtils.isNotEmpty(id)) {
            sbd.append(" AND id = ? ");
            params.add(id);
            return new F.T2<>(sbd.toString(), params);
        }
        sbd.append(" AND createDate >= ? AND createDate <= ? ");
        params.add(Dates.morning(this.from));
        params.add(Dates.night(this.to));
        if(status != null) {
            sbd.append(" AND status = ? ");
            params.add(this.status);
        }
        if(cooperatorId != null) {
            sbd.append(" AND cooperator.id = ? ");
            params.add(cooperatorId);
        }
        if(type != null) {
            sbd.append(" AND type = ? ");
            params.add(type);
        }
        sbd.append(" ORDER BY createDate DESC");
        return new F.T2<>(sbd.toString(), params);
    }


    @Override
    public List<Inbound> query() {
        this.count = this.count();
        F.T2<String, List<Object>> params = params();
        return Inbound.find(params._1, params._2.toArray()).fetch(this.page, this.perSize);
    }

    @Override
    public Long getTotalCount() {
        return this.count();
    }


    @Override
    public Long count(F.T2<String, List<Object>> params) {
        return Inbound.count(params._1, params._2.toArray());
    }

    private String isSearchForId() {
        if(StringUtils.isNotBlank(this.search)) {
            Matcher matcher = ID.matcher(this.search);
            if(matcher.find()) return matcher.group(1);
        }
        return null;
    }
}
