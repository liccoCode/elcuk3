package models.whouse;

import com.google.gson.annotations.Expose;
import play.db.jpa.Model;

import javax.persistence.Embedded;
import javax.persistence.Entity;
import javax.persistence.ManyToOne;
import java.util.Date;

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
    @Expose
    @ManyToOne
    public Whouse whouse;

    /**
     * 数量
     */
    @Expose
    public Integer qty;

    /**
     * 异动对象(SKU or 物料)
     */
    @Embedded
    @Expose
    public StockObj stockObj;

    /**
     * 类型
     */
    public T type;

    public enum T {
        Inbound {
            @Override
            public String label() {
                return "入库";
            }
        },
        Outbound {
            @Override
            public String label() {
                return "出库";
            }
        },
        Stocktaking {
            @Override
            public String label() {
                return "盘库";
            }
        };

        public abstract String label();
    }

    @Expose
    public Date createDate = new Date();

    @Expose
    public Date updateDate = new Date();
}
