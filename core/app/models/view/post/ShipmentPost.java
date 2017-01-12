package models.view.post;

import helper.DBUtils;
import helper.Dates;
import models.procure.ProcureUnit;
import models.procure.Shipment;
import models.procure.iExpress;
import org.apache.commons.lang.StringUtils;
import org.joda.time.DateTime;
import play.db.helper.SqlSelect;
import play.libs.F;

import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Created by IntelliJ IDEA.
 * User: wyattpan
 * Date: 9/8/12
 * Time: 11:06 AM
 */
public class ShipmentPost extends Post<Shipment> {
    public static final List<F.T2<String, String>> DATE_TYPES;
    private static final Pattern ID = Pattern.compile("^(\\w{2}\\|\\d{6}\\|\\d+)$");
    private static final Pattern NUM = Pattern.compile("^[0-9]*$");
    private static Pattern SHIPITEMS_NUM_PATTERN = Pattern.compile("^\\+(\\d+)$");
    private static final Pattern UNITID = Pattern.compile("^DL(\\|\\d{6}\\|\\d+)$");

    public ShipmentPost() {
        DateTime now = DateTime.now(Dates.timeZone(null));
        this.from = now.minusDays(7).toDate();
        this.to = now.plusDays(7).toDate();
        this.states = Arrays.asList(Shipment.S.PLAN, Shipment.S.CONFIRM, Shipment.S.SHIPPING,
                Shipment.S.CLEARANCE, Shipment.S.PACKAGE, Shipment.S.BOOKED, Shipment.S.DELIVERYING,
                Shipment.S.RECEIPTD, Shipment.S.RECEIVING, Shipment.S.DONE);
        this.perSize = 50;
    }

    static {
        DATE_TYPES = new ArrayList<>();
        DATE_TYPES.add(new F.T2<>("planBeginDate", "预计开始运输时间"));
        DATE_TYPES.add(new F.T2<>("beginDate", "开始运输时间"));
        DATE_TYPES.add(new F.T2<>("createDate", "创建时间"));
        DATE_TYPES.add(new F.T2<>("planArrivDate", "预计 [到库] 时间"));
        DATE_TYPES.add(new F.T2<>("arriveDate", "实际 [到库] 时间"));
    }

    // 默认的搜索排序时间
    public String dateType = "planBeginDate";

    public Shipment.T type;

    public List<Shipment.S> states = new ArrayList<>();

    public iExpress iExpress;

    public Long cooperId;

    public long whouseId;

    @Override
    public List<Shipment> query() {
        F.T2<String, List<Object>> params = this.params();
        List<Map<String, Object>> rows = DBUtils.rows(params._1, params._2.toArray());
        List<Shipment> list = new ArrayList<>();

        for(Map<String, Object> row : rows) {
            Shipment shipment = new Shipment();
            shipment.id = row.get("id").toString();
            shipment.itemsNum = Integer.parseInt(this.returnStringOrNull(row.get("itemsNum")));
            shipment.trackNo = this.returnStringOrNull(row.get("trackNo"));
            shipment.arryParamSetUP(Shipment.FLAG.STR_TO_ARRAY);

            shipment.iExpressName = this.returnStringOrNull(row.get("internationExpress"));
            shipment.cname = this.returnStringOrNull(row.get("cname"));

            shipment.type = Shipment.T.valueOf(row.get("type").toString());
            shipment.wname = this.returnStringOrNull(row.get("wname"));
            shipment.target = this.returnStringOrNull(row.get("target"));
            shipment.state = Shipment.S.valueOf(row.get("state").toString());
            shipment.dates.planBeginDate = (Date) row.get("planBeginDate");
            shipment.createDate = (Date) row.get("createDate");
            shipment.dates.planArrivDate = (Date) row.get("planArrivDate");
            shipment.uname = this.returnStringOrNull(row.get("username"));
            shipment.memo = this.returnStringOrNull(row.get("memo"));
            shipment.applyId = this.returnStringOrNull(row.get("applyId"));
            shipment.outId = this.returnStringOrNull(row.get("outId"));
            shipment.realDay = row.get("realDay") == null ? null : Integer.parseInt(row.get("realDay").toString());
            list.add(shipment);
        }
        return list;
    }


