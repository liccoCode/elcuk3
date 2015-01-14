package models;

import com.google.gson.annotations.Expose;
import helper.DBUtils;
import helper.Dates;
import helper.Reflects;
import models.embedded.ERecordBuilder;
import models.product.Category;
import models.product.Team;
import org.apache.commons.lang.StringUtils;
import org.joda.time.DateTime;
import play.data.validation.Validation;
import play.db.jpa.Model;
import services.MetricProfitService;

import javax.persistence.*;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Map;

import models.market.M;
import helper.Currency;

/**
 * 运营销售目标
 * <p/>
 * Created by IntelliJ IDEA.
 * User: mac
 * Date: 14-3-11
 * Time: PM5:07
 */
@Entity
public class SaleOpTarget extends Model {
    /**
     * 销售目标年份
     */
    @Expose
    public Integer targetYear;


    /**
     * 销售目标市场
     */
    @Expose
    public M targetMarket;

    /**
     * 销售目标月份
     */
    @Expose
    public Integer targetMonth;

    /**
     * 销售目标季度
     */
    @Expose
    public Integer targetSeason;


    /**
     * 主题
     */
    @Lob
    @Expose
    public String name = " ";

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
         * 市场销售目标
         */
        MARKET,

        /**
         * 季度销售目标
         */
        SEASON,

        /**
         * 产品线销售目标
         */
        CATEGORY
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
    public String memo = " ";

    /**
     * 这个销售目标的外键Id，标示谁需要完成这个销售目标
     * 例如 SaleOpTarget 类型为 TEAM ，那么 fid 则保存的是 TEAM 的主键 ID
     */
    @Expose
    public String fid;

    /**
     * 销售金额
     */
    @Expose
    public double saleAmounts = 0d;


    /**
     * 销售金额人民币
     */
    @Transient
    public double saleAmountRmbs = 0d;


    /**
     * 日销售数量
     */
    @Expose
    public Integer saleQty = 0;


    /**
     * 日销售数量修正值
     */
    @Expose
    public Integer saleQtyLast = 0;

    /**
     * 销售金额修正值
     */
    @Expose
    public double saleAmountsLast = 0d;

    /**
     * 销售金额人民币
     */
    @Transient
    public double saleAmountsLastRmb = 0d;


    public SaleOpTarget() {
        DateTime dateTime = new DateTime().now();
        this.saleTargetType = T.YEAR;
        this.createDate = dateTime.toDate();
        this.targetYear = dateTime.getYear();
    }

    /**
     * 数据校验
     */
    public void validate() {
        Validation.required("年份", this.targetYear);
        Validation.required("销售额", this.saleAmounts);
        Validation.required("日均销量", this.saleQty);
    }

    public void validateChild(List<SaleOpTarget> sts) {
        for(SaleOpTarget st : sts) {
            st.createuser = this.createuser;
            st.targetYear = this.targetYear;
            st.validate();
        }
    }

    /**
     * 准备年度目标的子 category 销售目标对象
     *
     * @param categorys
     * @return
     */
    public List<SaleOpTarget> loadCategorySaleTargets(List<Category> categorys) {
        List<SaleOpTarget> saleOpTargets = new ArrayList<SaleOpTarget>();
        for(Category category : categorys) {
            SaleOpTarget categorySt = new SaleOpTarget();
            categorySt.fid = category.categoryId;
            categorySt.targetYear = this.targetYear;
            categorySt.saleTargetType = T.CATEGORY;
            categorySt.createDate = new Date();
            saleOpTargets.add(categorySt);
        }
        return saleOpTargets;
    }

    /**
     * 准备 Category 的子月度销售目标
     *
     * @return
     */
    public List<SaleOpTarget> loadMonthSaleTargets(User user) {
        List<SaleOpTarget> saleOpTargets = new ArrayList<SaleOpTarget>();
        for(int i = 1; i <= 12; i++) {
            SaleOpTarget monthSt = new SaleOpTarget();
            monthSt.targetYear = this.targetYear;
            monthSt.fid = this.fid;
            monthSt.saleTargetType = T.MONTH;
            if(this.targetMarket != null) monthSt.targetMarket = this.targetMarket;
            monthSt.targetMonth = i;
            monthSt.createDate = new Date();
            monthSt.createuser = user;
            monthSt.save();
            saleOpTargets.add(monthSt);
        }
        return saleOpTargets;
    }


