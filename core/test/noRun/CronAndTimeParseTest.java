package noRun;

import ext.ProcuresHelper;
import helper.Dates;
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

    @Test
    public void testCronEveryWeekOne() {
        String cron = "0 40 1 1,8,15,22,29 * ?";
        System.out.println(Dates.date2DateTime(Time.parseCRONExpression(cron)));
    }

    @Test
    public void testCronEveryThreeDay() {
        String cron = "0 10 0 */3 * ?";
        System.out.println(Dates.date2DateTime(Time.parseCRONExpression(cron)));
    }

    @Test
    public void testCronEveryDay() {
        String cron = "0 20 0 * * ?";
        System.out.println(Dates.date2DateTime(Time.parseCRONExpression(cron)));
    }

    @Test
    public void testColor() {
        System.out.println(Integer.toHexString("FBA5KCDKZ".hashCode()));
        System.out.println(Integer.toHexString("FBA5RF72D".hashCode()));
        System.out.println(Integer.toHexString("FBA5K".hashCode()));
        System.out.println(Integer.toHexString("CDKZ".hashCode()));

        String fba = "FBA5RF72D";
        String prefix = fba.substring(0, 5);
        String suffix = fba.substring(5);
        System.out.println(prefix);
        System.out.println(suffix);

        System.out.println("-------------------");
    }

}
