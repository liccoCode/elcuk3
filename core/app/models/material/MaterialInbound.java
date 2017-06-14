package models.material;

import com.google.gson.annotations.Expose;
import models.User;
import models.procure.Cooperator;
import org.hibernate.annotations.DynamicUpdate;
import play.data.validation.Required;
import play.db.jpa.GenericModel;

import javax.persistence.*;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

/**
 * Created by licco on 2016/11/2.
 * 物料收货入库单
 */
@Entity
@DynamicUpdate
public class MaterialInbound extends GenericModel {

    private static final long serialVersionUID = -4192529114985615298L;

    @Id
    @Column(length = 30)
    @Expose
    @Required
    public String id;

    /**
     * 名称
     */
    public String name;

    /**
     * 收货类型
     */
    @Required
    @Expose
    @Enumerated(EnumType.STRING)
    public T type;

    public enum T {
        Purchase {
            @Override
            public String label() {
                return "采购入库";
            }
        },
        Machining {
            @Override
            public String label() {
                return "加工入库";
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
     * 出货单
     */
    @ManyToOne
    public MaterialPlan plan;

    /**
     * 物料入库单元
     */
    @OneToMany(mappedBy = "materialInbound", cascade = {CascadeType.PERSIST})
    public List<MaterialInboundUnit> units = new ArrayList<>();

    /**
     * 状态
     */
    @Required
    @Expose
    @Enumerated(EnumType.STRING)
    public S status;

    public enum S {
        Create {
            @Override
            public String label() {
                return "待收货";
            }
        },

        Handing {
            @Override
            public String label() {
                return "质检中";
            }
        },

        End {
            @Override
            public String label() {
                return "已入库";
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
     * 创建时间
     */
    @Required
    public Date createDate;

    /**
     * 收货时间
     */
    @Temporal(TemporalType.DATE)
    public Date receiveDate;

    /**
     * 收货人
     */
    @OneToOne
    public User receiver;

    /**
     * 备注
     */
    public String memo;

    /**
     * 公司名称
     */
    @Enumerated(EnumType.STRING)
    @Column(length = 20)
    public User.COR projectName;

    @Transient
    public List<String> qcDtos = new ArrayList<>();

    @Transient
    public List<String> inboundDtos = new ArrayList<>();


}
