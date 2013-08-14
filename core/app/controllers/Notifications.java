package controllers;

import helper.J;
import models.Notification;
import models.view.Ret;
import org.apache.commons.lang.StringUtils;
import play.libs.F;
import play.mvc.Controller;
import play.mvc.With;

import java.util.List;

/**
 * 每个用户的 Notification
 * User: wyattpan
 * Date: 10/26/12
 * Time: 3:12 PM
 */
@With({GlobalExceptionHandler.class, Secure.class})
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
        if(StringUtils.isBlank(c)) {
            flash.error("请填写 Notification 内容!");
            redirect("/users/home");
        }
        Notification.notificationAll(t, c);
        flash.success("发送成功");
        redirect("/users/home");
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

    /**
     * 当前用户的通知信息列表
     */
    public static void index(){

        renderArgs.put("notifications", Login.current().notificationFeeds(1));

        render();
    }

    /**
     * 计算当前用户的通知信息的数量
    */
    public static void amount(){

        renderText(  Notification.count("user=? and state = 'UNCHECKED' ",Login.current() ) );
    }

    /**
     *
     * 修改通知状态为 已阅
     */
    public static void  updateState(List<Long> noteIDs){

       if( noteIDs != null ){
         for( Long tempNoteID : noteIDs ) {

            Notification temp = Notification.findById( tempNoteID );
            temp.state = Notification.S.CHECKED;
            temp.save();
         }
       }

       index();
    }

}
