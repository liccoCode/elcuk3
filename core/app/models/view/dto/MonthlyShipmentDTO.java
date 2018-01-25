package models.view.dto;

import models.market.M;
import models.procure.Shipment;

import java.io.Serializable;
import java.util.Date;

/**
 * Created by IntelliJ IDEA.
 * User: licco
 * Date: 2017/6/21
 * Time: 下午3:14
 */
public class MonthlyShipmentDTO implements Serializable {

    private static final long serialVersionUID = 553763968273382025L;

    public Date from;
    public Date to;
    public String sku;
    public String categoryId;

    public M market;
    public Shipment.T type;

    public int seaQty;
    public int airQty;
    public int expressQty;
    public int dedicatedQty;
    public int railwayQty;

    public Float seaWeight;
    public Float airWeight;
    public Float expressWeight;
    public Float dedicatedWeight;
    public Float railwayWeight;

    public Float seaCbm;
    public Float airCbm;
    public Float expressCbm;
    public Float dedicatedCbm;
    public Float railwayCbm;

    public String centerId;




}
