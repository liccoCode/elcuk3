package helper;

import models.market.Selling;
import org.apache.commons.lang.StringUtils;
import play.Play;
import play.templates.JavaExtensions;

/**
 * Created by IntelliJ IDEA.
 * User: mac
 * Date: 13-12-17
 * Time: 下午4:53
 */
public class ImageUrls extends JavaExtensions {

    /**
     * 生成该Selling对象全部图片的URL集合
     * 注：上架的时候, 如果系统中的 Product 没有图片, main_image 使用 Amazon 笑脸图, 其他的不考虑
     *
     * @param selling
     * @return 图片URL，顺序为(如果有的话)main,1,2,3,4,5...
     */
    public static String generateSellingImageURL(Selling selling, int target) {
        String returnStr = "";
        String imageUrl = Play.configuration.getProperty("application.baseUrl") + "attachs/image?a.fileName=";
        String[] imageNames = StringUtils.splitByWholeSeparator(selling.aps.imageName, Webs.SPLIT);
        if(imageNames != null) {
            if(imageNames.length > 0 && imageNames.length > target) {
                returnStr = imageUrl + imageNames[target];
            }
        } else if(imageNames == null && target == 0) {
            returnStr = Play.configuration.getProperty("application.baseUrl")+"images/amazon-logo.png";
        }
        return returnStr;
    }
}
