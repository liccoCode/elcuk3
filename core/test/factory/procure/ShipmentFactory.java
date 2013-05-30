package factory.procure;

import factory.ModelFactory;
import models.procure.Shipment;
import models.procure.iExpress;

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
        return shipment;
    }
}