    /**
     * 准备 Category 的市场销售目标
     *
     * @return
     */
    public List<SaleOpTarget> loadMarketSaleTargets(User user) {
        List<SaleOpTarget> saleOpTargets = new ArrayList<SaleOpTarget>();
        for(M m : M.values()) {
            SaleOpTarget monthSt = new SaleOpTarget();
            monthSt.targetYear = this.targetYear;
            monthSt.fid = this.fid;
            monthSt.saleTargetType = T.MARKET;
            monthSt.targetMarket = m;
            monthSt.createDate = new Date();
            monthSt.createuser = user;
            monthSt.save();
            saleOpTargets.add(monthSt);
        }
        return saleOpTargets;
    }


    /**
     * 准备 Category 的市场销售目标
     *
     * @return
     */
    public List<SaleOpTarget> loadSeasonSaleTargets(User user) {
        List<SaleOpTarget> saleOpTargets = new ArrayList<SaleOpTarget>();
        for(int i = 1; i <= 4; i++) {
            SaleOpTarget monthSt = new SaleOpTarget();
            monthSt.targetYear = this.targetYear;
            monthSt.fid = this.fid;
            monthSt.saleTargetType = T.SEASON;
            if(this.targetMarket != null) monthSt.targetMarket = this.targetMarket;
            monthSt.targetSeason = i;
            monthSt.createDate = new Date();
            monthSt.createuser = user;
            monthSt.save();
            saleOpTargets.add(monthSt);
        }
        return saleOpTargets;
    }


    /**
     * 通过 fid 返回所属 Team
     *
     * @return
     */
    public Team fetchTeam() {
        Category category = Category.findById(this.fid);
        return category.team;
    }

    /**
     * 唯一性验证
     *
     * @return
     */
    public boolean isExist() {
        switch(this.saleTargetType) {
            case YEAR:
                return SaleOpTarget.find("targetYear=? AND saleTargetType=?", this.targetYear,
                        this.saleTargetType).fetch().size() != 0;
            case CATEGORY:
                return SaleOpTarget.find("targetYear=? AND targetMonth=? AND saleTargetType=?", this.targetYear,
                        this.targetMonth, this.saleTargetType).fetch().size() != 0;
            default:
                return true;
        }
    }

    public void update(SaleOpTarget newSt, Long id) {
        if(newSt.saleQty == null) newSt.saleQty = 0;
        this.beforeUpdateLog(newSt, id);
        this.saleAmounts = new BigDecimal(newSt.saleAmounts).setScale(2, BigDecimal.ROUND_HALF_UP).doubleValue();
        this.saleQty = newSt.saleQty;

        if(this.saleTargetType == T.MONTH || this.saleTargetType == T.SEASON || this.saleTargetType == T.MARKET) {
            this.saleAmountsLast = newSt.saleAmountsLast;
            this.saleQtyLast = newSt.saleQtyLast;
        } else {
            if(this.saleAmountsLast == 0) this.saleAmountsLast = newSt.saleAmounts;
            if(this.saleQtyLast == 0) this.saleQtyLast = newSt.saleQty;
        }
        if(newSt.targetMarket != null) this.targetMarket = newSt.targetMarket;
        this.validate();
        if(Validation.hasErrors()) return;
        this.save();
    }


    public void updateCategory() {
        SaleOpTarget target = SaleOpTarget.find("fid=? AND targetYear=? AND saleTargetType=? ",
                this.fid, this.targetYear, T.CATEGORY).first();
        List<SaleOpTarget> ts = SaleOpTarget
                .find("fid=? AND targetYear=? AND saleTargetType=? ",
                        this.fid,
                        this.targetYear,
                        T.MARKET).fetch();
        updateTarget(target, ts);
    }

    public void updateYear() {
        SaleOpTarget target = SaleOpTarget.find("targetYear=? AND saleTargetType=? ",
                this.targetYear, T.YEAR).first();
        List<SaleOpTarget> ts = SaleOpTarget
                .find("targetYear=? AND saleTargetType=? ",
                        this.targetYear,
                        T.CATEGORY).fetch();
        updateTarget(target, ts);
    }

    private void updateTarget(SaleOpTarget target, List<SaleOpTarget> ts) {
        int sumqty = 0;
        double sumamount = 0d;
        for(SaleOpTarget t : ts) {
            sumqty = sumqty + t.saleQtyLast;
            sumamount = sumamount + t.saleAmountsLast;
        }
        if(sumqty != 0) {
                target.saleQtyLast = sumqty;
        }
        if(sumamount != 0) target.saleAmountsLast = sumamount;

        target.save();
    }


