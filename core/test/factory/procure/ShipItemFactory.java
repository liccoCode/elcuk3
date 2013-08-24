package factory.procure;

import factory.FactoryBoy;
import factory.ModelFactory;
import models.procure.ProcureUnit;
import models.procure.ShipItem;
import models.procure.Shipment;

/**
 * Created by IntelliJ IDEA.
 * User: wyatt
 * Date: 5/30/13
 * Time: 12:25 PM
 */
public class ShipItemFactory extends ModelFactory<ShipItem> {

    @Override
    public ShipItem define() {
        ShipItem shipItem = new ShipItem();
        shipItem.qty = 200;
        shipItem.unit = FactoryBoy.lastOrCreate(ProcureUnit.class);
        shipItem.recivedQty = 100;
        shipItem.shipment = FactoryBoy.lastOrCreate(Shipment.class);
        return shipItem;
    }
}
