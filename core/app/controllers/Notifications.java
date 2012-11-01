package controllers;

import helper.J;
import models.Notification;
import models.view.Ret;
import play.libs.F;
import play.mvc.Controller;
import play.mvc.With;

/**
 * 每个用户的 Notification
 * User: wyattpan
 * Date: 10/26/12
 * Time: 3:12 PM
 */
@With({GlobalExceptionHandler.class, Secure.class, GzipFilter.class})
public class Notifications extends Controller {

    @Check("notifications.notifys")
    public static void notifys(String content) {
        /**
         * 1. 将 content 中的内容根据 @xxx 解析出需要通知的人
         * 2. 对每一个人创建一个 Notification
         */
        Notification.notifies(Login.current(), content);
        renderJSON(new Ret(true));
    }

    @Check("notifications.notifysall")
    public static void notifysAll(String t, String c) {
        Notification.notificationAll(t, c);
        renderJSON(new Ret(true));
    }

    /**
     * 下一个通知
     */
    public static void nextNotification() {
        F.Option<Notification> notification = Notification.next(Login.current());
        if(notification.isDefined())
            renderJSON(J.G(notification.get()));
        else
            renderJSON(new Ret(false));
    }
}
