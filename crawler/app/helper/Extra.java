package helper;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Created by IntelliJ IDEA.
 * User: Wyatt
 * Date: 12-1-2
 * Time: 下午5:40
 */
public class Extra {
    private static Pattern number = Pattern.compile("(-?\\d+)(\\.\\d+)?");

    public static Float flt(String input) {
        Matcher matcher = number.matcher(input);
        if(matcher.find()) {
            return Float.parseFloat(matcher.group());
        }
        return 0f;
    }
}
