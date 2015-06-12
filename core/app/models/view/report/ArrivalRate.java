package models.view.report;

import java.io.Serializable;

/**
 * Created by licco on 15/6/11.
 */
public class ArrivalRate implements Serializable {

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


}
