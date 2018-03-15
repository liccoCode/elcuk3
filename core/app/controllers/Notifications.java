package controllers;

import controllers.api.SystemOperation;
import helper.GTs;
import helper.J;
import models.Notification;
import models.User;
import models.activiti.ActivitiProcess;
import models.view.Ret;
import models.view.post.NotificationPost;
import org.apache.commons.lang.StringUtils;
import play.libs.F;
import play.mvc.Controller;
import play.mvc.With;

import java.util.List;
import java.util.stream.Collectors;

/**
 * 每个用户的 Notification
 * User: wyattpan
 * Date: 10/26/12
 * Time: 3:12 PM
 */
@With({GlobalExceptionHandler.class, Secure.class, SystemOperation.class})
public class Notifications extends Controller {

    // TODO 取消这个 @Check("notifications.notifys")

    @Check("notifications.notifysall")
    public static void notifysAll(String t, String c) {
        if(StringUtils.isBlank(c)) {
            flash.error("请填写 Notification 内容!");
            redirect("/users/home");
        }
        Notification.newNoty(t, c, Notification.INDEX).notifiAll();
        flash.success("发送成功");
        redirect("/users/home");
    }

    /**
     * 当前用户的通知信息列表
     */
    public static void index(NotificationPost p) {
        if(p == null) p = new NotificationPost();
        List<Notification> notifications = p.query();

        render(notifications, p);
    }

    /**
     * 计算当前用户的通知信息的数量
     */
    public static void amount() {
        //改成未执行流程的数量
        List<String> tasks = ActivitiProcess.userTask(User.username());
        long count = tasks.size();
        renderJSON(GTs.MapBuilder
                .map("user", User.username())
                .put("count", count + "")
                .build()
        );
    }

    /**
     * 修改通知状态为 已阅
     */
    public static void updateState(List<Long> noteID) {
        if(noteID != null) {
            Notification.markAsRead(noteID);
        } else {
            renderJSON(new Ret("未选中，无法更新状态"));
        }
        renderJSON(new Ret(true, "通知已标记为已读"));
    }

    /**
     * 查看源链接并且标记已读
     *
     * @param id
     */
    public static void viewSource(Long id) {
        Notification noty = Notification.findById(id);
        if(noty.type == Notification.T.PERSONAL) {
            if(noty.state != Notification.S.CHECKED) {
                noty.changState(Notification.S.CHECKED);
            }
        } else {
            Long userId = Login.current().id;
            StringBuilder sb = new StringBuilder(noty.readUser);
            noty.readUser = sb.append("," + userId).toString();
            noty.save();
        }
        redirect(noty.sourceURL);
    }

    /**
     * 下一个通知
     */
    public static void nextNotification() {
        F.Option<Notification> notification = Notification.next(Login.current());
        if(notification.isDefined())
            renderJSON(J.g(notification.get()));
        else
            renderJSON(new Ret(false));
    }

    public static int notifyNum() {
        Long id = Login.current().id;
        List<Notification> personals = Notification.find("user.id=? AND state=?", id, Notification.S.UNCHECKED)
                .fetch();
        int num = personals.size();
        List<Notification> systems = Notification.find("state=? AND type=?",
                Notification.S.UNCHECKED, Notification.T.SYSTEM).fetch();
        List<Notification> unReads = systems.stream().filter(system -> !Notification.containUser(id, system.readUser))
                .collect(Collectors.toList());
        int totalUnReadNum = num + unReads.size();
        return totalUnReadNum;
    }

    public static List<Notification> unReadNotify() {
        Long id = Login.current().id;
        List<Notification> personals = Notification.find("user.id=? AND state=?", id, Notification.S.UNCHECKED)
                .fetch();
        List<Notification> systems = Notification.find("state=? AND type=?",
                Notification.S.UNCHECKED, Notification.T.SYSTEM).fetch();
        List<Notification> unReads = systems.stream().filter(system -> !Notification.containUser(id, system.readUser))
                .collect(Collectors.toList());
        unReads.addAll(personals);
        return unReads;
    }

}
