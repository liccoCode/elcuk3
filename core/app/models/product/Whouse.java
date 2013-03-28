package models.product;

import com.google.gson.annotations.Expose;
import helper.Dates;
import models.market.Account;
import models.market.M;
import models.procure.Shipment;
import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.collections.Predicate;
import org.apache.commons.lang.StringUtils;
import org.joda.time.DateTime;
import play.Logger;
import play.data.validation.Required;
import play.data.validation.Validation;
import play.db.jpa.Model;

import javax.persistence.*;
import java.util.ArrayList;
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
        this.address = String.format("%s %s %s %s", this.country, this.province, this.city, this.postalCode);
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

    public static List<Whouse> findByType(T  type) {
        return Whouse.find("type=?", type).fetch();
    }

    /**
     * 根据星期判断shipmentType来处理运往某仓库的Shipment
     * @param planShipments 新建的运输单
     * @param now 今天
     * @return 暂时无返回
     */
    public void checkWhouseNewShipment(List<Shipment> planShipments,DateTime now){
        // 处理 60 天内的运输单; 快递 2,4; 空运 3,5; 海运 GB:2 US:2 DE:3
        for(int i = 0; i <60 ; i++) {
            DateTime tmp=now.plusDays(i);
            Object exist = CollectionUtils
                          .find(planShipments,new PlanDateEqual(tmp.toDate()));
            if(exist!=null)
                continue;

            M type=this.account.type;
            if(tmp.dayOfWeek().get() == 2) {
                checkWhouseNewShipment(tmp.toDate(), Shipment.T.EXPRESS,tmp.plus(7).toDate());
                if(type.equals(M.AMAZON_UK)||type.equals(M.AMAZON_US))
                   checkWhouseNewShipment(tmp.toDate(), Shipment.T.SEA,tmp.plus(45).toDate());
            }else if(tmp.dayOfWeek().get() == 3){
                if(type.equals(M.AMAZON_DE))
                    checkWhouseNewShipment(tmp.toDate(),Shipment.T.SEA,tmp.plus(45).toDate());
                checkWhouseNewShipment(tmp.toDate(),Shipment.T.AIR,tmp.plus(14).toDate());
            }else if(tmp.dayOfWeek().get()==4){
                checkWhouseNewShipment(tmp.toDate(),Shipment.T.EXPRESS,tmp.plus(7).toDate());
                //除 GB US DE 创建的时间不同,其他国家的都是周4
                if(!type.equals(M.AMAZON_DE)&&!type.equals(M.AMAZON_UK)&&!type.equals(M.AMAZON_US))
                     checkWhouseNewShipment(tmp.toDate(),Shipment.T.SEA,tmp.plus(45).toDate());
            }else if(tmp.dayOfWeek().get()==5)
                checkWhouseNewShipment(tmp.toDate(),Shipment.T.AIR,tmp.plus(14).toDate());
        }

    }

    /**
     * 确定新建运输单的目的地仓库
     * @param planBeginDate 计划开始时间
     * @param shipmentType  运输类型
     */
    private void  checkWhouseNewShipment(Date planBeginDate,Shipment.T shipmentType,Date arriveDate){
        if(Shipment.count("planBeginDate=? AND whouse=? AND type=? AND cycle=true AND state IN (?,?)",planBeginDate, this, shipmentType, Shipment.S.PLAN, Shipment.S.CONFIRM) > 0)
            return ;
        Shipment.create(planBeginDate,this,shipmentType,arriveDate);

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
                return Dates.morning(ship.planBeginDate).equals(Dates.morning(this.date));
            }
    }
}
