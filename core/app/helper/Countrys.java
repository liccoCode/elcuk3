package helper;

/**
 * 国家名称帮助类
 * <p/>
 * Created by IntelliJ IDEA.
 * User: DyLanM
 * Date: 13-9-4
 * Time: 下午11:29
 */
public class Countrys {

    public static String countryName(String countryCode) {
        if(countryCode.equals("GB")) return "英国";
        else if(countryCode.equals("US")) return "美国";
        else if(countryCode.equals("CA")) return "加拿大";
        else if(countryCode.equals("CN")) return "中国";
        else if(countryCode.equals("DE")) return "德国";
        else if(countryCode.equals("FR")) return "法国";
        else if(countryCode.equals("IT")) return "意大利";
        else if(countryCode.equals("JP")) return "日本";
        return "";
    }

}
