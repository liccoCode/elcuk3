package mails;

import models.procure.FBAShipment;
import models.procure.ShipItem;
import notifiers.FBAMails;
import org.junit.Test;
import play.test.UnitTest;

import java.util.List;

/**
 * Created by IntelliJ IDEA.
 * User: wyattpan
 * Date: 10/18/12
 * Time: 11:17 AM
 */
public class FBAMailsTest extends UnitTest {

    @Test
    public void testItemsReceivingCheck() {
        FBAShipment fba = FBAShipment.findById(63l);
        List<ShipItem> shipItemList = ShipItem.sameFBAShipItems(fba.shipmentId);
        FBAMails.itemsReceivingCheck(fba, shipItemList, shipItemList);
    }
}
