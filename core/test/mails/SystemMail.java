package mails;

import helper.Webs;
import models.procure.FBAShipment;
import notifiers.FBAMails;
import notifiers.SystemMails;
import org.junit.Test;
import play.test.UnitTest;

/**
 * Created by IntelliJ IDEA.
 * User: wyattpan
 * Date: 3/8/12
 * Time: 1:19 PM
 */
public class SystemMail extends UnitTest {
    //    @Test
    public void sendSuccessble() {
        Webs.systemMail("SYSTEM MAIL TEST!", "dfjkjkdjfkdjfkjdjfkj");
    }

    //    @Test
    public void testDailyReviewMail() {
        assertEquals(true, SystemMails.dailyReviewMail(null));
    }

    @Test
    public void testFBAShipmentStateChangeMail() {
        FBAShipment fba = FBAShipment.all().first();
        FBAMails.shipmentStateChange(fba, fba.state, FBAShipment.S.RECEIVING);
    }
}
