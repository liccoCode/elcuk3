package models;

import com.google.gson.annotations.Expose;
import models.product.Category;
import models.product.Product;
import models.product.Team;
import org.apache.commons.lang.math.NumberUtils;
import play.data.validation.Required;
import play.db.jpa.Model;

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
    @Required
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
    @Required
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

    public SaleTarget() {
    }

    public SaleTarget(String fid) {
        this.fid = fid;
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
            case CATEGORY:
                Category category = Category.findById(this.fid);
                return category.name;
            case SKU:
                Product product = Product.findById(this.fid);
                return product.productName;
        }
        return "";
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
                //所有的应该包含的 TEAM类型 的子销售目标
                List<Team> teams = Team.findAll();
                for(Team t : teams) {
                    SaleTarget sa = new SaleTarget(t.id.toString());
                    sa.parentId = this.id;
                    sa.saleTargetType = SaleTarget.T.TEAM;
                    saleTargetList.add(sa);
                }
                break;
            case TEAM:
                List<Category> categorys = Category.find("team_id=?", this.fid).fetch();
                for(Category t : categorys) {
                    SaleTarget sa = new SaleTarget(t.categoryId);
                    sa.parentId = this.id;
                    sa.saleTargetType = SaleTarget.T.CATEGORY;
                    saleTargetList.add(sa);
                }
                break;
            case CATEGORY:
                List<Product> products = Product.find("category_categoryId=?", this.fid).fetch();
                for(Product t : products) {
                    SaleTarget sa = new SaleTarget(t.sku);
                    sa.parentId = this.id;
                    sa.saleTargetType = SaleTarget.T.SKU;
                    saleTargetList.add(sa);
                }
                break;
            case SKU:
                //SKU 类型的销售目标已经是最小的，不允许再拥有子 销售目标
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
                return T.CATEGORY;
            case CATEGORY:
                return T.SKU;
            case SKU:
                //SKU 类型的 销售目标 是不允许再拥有 子对象的
                return null;
        }
        return null;
    }

    /**
     * 从父对象 copy 出 主题、备注 属性赋值给子对象
     * 同时根据父对象的 saleTargetType 来推导设置子对象的 saleTargetType
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
        return child;
    }
}
