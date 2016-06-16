package models.view.post;

import helper.DBUtils;
import helper.Dates;
import models.finance.PaymentUnit;
import models.procure.Cooperator;
import models.procure.ProcureUnit;
import models.procure.Shipment;
import models.whouse.Whouse;
import org.apache.commons.lang.StringUtils;
import org.apache.commons.lang.math.NumberUtils;
import org.joda.time.DateTime;
import org.jsoup.helper.StringUtil;
import play.db.helper.SqlSelect;
import play.i18n.Messages;
import play.libs.F;

import java.util.*;

/**
 * Created by IntelliJ IDEA.
 * User: wyattpan
 * Date: 9/3/12
 * Time: 4:32 PM
 */
public class ProcurePost extends Post<ProcureUnit> {
    public static final List<F.T2<String, String>> DATE_TYPES;

    static {
        DATE_TYPES = new ArrayList<>();
        DATE_TYPES.add(new F.T2<>("p.createDate", "创建时间"));
        DATE_TYPES.add(new F.T2<>("p.attrs.planDeliveryDate", "预计 [交货] 时间"));
        DATE_TYPES.add(new F.T2<>("p.attrs.deliveryDate", "实际 [交货] 时间"));
        DATE_TYPES.add(new F.T2<>("p.attrs.planArrivDate", "预计 [到库] 时间"));
        DATE_TYPES.add(new F.T2<>("p.attrs.planShipDate", "预计 [发货] 时间"));
    }

    /**
     * 在 ProcureUnits中，planView 和noPlaced 方法 需要调用 index，必须重写，否则总是构造方法中的时间
     */
    public Date from;
    public Date to;

    public long whouseId;

    public long cooperatorId;

    public ProcureUnit.STAGE stage;

    public PLACEDSTATE isPlaced;

    public Shipment.T shipType;

    public String unitIds;
    /**
     * 选择过滤的日期类型
     */
    public String dateType;

    public List<String> categories = new ArrayList<>();

    /**
     * 在 ProcureUnits中，downloadFBAZIP 方法 需要调用 传入 POST 查询条件， PLay 无法解析父类的属性，必须重写
     */
    public String search;

    public enum PLACEDSTATE {
        ARRIVE {
            @Override
            public String label() {
                return "抵达货代处";
            }
        },
        NOARRIVE {
            @Override
            public String label() {
                return "未抵达货代处";
            }

        };

        public abstract String label();
    }

    public ProcureUnit.OST isOut;

    public ProcurePost() {
        this.from = DateTime.now().minusDays(25).toDate();
        this.to = new Date();
        this.stage = ProcureUnit.STAGE.PLAN;
        this.dateType = "p.createDate";
        this.perSize = 70;
    }

    public ProcurePost(ProcureUnit.STAGE stage) {
        this();
        this.stage = stage;
    }

    public Long getTotalCount() {
        return this.count();
    }

    public List<ProcureUnit> query() {
        F.T2<String, List<Object>> params = params();
        this.count = this.count();
        return ProcureUnit.find(params._1 + " ORDER BY p.createDate DESC", params._2.toArray())
                .fetch(this.page, this.perSize);
    }

    public List<ProcureUnit> queryForExcel() {
        F.T2<String, List<Object>> params = params();
        return ProcureUnit.find(params._1 + " ORDER BY p.createDate DESC", params._2.toArray()).fetch();
    }

    @Override
    public Long count(F.T2<String, List<Object>> params) {
        return (long) ProcureUnit.find(params._1, params._2.toArray()).fetch().size();
    }

