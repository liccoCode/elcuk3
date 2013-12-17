package helper;

import models.market.Selling;
import play.Play;
import play.templates.JavaExtensions;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by IntelliJ IDEA.
 * User: mac
 * Date: 13-12-17
 * Time: 下午4:53
 */
public class ImageUrls extends JavaExtensions {

    /**
     * 生成该Selling对象全部图片的URL集合
     * @param selling
     * @return 图片URL，顺序为(如果有的话)main,1,2,3,4,5...
     */
    public static String generateSellingImageURL(Selling selling, int target) {
        String imageUrl = Play.configuration.getProperty("application.baseUrl") + "attachs/image?a.fileName=";
        String[] imageNames = selling.aps.imageName.split(Webs.SPLIT);
        if(imageNames.length < target) return "";
        return imageUrl + imageNames[0];
    }
}
