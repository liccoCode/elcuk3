package models;

import com.google.gson.annotations.Expose;
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
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

/**
 * 销售目标
 * <p/>
 * Created by IntelliJ IDEA.
 * User: mac
 * Date: 14-3-11
 * Time: PM5:07
 * @deprecated 已经无人使用
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
     * 例如 SaleTarget 类型为 TEAM ，那么 fid 则保存的是 TEAM 的主键 ID
     */
    @Expose
    public String fid;

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

    public SaleTarget() {
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
        Validation.required("利润率", this.profitMargin);
    }

    public void validateChild(List<SaleTarget> sts) {
        for(SaleTarget st : sts) {
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
    public List<SaleTarget> loadCategorySaleTargets(List<Category> categorys) {
        List<SaleTarget> saleTargets = new ArrayList<SaleTarget>();
        for(Category category : categorys) {
            SaleTarget categorySt = new SaleTarget();
            categorySt.fid = category.categoryId;
            categorySt.targetYear = this.targetYear;
            categorySt.saleTargetType = T.CATEGORY;
            categorySt.createDate = new Date();
            saleTargets.add(categorySt);
        }
        return saleTargets;
    }

    /**
     * 准备 Category 的子月度销售目标
     *
     * @return
     */
    public List<SaleTarget> loadMonthSaleTargets(User user) {
        List<SaleTarget> saleTargets = new ArrayList<SaleTarget>();
        for(int i = 1; i <= 12; i++) {
            SaleTarget monthSt = new SaleTarget();
            monthSt.targetYear = this.targetYear;
            monthSt.fid = this.fid;
            monthSt.saleTargetType = T.MONTH;
            monthSt.targetMonth = i;
            monthSt.createDate = new Date();
            monthSt.createuser = user;
            monthSt.save();
            saleTargets.add(monthSt);
        }
        return saleTargets;
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
                return SaleTarget.find("targetYear=? AND saleTargetType=?", this.targetYear,
                        this.saleTargetType).fetch().size() != 0;
            case CATEGORY:
                return SaleTarget.find("targetYear=? AND targetMonth=? AND saleTargetType=?", this.targetYear,
                        this.targetMonth, this.saleTargetType).fetch().size() != 0;
            default:
                return true;
        }
    }

    public void update(SaleTarget newSt, Long id) {
        this.beforeUpdateLog(newSt, id);
        this.saleAmounts = newSt.saleAmounts;
        this.profitMargin = newSt.profitMargin;
        this.validate();
        if(Validation.hasErrors()) return;
        this.save();
    }

    /**
     * 获取真实销量数据
     *
     * @return
     */
    public float reallySaleAmounts() {
        MetricProfitService me = new MetricProfitService(Dates.getMonthFirst(this.targetYear, this.targetMonth),
                Dates.getMonthLast(this.targetYear, this.targetMonth), this.fid);
        me.esAmazonFee();
        return me.esSaleFee();
    }

    /**
     * 修改时的日志 指定保存的日志的外键
     *
     * @param newSt
     * @param parentId
     */
    public void beforeUpdateLog(SaleTarget newSt, Long parentId) {
        List<String> logs = new ArrayList<String>();
        logs.addAll(Reflects.updateAndLogChanges(this, "saleAmounts", newSt.saleAmounts));
        logs.addAll(Reflects.updateAndLogChanges(this, "profitMargin", newSt.profitMargin));
        if(logs.size() > 0) {
            Long fid = parentId == null ? this.id : parentId;
            String[] logMsg = this.to_log();
            new ERecordBuilder("saletarget.update", "saletarget.update.msg").msgArgs(logMsg[0], logMsg[1],
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
                logMsg[0] = this.fid;
                logMsg[1] = " 产品线";
                break;
            case MONTH:
                logMsg[0] = this.targetMonth + "";
                logMsg[1] = "月份";
                break;

        }
        return logMsg;
    }
}
