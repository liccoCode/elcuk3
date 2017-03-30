package models;

import models.market.M;
import play.db.jpa.GenericModel;

import javax.persistence.Entity;
import javax.persistence.EnumType;
import javax.persistence.Enumerated;
import javax.persistence.Id;
import java.util.Date;

/**
 * Created by IntelliJ IDEA.
 * User: duan
 * Date: 3/18/17
 * Time: 3:17 PM
 */
@Entity
public class InventoryCostUnit extends GenericModel {
    /**
     * 产品
     */
    private String sku;

    /**
     * 产品线
     */
    private String categoryId;

    /**
     * 市场
     */
    @Enumerated(EnumType.STRING)
    private M market;

    /**
     * 采购单价
     */
    private Float procurementPrice;

    /**
     * 运输单价
     */
    private Float transportPrice;

    /**
     * 缴税单价(VAT、关税)
     */
    private Float taxPrice;

    /**
     * 在途数量
     */
    private Integer transitQty;

    /**
     * 在库数量(入库中+在库)
     */
    private Integer stockQty;

    /**
     * 生产中的数量(制作中+已交货)
     */
    private Integer productionQty;

    /**
     * 主键 ID
     */
    @Id
    private String id;

    /**
     * 日期(任务执行时月份的最后一天)
     */
    private Date date;


    public static final String FILE_HEADER = "CategoryId,Sku,Market,制作中已交货,在途,在库,采购单价,运输单价,关税VAT单价";

    /**
     * 将当前对象映射成 CSV 文件中的一行
     *
     * @return
     */
    public String toCSV() {
        return this.categoryId + "," +
                this.sku + "," +
                this.market + "," +
                String.valueOf(productionQty) + "," +
                String.valueOf(transitQty) + "," +
                String.valueOf(stockQty) + "," +
                String.valueOf(procurementPrice) + "," +
                String.valueOf(transportPrice) + "," +
                String.valueOf(taxPrice) + "\n";
    }
}
