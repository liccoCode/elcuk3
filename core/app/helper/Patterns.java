package helper;

import java.util.regex.Pattern;

/**
 * Created by IntelliJ IDEA.
 * User: Wyatt
 * Date: 12-1-10
 * Time: 下午2:52
 */
public class Patterns {
    /**
     * A2Z 的全字母正则表达式
     */
    public static final Pattern A2Z = Pattern.compile("[a-zA-Z]*");

    /**
     * 全是数字
     */
    public static final Pattern Nub = Pattern.compile("[0-9]*");
}
