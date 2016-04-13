package helper;

/**
 * 一些常量
 * User: wyattpan
 * Date: 3/11/12
 * Time: 8:35 PM
 */
public class Constant {
    /**
     * 操作系统的 HOME 目录
     */
    public static final String HOME = System.getProperty("user.home");

    /**
     * 系统中产生的 "数据" 类型的文件
     */
    public static final String E_DATE = String.format("%s/elcuk2-data", Constant.HOME);

    /**
     * 系统中产生的 "日志" 类型的文件
     */
    public static final String E_LOGS = String.format("%s/elcuk2-logs", Constant.HOME);

    /**
     * 存在系统的 "数据" 文件夹中的 Finance 数据
     */
    public static final String D_FINANCE = String.format("%s/finance", E_DATE);

    /**
     * 存在系统的 "日志" 文件夹中的 Selling Deploye 异常 html 页面
     */
    public static final String L_SELLING = String.format("%s/selling_deploy", E_LOGS);

    /**
     * 存在系统的 "日志" 文件夹中的 Listing 上架(saleAmazon) 异常 html 页面
     */
    public static final String L_LISTING = String.format("%s/listing_sale", E_LOGS);

    /**
     * 存在系统的 "日志" 文件夹中的模拟 Account 登陆页面的日志
     */
    public static final String L_LOGIN = String.format("%s/login", E_LOGS);

    /**
     * 存在系统的 "日志" 文件夹中的 Listing 上传图片的异常 html 页面
     */
    public static final String L_IMAGEUPLOAD = String.format("%s/image_upload", E_LOGS);

    /**
     * 存在系统的 "日志" 文件夹中 Selling 读取 SellingRecord 销量数据的异常 json 值
     */
    public static final String S_RECORDS = String.format("%s/selling_records", E_LOGS);

    /**
     * 存在系统的 "日志" 文件夹中 Jobs 各项任务的日志文件
     */
    public static final String J_LOGS = String.format("%s/jobs_log", E_LOGS);

    /**
     * 上传的文件的地址
     */
    public static final String UPLOAD_PATH = String.format("%s/uploads", Constant.E_DATE);

    public static final String HTTP_PATH = String.format("%s/http", Constant.E_DATE);

    /**
     * Amazon FBA Label 的地址
     */
    public static final String LABEL_PATH = String.format("%s/label_path", Constant.E_DATE);

    public static final String TMP = System.getProperty("java.io.tmpdir");

    /**
     * 系统自己的 url
     */
    public static final String ROOT_URL = "ROOT_URL";

    /**
     * 系统中产生的 "数据" 类型的文件
     */
    public static final String REPORT_PATH = String.format("%s/elcuk2-report", Constant.HOME);

    /**
     * Rockend HOST 环境变量
     */
    public static final String ROCKEND_HOST = "ROCKEND_HOST";
}
