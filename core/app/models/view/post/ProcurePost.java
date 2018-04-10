package models.view.post;

import controllers.Login;
import helper.Currency;
import helper.DBUtils;
import helper.Dates;
import models.OperatorConfig;
import models.finance.PaymentUnit;
import models.procure.Cooperator;
import models.procure.ProcureUnit;
import models.procure.Shipment;
import models.product.Category;
import models.view.highchart.HighChart;
import models.view.highchart.Series;
import models.whouse.InboundUnit;
import models.whouse.Whouse;
import org.apache.commons.lang.StringUtils;
import org.apache.commons.lang.math.NumberUtils;
import org.joda.time.DateTime;
import org.jsoup.helper.StringUtil;
import play.db.helper.SqlSelect;
import play.i18n.Messages;
import play.libs.F;

import java.math.BigDecimal;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

/**
 * Created by IntelliJ IDEA.
 * User: wyattpan
 * Date: 9/3/12
 * Time: 4:32 PM
 */
public class ProcurePost extends Post<ProcureUnit> {

    private static final long serialVersionUID = 8821986351359739776L;

    private static final Pattern ID = Pattern.compile("^[0-9]*$");
    private static final Pattern FBA = Pattern.compile("^fba:(\\w*)$");
    public static final List<F.T2<String, String>> DATE_TYPES;
    public static final List<String> projectNames = new ArrayList<>();

    static {
        DATE_TYPES = new ArrayList<>();
        DATE_TYPES.add(new F.T2<>("createDate", "创建时间"));
        DATE_TYPES.add(new F.T2<>("attrs.planDeliveryDate", "预计 [交货] 时间"));
        DATE_TYPES.add(new F.T2<>("attrs.deliveryDate", "实际 [交货] 时间"));
        DATE_TYPES.add(new F.T2<>("attrs.planArrivDate", "预计 [到库] 时间"));
        DATE_TYPES.add(new F.T2<>("attrs.planShipDate", "预计 [发货] 时间"));
    }

    /**
     * 在 ProcureUnits中，planView 和noPlaced 方法 需要调用 index，必须重写，否则总是构造方法中的时间
     */
    public Date from;
    public Date to;

    public long whouseId;
    public long cooperatorId;
    public List<ProcureUnit.STAGE> stages = new ArrayList<>();
    public PLACEDSTATE isPlaced;
    public Shipment.T shipType;
    public String unitIds;
    public InboundUnit.R result;
    public List<String> categories = new ArrayList<>();

    /**
     * 选择过滤的日期类型
     */
    public String dateType;

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

    public enum C {
        YES {
            @Override
            public String label() {
                return "已核单";
            }
        },
        NO {
            @Override
            public String label() {
                return "未核单";
            }

        };

        public abstract String label();
    }

    public String projectName;
    public ProcureUnit.OST isOut;
    public C isConfirm;
    public ProcureUnit.T type;

    /**
     * 屏蔽无效计划
     */
    public Boolean shield;

    public ProcurePost() {
        this.from = DateTime.now().minusDays(25).toDate();
        this.to = new Date();
        this.stages.add(ProcureUnit.STAGE.DONE);
        this.stages.add(ProcureUnit.STAGE.DELIVERY);
        this.stages.add(ProcureUnit.STAGE.IN_STORAGE);
        this.dateType = "createDate";
        this.perSize = 20;
        projectNames.clear();
        projectNames.add(OperatorConfig.getVal("brandname"));
        projectNames.add("B2B");
    }

    public ProcurePost(ProcureUnit.STAGE stage) {
        this();
        this.stages.add(stage);
    }

    public Long getTotalCount() {
        return this.count();
    }

    public List<ProcureUnit> query() {
        F.T2<String, List<Object>> params = params();
        this.count = this.count();
        if(this.pagination)
            return ProcureUnit.find(params._1 + " ORDER BY p.createDate DESC", params._2.toArray())
                    .fetch(this.page, this.perSize);
        else
            return ProcureUnit.find(params._1 + " ORDER BY p.createDate DESC", params._2.toArray()).fetch();

    }

    public List<ProcureUnit> queryForExcel() {
        F.T2<String, List<Object>> params = params();
        String sql = params._1 + " AND (p.type = 'ProcureSplit' OR p.type IS  NULL) ";
        return ProcureUnit.find(sql + " ORDER BY p.createDate DESC", params._2.toArray()).fetch();
    }

    @Override
    public Long count(F.T2<String, List<Object>> params) {
        return (long) ProcureUnit.find(params._1, params._2.toArray()).fetch().size();
    }

