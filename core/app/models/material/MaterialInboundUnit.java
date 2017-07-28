package models.material;

import models.User;
import org.hibernate.annotations.DynamicUpdate;
import play.db.jpa.Model;

import javax.persistence.*;
import java.util.Date;

/**
 * 物料入库单元
 * Created by licco on 2016/11/2.
 */
@Entity
@DynamicUpdate
public class MaterialInboundUnit extends Model {

    @ManyToOne
    public MaterialInbound materialInbound;

    /**
     * 物料计划
     */
    @ManyToOne
    public MaterialUnit materialUnit;

    /**
     * 计划交货数量
     */
    public int planQty;

    /**
     * 实际交货数量
     */
    public int qty;

    /**
     * 交货不足处理方式
     */
    @Enumerated(EnumType.STRING)
    public H handType;

    public enum H {
        Actual {
            @Override
            public String label() {
                return "按实际到货处理";
            }
        },
        Delivery {
            @Override
            public String label() {
                return "收货且创建尾货单";
            }
        };

        public abstract String label();
    }

    /**
     * 状态
     */
    @Enumerated(EnumType.STRING)
    public S status;

    public enum S {
        Create {
            @Override
            public String label() {
                return "待收货";
            }
        },
        Receive {
            @Override
            public String label() {
                return "质检中";
            }
        },
        Check {
            @Override
            public String label() {
                return "已质检";
            }
        },
        Inbound {
            @Override
            public String label() {
                return "已入库";
            }
        },
        Abort {
            @Override
            public String label() {
                return "异常结束";
            }
        };

        public abstract String label();
    }

    /**
     * 质检结果
     */
    @Enumerated(EnumType.STRING)
    public R result;

    public enum R {
        UnCheck {
            @Override
            public String label() {
                return "未检";
            }
        },
        Qualified {
            @Override
            public String label() {
                return "合格";
            }
        },
        Unqualified {
            @Override
            public String label() {
                return "不合格";
            }
        };

        public abstract String label();
    }


    /**
     * 合格数
     */
    public int qualifiedQty;

    /**
     * 不合格数量
     */
    public int unqualifiedQty;

    /**
     * 实际入库数
     */
    public int inboundQty;

    /**
     * 质检时间
     */
    public Date qcDate;

    /**
     * 质检人
     */
    @OneToOne
    public User qcUser;

    /**
     * 入库时间
     */
    public Date inboundDate;

    /**
     * 确认入库人
     */
    @OneToOne
    public User confirmUser;


}
