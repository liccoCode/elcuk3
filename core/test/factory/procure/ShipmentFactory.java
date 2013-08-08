package factory.procure;

import factory.FactoryBoy;
import factory.ModelFactory;
import models.User;
import models.procure.Cooperator;
import models.procure.Shipment;
import models.procure.iExpress;
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
        Shipment shipment = new Shipment();
        shipment.id = "SP|201207|00";
        shipment.type = Shipment.T.EXPRESS;
        shipment.internationExpress = iExpress.DHL;
        shipment.trackNo = "this_is_trackNo";
        shipment.cooper = FactoryBoy.lastOrCreate(Cooperator.class);
        DateTime now = DateTime.now();
        shipment.dates.planBeginDate = now.minusDays(45).toDate();
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
}
