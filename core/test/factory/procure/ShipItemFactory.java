package factory.procure;

import factory.ModelFactory;
import models.procure.ShipItem;

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
        return shipItem;
    }
}
