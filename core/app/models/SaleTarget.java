package models;

import com.google.gson.annotations.Expose;
import helper.Dates;
import models.product.Category;
import models.product.Team;
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
        this.createDate = dateTime.toDate();
        this.targetYear = dateTime.getYear();
    }

    public SaleTarget(Category category) {
        DateTime dateTime = new DateTime().now();
        this.fid = category.categoryId;
        this.saleTargetType = T.CATEGORY;
        this.targetYear = dateTime.getYear();
        this.targetMonth = dateTime.getMonthOfYear();
    }

    public SaleTarget(SaleTarget categorySt) {
        this.memo = categorySt.memo;
        this.name = categorySt.name;
        this.targetYear = categorySt.targetYear;
        this.fid = categorySt.fid;
        this.saleTargetType = T.MONTH;
    }

    public String to_log() {
        SaleTarget old = SaleTarget.findById(this.id);
        switch(this.saleTargetType) {
            case YEAR:
                return String.format("%s 年度目标 销售额从 %s 万变更为 %s 万，利润率从 %s 变更为 %s", this.targetYear, old.saleAmounts,
                        this.saleAmounts, old.profitMargin, this.profitMargin);
            case CATEGORY:
                return String.format("%s 产品线销售额从 %s 万变更为 %s 万，利润率从 %s 变更为 %s",
                        this.fid, old.saleAmounts, this.saleAmounts, this.profitMargin, old.profitMargin);
            case MONTH:
                return String.format("%s 产品线 %s 月份销售额从 %s 万变更为 %s 万，利润率从 %s 变更为 %s", this.fid, this.targetMonth,
                        old.saleAmounts, this.saleAmounts, old.profitMargin, this.profitMargin);
            default:
                return "";
        }
    }

    /**
     * 数据校验
     */
    public void validate() {
        Validation.required("年份", this.targetYear);
        Validation.required("名称", this.name);
        Validation.required("销售额", this.saleAmounts);
        Validation.required("利润率", this.profitMargin);
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
            SaleTarget categorySt = new SaleTarget(category);
            saleTargets.add(categorySt);
        }
        return saleTargets;
    }

    /**
     * 准备 Category 的子月度销售目标
     *
     * @return
     */
    public List<SaleTarget> loadMonthSaleTargets() {
        List<SaleTarget> saleTargets = SaleTarget
                .find("fid=? AND targetYear=? AND saleTargetType=?", this.fid, this.targetYear, T.MONTH).fetch();
        if(saleTargets == null || saleTargets.size() == 0) {
            for(int i = 1; i <= 12; i++) {
                SaleTarget monthSt = new SaleTarget(this);
                monthSt.targetMonth = i;
                monthSt.save();
                saleTargets.add(monthSt);
            }
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


    /**
     * copy 出主题、备注 属性赋值给新销售目标对象
     */
    public List<SaleTarget> copySaleTarget(List<SaleTarget> sts) {
        for(SaleTarget st : sts) {
            st.memo = this.memo;
            st.name = this.name;
            st.targetYear = this.targetYear;
            st.createuser = this.createuser;
            st.saleTargetType = T.CATEGORY;
            st.createDate = new Date();
            st.validate();
        }
        return sts;
    }

    /**
     * 将数据更新到数据库内
     * 采用这种方式是由于直接保存会存在一个Hibernate 的 detached entity passed to persist问题
     */
    public void updateOld() {
        SaleTarget old = SaleTarget.findById(this.id);
        old.profitMargin = this.profitMargin;
        old.saleAmounts = this.saleAmounts;
        old.save();
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
}
