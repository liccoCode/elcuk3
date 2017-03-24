package models.whouse;

import com.google.gson.annotations.Expose;
import models.User;
import models.procure.ProcureUnit;
import play.data.validation.Required;
import play.db.jpa.Model;

import javax.persistence.*;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 库存异动记录
 * Created by IntelliJ IDEA.
 * User: duan
 * Date: 4/1/16
 * Time: 11:12 AM
 */
@Entity
public class StockRecord extends Model {
    /**
     * 仓库
     */
    @Required
    @Expose
    @ManyToOne
    public Whouse whouse;

    @Required
    @Expose
    @ManyToOne
    public ProcureUnit unit;

    @Expose
    @ManyToOne
    public Outbound outbound;

    /**
     * 数量
     */
    @Required
    @Expose
    public Integer qty;

    /**
     * 调整后采购计划对应的库存
     */
    @Required
    @Expose
    public Integer currQty;


    /**
     * 类型
     */
    @Enumerated(EnumType.STRING)
    @Expose
    public T type;

    public enum T {
        Inbound {
            @Override
            public String label() {
                return "采购入库";
            }
        },
        Outbound {
            @Override
            public String label() {
                return "出库";
            }
        },
        OtherOutbound {
            @Override
            public String label() {
                return "其它出库";
            }
        },
        Split {
            @Override
            public String label() {
                return "库存分拆";
            }
        },
        Refund {
            @Override
            public String label() {
                return "入库后退货";
            }
        },
        Stocktaking {
            @Override
            public String label() {
                return "库存调整";
            }
        },
        Split_Stock {
            @Override
            public String label() {
                return "库存分拆修改";
            }
        },
        Unqualified_Refund {
            @Override
            public String label() {
                return "不良品退货";
            }
        },
        Unqualified_Transfer {
            @Override
            public String label() {
                return "不良品转入";
            }
        };


        public abstract String label();
    }

    public enum C {
        Check {
            @Override
            public String label() {
                return "盘点";
            }
        },
        Normal {
            @Override
            public String label() {
                return "Amazon 出库";
            }
        },
        B2B {
            @Override
            public String label() {
                return "B2B 出库";
            }
        },
        Refund {
            @Override
            public String label() {
                return "退回工厂";
            }
        },
        Process {
            @Override
            public String label() {
                return "品拓生产";
            }
        },
        Sample {
            @Override
            public String label() {
                return "取样";
            }
        },
        Other {
            @Override
            public String label() {
                return "其他出库";
            }
        };

        public abstract String label();
    }

    /**
     * 类别
     */
    @Enumerated(EnumType.STRING)
    @Expose
    public C category;

    /**
     * 记录 ID(入库 Or 出库)
     */
    public Long recordId;

    @Expose
    public Date createDate = new Date();

    @OneToOne
    @Expose
    public User creator;

    public String memo;

    @Transient
    public Long unitId;


    public StockRecord() {
        this.createDate = new Date();
    }

    public static Map<Integer, List<StockRecord>> pageNumForTen(List<Outbound> outbounds) {
        Map<Integer, List<StockRecord>> ten = new HashMap<>();
        int k = 0;
        for(Outbound outbound : outbounds) {
            int max = outbound.records.size();
            for(int i = 0; i < max; i += 10) {
                int num = max - i;
                ten.put(k, outbound.records.subList(i, num > 10 ? i + 10 : i + num));
                k++;
            }
        }
        return ten;
    }

}
