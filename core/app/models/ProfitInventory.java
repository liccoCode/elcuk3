package models;

import com.google.gson.annotations.Expose;
import play.db.jpa.Model;

import javax.persistence.Entity;

/**
 * 系统内的操作日志的记录;
 * <p/>
 * User: cay
 * Date: 1/16/15
 * Time: 11:50 AM
 */
@Entity
public class ProfitInventory extends Model {

    @Expose
    public String cooperator;

    @Expose
    public String sku;

    @Expose
    public String market;

    /**
     * 平均采购价
     */
    @Expose
    public double procureprice;
    /**
     * 平均运费单价
     */
    @Expose
    public double shipprice;
    /**
     * 关税和VAT单价
     */
    @Expose
    public double vatprice;

    /**
     * 正在制作+已交货的数量
     */
    @Expose
    public int workingqty = 0;
    /**
     * 在途库存
     */
    @Expose
    public int wayqty = 0;
    /**
     * 入库+在库
     */
    @Expose
    public int inboundqty = 0;

    /**
     * (制作中+已交货)库存占用资金总金额(USD)
     */
    @Expose
    public double workingfee = 0f;

    /**
     * 在途库存占用资金总金额(USD)
     */
    @Expose
    public double wayfee = 0f;

    /**
     * (入库+在库)库存占用资金总金额(USD)
     */
    @Expose
    public double inboundfee = 0f;

}
