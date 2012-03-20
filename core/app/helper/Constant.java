package helper;

/**
 * 一些常量
 * User: wyattpan
 * Date: 3/11/12
 * Time: 8:35 PM
 */
public class Constant {
    public static final String HOME = System.getProperty("user.home");

    public static final String E_DATE = String.format("%s/elcuk2-data", Constant.HOME);

    public static final String E_FINANCE = String.format("%s/finance", E_DATE);
}