    public F.T2<String, List<Object>> params() {
        StringBuilder sbd = new StringBuilder();
        List<Object> params = new ArrayList<>();
        sbd.append("SELECT DISTINCT p FROM ProcureUnit p LEFT JOIN p.fba f LEFT JOIN p.selling s ");
        sbd.append("LEFT JOIN p.deliverplan d LEFT JOIN p.product o WHERE 1=1 ");
        Long procrueId = isSearchForId();
        if(procrueId != null) {
            sbd.append(" AND p.id=?");
            params.add(procrueId);
            return new F.T2<>(sbd.toString(), params);
        }

        if(StringUtils.isNotEmpty(isSearchForFBA())) {
            sbd.append(" AND p.fba.shipmentId=?");
            params.add(this.search);
            return new F.T2<>(sbd.toString(), params);
        }

        if(StringUtils.isBlank(this.dateType)) this.dateType = "attrs.planDeliveryDate";
        sbd.append(" AND p.").append(this.dateType).append(">=?").append(" AND p.").append(this.dateType)
                .append("<=?");
        params.add(Dates.morning(this.from));
        params.add(Dates.night(this.to));

        if(this.whouseId > 0) {
            sbd.append(" AND p.whouse.id=?");
            params.add(this.whouseId);
        }

        if(this.cooperatorId > 0) {
            sbd.append(" AND p.cooperator.id=? ");
            params.add(this.cooperatorId);
        }

        if(stages.size() > 0) {
            sbd.append(" AND p.stage IN ").append(SqlSelect.inlineParam(stages));
        }

        if(this.shipType != null) {
            sbd.append(" AND p.shipType=? ");
            params.add(this.shipType);
        }

        if(this.isConfirm != null) {
            sbd.append(" AND p.isConfirm=? ");
            params.add(this.isConfirm == C.YES);
        }

        if(categories.size() > 0) {
            sbd.append(" AND p.product.category.id IN ").append(SqlSelect.inlineParam(categories));
        }

        if(StringUtils.isNotEmpty(this.projectName)) {
            sbd.append(" AND p.projectName=? ");
            params.add(this.projectName);
        }

        if(result != null) {
            sbd.append(" AND p.result = ? ");
            params.add(this.result);
        }

        if(type != null) {
            sbd.append(" AND p.type = ? ");
            params.add(this.type);
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
            sbd.append(" AND (").append("p.product.sku LIKE ? OR ").append("s.sellingId LIKE ? OR ");
            sbd.append("d.id LIKE ? OR ").append("f.shipmentId LIKE ? ").append(") ");
            for(int i = 0; i < 4; i++) params.add(word);
        }
        if(StringUtils.isNotBlank(this.unitIds)) {
            List<String> unitIdList = Arrays.asList(StringUtils.split(this.unitIds, "_"));
            sbd.append(" AND p.id IN ").append(SqlSelect.inlineParam(unitIdList));
        }

        String username = Login.currentUserName();
        List<String> categoryList = Category.categories(username).stream().map(category -> category.categoryId)
                .collect(Collectors.toList());
        if(categoryList != null && categoryList.size() > 0) {
            sbd.append(" AND p.product.category.categoryId IN ").append(SqlSelect.inlineParam(categoryList));
        } else {
            categoryList = new ArrayList<>();
            categoryList.add("-1");
            sbd.append(" AND p.product.category.categoryId IN ").append(SqlSelect.inlineParam(categoryList));
        }
        if(shield != null && shield) {
            sbd.append(" AND p.attrs.planQty > 0 ");
        }
        return new F.T2<>(sbd.toString(), params);
    }

    /**
     * 根据正则表达式搜索是否有类似 id:123 这样的搜索如果有则直接进行 id 搜索
     *
     * @return
     */
    private Long isSearchForId() {
        if(StringUtils.isNotBlank(this.search)) {
            Matcher matcher = ID.matcher(this.search);
            if(matcher.find()) return NumberUtils.toLong(matcher.group(0));
        }
        return null;
    }

    /**
     * 根据正则表达式搜索是否有类似 id:123 这样的搜索如果有则直接进行 id 搜索
     *
     * @return
     */
    private String isSearchForFBA() {
        if(StringUtils.isNotBlank(this.search) && this.search.substring(0, 3).equals("FBA")) {
            return this.search;
        }
        return null;
    }

