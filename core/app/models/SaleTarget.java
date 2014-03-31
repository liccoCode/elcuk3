package models;

import com.google.gson.annotations.Expose;
import helper.Cached;
import helper.Caches;
import helper.DBUtils;
import models.product.Category;
import models.product.Product;
import models.product.Team;
import models.view.highchart.HighChart;
import models.view.highchart.Series;
import models.view.report.Profit;
import org.apache.commons.lang.math.NumberUtils;
import org.joda.time.DateTime;
import play.cache.*;
import play.cache.Cache;
import play.data.validation.Validation;
import play.db.jpa.Model;
import services.MetricProfitService;

import javax.persistence.*;
import java.util.*;

/**
 * 销售目标
 * <p/>
 * Created by IntelliJ IDEA.
 * User: mac
 * Date: 14-3-11
 * Time: PM5:07
 */
@Entity
public class SaleTarget extends Model {
    /**
     * 销售目标年份
     */
    @Expose
    public Integer targetYear;

    /**
     * 销售目标月份
     */
    @Expose
    public Integer targetMonth;

    /**
     * 主题
     */
    @Lob
    @Expose
    public String theme;

    /**
     * 创建人,一个销售目标只能被一个人创建
     */
    @OneToOne
    @Expose
    public User createuser;

    /**
     * 创建日期
     */
    @Temporal(TemporalType.DATE)
    @Expose
    public Date createDate = new Date();

    public enum S {
        /**
         * 已审核
         */
        AUDITED {
            @Override
            public String label() {
                return "已审核";
            }
        },

        /**
         * 未审核
         */
        UNAUDITED {
            @Override
            public String label() {
                return "未审核";
            }
        };

        public abstract String label();
    }

    /**
     * 销售目标状态
     */
    @Expose
    @Enumerated(EnumType.STRING)
    public S state = S.UNAUDITED;

    public enum T {
        /**
         * 年度销售目标
         */
        YEAR,

        /**
         * 月度销售目标
         */
        MONTH,

        /**
         * TEAM销售目标
         */
        TEAM,

        /**
         * 产品线销售目标
         */
        CATEGORY,

        /**
         * SKU销售目标
         */
        SKU
    }

    /**
     * 销售目标类型
     */
    @Column(nullable = false)
    @Expose
    @Enumerated(EnumType.STRING)
    public T saleTargetType;

    /**
     * 备注
     */
    @Expose
    @Lob
    public String memo;

    /**
     * 这个销售目标的外键Id，标示谁需要完成这个销售目标
     * 例如 SaleTarget 类型为 TEAM ，那么 fid 则保存的是 TEAM 的主键 ID
     */
    @Expose
    public String fid;

    /**
     * 父销售目标的 id
     */
    public Long parentId;

    /**
     * 销售金额
     */
    @Expose
    public Float saleAmounts = 0f;

    /**
     * 利润率
     */
    @Expose
    public Float profitMargin = 0f;

    /**
     * SKU PS
     */
    @Expose
    public Float ps = 0f;

    /**
     * 当月天数
     */
    @Expose
    public Integer days;

    /**
     * 期望售价
     */
    @Expose
    public Float expectPrice = 0f;

    /**
     * 期望采购价
     */
    @Expose
    public Float expectDeliveryPrice = 0f;

    /**
     * SKU运费单价
     */
    @Expose
    public Float shipmentUnit = 0f;

    /**
     * 关税和VAT单价
     */
    @Expose
    public Float tariffAndVAT = 0f;

    /**
     * 预计亚马逊费用
     */
    @Expose
    public Float expectAmazonPrice = 0f;

    /**
     * 预计FBA费用
     */
    @Expose
    public Float expectFBAPrice = 0f;

    public SaleTarget() {
    }

    public SaleTarget(String fid) {
        this.fid = fid;
    }

    /**
     * 数据校验
     */
    public void validate() {
        Validation.required("年份", this.targetYear);
        Validation.required("主题", this.theme);
        if(!this.saleTargetType.equals(T.YEAR) && !this.saleTargetType.equals(T.TEAM)) {
            Validation.required("月份", this.targetMonth);
        }
    }

