package ext;

import play.templates.JavaExtensions;

/**
 * Created by IntelliJ IDEA.
 * User: mac
 * Date: 14-4-3
 * Time: PM2:54
 */
public class PmDashBoardsHelper extends JavaExtensions {
    /**
     * 根据差值 返回不同颜色来表示差值程度
     *
     * @param difference
     * @return
     */
    public static String diffRGB(float difference) {
        if(difference <= 0.15) return "#FE502A";
        if(difference <= 0.3) return "#FF0000";
        return "#8b0000";
    }
}
