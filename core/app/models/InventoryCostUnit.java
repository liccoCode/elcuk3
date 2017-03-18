package models;

import models.market.M;
import play.db.jpa.GenericModel;

import javax.persistence.Entity;
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
}
