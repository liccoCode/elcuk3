package noRun;

import org.junit.Test;
import play.libs.Time;

/**
 * Created by IntelliJ IDEA.
 * User: wyattpan
 * Date: 5/30/12
 * Time: 10:02 AM
 */
public class CronAndTimeParseTest {
    @Test
    public void testTime() {
        String cron = "0 40 0,23 * * ?";
        String duration = "23h";
        Time.parseDuration(cron);
        System.out.println(Time.parseDuration(duration));
//        Time.CronExpression
        System.out.println(Time.parseCRONExpression(cron));
    }
}
