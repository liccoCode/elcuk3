package models.view.report;

import models.market.M;

import java.io.Serializable;
import java.util.Date;

/**
 * Created by IntelliJ IDEA.
 * User: cary
 * Date: 14-6-9
 * Time: 上午9:27
 */
public class TrafficRate implements Serializable {
    public String sellingId;

    public Date sellDate;

    public M market;

    public float pageViews;

    public float sessions;

    public float orders;

    public float turnRatio;

}