    public F.T2<String, List<Object>> params() {
        StringBuilder sbd = new StringBuilder("SELECT DISTINCT p FROM ProcureUnit p")
                .append(" LEFT JOIN p.product pd WHERE 1=1 ");
        List<Object> params = new ArrayList<>();

        if(NumberUtils.isNumber(this.search)) {
            sbd.append(" AND p.id=?");
            params.add(NumberUtils.toLong(this.search));
            return new F.T2<>(sbd.toString(), params);
        }
        if(StringUtils.isNotBlank(this.dateType)) {
            sbd.append(" AND ").append(this.dateType).append(">=?").append(" AND ").append(this.dateType)
                    .append("<=?");
            params.add(Dates.morning(this.from));
            params.add(Dates.night(this.to));
        }
        if(this.categories != null && !this.categories.isEmpty()) {
            sbd.append("AND pd.category.categoryId IN ").append(SqlSelect.inlineParam(this.categories));
        }
        if(this.whouseId > 0) {
            sbd.append(" AND p.whouse.id=?");
            params.add(this.whouseId);
        }
        if(this.cooperatorId > 0) {
            sbd.append(" AND p.cooperator.id=? ");
            params.add(this.cooperatorId);
        }
        if(this.stage != null) {
            sbd.append(" AND p.stage=? ");
            params.add(this.stage);
        }
        sbd.append(" AND p.stage != 'APPROVE'");
        if(this.shipType != null) {
            sbd.append(" AND p.shipType=? ");
            params.add(this.shipType);
        }
        if(this.isPlaced != null) {
            sbd.append(" AND p.isPlaced=? ");
            params.add(this.isPlaced == PLACEDSTATE.ARRIVE);
        }
        if(this.isOut != null) {
            sbd.append(" AND p.isOut=?");
            params.add(this.isOut);
        }
        if(StringUtils.isNotBlank(this.search)) {
            String word = this.word();
            sbd.append(" AND (")
                    .append("pd.sku LIKE ?")
                    .append(" OR pd.abbreviation LIKE ?")
                    .append(" OR p.selling.sellingId LIKE ?")
                    .append(" OR p.fba.shipmentId LIKE ?")
                    .append(") ");
            for(int i = 0; i < 2; i++) params.add(word);
        }
        if(StringUtils.isNotBlank(this.unitIds)) {
            List<String> unitIdList = Arrays.asList(StringUtils.split(this.unitIds, "_"));
            sbd.append(" AND p.id IN ").append(SqlSelect.inlineParam(unitIdList));
        }
        sbd.append(" AND p.attrs.planQty != 0");
        return new F.T2<>(sbd.toString(), params);
    }

    /**
     * 查询出所有采购计划的修改日志(写了修改原因的那些)
     *
     * @return
     */
    public List<HashMap<String, Object>> queryLogs() {
        String sql = "SELECT e.createAt as createAt, e.username as username, fid as fid," +
                "p.selling_sellingId as sellingId,p.isPlaced as isPlaced,f.shipmentId as fba,e.message as message " +
                "FROM ElcukRecord e LEFT JOIN ProcureUnit p ON e.fid = p.id LEFT JOIN FBAShipment f ON p.fba_id = f.id " +
                "WHERE e.action=? AND e.createAt >= ? AND e.createAt <= ? ORDER BY e.createAt DESC";
        List<Map<String, Object>> rows = DBUtils
                .rows(sql, Messages.get("procureunit.deepUpdate"), Dates.morning(this.from), Dates.night(this.to));
        List<HashMap<String, Object>> logs = new ArrayList<HashMap<String, Object>>();
        for(Map<String, Object> row : rows) {
            HashMap<String, Object> log = new HashMap<String, Object>();
            log.put("date", row.get("createAt"));
            log.put("user", row.get("username"));
            log.put("fid", row.get("fid"));
            log.put("sellingId", row.get("sellingId"));
            log.put("isPlaced", row.get("isPlaced"));
            log.put("fba", row.get("fba"));
            log.put("payInfo", this.generatePayInfo((String) row.get("fid")));
            String message = (String) row.get("message");
            log.put("reason", StringUtils.substringsBetween(message, "因[", "]")[0]);
            log.put("detail", StringUtils.substringsBetween(message, "更新内容[", "]")[0]);
            logs.add(log);
        }
        return logs;
    }

    /**
     * 生成采购计划的付款信息
     *
     * @param id
     * @return
     */
    public String generatePayInfo(String id) {
        ProcureUnit unit = ProcureUnit.<ProcureUnit>findById(NumberUtils.toLong(id));
        if(unit == null) {
            return "";
        } else {
            String paymentInfo = "";
            PaymentUnit prePay = unit.fetchPrePay();
            PaymentUnit tailPay = unit.fetchTailPay();
            if(prePay != null) {
                if(prePay.state == PaymentUnit.S.APPLY) paymentInfo += "已申请预付款";
                if(prePay.state == PaymentUnit.S.PAID) paymentInfo += "已付预付款";
            }
            if(tailPay != null) {
                if(tailPay.state == PaymentUnit.S.APPLY) paymentInfo += " 已申请尾款";
                if(tailPay.state == PaymentUnit.S.PAID) paymentInfo += " 已付尾款";
            }
            return paymentInfo;
        }
    }

    public String returnDateType() {
        if(StringUtil.isBlank(this.dateType)) return "";
        for(F.T2<String, String> params : DATE_TYPES) {
            if(dateType.equals(params._1)) return params._2;
        }
        return "";
    }

    public String returnWhouses() {
        if(this.whouseId == 0) return "";
        Whouse w = Whouse.findById(this.whouseId);
        return w.name;
    }

    public String returnCooperatorName() {
        if(this.cooperatorId == 0) return "供应商";
        Cooperator cooperator = Cooperator.findById(this.cooperatorId);
        return cooperator.name;
    }

    public String returnIsPlaced() {
        if(this.isPlaced == null) return "";
        return this.isPlaced.label();
    }

    public String returnShipType() {
        if(this.shipType == null) return "运输方式";
        return this.shipType.label();
    }
}
