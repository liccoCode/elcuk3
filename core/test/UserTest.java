import org.joda.time.DateTime;
import org.junit.Test;
import play.test.UnitTest;

import static org.hamcrest.core.Is.is;
import static org.hamcrest.number.IsCloseTo.closeTo;

/**
 * Created by IntelliJ IDEA.
 * User: mac
 * Date: 13-3-26
 * Time: 下午3:51
 */
public class UserTest extends UnitTest {

    @Test
    public void testMath() {
        System.out.println(Math.ceil((float) 50 / 3));
        int a = (int) Math.ceil((float) 50 / 3);
        assertThat(a, is(17));
        assertThat(18.238, is(closeTo(18.2, 0.1)));
    }

    @Test
    public void testDayOfWeek() {
        DateTime now = DateTime.parse("2013-08-07");
        DateTime sunday = DateTime.parse("2013-08-11");
        assertThat(now.getDayOfWeek(), is(3));
        assertThat(now.minusDays(1).getDayOfWeek(), is(2));
        assertThat(sunday.getDayOfWeek(), is(7));
        assertThat(sunday.plusDays(1).getDayOfWeek(), is(1));
    }
}
