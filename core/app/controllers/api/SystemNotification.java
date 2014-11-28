package controllers.api;

import helper.Constant;
import models.Notification;
import models.User;
import models.market.Feed;
import org.apache.commons.lang.StringUtils;
import play.mvc.Controller;
import play.mvc.With;

/**
 * 创建系统通知
 * User: mac
 * Date: 14-11-27
 * Time: PM2:58
 */
@With(APIChecker.class)
public class SystemNotification extends Controller {
    /**
     * 为 Feed 处理结果创建系统通知
     *
     * @param id
     * @param result
     */
    public static void notifyFeedProcessResult(Long id, String result) {
        Feed feed = Feed.findById(id);
        if(StringUtils.isNotBlank(result)) {
            feed.result = result;
            feed.save();
        }
        Notification.newNoty(feed.checkResult(),
                String.format("Selling Id: %s", feed.fid), "", User.findByUserName(feed.byWho))
                .save();
        renderText("创建通知成功!");
    }
}