    /**
     * 返回销售目标 所属对象 的名称用于展示
     * 例: 属于 TeamA 则返回 Team 的名称
     *
     * @return
     */
    public String fidToName() {
        switch(this.saleTargetType) {
            case YEAR:
                return "";
            case TEAM:
                Team team = Team.findById(NumberUtils.toLong(this.fid));
                return team.name;
            case MONTH:
                return "";
            case CATEGORY:
                Category category = Category.findById(this.fid);
                return category.name;
            case SKU:
                Product product = Product.findById(this.fid);
                return product.sku;
            default:
                return "";
        }
    }

    /**
     * 获取此销售目标所有的子销售目标
     *
     * @return List<SaleTarget>
     */
    public List<SaleTarget> getChild() {
        return SaleTarget.find("parentId=?", this.id).fetch();
    }

    /**
     * 准备该销售目标的明细数据
     *
     * @return List<SaleTarget>
     */
    public List<SaleTarget> beforeDetails() {
        List<SaleTarget> saleTargetList = new ArrayList<SaleTarget>();
        switch(this.saleTargetType) {
            case YEAR:
                List<Team> teams = Team.findAll();
                for(Team t : teams) {
                    SaleTarget sa = new SaleTarget(t.id.toString());
                    sa.parentId = this.id;
                    sa.saleTargetType = this.getChlidType();
                    saleTargetList.add(sa);
                }
                break;

            case TEAM:
                //一年十二个月，每个TEAM 每个月有一个销售目标
                for(int temp = 1; temp <= 12; temp++) {
                    SaleTarget sa = new SaleTarget(this.fid);
                    sa.parentId = this.id;
                    sa.targetMonth = temp;
                    sa.saleTargetType = this.getChlidType();
                    saleTargetList.add(sa);
                }
                break;

            case MONTH:
                List<Category> categorys = Category.find("team_id=?", this.fid).fetch();
                for(Category t : categorys) {
                    SaleTarget sa = new SaleTarget(t.categoryId);
                    sa.parentId = this.id;
                    sa.saleTargetType = this.getChlidType();
                    saleTargetList.add(sa);
                }
                break;

            case CATEGORY:
                List<Product> products = Product.find("category_categoryId=?", this.fid).fetch();
                for(Product pro : products) {
                    SaleTarget sa = new SaleTarget(pro.sku);
                    sa.parentId = this.id;
                    sa.saleTargetType = this.getChlidType();
                    saleTargetList.add(sa);
                }
                break;

            case SKU:
                //SKU 类型的销售目标已经是最小的销售目标
                saleTargetList = null;
        }
        return saleTargetList;
    }

    /**
     * 根据 父对象 获取 直接子对象 的 saleTargetType
     *
     * @return
     */
    public T getChlidType() {
        switch(this.saleTargetType) {
            case YEAR:
                return T.TEAM;
            case TEAM:
                return T.MONTH;
            case MONTH:
                return T.CATEGORY;
            case CATEGORY:
                return T.SKU;
            case SKU:
                //SKU 类型的 销售目标 是不允许再拥有 子对象的
                return null;
            default:
                return null;
        }
    }

    /**
     * 从父对象 copy 出 主题、备注 属性赋值给子对象
     *
     * @param child      子销售对象
     * @param createUser 创建人
     * @return SaleTarget
     */
    public SaleTarget copySaleTarget(SaleTarget child, User createUser) {
        child.memo = this.memo;
        child.theme = this.theme;
        child.saleTargetType = this.getChlidType();
        child.createuser = createuser;
        child.targetYear = this.targetYear;
        if(child.targetMonth == null) child.targetMonth = this.targetMonth;
        return child;
    }

