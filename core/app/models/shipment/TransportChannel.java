package models.shipment;

import com.google.gson.annotations.Expose;
import models.User;
import models.procure.Shipment;
import org.apache.commons.lang.StringUtils;
import org.hibernate.annotations.DynamicUpdate;
import play.data.validation.Required;
import play.db.jpa.Model;

import javax.persistence.*;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Created by IntelliJ IDEA.
 * User: licco
 * Date: 2018/3/21
 * Time: 下午2:15
 */
@Entity
@DynamicUpdate
public class TransportChannel extends Model {

    private static final long serialVersionUID = -2995501250565459161L;

    @Enumerated(EnumType.STRING)
    @Expose
    @Required
    public Shipment.T type;

    /**
     * 国际快递商人
     */
    public String internationExpress;

    public String channel;

    @OneToMany(mappedBy = "channel", fetch = FetchType.LAZY)
    public List<TransportChannelDetail> detailList = new ArrayList<>();

    @ManyToOne(fetch = FetchType.LAZY)
    public User creator;

    public Date createDate;

    public static List<String> initShipChannelByType(Shipment.T type, String internationExpress) {
        List<TransportChannel> list = TransportChannel.find("type=? AND internationExpress=?",
                type, StringUtils.isNotBlank(internationExpress) ? internationExpress : "").fetch();
        return list.stream().map(detail -> detail.channel).collect(Collectors.toList());
    }

}
