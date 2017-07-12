package models.view.post;

import helper.Dates;
import models.User;
import models.procure.ProcureUnit;
import models.procure.Shipment;
import models.procure.iExpress;
import org.apache.commons.lang.StringUtils;
import org.joda.time.DateTime;
import play.db.helper.SqlSelect;
import play.libs.F;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Created by IntelliJ IDEA.
 * User: wyattpan
 * Date: 9/8/12
 * Time: 11:06 AM
 */
public class ShipmentPost extends Post<Shipment> {
    private static final long serialVersionUID = -2199641514934402388L;
    public static final List<F.T2<String, String>> DATE_TYPES;
    private static final Pattern ID = Pattern.compile("^(\\w{2}\\|\\d{6}\\|\\d+)$");
    private static final Pattern NUM = Pattern.compile("^[0-9]*$");
    private static Pattern SHIPITEMS_NUM_PATTERN = Pattern.compile("^\\+(\\d+)$");
    private static final Pattern DELIVER_ID = Pattern.compile("^DL(\\|\\d{6}\\|\\d+)$");

    public User.COR projectName;

    public ShipmentPost() {
        DateTime now = DateTime.now(Dates.timeZone(null));
        this.from = now.minusDays(7).toDate();
        this.to = now.plusDays(7).toDate();
        this.states = Arrays.asList(Shipment.S.PLAN, Shipment.S.CONFIRM, Shipment.S.SHIPPING,
                Shipment.S.CLEARANCE, Shipment.S.BOOKED, Shipment.S.DELIVERYING,
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

    public List<Long> whouseIds = new ArrayList<>();

    @Override
    public List<Shipment> query() {
        F.T2<String, List<Object>> params = this.params();
        this.count = this.count();
        if(this.pagination)
            return Shipment.find(params._1 + " GROUP BY s ORDER BY s.createDate DESC", params._2.toArray())
                    .fetch(this.page, this.perSize);
        else
            return Shipment.find(params._1 + " GROUP BY s ORDER BY s.createDate DESC", params._2.toArray()).fetch();
    }

    @Override
    public F.T2<String, List<Object>> params() {
        List<Object> params = new ArrayList<>();
        StringBuilder sql = new StringBuilder("SELECT s FROM Shipment s LEFT JOIN s.items i ");
        sql.append(" LEFT JOIN i.unit.fba f WHERE 1=1 ");

        /*如果传入进来的是shipmentId或者采购单Id*/
        if(StringUtils.isNotBlank(this.search)) {
            this.search = this.search.trim();
            Matcher deliver_matcher = DELIVER_ID.matcher(this.search);
            if(deliver_matcher.matches()) {
                String deliver_mentId = deliver_matcher.group();
                sql.append(" AND i.unit.deliveryment.id = ? ");
                params.add(deliver_mentId);
                return new F.T2<>(sql.toString(), params);
            }

            Matcher matcher = ID.matcher(this.search);
            if(matcher.matches()) {
                String shipmentId = matcher.group(1);
                sql.append(" AND s.id = ?");
                params.add(shipmentId);
                return new F.T2<>(sql.toString(), params);
            }
            Matcher num_matcher = NUM.matcher(this.search);
            if(num_matcher.matches()) {
                ProcureUnit unit = ProcureUnit.findById(Long.parseLong(this.search.trim()));
                if(unit != null) {
                    sql.append(" AND i.unit.id =? ");
                    params.add(Long.parseLong(this.search.trim()));
                    return new F.T2<>(sql.toString(), params);
                }
            }
        }

        if(this.dateType.equals("createDate")) {
            sql.append(" AND s.").append(this.dateType).append(">=?");
            sql.append(" AND s.").append(this.dateType).append("<=?");
        } else {
            sql.append(" AND s.dates.").append(this.dateType).append(">=?");
            sql.append(" AND s.dates.").append(this.dateType).append("<=?");
        }
        params.add(Dates.morning(this.from));
        params.add(Dates.morning(this.to));

        if(this.type != null) {
            sql.append(" AND s.type=?");
            params.add(this.type);
        }

        if(this.states != null && this.states.size() > 0) {
            List<String> states = new ArrayList<>();
            for(Shipment.S state : this.states) {
                if(state == null) continue;
                states.add(state.name());
            }
            if(states.size() > 0) sql.append(" AND s.state IN ").append(SqlSelect.inlineParam(states));
        }

        if(this.iExpress != null) {
            sql.append(" AND s.internationExpress=?");
            params.add(this.iExpress);
        }

        if(this.whouseIds.size() > 0) {
            sql.append(" AND s.whouse.id in ").append(SqlSelect.inlineParam(whouseIds));
        }

        if(this.cooperId != null) {
            sql.append(" AND s.cooper.id=?");
            params.add(this.cooperId);
        }

        if(this.projectName != null) {
            sql.append(" AND s.projectName = ?");
            params.add(this.projectName);
        }

        if(StringUtils.isNotBlank(this.search)) {
            String word = this.word();
            sql.append(" AND (s.trackNo LIKE ? OR s.jobNumber LIKE ? OR i.unit.sku LIKE ? OR f.shipmentId LIKE ? )");
            for(int i = 0; i < 4; i++) params.add(word);
        }
        return new F.T2<>(sql.toString(), params);
    }

    public Long count(F.T2<String, List<Object>> params) {
        return (long) ProcureUnit.find(params._1, params._2.toArray()).fetch().size();
    }

}
