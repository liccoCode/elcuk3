package query;

import models.market.M;
import models.view.highchart.Series;
import org.junit.Before;
import org.junit.Test;
import play.test.UnitTest;
import util.DateHelper;

import static org.hamcrest.core.Is.is;

/**
 * Created by IntelliJ IDEA.
 * User: wyatt
 * Date: 11/1/13
 * Time: 11:45 AM
 */
public class OrderItemESQueryTest extends UnitTest {

    public OrderItemESQuery esQuery;

    @Before
    public void setUp() {
        esQuery = new OrderItemESQuery();
    }

    @Test
    public void testSkuSalesAndUnitsDE() {
        Series.Line line = esQuery.skuSalesAndUnits("80QW84-2AECUPB", M.AMAZON_DE, DateHelper.t("2013-10-22 00:00:00"),
                DateHelper.t("2013-10-25 00:00:00"));

        assertThat(line.data.size(), is(3));
        Object[] row1 = line.data.get(0);
        assertThat((Long) row1[0], is(1382371200000l));
        assertThat((Float) row1[1], is(74f));

        Object[] row2 = line.data.get(1);
        assertThat((Long) row2[0], is(1382457600000l));
        assertThat((Float) row2[1], is(77f));


        Object[] row3 = line.data.get(2);
        assertThat((Long) row3[0], is(1382544000000l));
        assertThat((Float) row3[1], is(56f));
    }

    @Test
    public void testSkuSalesAndUnitsUS() {
        Series.Line line = esQuery.skuSalesAndUnits("80DBK12000-AB", M.AMAZON_US, DateHelper.t("2013-10-22 00:00:00"),
                DateHelper.t("2013-10-25 00:00:00"));

        assertThat(line.data.size(), is(3));
        Object[] row1 = line.data.get(0);
        assertThat((Long) row1[0], is(1382371200000l));
        assertThat((Float) row1[1], is(36f));

        Object[] row2 = line.data.get(1);
        assertThat((Long) row2[0], is(1382457600000l));
        assertThat((Float) row2[1], is(35f));


        Object[] row3 = line.data.get(2);
        assertThat((Long) row3[0], is(1382544000000l));
        assertThat((Float) row3[1], is(34f));
    }

    @Test
    public void testMskuSalesAndUnitsDE() {
        Series.Line line = esQuery
                .mskuSalesAndUnits("71KDPW-BLCPU,607198983568", M.AMAZON_DE, DateHelper.t("2013-10-22 00:00:00"),
                        DateHelper.t("2013-10-25 00:00:00"));

        assertThat(line.data.size(), is(3));
        Object[] row1 = line.data.get(0);
        assertThat((Long) row1[0], is(1382371200000l));
        assertThat((Float) row1[1], is(19f));

        Object[] row2 = line.data.get(1);
        assertThat((Long) row2[0], is(1382457600000l));
        assertThat((Float) row2[1], is(17f));


        Object[] row3 = line.data.get(2);
        assertThat((Long) row3[0], is(1382544000000l));
        assertThat((Float) row3[1], is(9f));
    }

    @Test
    public void testMSkuSalesAndUnitsUS() {
        Series.Line line = esQuery.mskuSalesAndUnits("82WSWIZARD-B8800,632672530648", M.AMAZON_US,
                DateHelper.t("2013-10-22 00:00:00"),
                DateHelper.t("2013-10-25 00:00:00"));

        assertThat(line.data.size(), is(3));
        Object[] row1 = line.data.get(0);
        assertThat((Long) row1[0], is(1382371200000l));
        assertThat((Float) row1[1], is(5f));

        Object[] row2 = line.data.get(1);
        assertThat((Long) row2[0], is(1382457600000l));
        assertThat((Float) row2[1], is(4f));


        Object[] row3 = line.data.get(2);
        assertThat((Long) row3[0], is(1382544000000l));
        assertThat((Float) row3[1], is(3f));
    }