    public String returnStringOrNull(Object o) {
        return o == null ? "" : o.toString();
    }

    @Override
    public F.T2<String, List<Object>> params() {
        SqlSelect sql = new SqlSelect().select("s.id, s.internationExpress, s.memo, s.apply_id as applyId, s.trackNo, " +
                "(SELECT count(1) FROM ShipItem si WHERE si.shipment_id=s.id) as itemsNum, c.name as cname, s.type," +
                " w.name AS wname, s.target, s.state, s.planBeginDate, s.createDate, s.planArrivDate, u.username, " +
                " TO_DAYS(s.arriveDate) - TO_DAYS(s.beginDate) as realDay,s.out_id as outId "
        ).from(" Shipment s ").leftJoin(" ShipItem i ON i.shipment_id = s.id "
        ).leftJoin(" Cooperator c ON c.id = s.cooper_id "
        ).leftJoin(" ProcureUnit pu ON pu.id = i.unit_id "
        ).leftJoin(" FBAShipment f ON f.id = pu.fba_id "
        ).leftJoin(" Whouse w ON w.id = s.whouse_id "
        ).leftJoin(" User u ON u.id = s.creater_id ");

        /**如果传入进来的是shipmentId或者采购单Id**/
        if(StringUtils.isNotBlank(this.search)) {
            this.search = this.search.trim();

            Matcher unitmatcher = UNITID.matcher(this.search);
            if(unitmatcher.matches()) {
                String unitmentId = unitmatcher.group();
                sql.where("pu.deliveryment_id = ?").params(unitmentId);
                sql.groupBy(" s.id");
                sql.orderBy(" s.createDate DESC");
                return new F.T2<>(sql.toString(), sql.getParams());
            }

            Matcher matcher = ID.matcher(this.search);
            if(matcher.matches()) {
                String shipmentId = matcher.group(1);
                sql.where("s.id = ?").params(shipmentId);
                sql.groupBy(" s.id");
                sql.orderBy(" s.createDate DESC");
                return new F.T2<>(sql.toString(), sql.getParams());
            }
            Matcher num_matcher = NUM.matcher(this.search);
            if(num_matcher.matches()) {
                ProcureUnit unit = ProcureUnit.findById(Long.parseLong(this.search.trim()));
                if(unit != null) {
                    sql.where("pu.id =? ").params(this.search.trim());
                    return new F.T2<>(sql.toString(), sql.getParams());
                }
            }
        }

        sql.where(" s." + this.dateType + ">=?").params(Dates.morning(this.from));
        sql.where(" s." + this.dateType + "<=?").params(Dates.night(this.to));

        if(this.type != null) {
            sql.andWhere(" s.type=?").params(this.type.name());
        }

        if(this.states != null && this.states.size() > 0) {
            List<String> states = new ArrayList<>();
            for(Shipment.S state : this.states) {
                if(state == null) continue;
                states.add(state.name());
            }
            if(states.size() > 0) sql.andWhere(sql.whereIn("s.state", states));
        }

        if(this.iExpress != null) {
            sql.andWhere(" s.internationExpress=?").params(this.iExpress.name());
        }

        if(this.whouseId > 0) {
            sql.andWhere(" s.whouse_id=?").params(this.whouseId);
        }

        if(this.cooperId != null) {
            sql.andWhere(" s.cooper_id=?").params(this.cooperId);
        }

        if(StringUtils.isNotBlank(this.search)) {
            String word = this.word();

            sql.andWhere("( s.trackNo LIKE ? ").param(word)
                    .orWhere("s.jobNumber LIKE ? ").params(word)
                    .orWhere("pu.sku LIKE ? ").params(word)
                    .orWhere("f.shipmentId LIKE ? ) ").params(word);
        }
        sql.groupBy(" s.id");
        sql.orderBy(" s.createDate DESC");
        return new F.T2<>(sql.toString(), sql.getParams());
    }
}
