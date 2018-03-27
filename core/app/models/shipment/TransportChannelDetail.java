package models.shipment;

import models.User;
import models.market.M;
import org.hibernate.annotations.DynamicUpdate;
import play.db.jpa.Model;

import javax.persistence.*;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.stream.Collectors;

/**
 * 运输渠道
 * Created by IntelliJ IDEA.
 * User: licco
 * Date: 2018/3/15
 * Time: 下午3:06
 */
@Entity
@DynamicUpdate
public class TransportChannelDetail extends Model {

    private static final long serialVersionUID = 3026692848164828177L;

    @ManyToOne(fetch = FetchType.LAZY)
    public TransportChannel channel;

    /**
     * 目的地
     */
    public String destination;

    /**
     * 运输时效
     */
    public String transportDay;

    public String memo;

    @ManyToOne(fetch = FetchType.LAZY)
    public User creator;

    public Date createDate;

    @OneToMany(mappedBy = "detail", fetch = FetchType.LAZY)
    public List<TransportRange> ranges = new ArrayList<>();

    @Transient
    public int rowspan;


    public static List<TransportRange> findOptimalChannelList(double weight, M market) {
        List<TransportRange> rangeList = TransportRange.find("detail.destination LIKE ? ",
                "%" + market.name() + "%").fetch();
        return rangeList.stream().filter(range -> range.containWeight(weight)).collect(Collectors.toList());
    }

}