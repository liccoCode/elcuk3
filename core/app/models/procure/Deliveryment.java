package models.procure;

import com.google.gson.annotations.Expose;
import models.User;
import org.joda.time.DateTime;
import play.db.jpa.GenericModel;
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
public class Deliveryment extends GenericModel {

    public enum S {
        /**
         * 预定
         */
        PENDING,
        /**
         * 部分付款
         */
        PARTPAY,
        /**
         * 全部付款
         */
        FULPAY,
        /**
         * 部分交货
         */
        PART_DELIVERY,
        /**
         * 完成, 交货
         */
        DELIVERY
    }


    @OneToMany(mappedBy = "deliveryment")
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


    @Id
    @Column(length = 30)
    @Expose
    public String id;

    public static String id() {
        DateTime dt = DateTime.now();
        String count = Deliveryment.count("createDate>=? AND createDate<=?",
                DateTime.parse(String.format("%s-%s-01", dt.getYear(), dt.getMonthOfYear())).toDate(),
                DateTime.parse(String.format("%s-%s-30", dt.getYear(), dt.getMonthOfYear())).toDate()) + "";
        return String.format("DL|%s|%s", dt.toString("yyyyMM"), count.length() == 1 ? "0" + count : count);
    }

    public static List<Deliveryment> openDeliveryments() {
        return Deliveryment.find("state=?", S.PENDING).fetch();
    }

    public static Deliveryment checkAndCreate(User user) {
        if(user == null) throw new FastRuntimeException("必须拥有创建者.");
        Deliveryment deliveryment = new Deliveryment();
        deliveryment.id = Deliveryment.id();
        deliveryment.state = S.PENDING;
        return deliveryment.save();
    }
}
