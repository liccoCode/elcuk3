package models.view.report;

import com.google.gson.annotations.Expose;
import models.market.M;
import org.hibernate.annotations.DynamicUpdate;
import play.db.jpa.GenericModel;

import javax.persistence.*;
import java.util.Date;

/**
 * Created by IntelliJ IDEA.
 * User: cary
 * Date: 14-6-9
 * Time: 上午9:27
 */
@Entity
@DynamicUpdate
public class TrafficRate extends GenericModel {

    @Id
    @Column(length = 80)
    @Expose
    public String id;

    public String sellingId;

    @Temporal(TemporalType.DATE)
    public Date sellDate;

    @Enumerated(EnumType.STRING)
    public M market;

    public float pageViews;

    public float sessions;

    public int orders;

    public float turnRatio;

    public int sales;

    public int returnd;

    public Date updateDate;
}
