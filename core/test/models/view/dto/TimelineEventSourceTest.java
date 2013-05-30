package models.view.dto;

import factory.FactoryBoy;
import factory.callback.BuildCallback;
import models.procure.ProcureUnit;
import models.procure.ShipItem;
import models.procure.Shipment;
import org.junit.Before;
import org.junit.Test;
import play.test.UnitTest;

import java.util.ArrayList;

import static org.hamcrest.core.Is.is;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.when;
import static util.DateHelper.t;

/**
 * Created by IntelliJ IDEA.
 * User: wyatt
 * Date: 5/30/13
 * Time: 12:08 PM
 */
public class TimelineEventSourceTest extends UnitTest {

    @Before
    public void setUp() {
        FactoryBoy.deleteAll();
    }

    @Test
    public void testEndDateChange() {
        ProcureUnit unit = spy(FactoryBoy.build(ProcureUnit.class, new BuildCallback<ProcureUnit>() {
            @Override
            public void build(ProcureUnit target) {
                target.attrs.qty = 200;
                target.attrs.planArrivDate = t("2013-05-06 00:00:00");
                target.shipItems = new ArrayList<ShipItem>();
                for(int i = 0; i < 2; i++) {
                    target.shipItems.add(FactoryBoy.build(ShipItem.class, new BuildCallback<ShipItem>() {
                        @Override
                        public void build(ShipItem shipItem) {
                            shipItem.recivedQty = 50;
                        }
                    }));
                }
            }
        }));

        when(unit.relateShipment()).thenReturn(new ArrayList<Shipment>());

        unit.stage = ProcureUnit.STAGE.PLAN;
        TimelineEventSource.Event event = spy(new TimelineEventSource.Event(new AnalyzeDTO(""), unit));

        when(event.ps("sid")).thenReturn(10f);

        event.startAndEndDate("sid");

        assertThat(event.start, is("2013-05-06 08:00:00"));
        // 200 /  10 -> 20; 100 / 10 -> 10
        assertThat(event.end, is("2013-05-26 08:00:00"));

        unit.stage = ProcureUnit.STAGE.INBOUND;
        event.startAndEndDate("sid");

        assertThat(event.start, is("2013-05-06 08:00:00"));
        // 200 /  10 -> 20; 100 / 10 -> 10
        assertThat(event.end, is("2013-05-16 08:00:00"));
    }
}
