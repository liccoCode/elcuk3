package models.procure;

import com.amazonservices.mws.FulfillmentInboundShipment._2010_10_01.FBAInboundServiceMWSException;
import helper.FBA;
import models.market.Account;
import org.junit.Test;
import play.test.UnitTest;

import java.util.Arrays;

/**
 * Created by IntelliJ IDEA.
 * User: wyatt
 * Date: 12/28/12
 * Time: 11:37 AM
 */
public class FBAShipmentTest extends UnitTest {
    //    @Test
    public void testUpdateFBAShipment() {
        ProcureUnit unit = ProcureUnit.findById(827l);
        unit.attrs.planQty += 5;
        unit.fba.updateFBAShipment(null);
    }

    @Test
    public void testlistShipments() throws FBAInboundServiceMWSException {
        FBA.listShipments(Arrays.asList("FBA8F3YC8"), Account.<Account>findById(2l));
    }

}
