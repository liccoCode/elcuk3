package models.whouse;

import com.google.gson.annotations.Expose;
import models.User;
import models.procure.Cooperator;
import play.data.validation.Required;
import play.db.jpa.Model;

import javax.persistence.EnumType;
import javax.persistence.Enumerated;
import java.util.Date;

/**
 * Created by licco on 2016/11/2.
 * 收货入库
 */
public class Inbound extends Model {

    /**
     * 名称
     */
    @Required
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
    public Cooperator cooperator;

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
                return "已创建";
            }
        },

        Handing {
            @Override
            public String label() {
                return "处理中";
            }
        },

        End {
            @Override
            public String label() {
                return "已结束";
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
    public Date receiveDate;

    /**
     * 入库时间
     */
    public Date inboundDate;

    /**
     * 收货人
     */
    public User receiver;

    /**
     * 确认入库人
     */
    public User comfirmName;

    /**
     * 备注
     */
    public String memo;

}
