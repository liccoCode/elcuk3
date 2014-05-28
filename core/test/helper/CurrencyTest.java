package helper;

import org.joda.time.DateTime;
import org.junit.BeforeClass;
import org.junit.Test;
import play.test.UnitTest;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by IntelliJ IDEA.
 * User: wyatt
 * Date: 3/29/13
 * Time: 5:02 PM
 */
public class CurrencyTest extends UnitTest {

    @Test
    public void testTime(){
        java.util.Date a  = DateTime.now().withHourOfDay(6).withMinuteOfHour(0).withSecondOfMinute(0).toDate();
        System.out.println("aaa"+a.toString());
    }
}
