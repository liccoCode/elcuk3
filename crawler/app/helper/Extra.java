package helper;

import org.apache.commons.lang.math.NumberUtils;

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

    private static Pattern rank = Pattern.compile("(\\d)");

    public static Float flt(String input) {
        Matcher matcher = number.matcher(input);
        if(matcher.find()) {
            return Float.parseFloat(matcher.group());
        }
        return 0f;
    }

    public static Integer rank(String rankstr) {
        Matcher matcher = rank.matcher(rankstr);
        if(matcher.find()) {
            return NumberUtils.toInt(matcher.group(1));
        }
        return 0;
    }
}
