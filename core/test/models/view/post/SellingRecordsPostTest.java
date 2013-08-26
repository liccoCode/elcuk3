package models.view.post;

import factory.FactoryBoy;
import models.market.M;
import models.market.SellingRecord;
import org.junit.Before;
import org.junit.Test;
import play.test.UnitTest;

import java.io.FileInputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

import static org.hamcrest.core.Is.is;
import static org.hamcrest.number.IsCloseTo.closeTo;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.spy;

/**
 * Created by IntelliJ IDEA.
 * User: wyatt
 * Date: 8/21/13
 * Time: 10:04 AM
 */
public class SellingRecordsPostTest extends UnitTest {
    public List<SellingRecord> records;

    @Before
    public void setUp() throws IOException, ClassNotFoundException {
        records = null;
        FactoryBoy.deleteAll();
        ObjectInputStream oi = new ObjectInputStream(
                // 这是 7.2 号的 80 销售财务数据
                new FileInputStream("./test/models/view/post/sellings"));
        records = (List<SellingRecord>) oi.readObject();
        Collections.sort(this.records, new Comparator<SellingRecord>() {
            @Override
            public int compare(SellingRecord o1, SellingRecord o2) {
                return o1.selling.sellingId.compareTo(o2.selling.sellingId);
            }
        });
//        for(SellingRecord rcd : records) {
//            System.out.println(rcd.selling.sellingId + ":" + rcd);
//        }
    }

    @Test
    public void testRecordToSKU() {
        SellingRecordsPost post = spy(new SellingRecordsPost());
        doReturn(this.records).when(post).records();
        assertThat(this.records.hashCode(), is(post.records().hashCode()));

        List<SellingRecord> afterRecords = post.recordsToSKU(this.records);
        assertThat(afterRecords.size(), is(19));
        for(SellingRecord rcd : afterRecords) {
            if(!rcd.selling.sellingId.equals("80DBK12000-AB")) continue;
            assertThat(rcd.units, is(294));
            assertThat((double) rcd.sales, closeTo(14530.2114, 0.2));
            assertThat((double) rcd.income, closeTo(11564.7817, 0.2));
            assertThat((double) rcd.profit, closeTo(9948.8510, 0.2));
            // TODO 还有其他需要计算的值需要测试
        }
        post.market = M.AMAZON_US.name();
        assertThat(post.recordsToSKU(this.records).size(), is(15));
    }

    @Test
    public void testRecordToCategory() {
        SellingRecordsPost post = spy(new SellingRecordsPost());
        doReturn(this.records).when(post).records();
        assertThat(this.records.hashCode(), is(post.records().hashCode()));
        List<SellingRecord> afterRecords = post.recordToCategory(this.records);
        assertThat(afterRecords.size(), is(1));

        SellingRecord rcd = afterRecords.get(0);
        assertThat(rcd.units, is(827));
        assertThat((double) rcd.sales, closeTo(34389.547, 0.2));
        assertThat((double) rcd.profit, closeTo(25478.893, 0.2));
    }
}
