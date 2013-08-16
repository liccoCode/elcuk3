package factory.procure;

import factory.FactoryBoy;
import factory.ModelFactory;
import factory.annotation.Factory;
import models.User;
import models.procure.Cooperator;
import models.procure.Shipment;
import models.procure.iExpress;
import models.product.Whouse;
import org.joda.time.DateTime;

/**
 * Created by IntelliJ IDEA.
 * User: wyatt
 * Date: 5/29/13
 * Time: 6:39 PM
 */
public class ShipmentFactory extends ModelFactory<Shipment> {
    @Override
    public Shipment define() {
        DateTime now = DateTime.now();
        Shipment shipment = new Shipment();
        shipment.id = Shipment.id();
        shipment.type = Shipment.T.EXPRESS;
        shipment.dates.planBeginDate = now.minusDays(45).toDate();
        shipment.calcuPlanArriveDate();
        shipment.internationExpress = iExpress.DHL;
        shipment.trackNo = "this_is_trackNo";
        shipment.whouse = FactoryBoy.lastOrCreate(Whouse.class);
        shipment.cooper = FactoryBoy.lastOrCreate(Cooperator.class);
        shipment.dates.beginDate = shipment.dates.planBeginDate;
        shipment.dates.atPortDate = now.minusDays(15).toDate();
        shipment.dates.bookDate = now.minusDays(14).toDate();
        shipment.dates.deliverDate = now.minusDays(14).toDate();
        shipment.dates.inbondDate = now.minusDays(13).toDate();
        shipment.dates.pickGoodDate = now.minusDays(13).toDate();
        shipment.dates.receiptDate = now.minusDays(10).toDate();

        shipment.creater = FactoryBoy.lastOrCreate(User.class);
        return shipment;
    }

    @Factory(name = "sea")
    public Shipment sea() {
        Shipment shipment = define();
        shipment.type = Shipment.T.SEA;
        shipment.calcuPlanArriveDate();
        return shipment;
    }

    @Factory(name = "air")
    public Shipment air() {
        Shipment shipment = define();
        shipment.type = Shipment.T.AIR;
        shipment.calcuPlanArriveDate();
        return shipment;
    }
}
