package helper;

import models.ElcukRecord;
import models.embedded.ERecordBuilder;
import notifiers.Mails;
import org.junit.BeforeClass;
import org.junit.Test;
import play.test.Fixtures;
import play.test.UnitTest;

/**
 * Created by IntelliJ IDEA.
 * User: wyatt
 * Date: 1/14/13
 * Time: 2:56 PM
 */
public class ERecordBuilderTest extends UnitTest {
    @BeforeClass
    public static void setUPClass() {
        Fixtures.deleteDatabase();
    }

    @Test
    public void testSaveRecord() {
        String from = "wyatt@easya.cc";
        String to = "wppurking@gmail.com";
        new ERecordBuilder().mail()
                .msgArgs(from, to)
                .fid(Mails.CLEARANCE).save();

        assertEquals(1l, ElcukRecord.count(), 0);

        ElcukRecord record = ElcukRecord.all().first();
        assertEquals(String.format("Email From %s to %s", from, to), record.message);
        assertEquals(Mails.CLEARANCE, record.fid);
    }
}
