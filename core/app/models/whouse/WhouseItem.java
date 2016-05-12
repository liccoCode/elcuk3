package models.whouse;

import com.google.gson.annotations.Expose;
import play.db.jpa.Model;

import javax.persistence.Embedded;
import javax.persistence.Entity;
import javax.persistence.ManyToOne;
import java.util.Date;

/**
 * 库存项
 * Created by IntelliJ IDEA.
 * User: duan
 * Date: 4/1/16
 * Time: 10:52 AM
 */
@Entity
public class WhouseItem extends Model {

    @ManyToOne
    public Whouse whouse;

    /**
     * 库存对象(到底存的是什么东西, SKU or 物料)
     */
    @Embedded
    @Expose
    public StockObj stockObj;

    @Expose
    public Integer qty = 0;

    /**
     * 待处理库存数量
     */
    @Expose
    public Integer pendingQty = 0;

    @Expose
    public Date createDate = new Date();

    @Expose
    public Date updateDate = new Date();

    public WhouseItem() {
    }

    public WhouseItem(StockObj stockObj, Whouse whouse) {
        this.stockObj = stockObj.dump();
        this.whouse = whouse;
    }

    public static WhouseItem findItem(StockObj stockObj, Whouse whouse) {
        WhouseItem whouseItem = WhouseItem.find("stockObjId=? AND stockObjType=? AND whouse_id=?",
                stockObj.stockObjId, stockObj.stockObjType.name(), whouse.id).first();
        if(whouseItem == null) {
            return new WhouseItem(stockObj, whouse).save();
        }
        return whouseItem;
    }
}