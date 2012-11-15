package mails;

import models.procure.FBAShipment;
import models.procure.ShipItem;
import notifiers.FBAMails;
import org.junit.Test;
import play.test.UnitTest;

import java.util.HashSet;
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
        List<FBAShipment> fbas = FBAShipment.find("createAt>='2012-10-17 00:00:00'").fetch();
        FBAMails.itemsReceivingCheck(new HashSet<FBAShipment>(fbas));
    }
}
