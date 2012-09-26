package noRun;

import org.junit.Assert;
import org.junit.Test;

import java.util.Scanner;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Created by IntelliJ IDEA.
 * User: wyattpan
 * Date: 8/22/12
 * Time: 4:19 PM
 */
public class RegexTest {
    @Test
    public void testPlusAndNumber() {
        // 测试 +2 +12 这种类型
        Pattern pa = Pattern.compile("^\\+(\\d+)$");
        Assert.assertEquals(true, pa.matcher("+2").matches());
        Assert.assertEquals(true, pa.matcher("+223").matches());
        Assert.assertEquals(false, pa.matcher("+223 sdfj").matches());
        Matcher ma = pa.matcher("+223");
        if(ma.matches()) {
            Assert.assertEquals("223", ma.group(1));
        }
    }

    @Test
    public void deliveryIdPattern() {
        Pattern ID = Pattern.compile("^(\\w{2}\\|\\d{6}\\|\\d{2})$");
        String str = "DL|201209|17";
        Matcher matcher = ID.matcher(str);
        matcher.find();
        Assert.assertEquals(str, matcher.group(1));
    }
}
