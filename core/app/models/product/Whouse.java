package models.product;

import com.google.gson.annotations.Expose;
import helper.Dates;
import models.market.Account;
import models.market.M;
import models.procure.Shipment;
import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.collections.Predicate;
import org.joda.time.DateTime;
import play.data.validation.Required;
import play.data.validation.Validation;
import play.db.jpa.Model;
import play.utils.FastRuntimeException;

import javax.persistence.*;
import java.util.Arrays;
import java.util.Date;
import java.util.List;

/**
 * 不同的仓库的抽象
 * User: Wyatt
 * Date: 12-1-8
 * Time: 上午6:06
 */
@Entity
@org.hibernate.annotations.Entity(dynamicUpdate = true)
public class Whouse extends Model {

    /**
     * 如果这个 Whouse 为 FBA 的, 那么则一定需要绑定一个 Account, 只有这样才能从 Account 获取必要的信息到 Amazon FBA 下载
     * FBA Inventory 库存信息
     */
    @OneToOne(fetch = FetchType.LAZY)
    public Account account;

    public enum T {
        /**
         * FBA 仓库, 独立出来
         */
        FBA,
        // 如果还有新的第三方仓库, 则再从代码中添加新类别
        /**
         * 自有仓库
         */
        SELF
    }


    @Required
    @Column(nullable = false, unique = true)
    @Expose
    public String name;

    @Column(nullable = false)
    @Expose
    public String address;

    @Column(nullable = false)
    @Expose
    public String city;

    @Column(nullable = false)
    @Expose
    public String province;

    @Column(nullable = false)
    @Expose
    public String postalCode;

    @Column(nullable = false)
    @Expose
    public String country;

    @Column(nullable = false)
    @Expose
    @Required
    public T type;

    @Lob
    @Expose
    public String memo;

    /**
     * 容量的提示字符串
     */
    @Lob
    public String capaticyContent = "";


    public void validate() {
        if(this.type == T.FBA) {
            if(this.account == null) Validation.addError("", "wh.fba.account");
        }
    }

    @PrePersist
    @PreUpdate
    public void prePersist() {
        this.address = String.format("%s %s %s %s", this.country, this.province, this.city,
                this.postalCode);
    }


    public void setName(String name) {
        this.name = name;
        if(this.name != null) this.name = this.name.toUpperCase();
    }


    @Override
    public boolean equals(Object o) {
        if(this == o) return true;
        if(o == null || getClass() != o.getClass()) return false;
        if(!super.equals(o)) return false;

        Whouse whouse = (Whouse) o;

        if(name != null ? !name.equals(whouse.name) : whouse.name != null) return false;

        return true;
    }

    @Override
    public int hashCode() {
        int result = super.hashCode();
        result = 31 * result + (name != null ? name.hashCode() : 0);
        return result;
    }

    public String name() {
        return String.format("%s: %s", this.type, this.name);
    }

    public static List<Whouse> findByType(T type) {
        return Whouse.find("type=?", type).fetch();
    }

    public static List<Whouse> findByAccount(Account account) {
        return Whouse.find("account=?", account).fetch();
    }

    /**
     * 根据星期判断shipmentType来处理运往某仓库的Shipment
     *
     * @param planShipments 用于判断的已经存在的运输单
     * @param now           今天
     * @return 暂时无返回
     */
    public void checkWhouseNewShipment(List<Shipment> planShipments) {
        /**
         * 1. 处理 60 天内的运输单;
         * 2. 规则: 空运每周 4; 海运 UK/US 每周 2, DE 每周 3
         */
        DateTime now = new DateTime(Dates.morning(new Date()));
        for(int i = 0; i < 60; i++) {
            DateTime nextBeginDate = now.plusDays(i);
            Object exist = CollectionUtils
                    .find(planShipments, new PlanDateEqual(nextBeginDate.toDate()));
            if(exist != null)
                continue;

            M type = this.account.type;
            if(nextBeginDate.getDayOfWeek() == 2) {
                if(Arrays.asList(M.AMAZON_UK, M.AMAZON_US).contains(type))
                    Shipment.checkNotExistAndCreate(nextBeginDate.toDate(), Shipment.T.SEA, this);

            } else if(nextBeginDate.getDayOfWeek() == 3) {
                if(Arrays.asList(M.AMAZON_DE).contains(type))
                    Shipment.checkNotExistAndCreate(nextBeginDate.toDate(), Shipment.T.SEA, this);

            } else if(nextBeginDate.getDayOfWeek() == 4) {
                if(Arrays.asList(M.AMAZON_DE, M.AMAZON_UK, M.AMAZON_US, M.AMAZON_IT).contains(type))
                    Shipment.checkNotExistAndCreate(nextBeginDate.toDate(), Shipment.T.AIR, this);
                else
                    throw new FastRuntimeException("还不支持向 " + type.name() + " 仓库创建运输单");
            }
        }
    }

    class PlanDateEqual implements Predicate {
        // 期待的日期
        private Date date;

        PlanDateEqual(Date date) {
            this.date = date;
        }

        @Override
        public boolean evaluate(Object o) {
            Shipment ship = (Shipment) o;
            return Dates.morning(ship.dates.planBeginDate).equals(Dates.morning(this.date));
        }
    }
}
