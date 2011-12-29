package noRun;

import org.junit.Test;
import play.libs.Time;

/**
 * Created by IntelliJ IDEA.
 * User: wyattpan
 * Date: 12/29/11
 * Time: 12:57 AM
 */
public class TimeParseTest {

    @Test
    public void testTimeParser() {
        System.out.println(Time.parseDuration("1"));
    }

}
