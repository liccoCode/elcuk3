package models.view.post;

import helper.Dates;
import models.procure.Shipment;
import models.whouse.Outbound;
import models.whouse.StockRecord;
import org.apache.commons.lang.StringUtils;
import org.joda.time.DateTime;
import play.libs.F;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Created by licco on 2016/11/30.
 */
public class OutboundPost extends Post<Outbound> {

    private static final Pattern ID = Pattern.compile("^PTC(\\|\\d{6}\\|\\d+)$");
    private static final Pattern NUM = Pattern.compile("^[0-9]*$");
    private static final long serialVersionUID = -6309634618116762879L;
    public Outbound.S status;
    public StockRecord.C type;

    public String projectName;
    public Shipment.T shipType;
    public String search;
    public String flag = "Normal";
    public String whichPage;
    public DateType dateType;

    public enum DateType {
        /**
         * 创建时间
         */
        CREATE {
            @Override
            public String label() {
                return "创建时间";
            }
        },
        /**
         * 出库时间
         */
        OUTBOUND {
            @Override
            public String label() {
                return "出库时间";
            }
        };

        public abstract String label();
    }

    public OutboundPost() {
        DateTime now = DateTime.now(Dates.timeZone(null));
        this.from = now.minusDays(5).toDate();
        this.to = now.toDate();
        this.status = Outbound.S.Create;
        this.perSize = 25;
        this.page = 1;
    }

    public OutboundPost(OutboundPost p) {
        DateTime now = DateTime.now(Dates.timeZone(null));
        this.from = p.from;
        this.to = p.to;
        this.status = p.status;
        this.type = p.type;
        this.shipType = p.shipType;
        this.search = p.search;
        this.projectName = p.projectName;
        this.perSize = 25;
        this.page = 1;
    }

    public F.T2<String, List<Object>> params() {
        StringBuilder sbd = new StringBuilder("SELECT DISTINCT o FROM Outbound o ");
        if(Objects.equals(flag, StockRecord.C.Normal.name()) || Objects.equals(flag, StockRecord.C.B2B.name()))
            sbd.append(" LEFT JOIN o.units u WHERE 1=1");
        else
            sbd.append(" LEFT JOIN o.records u WHERE 1=1");
        List<Object> params = new ArrayList<>();

        /* 时间参数 **/
        if(this.dateType != null) {
            if(this.dateType == OutboundPost.DateType.OUTBOUND) {
                sbd.append(" AND o.outboundDate >= ? AND o.outboundDate <= ? ");
            } else {
                sbd.append(" AND o.createDate >= ? AND o.createDate <= ? ");
            }
            params.add(Dates.morning(this.from));
            params.add(Dates.night(this.to));
        }

        if(StringUtils.isNotEmpty(this.search)) {
            if(StringUtils.isNotEmpty(isSearchForId())) {
                sbd.append(" AND o.id = ? ");
                params.add(isSearchForId());
                return new F.T2<>(sbd.toString(), params);
            }
            if(isNumForSearch() != null) {
                if(Objects.equals(flag, StockRecord.C.Normal.name()) || Objects.equals(flag, StockRecord.C.B2B.name())) {
                    sbd.append(" AND u.id = ? ");
                    params.add(isNumForSearch());
                    sbd.append(" AND o.type = ? ");
                    params.add(StockRecord.C.valueOf(flag));
                } else {
                    sbd.append(" AND u.unit.id = ? ");
                    params.add(isNumForSearch());
                }
                return new F.T2<>(sbd.toString(), params);
            }
            if(Objects.equals(flag, StockRecord.C.Normal.name()) || Objects.equals(flag, StockRecord.C.B2B.name())) {
                sbd.append(" AND u.sku LIKE ? ");
            } else {
                sbd.append(" AND u.unit.sku LIKE ? ");
            }
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
        if(StringUtils.isNotEmpty(this.projectName)) {
            sbd.append(" AND o.projectName=? ");
            params.add(this.projectName);
        }
        if(this.shipType != null) {
            sbd.append(" AND o.shipType=? ");
            params.add(this.shipType);
        }
        if(Objects.equals(flag, StockRecord.C.Normal.name())) {
            sbd.append(" AND o.type = ? ");
            params.add(StockRecord.C.Normal);
        } else if(Objects.equals(flag, StockRecord.C.B2B.name())) {
            sbd.append(" AND o.type = ? ");
            params.add(StockRecord.C.B2B);
        } else {
            sbd.append(" AND o.type NOT IN (? , ?) ");
            params.add(StockRecord.C.Normal);
            params.add(StockRecord.C.B2B);
        }
        sbd.append(" ORDER BY o.createDate DESC");
        return new F.T2<>(sbd.toString(), params);
    }

    public List<Outbound> query() {
        this.count = this.count();
        F.T2<String, List<Object>> params = params();
        if(this.pagination)
            return Outbound.find(params._1, params._2.toArray()).fetch(this.page, this.perSize);
        else
            return Outbound.find(params._1, params._2.toArray()).fetch();
    }

    public List<Outbound> queryForB2B() {
        F.T2<String, List<Object>> params = params();
        if(this.pagination)
            return Outbound.find(params._1, params._2.toArray()).fetch(this.page, this.perSize);
        else
            return Outbound.find(params._1, params._2.toArray()).fetch();
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