    /**
     * 获取真实销量数据
     *
     * @return
     */
    public String reallySaleAmounts() {
        DateTime now = DateTime.now();
        int nowyear = now.getYear();
        int nowmonth = now.getMonthOfYear();
        int startmonth = 1, endmonth = 12;
        boolean isafter = false;
        switch(this.saleTargetType) {
            case MONTH:
                startmonth = this.targetMonth;
                endmonth = this.targetMonth;
                if(this.targetMonth >= nowmonth) isafter = true;
                break;
            case SEASON:
                startmonth = this.targetSeason * 3 - 2;
                endmonth = this.targetSeason * 3;
                if(endmonth >= nowmonth) isafter = true;
                break;
        }

        //如果此月份还没有到则为0
        if(this.targetYear == nowyear && isafter) return "";

        MetricProfitService me = new MetricProfitService(Dates.getMonthFirst(this.targetYear, startmonth),
                Dates.getMonthLast(this.targetYear, endmonth), this.targetMarket, "", "", this.fid);
        return String.valueOf(me.esSaleFee());
    }


    /**
     * 获取真实销售数量数据
     *
     * @return
     */
    public String reallySaleQtys() {
        DateTime now = DateTime.now();
        int nowyear = now.getYear();
        int nowmonth = now.getMonthOfYear();

        int startmonth = 1, endmonth = 12;
        boolean isafter = false;
        switch(this.saleTargetType) {
            case MONTH:
                startmonth = this.targetMonth;
                endmonth = this.targetMonth;
                if(this.targetMonth >= nowmonth) isafter = true;
                break;
            case SEASON:
                startmonth = this.targetSeason * 3 - 2;
                endmonth = this.targetSeason * 3;
                if(endmonth >= nowmonth) isafter = true;
                break;
        }

        //如果此月份还没有到则为0
        if(this.targetYear == nowyear && isafter) return "";

        MetricProfitService me = new MetricProfitService(Dates.getMonthFirst(this.targetYear, startmonth),
                Dates.getMonthLast(this.targetYear, endmonth),
                this.targetMarket, "", "", this.fid);
        return String.valueOf(me.esSaleQty() / 30);
    }


    /**
     * 修改时的日志 指定保存的日志的外键
     *
     * @param newSt
     * @param parentId
     */
    public void beforeUpdateLog(SaleOpTarget newSt, Long parentId) {
        List<String> logs = new ArrayList<String>();
        logs.addAll(Reflects.updateAndLogChanges(this, "saleAmounts", newSt.saleAmounts));
        logs.addAll(Reflects.updateAndLogChanges(this, "saleQty", newSt.saleQty));
        if(logs.size() > 0) {
            Long fid = parentId == null ? this.id : parentId;
            String[] logMsg = this.to_log();
            new ERecordBuilder("saleoptarget.update", "saletarget.update.msg").msgArgs(logMsg[0], logMsg[1],
                    StringUtils.join(logs, "，")).fid(fid).save();
        }
    }

    public String[] to_log() {
        String[] logMsg = new String[2];
        switch(this.saleTargetType) {
            case YEAR:
                logMsg[0] = this.targetYear + "";
                logMsg[1] = "年度";
                break;
            case CATEGORY:
                logMsg[0] = this.targetYear + " " + this.fid;
                logMsg[1] = "产品线";
                break;
            case MARKET:
                logMsg[0] = this.targetYear + " " + this.fid + " " + this.targetMarket;
                logMsg[1] = "市场";
                break;
            case SEASON:
                logMsg[0] = this.targetYear + " " + this.fid + " " + this.targetMarket + " " + this.targetSeason + "";
                logMsg[1] = "季度";
                break;
            case MONTH:
                logMsg[0] = this.targetYear + " " + this.fid + " " + this.targetMarket + " " + this.targetMonth + "";
                logMsg[1] = "月份";
                break;
        }
        return logMsg;
    }

    public void usdToRmb() {
        this.saleAmountRmbs = new BigDecimal(Currency.USD.toCNY(Double.valueOf(this.saleAmounts).floatValue()))
                .setScale(2,
                        BigDecimal.ROUND_HALF_UP).doubleValue();
        this.saleAmountsLastRmb = new BigDecimal(Currency.USD.toCNY(Double.valueOf(this.saleAmountsLast).floatValue()))
                .setScale(2,
                        BigDecimal.ROUND_HALF_UP).doubleValue();
    }

    public static List<SaleOpTarget> salesToRbm(List<SaleOpTarget> sts) {
        for(int i = 0; i < sts.size(); i++) {
            SaleOpTarget target = sts.get(i);
            target.usdToRmb();
            sts.set(i, target);
        }
        return sts;
    }


}