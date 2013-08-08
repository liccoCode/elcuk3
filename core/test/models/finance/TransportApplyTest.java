package models.finance;

import factory.FactoryBoy;
import factory.callback.BuildCallback;
import models.procure.Cooperator;
import models.procure.Shipment;
import org.junit.Before;
import org.junit.Test;
import play.data.validation.Validation;
import play.db.jpa.JPA;
import play.test.UnitTest;

import java.util.ArrayList;
import java.util.List;

import static org.hamcrest.core.Is.is;

/**
 * 运输请款单
 * User: wyatt
 * Date: 7/17/13
 * Time: 11:37 AM
 */
public class TransportApplyTest extends UnitTest {
    @Before
    public void setUP() {
        FactoryBoy.deleteAll();
        Validation.clear();
    }

    @Test
    public void testBuildTransportApply() {
        List<Shipment> shipments = new ArrayList<Shipment>();
        List<String> shipmentIds = new ArrayList<String>();
        for(int i = 0; i < 5; i++) {
            Shipment ship = FactoryBoy.create(Shipment.class, new BuildCallback<Shipment>() {
                @Override
                public void build(Shipment target) {
                    target.id = Shipment.id();
                    target.cooper = FactoryBoy.lastOrCreate(Cooperator.class, "shipper");
                }
            });
            shipments.add(ship);
            shipmentIds.add(ship.id);
        }
        JPA.em().clear();

        assertThat(Shipment.count(), is(5l));
        TransportApply apply = TransportApply.buildTransportApply(shipmentIds);
        assertThat(apply.serialNumber, is("SQK-cooperName-001-13"));

        List<String> subIds = shipmentIds.subList(1, shipmentIds.size() - 1);
        subIds.add("not_exist_id");
        apply = TransportApply.buildTransportApply(subIds);
        assertThat(Validation.hasErrors(), is(true));
    }
}
