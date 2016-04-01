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

    //TODO pending qty
    @Expose
    public Integer qty = 0;

    @Expose
    public Date createDate = new Date();

    @Expose
    public Date updateDate = new Date();
}
