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

    public static final String E_ERROR = String.format("%s/error", E_DATE);

    public static final String UPLOAD_PATH = String.format("%s/uploads", Constant.E_DATE);

    public static final String TMP = System.getProperty("java.io.tmpdir");
}
