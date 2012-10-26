package jobs.promise;

import models.Notification;
import models.User;
import play.jobs.Job;
import play.libs.F;

/**
 * Notification 获取的 Promise, 使用 Jobs 的线程池, 避免使用请求线程池
 * User: wyattpan
 * Date: 10/26/12
 * Time: 5:19 PM
 */
public class NotificationPromise extends Job<F.Option<Notification>> {
    private User user;

    public NotificationPromise(User user) {
        this.user = user;
    }

    @Override
    public F.Option<Notification> doJobWithResult() {
        return Notification.next(this.user);
    }
}
