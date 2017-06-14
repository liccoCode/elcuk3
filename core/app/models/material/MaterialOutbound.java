package models.material;

import com.google.gson.annotations.Expose;
import controllers.Login;
import models.OperatorConfig;
import models.User;
import models.procure.Cooperator;
import models.procure.ProcureUnit;
import models.procure.Shipment;
import models.whouse.Outbound;
import models.whouse.StockRecord;
import models.whouse.Whouse;
import org.apache.commons.lang.StringUtils;
import org.hibernate.annotations.DynamicUpdate;
import org.joda.time.DateTime;
import play.data.validation.Required;
import play.data.validation.Validation;
import play.db.helper.SqlSelect;
import play.db.jpa.GenericModel;

import javax.persistence.*;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.stream.Collectors;

/**
 * 物料出库单model
 * 直接跟MaterialUnit建立一对多的关系
 * Created by IntelliJ IDEA.
 * User: Even
 * Date: 17/6/8
 * Time: AM10:18
 */
@Entity
@DynamicUpdate
public class MaterialOutbound extends GenericModel {

    private static final long serialVersionUID = 163177419089864527L;

    @Id
    @Column(length = 30)
    @Expose
    @Required
    public String id;

    /**
     * 物料采购计划
     */
    @OneToMany(mappedBy = "outbound", cascade = {CascadeType.PERSIST})
    public List<MaterialUnit> units = new ArrayList<>();

    /**
     * 名称
     */
    @Required
    public String name;

    /**
     * 出库类型
     */
    @Enumerated(EnumType.STRING)
    @Required
    @Expose
    public StockRecord.C type;


    @Enumerated(EnumType.STRING)
    @Required
    @Expose
    public S status;

    public enum S {
        Create {
            @Override
            public String label() {
                return "已创建";
            }
        },
        Outbound {
            @Override
            public String label() {
                return "已出库";
            }
        },
        Cancel {
            @Override
            public String label() {
                return "已取消";
            }
        };

        public abstract String label();
    }


    /**
     * 供应商
     * 一个采购单只能拥有一个供应商
     */
    @ManyToOne
    public Cooperator cooperator;


    /**
     * 项目名称
     */
    @Required
    public String projectName;


    /**
     * 制单人
     */
    @OneToOne
    public User creator;

    /**
     * 创建时间
     */
    @Required
    public Date createDate;

    /**
     * 备注
     */
    public String memo;



    public MaterialOutbound(MaterialUnit unit) {
        init();
        this.cooperator = unit.cooperator;
        this.projectName = unit.projectName.label();
    }

    public void init() {
        this.id = id();
        this.status = S.Create;
        this.createDate = new Date();
        this.creator = Login.current();
    }


    public static String id() {
        DateTime dt = DateTime.now();
        DateTime nextMonth = dt.plusMonths(1);
        String count = MaterialOutbound.count("createDate>=? AND createDate<?",
                DateTime.parse(String.format("%s-%s-01", dt.getYear(), dt.getMonthOfYear())).toDate(),
                DateTime.parse(String.format("%s-%s-01", nextMonth.getYear(), nextMonth.getMonthOfYear())).toDate()) +
                "";
        return String.format("WDP|%s|%s", dt.toString("yyyyMM"), count.length() == 1 ? "0" + count : count);
    }


}