    /**
     * 唯一性验证
     *
     * @return
     */
    public boolean isNotExist() {
        switch(this.saleTargetType) {
            case YEAR:
                return SaleTarget.find("targetYear=? AND saleTargetType=?", this.targetYear,
                        this.saleTargetType).fetch().size() == 0;
            case TEAM:
                return SaleTarget.find("targetYear=? AND saleTargetType=?", this.targetYear,
                        this.saleTargetType).fetch().size() == 0;
            case CATEGORY:
                return SaleTarget.find("targetYear=? AND targetMonth=? AND saleTargetType=?", this.targetYear,
                        this.targetMonth, this.saleTargetType).fetch().size() == 0;
            case SKU:
                return SaleTarget.find("targetYear=? AND targetMonth=? AND saleTargetType=?", this.targetYear,
                        this.targetMonth, this.saleTargetType).fetch().size() == 0;
            default:
                return false;
        }
    }

    /**
     * 保存或者更新子销售目标
     *
     * @param childs
     * @param user
     */
    public void saveOrUpdateChild(List<SaleTarget> childs, User user) {
        for(SaleTarget child : childs) {
            this.copySaleTarget(child, user);
            child.validate();
            if(Validation.hasErrors()) {
                break;
            }
            if(child.id == null) {
                child.save();
            } else {
                child.updateOld((SaleTarget) SaleTarget.findById(child.id));
            }
        }
    }

    /**
     * 将数据更新到数据库内
     * 采用这种方式是由于直接保存会存在一个Hibernate 的 detached entity passed to persist问题
     */
    public void updateOld(SaleTarget old) {
        old.targetYear = this.targetYear;
        old.profitMargin = this.profitMargin;
        old.saleAmounts = this.saleAmounts;
        old.targetMonth = this.targetMonth;
        old.save();
    }

    public String getFidName() {
        if(saleTargetType == T.TEAM) {
            Team team = Team.findById(new Long(this.fid));
            return team.name;
        }
        if(saleTargetType == T.MONTH) {
            Team team = Team.findById(new Long(this.fid));
            return team.name + targetMonth + "月";
        }
        return "";
    }

    /**
     * 返回 Category 全年每个月的销售额和销售额目标
     * 和已经完成的销售额目标, 并组装成 HightChart 使用的格式返回
     *
     * @return
     */
    @Cached("2h")
    public static HighChart ajaxHighChartCategorySalesAmount(String categoryId, int year) {
        String cacked_key = String.format("%s_%s_categoryinfo_salesamount", year, categoryId);
        HighChart columnChart = play.cache.Cache.get(cacked_key, HighChart.class);
        if(columnChart != null) return columnChart;
        synchronized(cacked_key.intern()) {
            columnChart = new HighChart(Series.COLUMN);
            columnChart.title = String.format("%s年度%s产品线销售额", year, categoryId);
            //已完成的柱状图
            columnChart.series(salesAmountColom(categoryId, year));
            //目标柱状图
            columnChart.series(salesAmountTargetColom(categoryId, year));
            Cache.add(cacked_key, columnChart, "2h");
        }
        return columnChart;
    }

    /**
     * 销售额目标的柱状图
     *
     * @return
     */
    public static Series.Column salesAmountTargetColom(String categoryId, int year) {
        DateTime now = new DateTime().now().withYear(year);
        List<SaleTarget> saleTargetList = SaleTarget.find("fid=? AND targetYear=? AND saleTargetType=?", categoryId,
                now.getYear(), T.CATEGORY).fetch();

        Series.Column column = new Series.Column("月度销售额目标");
        column.color = "#0000ff";
        for(int i = 0; i < saleTargetList.size(); i++) {
            float target = saleTargetList.get(i).saleAmounts;
            column.add(target, i + 1 + "月");
        }
        return column;
    }

    /**
     * 已经完成的销售额的柱状图
     *
     * @return
     */
    public static Series.Column salesAmountColom(String categoryId, int year) {
        DateTime now = new DateTime().now().withYear(year);
        Series.Column column = new Series.Column("月度销售额");
        column.color = "#FFA500";
        float totalsale = 0f;
        for(int i = 1; i <= 12; i++) {
            DateTime month = now.withMonthOfYear(i);
            //获得每个月份的完整的开始日期
            DateTime begin = month.withDayOfMonth(1);
            //获得每个月份的完整的结束日期
            DateTime end = getMonthEndDate(month);

            MetricProfitService service = new MetricProfitService(begin.toDate(), end.toDate(), null,
                    null, null, categoryId);
            totalsale = service.esSaleFee();
            column.add(totalsale, i + "月");
        }
        return column;
    }

