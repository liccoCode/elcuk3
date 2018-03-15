package models.shipment;

import com.google.gson.annotations.Expose;
import models.User;
import models.procure.Shipment;
import org.hibernate.annotations.DynamicUpdate;
import play.data.validation.Required;
import play.db.jpa.Model;

import javax.persistence.*;
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

    @Enumerated(EnumType.STRING)
    @Expose
    @Required
    public Shipment.T type;

    /**
     * 国际快递商人
     */
    public String internationExpress;

    public String channel;

    @ManyToOne(fetch = FetchType.LAZY)
    public User creator;

    public Date createDate;

    public static List<TransportChannelDetail> initShipChannel() {
        List<TransportChannelDetail> details = TransportChannelDetail.find("").fetch();
        return details;
    }

    public static List<String> initShipChannelByType(Shipment ship) {
        List<TransportChannelDetail> list = TransportChannelDetail.find("type=? AND internationExpress=?",
                ship.type, ship.internationExpress != null ? ship.internationExpress.name() : "").fetch();
        return list.stream().map(detail -> detail.channel).collect(Collectors.toList());
    }

}
