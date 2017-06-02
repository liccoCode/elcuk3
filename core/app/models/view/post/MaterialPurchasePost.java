package models.view.post;

import helper.Dates;
import models.material.MaterialPurchase;
import models.procure.Deliveryment;
import models.whouse.Inbound;
import org.joda.time.DateTime;
import play.libs.F;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by IntelliJ IDEA.
 * User: licco
 * Date: 2017/5/31
 * Time: 下午5:39
 */
public class MaterialPurchasePost extends Post<MaterialPurchase> {

    private static final long serialVersionUID = 503452890747084694L;

    public MaterialPurchasePost() {
        DateTime now = DateTime.now(Dates.timeZone(null));
        this.from = now.minusDays(7).toDate();
        this.to = now.toDate();
        this.dateType = DeliveryPost.DateType.DELIVERY;
        this.perSize = 25;
    }

    public Deliveryment.S state;
    public Long cooperId;

    public DeliveryPost.DateType dateType;

    @Override
    public F.T2<String, List<Object>> params() {
        StringBuilder sbd = new StringBuilder("SELECT DISTINCT  m FROM MaterialPurchase m ");
        List<Object> params = new ArrayList<>();

        return new F.T2<>(sbd.toString(), params);
    }


    public List<MaterialPurchase> query() {
        F.T2<String, List<Object>> params = params();
        this.count = Inbound.find(params._1, params._2.toArray()).fetch().size();
        return MaterialPurchase.find(params._1, params._2.toArray()).fetch(this.page, this.perSize);
    }

    @Override
    public Long getTotalCount() {
        return this.count;
    }

    @Override
    public Long count(F.T2<String, List<Object>> params) {
        return this.count;
    }

}
