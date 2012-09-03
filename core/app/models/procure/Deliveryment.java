package models.procure;

import com.google.gson.annotations.Expose;
import models.User;
import org.hibernate.annotations.CacheConcurrencyStrategy;
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
@org.hibernate.annotations.Cache(usage = CacheConcurrencyStrategy.READ_WRITE)
@org.hibernate.annotations.Entity(dynamicUpdate = true)
public class Deliveryment extends GenericModel {

    public enum S {
        /**
         * 预定, 已经下单
         */
        PENDING {
            @Override
            public String color() {
                return "#5CB85C";
            }
        },
        /**
         * 部分交货
         */
        DELIVERING {
            @Override
            public String color() {
                return "#FAA52C";
            }
        },
        /**
         * 完成, 交货
         */
        DELIVERY {
            @Override
            public String color() {
                return "#F67300";
            }
        },
        /**
         * 需要付款, 表示货物已经全部完成.
         */
        NEEDPAY {
            @Override
            public String color() {
                return "#4DB2D0";
            }
        },
        /**
         * 全部付款
         */
        FULPAY {
            @Override
            public String color() {
                return "#007BCC";
            }
        },
        CANCEL {
            @Override
            public String color() {
                return "red";
            }
        };

        /**
         * 转换为 html
         *
         * @return
         */
        public String to_h() {
            return String.format("<span style='color:%s'>%s</span>", this.color(), this);
        }

        public abstract String color();
    }

    @OneToMany(mappedBy = "deliveryment")
    @OrderBy("state DESC")
    public List<Payment> payments = new ArrayList<Payment>();


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

}
