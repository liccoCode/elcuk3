package models.whouse;

import com.google.gson.annotations.Expose;
import play.db.jpa.Model;

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

}
