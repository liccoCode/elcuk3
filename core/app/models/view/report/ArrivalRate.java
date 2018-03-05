package models.view.report;

import models.market.M;

import java.io.Serializable;

/**
 * Created by IntelliJ IDEA.
 * User: licco
 * Date: 15/6/11
 * Time: 下午2:11
 */
public class ArrivalRate implements Serializable {

    private static final long serialVersionUID = 2549758127620926692L;

    public M market;

    public String shipType;

    /**
     * 总票数
     */
    public long totalShipNum;

    /**
     * 准时票数
     */
    public long onTimeShipNum;

    /**
     * 准时率
     */
    public float onTimeRate;

    /**
     * 超时票数
     */
    public long overTimeShipNum;

    /**
     * 超时率
     */
    public float overTimeRate;

    /**
     * 提前票数
     */
    public long earlyTimeShipNum;

    /**
     * 提前率
     */
    public float earlyTimeRate;

    /**
     * 提前率
     */
    public float sumShipDay;


}
