package models.procure;

import com.google.gson.annotations.Expose;
import controllers.Secure;
import models.User;
import org.hibernate.annotations.CacheConcurrencyStrategy;
import org.joda.time.DateTime;
import play.db.helper.JpqlSelect;
import play.db.jpa.GenericModel;
import play.db.jpa.JPQL;
import play.utils.FastRuntimeException;

import javax.persistence.*;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

/**
 * 采购单, 用来记录所采购的 ProcureUnit
 * User: wyattpan
 * Date: 6/18/12
 * Time: 4:50 PM
 */
@Entity
@org.hibernate.annotations.Cache(usage = CacheConcurrencyStrategy.READ_WRITE)
@org.hibernate.annotations.Entity(dynamicUpdate = true)
public class Deliveryment extends GenericModel {

    public enum S {
        /**
         * 预定, 已经下单
         */
        PENDING,
        /**
         * 部分交货
         */
        DELIVERING,
        /**
         * 完成, 交货
         */
        DELIVERY,
        /**
         * 需要付款, 表示货物已经全部完成.
         */
        NEEDPAY,
        /**
         * 全部付款
         */
        FULPAY,
        CANCEL
    }

    @OneToMany(mappedBy = "deliveryment")
    @OrderBy("state DESC")
    public List<Payment> payments = new ArrayList<Payment>();


    @OneToMany(mappedBy = "deliveryment", cascade = {CascadeType.PERSIST})
    public List<ProcureUnit> units = new ArrayList<ProcureUnit>();

    @OneToOne
    public User handler;

    @Expose
    public Date createDate = new Date();

    /**
     * 此采购单的状态
     */
    @Enumerated(EnumType.STRING)
    @Expose
    @Column(nullable = false)
    public S state;

    /**
     * 可为每一个 Deliveryment 添加一个名称
     */
    public String name;


    @Id
    @Column(length = 30)
    @Expose
    public String id;

    @Lob
    public String memo = " ";

    public static String id() {
        DateTime dt = DateTime.now();
        String count = Deliveryment.count("createDate>=? AND createDate<=?",
                DateTime.parse(String.format("%s-%s-01", dt.getYear(), dt.getMonthOfYear())).toDate(),
                DateTime.parse(String.format("%s-%s-30", dt.getYear(), dt.getMonthOfYear())).toDate()) + "";
        return String.format("DL|%s|%s", dt.toString("yyyyMM"), count.length() == 1 ? "0" + count : count);
    }

    public static List<Deliveryment> openDeliveryments() {
        //TODO 需要将 Deliveryment 添加 supplier
        return Deliveryment.find("state NOT IN (?,?)", S.DELIVERY, S.CANCEL).fetch();
    }

    public static Deliveryment checkAndCreate(User user) {
        if(user == null) throw new FastRuntimeException("必须拥有创建者.");
        Deliveryment deliveryment = new Deliveryment();
        deliveryment.id = Deliveryment.id();
        deliveryment.state = S.PENDING;
        deliveryment.handler = user;
        return deliveryment.save();
    }

    /**
     * 通过 ProcureUnit 来创建采购单
     *
     * @param pids
     */
    public static Deliveryment createFromProcures(List<Long> pids, String name, User user) {
        List<ProcureUnit> units = ProcureUnit.find("id IN " + JpqlSelect.inlineParam(pids)).fetch();
        if(pids.size() != units.size()) throw new FastRuntimeException("有错误的 ProcureUnit 请仔细检查.");
        for(ProcureUnit unit : units) {
            if(unit.stage != ProcureUnit.STAGE.PLAN)
                throw new FastRuntimeException(String.format("ProcureUnit #%s stage 不为 PLAN, 无法创建运输单.", unit.id));
        }
        Deliveryment deliveryment = new Deliveryment();
        deliveryment.handler = user;
        deliveryment.state = S.PENDING;
        deliveryment.id = Deliveryment.id();
        deliveryment.name = name.trim();
        deliveryment.units.addAll(units);
        for(ProcureUnit unit : deliveryment.units) {
            unit.deliveryment = deliveryment;
            unit.stage = ProcureUnit.STAGE.DELIVERY;
        }
        deliveryment.save();
        return deliveryment;
    }

}