    /**
     * 查询出所有采购计划的修改日志(写了修改原因的那些)
     *
     * @return
     */
    public List<HashMap<String, Object>> queryLogs() {
        String sql = "SELECT e.createAt as createAt, e.username as username, fid as fid,"
                + "p.selling_sellingId as sellingId,p.isPlaced as isPlaced,f.shipmentId as fba,e.message as message "
                +
                "FROM ElcukRecord e LEFT JOIN ProcureUnit p ON e.fid = p.id LEFT JOIN FBAShipment f ON p.fba_id = f.id "
                + "WHERE e.action=? AND e.createAt >= ? AND e.createAt <= ? ORDER BY e.createAt DESC";
        List<Map<String, Object>> rows = DBUtils
                .rows(sql, Messages.get("procureunit.deepUpdate"), Dates.morning(this.from), Dates.night(this.to));
        List<HashMap<String, Object>> logs = new ArrayList<>();
        for(Map<String, Object> row : rows) {
            HashMap<String, Object> log = new HashMap<>();
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
        ProcureUnit unit = ProcureUnit.findById(NumberUtils.toLong(id));
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

    public static HighChart perCreateTotalNum() {
        HighChart columnChart = new HighChart(Series.COLUMN);
        DBUtils.rows("SELECT u.username, count(1) as perNum FROM ProcureUnit p "
                + " LEFT JOIN `User` u ON p.handler_id = u.id  GROUP BY p.handler_id ").forEach(row -> {
            Series.Column column = new Series.Column(row.get("username").toString());
            column.add(row.get("username").toString(), Float.parseFloat(row.get("perNum").toString()));
            columnChart.series(column);
        });
        return columnChart;
    }

    public Map<String, String> total(List<ProcureUnit> units) {
        Map<String, String> map = new HashMap<>();
        Integer totalQty = units.stream().mapToInt(ProcureUnit::qty).sum();
        Integer totalPlanQty = units.stream().filter(unit -> Objects.equals(ProcureUnit.STAGE.PLAN, unit.stage))
                .mapToInt(ProcureUnit::qty).sum();
        Integer totalDeliveryQty = units.stream().filter(unit -> Objects.equals(ProcureUnit.STAGE.DELIVERY, unit.stage))
                .mapToInt(ProcureUnit::qty).sum();
        Integer totalDoneQty = units.stream().filter(unit -> Objects.equals(ProcureUnit.STAGE.DONE, unit.stage))
                .mapToInt(ProcureUnit::qty).sum();
        map.put("totalQty", totalQty.toString());
        map.put("totalPlanQty", totalPlanQty.toString());
        map.put("totalDeliveryQty", totalDeliveryQty.toString());
        map.put("totalDoneQty", totalDoneQty.toString());
        Double totalCNY = units.stream().filter(unit -> Objects.equals(helper.Currency.CNY, unit.attrs.currency))
                .mapToDouble(unit -> unit.qty() * unit.attrs.price).sum();
        BigDecimal b = new BigDecimal(totalCNY);
        map.put("totalCNY", b.setScale(2, BigDecimal.ROUND_HALF_UP).toString());
        Double totalUSD = units.stream().filter(unit -> Objects.equals(Currency.USD, unit.attrs.currency))
                .mapToDouble(unit -> unit.qty() * unit.attrs.price).sum();
        b = new BigDecimal(totalUSD);
        map.put("totalUSD", b.setScale(2, BigDecimal.ROUND_HALF_UP).toString());
        F.T2<String, List<Object>> params = this.params();
        params._2.add(Dates.morning(new Date()));
        params._2.add(Dates.night(new Date()));
        List<ProcureUnit> details = ProcureUnit.find(params._1 + " AND p.createDate>=? AND p.createDate<=?",
                params._2.toArray()).fetch();
        Integer planQty = details.stream().filter(detail -> detail.parent == null)
                .filter(detail -> Objects.equals(ProcureUnit.STAGE.PLAN, detail.stage))
                .mapToInt(ProcureUnit::qty).sum();
        Integer deliveryQty = details.stream().filter(detail -> detail.parent == null)
                .filter(detail -> Objects.equals(ProcureUnit.STAGE.DELIVERY, detail.stage))
                .mapToInt(ProcureUnit::qty).sum();
        Integer doneQty = details.stream().filter(detail -> detail.parent == null)
                .filter(detail -> Objects.equals(ProcureUnit.STAGE.DONE, detail.stage))
                .mapToInt(ProcureUnit::qty).sum();
        map.put("planQty", planQty.toString());
        map.put("deliveryQty", deliveryQty.toString());
        map.put("doneQty", doneQty.toString());
        return map;
    }

}
