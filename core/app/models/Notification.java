package models;

import com.google.gson.annotations.Expose;
import controllers.Login;
import org.apache.commons.lang.StringUtils;
import play.data.validation.Required;
import play.db.jpa.Model;
import play.libs.F;

import javax.persistence.Entity;
import javax.persistence.ManyToOne;
import javax.persistence.PrePersist;
import java.util.*;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.LinkedBlockingQueue;

/**
 * 用户的提醒消息(push)
 * User: wyattpan
 * Date: 10/26/12
 * Time: 11:55 AM
 */
@Entity
@org.hibernate.annotations.Entity(dynamicUpdate = true)
public class Notification extends Model {
    public static final int SERVICE = 1;
    public static final int PROCURE = 2;
    public static final int SHIPPER = 3;
    public static final int PM = 4;
    /**
     * 用来记录用户 Notification 的 Queue Map
     */
    private static final Map<String, BlockingQueue<Notification>> USER_QUEUE_CACHE = new ConcurrentHashMap<String, BlockingQueue<Notification>>();


    public Notification() {
        this.createAt = new Date();
    }

    public Notification(User user, String title, String content) {
        this();
        this.user = user;
        this.title = title;
        this.content = content;
    }

    @ManyToOne
    public User user;

    @Expose
    @Required
    public String title;

    /**
     * 提醒消息的内容
     */
    @Expose
    @Required
    public String content;

    /**
     * 创建时间
     */
    @Expose
    public Date createAt;

    /**
     * 什么是否被通知了
     */
    public Date notifyAt;

    @PrePersist
    public void prePersist() {
        if(StringUtils.isBlank(this.title))
            this.title = "系统通知";
    }

    /**
     * 返回当前实例的简单内容
     *
     * @return
     */
    public Notification note() {
        Notification note = new Notification();
        note.id = this.id;
        note.title = this.title;
        note.content = this.content;
        note.createAt = this.createAt;
        note.notifyAt = this.notifyAt;
        return note;
    }

    public static void notificationAll(String title, String content) {
        List<User> users = User.openUsers();
        for(User u : users) {
            Notification.addUserNotification(u, new Notification(u, title, content).<Notification>save().note());
        }
    }

    /**
     * 创建一个 Notification
     *
     * @param user
     * @return
     */
    public static Notification notifies(User user, String content) {
        Notification notify = new Notification(user, null, content);
        Notification.addUserNotification(user, notify.<Notification>save());
        return notify;
    }

    /**
     * 通知某一个组(1: service group, 2: procure group, 3: shipper group, 4: PM group)
     *
     * @param group  1: service group, 2: procure group, 3: shipper group, 4: PM group
     * @param title
     * @param content
     * @return
     */
    public static void notifies(String title, String content, int... group) {
        Set<User> users = new HashSet<User>();
        for(int i : group) {
            if(i == SERVICE)
                users.addAll(User.find("isService=?", true).<User>fetch());
            else if(i == PROCURE)
                users.addAll(User.find("isProcure=?", true).<User>fetch());
            else if(i == SHIPPER)
                users.addAll(User.find("isShipper=?", true).<User>fetch());
            else if(i == PM)
                users.addAll(User.find("isPM=?", true).<User>fetch());
        }

        for(User u : users) {
            Notification notify = new Notification(u, title, content).save();
            Notification.addUserNotification(u, notify.note());
        }
    }

    /**
     * 系统消息
     *
     * @param group  1: service group, 2: procure group, 3: shipper group, 4: PM group
     * @param content
     */
    public static void notifies(String content, int... group) {
        notifies(null, content, group);
    }

    /**
     * 获取下一个通知.(每次一个)
     * 首先查找 Queue Cache, 如果 Queue 中用, 则返回 Queue 中的.
     * 如果 Queue Cache 没缓存, 重新初始化 Queue, 再返回 Queue 中的
     *
     * @return
     */
    public static F.Option<Notification> next(User user) {
        if(!USER_QUEUE_CACHE.containsKey(user.username))
            Notification.initUserNotificationQueue(user);

        Notification note = USER_QUEUE_CACHE.get(user.username).poll();
        if(note == null) return F.Option.None();
        else {
            Notification managedNote = Notification.findById(note.id);
            managedNote.notifyAt = new Date();
            return F.Option.Some(managedNote.<Notification>save());
        }
    }

    /**
     * 想缓存的 User Queue Cache 中添加 Notification
     *
     * @param user
     * @param notification
     */
    public static void addUserNotification(User user, Notification notification) {
        if(!USER_QUEUE_CACHE.containsKey(user.username) && Login.isUserLogin(user)) {
            // 登陆的用户才初始化 User Queue Cache, 没有登陆的, 等到登陆再重新缓存
            Notification.initUserNotificationQueue(user);
        }
        if(Login.isUserLogin(user))
            USER_QUEUE_CACHE.get(user.username).add(notification);
    }

    public static void initUserNotificationQueue(User user) {
        if(!USER_QUEUE_CACHE.containsKey(user.username)) {
            synchronized(USER_QUEUE_CACHE) {
                // double check
                if(USER_QUEUE_CACHE.containsKey(user.username))
                    return;

                BlockingQueue<Notification> blockingQueue = new LinkedBlockingQueue<Notification>();
                List<Notification> notifications = user.unNotifiedNotification();
                for(Notification note : notifications)
                    blockingQueue.add(note);
                USER_QUEUE_CACHE.put(user.username, /*不限制 Notification Queue 容量*/blockingQueue);
            }
        }
    }

    public static void clearUserNotificationQueue(User user) {
        USER_QUEUE_CACHE.remove(user.username);
    }
}