    /**
     * 返回 Category 全年每个月的利润率目标和利润率
     * 和已经完成的销售额目标, 并组装成 HightChart 使用的格式返回
     *
     * @return
     */
    @Cached("2h")
    public static HighChart ajaxHighChartCategorySalesProfit(String categoryId, int year) {
        String cacked_key = String.format("%s_%s_categoryinfo_salesprofit", year, categoryId);
        HighChart lineChart = play.cache.Cache.get(cacked_key, HighChart.class);
        if(lineChart != null) return lineChart;
        synchronized(cacked_key.intern()) {
            lineChart = new HighChart(Series.LINE);
            lineChart.title = String.format("%s年度%s产品线利润率", year, categoryId);
            //已完成曲线图
            lineChart.series(salesProfitLine(categoryId, year));
            //目标曲线图
            lineChart.series(salesProfitTargetLine(categoryId, year));
            Cache.add(cacked_key, lineChart, "2h");
        }
        return lineChart;
    }

    /**
     * 利润率
     *
     * @param categoryId
     * @return
     */
    public static Series.Line salesProfitLine(String categoryId, int year) {
        DateTime now = new DateTime().now().withYear(year);
        Series.Line line = new Series.Line("月度利润率");
        line.color = "#FFA500";

        String sql = "select sku From Product "
                + " where category_categoryid='" + categoryId + "' ";
        List<Map<String, Object>> rows = DBUtils.rows(sql);

        float totalsaleprofit = 0f;
        float totalsalefee = 0f;
        for(int i = 1; i <= 12; i++) {
            DateTime month = now.withMonthOfYear(i);
            //获得每个月份的完整的开始日期
            DateTime begin = month.withDayOfMonth(1);
            //获得每个月份的完整的结束日期
            DateTime end = getMonthEndDate(month);

            if(rows != null && rows.size() > 0) {
                for(Map<String, Object> product : rows) {
                    String sku = (String) product.get("sku");
                    MetricProfitService profitservice = new MetricProfitService(begin.toDate(), end.toDate(), null,
                            sku, null);
                    Profit profit = profitservice.calProfit();
                    totalsaleprofit = totalsaleprofit + profit.totalprofit;
                    totalsalefee = totalsalefee + profit.totalfee;
                }
            }
            MetricProfitService service = new MetricProfitService(begin.toDate(), end.toDate(), null,
                    null, null, categoryId);
            float rate = 0;
            if(totalsalefee != 0) {
                rate = totalsaleprofit / totalsalefee * 100;
            }
            line.add(rate, i + "月");
        }
        return line;
    }

    /**
     * 利润率目标
     *
     * @param categoryId
     * @return
     */
    public static Series.Line salesProfitTargetLine(String categoryId, int year) {
        DateTime now = new DateTime().now().withYear(year);
        Series.Line line = new Series.Line("月度利润率目标");
        List<SaleTarget> saleTargetList = SaleTarget.find("fid=? AND targetYear=? AND saleTargetType=?", categoryId,
                now.getYear(), SaleTarget.T.CATEGORY).fetch();

        line.color = "#0000ff";
        for(int i = 0; i < saleTargetList.size(); i++) {

            float target = saleTargetList.get(i).profitMargin;

            line.add(target, i + 1 + "月");
        }
        return line;
    }

    /**
     * 获取月份的最后一天
     *
     * @param month
     * @return
     */
    public static DateTime getMonthEndDate(DateTime month) {
        Calendar calendar = Calendar.getInstance();
        calendar.setTime(month.toDate());
        int maxDayOfMonth = calendar.getActualMaximum(Calendar.DAY_OF_MONTH);
        return month.withDayOfMonth(maxDayOfMonth);
    }
}
