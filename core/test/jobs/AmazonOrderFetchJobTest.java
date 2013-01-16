package jobs;

import com.sun.org.apache.xerces.internal.jaxp.datatype.XMLGregorianCalendarImpl;
import helper.Dates;
import models.market.Account;
import models.market.M;
import models.market.Orderr;
import org.joda.time.DateTime;
import org.joda.time.DateTimeZone;
import org.junit.BeforeClass;
import org.junit.Test;
import play.test.Fixtures;
import play.test.UnitTest;

import javax.xml.datatype.XMLGregorianCalendar;
import java.io.File;
import java.util.List;

/**
 * Created by IntelliJ IDEA.
 * User: wyatt
 * Date: 1/15/13
 * Time: 2:04 PM
 */
public class AmazonOrderFetchJobTest extends UnitTest {
    @BeforeClass
    public static void setUp() {
        Fixtures.deleteDatabase();
        Fixtures.loadModels("/jobs/Account.yml");
    }

    @Test
    public void test() {
        Account ac = Account.all().first();
        List<Orderr> orders = AmazonOrderFetchJob.allOrderXML(
                new File("/Volumes/wyatt/Programer/repos/elcuk2/core/test/html/17329365204.txt"),
                ac);
        assertEquals(3, orders.size());
        for(Orderr ord : orders) {
            if(!ord.orderId.equals("303-7430213-7282761"))
                continue;
            //2013-01-14T22:59:59+00:00----2013-01-15 06:59:59
            System.out.println("2013-01-14T22:59:59+00:00" + "----"
                    + ord.createDate.toString());
        }
    }


    @Test
    public void toGregorianCalendar() {
        String format = "yyyy-MM-dd HH:mm:ss";
        XMLGregorianCalendar xd = XMLGregorianCalendarImpl.parse("2013-01-14T22:59:59+00:00");


        DateTime dt = new DateTime(xd.toGregorianCalendar().getTime());
//        2013-01-15 06:59:59
        System.out.println(dt.withZone(DateTimeZone.UTC).plusHours(8).toString(format));
//        2013-01-14 23:59:59
        System.out.println(dt.withZone(Dates.timeZone(M.AMAZON_DE)).toString(format));
//        2013-01-14 23:59:59
        System.out.println(new DateTime(xd.toGregorianCalendar(), Dates.timeZone(M.AMAZON_DE))
                .toString(format));
//        2013-01-15 06:59:59
        System.out.println(dt.toString(format));

        Orderr or = new Orderr();
        or.orderId = "kjsd";
        or.state = Orderr.S.PENDING;
        or.createDate = dt.withZone(Dates.timeZone(M.AMAZON_DE)).toDate();
        //TODO 查看日期是 2013-01-14 23:59:59 还是  2013-01-15 06:59:59

        or.save();
    }
}
