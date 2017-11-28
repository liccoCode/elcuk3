package models.view.post;

import helper.Dates;
import models.whouse.Inbound;
import models.whouse.InboundUnit;
import org.apache.commons.lang.StringUtils;
import org.joda.time.DateTime;
import play.db.helper.SqlSelect;
import play.libs.F;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Created by IntelliJ IDEA.
 * User: licco
 * Date: 2016/11/11
 * Time: 下午2:06
 */
public class InboundPost extends Post<Inbound> {

    private static final Pattern ID = Pattern.compile("^SR(\\|\\d{6}\\|\\d+)$");
    private static final Pattern NUM = Pattern.compile("^[0-9]*$");
    private static final long serialVersionUID = 3427714858616879761L;

    public Inbound.S status;
    public Long cooperatorId;
    public Inbound.T type;
    public Inbound.DM deliveryMethod;
    public InboundUnit.R result;
    public String search;
    public String searchPage = "inbound";
    public List<String> categories = new ArrayList<>();

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
        if(deliveryMethod != null) {
            sbd.append(" AND i.deliveryMethod = ? ");
            params.add(deliveryMethod);
        }
        sbd.append(" ORDER BY i.createDate DESC ");
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
        sbd.append(" AND i.inbound.receiveDate >= ? AND i.inbound.receiveDate <= ? ");
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
        if(this.categories.size() > 0) {
            sbd.append(" AND u.product.category.id IN ").append(SqlSelect.inlineParam(this.categories));
        }
        sbd.append(" ORDER BY u.id DESC ");
        return new F.T2<>(sbd.toString(), params);
    }


    @Override
    public List<Inbound> query() {
        F.T2<String, List<Object>> params = params();
        this.count = Inbound.find(params._1, params._2.toArray()).fetch().size();
        String sql = params._1 + " ";
        return Inbound.find(sql, params._2.toArray()).fetch(this.page, this.perSize);
    }

    public List<InboundUnit> queryDetail() {
        F.T2<String, List<Object>> params = detailParams();
        this.count = InboundUnit.find(params._1, params._2.toArray()).fetch().size();
        if(this.pagination)
            return InboundUnit.find(params._1, params._2.toArray()).fetch(this.page, this.perSize);
        else
            return InboundUnit.find(params._1, params._2.toArray()).fetch();
    }

    @Override
    public Long getTotalCount() {
        return this.count;
    }

    @Override
    public Long count(F.T2<String, List<Object>> params) {
        return this.count;
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
