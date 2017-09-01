package controllers.api;

import controllers.Login;
import models.Notification;
import models.OperatorConfig;
import models.User;
import play.mvc.Before;
import play.mvc.Controller;

import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

/**
 * Created by IntelliJ IDEA.
 * User: mac
 * Date: 14-10-17
 * Time: 下午3:07
 */
public class SystemOperation extends Controller {

    @Before(unless = {"login", "authenticate", "logout"})
    static void monitBefore() throws Throwable {
        //这里应该是用于记录整个应用所有 Controller 的数据统计信息.
        boolean isB2B = Objects.equals(OperatorConfig.getVal("brandname"), User.COR.MengTop.name());
        Long id = Login.current().id;
        List<Notification> personals = Notification.find("user.id=? AND state=?", id, Notification.S.UNCHECKED)
                .fetch();
        int num = personals.size();
        List<Notification> systems = Notification.find("state=? AND type=?",
                Notification.S.UNCHECKED, Notification.T.SYSTEM).fetch();
        List<Notification> unReads = systems.stream().filter(system -> Notification.containUser(id, system.readUser))
                .collect(Collectors.toList());
        int totalUnReadNum = num + unReads.size();

        renderArgs.put("personals", personals);
        renderArgs.put("unReads", unReads);
        renderArgs.put("totalUnReadNum", totalUnReadNum);

        renderArgs.put("isB2B", isB2B);

    }
}