    @Test
    public void testCatSalesAndUnitsDE() {
        Series.Line line = esQuery.catSalesAndUnits("80", M.AMAZON_DE,
                DateHelper.t("2013-10-22 00:00:00"),
                DateHelper.t("2013-10-25 00:00:00"));

        assertThat(line.data.size(), is(3));
        Object[] row1 = line.data.get(0);
        assertThat((Long) row1[0], is(1382371200000l));
        assertThat((Float) row1[1], is(351f));

        Object[] row2 = line.data.get(1);
        assertThat((Long) row2[0], is(1382457600000l));
        assertThat((Float) row2[1], is(336f));


        Object[] row3 = line.data.get(2);
        assertThat((Long) row3[0], is(1382544000000l));
        assertThat((Float) row3[1], is(276f));
    }

    @Test
    public void testCatSalesAndUnitsUS() {
        Series.Line line = esQuery.catSalesAndUnits("80", M.AMAZON_US,
                DateHelper.t("2013-10-22 00:00:00"),
                DateHelper.t("2013-10-25 00:00:00"));

        assertThat(line.data.size(), is(3));
        Object[] row1 = line.data.get(0);
        assertThat((Long) row1[0], is(1382371200000l));
        assertThat((Float) row1[1], is(98f));

        Object[] row2 = line.data.get(1);

        assertThat((Long) row2[0], is(1382457600000l));
        assertThat((Float) row2[1], is(104f));


        Object[] row3 = line.data.get(2);
        assertThat((Long) row3[0], is(1382544000000l));
        assertThat((Float) row3[1], is(90f));
    }

    @Test
    public void testAllSalesAndUnitsDE() {
        Series.Line line = esQuery.allSalesAndUnits(M.AMAZON_DE,
                DateHelper.t("2013-10-22 00:00:00"),
                DateHelper.t("2013-10-25 00:00:00"));

        assertThat(line.data.size(), is(3));
        Object[] row1 = line.data.get(0);
        assertThat((Long) row1[0], is(1382371200000l));
        assertThat((Float) row1[1], is(1566f));

        Object[] row2 = line.data.get(1);
        assertThat((Long) row2[0], is(1382457600000l));
        assertThat((Float) row2[1], is(1605f));


        Object[] row3 = line.data.get(2);
        assertThat((Long) row3[0], is(1382544000000l));
        assertThat((Float) row3[1], is(1447f));
    }

    @Test
    public void testAllSalesAndUnitsUS() {
        Series.Line line = esQuery.allSalesAndUnits(M.AMAZON_US,
                DateHelper.t("2013-10-22 00:00:00"),
                DateHelper.t("2013-10-25 00:00:00"));

        assertThat(line.data.size(), is(3));
        Object[] row1 = line.data.get(0);
        assertThat((Long) row1[0], is(1382371200000l));
        assertThat((Float) row1[1], is(424f));

        Object[] row2 = line.data.get(1);
        assertThat((Long) row2[0], is(1382457600000l));
        assertThat((Float) row2[1], is(313f));


        Object[] row3 = line.data.get(2);
        assertThat((Long) row3[0], is(1382544000000l));
        assertThat((Float) row3[1], is(362f));
    }

    @Test
    //TODO
    public void testCategoryPieUS() {
        Series.Pie pie = esQuery.categoryPie(M.AMAZON_US,
                DateHelper.t("2013-10-25 00:00:00"),
                DateHelper.t("2013-10-26 00:00:00"));

        assertThat(pie.data.size(), is(8));

        for(Object[] data : pie.data) {
            String key = data[0].toString();
            Float val = (Float) data[1];
            if(key.equals("11"))
                assertThat(val, is(93f));
            else if(key.equals("70"))
                assertThat(val, is(42f));
            else if(key.equals("71"))
                assertThat(val, is(6f));
            else if(key.equals("72"))
                assertThat(val, is(9f));
            else if(key.equals("73"))
                assertThat(val, is(42f));
            else if(key.equals("80"))
                assertThat(val, is(87f));
            else if(key.equals("82"))
                assertThat(val, is(5f));
            else if(key.equals("88"))
                assertThat(val, is(31f));
        }
    }
}
