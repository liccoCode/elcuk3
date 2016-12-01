package models.whouse;

import com.google.gson.annotations.Expose;
import controllers.Login;
import models.User;
import models.procure.Cooperator;
import org.hibernate.annotations.DynamicUpdate;
import org.joda.time.DateTime;
import play.data.validation.Required;
import play.db.jpa.GenericModel;

import javax.persistence.*;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

/**
 * Created by licco on 2016/11/25.
 */
@Entity
@DynamicUpdate
public class Refund extends GenericModel {

    @Id
    @Column(length = 30)
    @Expose
    @Required
    public String id;

    /**
     * 退货单元
     */
    @OneToMany(mappedBy = "refund", cascade = {CascadeType.PERSIST})
    public List<RefundUnit> unitList = new ArrayList<>();

    /**
     * 名称
     */
    @Required
    public String name;

    public S status;

    public enum S {
        Create {
            @Override
            public String label() {
                return "已创建";
            }
        },
        Refund {
            @Override
            public String label() {
                return "已退货";
            }
        };

        public abstract String label();
    }

    /**
     * 收货类型
     */
    @Required
    @Expose
    @Enumerated(EnumType.STRING)
    public T type;

    public enum T {
        After_Receive {
            @Override
            public String label() {
                return "收货退货";
            }
        },
        After_Inbound {
            @Override
            public String label() {
                return "入库后退货";
            }
        };

        public abstract String label();
    }

    /**
     * 供应商
     */
    @Required
    @OneToOne
    public Cooperator cooperator;

    /**
     * 项目名称
     */
    @Required
    public String projectName;

    /**
     * 备注
     */
    public String memo;

    /**
     * 创建时间
     */
    @Required
    public Date createDate;

    /**
     * 退货时间
     */
    @Required
    public Date refundDate;

    /**
     * 仓库交接人
     */
    public User whouseUser;

    /**
     * 制单人
     */
    @OneToOne
    public User creator;

    /**
     * 物流信息
     */
    public String info;

    /**
     * 是否B2B
     */
    @Transient
    public boolean isb2b = false;


    public Refund() {
        this.status = S.Create;
        this.creator = Login.current();
        this.createDate = new Date();
        this.refundDate = new Date();
    }

    public void createRefund(List<InboundUnit> list) {
        this.id = id();
        this.save();
        for(InboundUnit unit : list) {
            RefundUnit runit = new RefundUnit();
            runit.refund = this;
            runit.planQty = unit.qty;
            runit.mainBoxInfo = unit.mainBoxInfo;
            runit.save();
        }
    }

    public static String id() {
        DateTime dt = DateTime.now();
        DateTime nextMonth = dt.plusMonths(1);
        String count = Refund.count("createDate>=? AND createDate<?",
                DateTime.parse(String.format("%s-%s-01", dt.getYear(), dt.getMonthOfYear())).toDate(),
                DateTime.parse(String.format("%s-%s-01", nextMonth.getYear(), nextMonth.getMonthOfYear())).toDate()) +
                "";
        return String.format("PTT|%s|%s", dt.toString("yyyyMM"), count.length() == 1 ? "0" + count : count);
    }

}
