package models;

import com.google.gson.annotations.Expose;
import org.apache.commons.lang.StringUtils;
import org.hibernate.annotations.DynamicUpdate;
import play.data.validation.Required;
import play.db.DB;
import play.db.helper.SqlSelect;
import play.db.jpa.GenericModel;
import play.libs.F;

import javax.persistence.*;
import java.util.Date;
import java.util.List;
import java.util.Map;
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
@DynamicUpdate
public class Notification extends GenericModel {
    /**
     * 用来记录用户 Notification 的 Queue Map
     */
    private static final Map<String, BlockingQueue<Notification>> USER_QUEUE_CACHE = new ConcurrentHashMap<>();

    public static final String INDEX = "http://e.easya.cc/Notifications/index";

    private static final long serialVersionUID = -3890283395796257638L;

    public Notification() {
        this.createAt = new Date();
    }

    public Notification(User user, String title, String content, String sourceURL) {
        this();
        this.user = user;
        this.title = title;
        this.content = content;
        this.sourceURL = sourceURL;
        clearUserNotificationQueue(user);
    }

    @Id
    @GeneratedValue
    @Expose
    public Long id;

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
    @Lob
    public String content;

    /**
     * 触发消息的源头 URL
     */
    @Expose
    public String sourceURL;

    /**
     * 创建时间
     */
    @Expose
    public Date createAt;

    /**
     * 是否阅读 通知
     */
    @Enumerated(EnumType.STRING)
    public S state = S.UNCHECKED;

    public enum S {
        CHECKED {
            @Override
            public String label() {
                return "已阅";
            }
        },
        UNCHECKED {
            @Override
            public String label() {
                return "未查看";
            }
        };

        public abstract String label();

    }

    @PrePersist
    public void prePersist() {
        if(StringUtils.isBlank(this.title))
            this.title = "系统通知";
    }

    /**
     * 通知所有用户. 为每一个打开的用户添加一条 Notification 记录
     */
    public void notifiAll() {
        List<User> users = User.openUsers();
        notifySomeone(users.toArray(new User[users.size()]));
    }

    /**
     * 通知某些人. 为指定用户添加 Notification 记录
     *
     * @param users
     */
    public void notifySomeone(User... users) {
        for(User u : users) {
            if(u.equals(this.user)) continue;
            Notification.newNotyDiffUser(this, u).save();
        }
    }

    /**
     * Copy 出一个新的 Notification 对象
     */
    public static Notification newNotyDiffUser(Notification oldNoty, User user) {
        return new Notification(user, oldNoty.title, oldNoty.content, oldNoty.sourceURL);
    }

    public static Notification newNoty(String title, String content, String sourceURL, User user) {
        return new Notification(user, title, content, sourceURL);
    }

    public static Notification newNoty(String title, String content, String sourceURL) {
        return newNoty(title, content, sourceURL, null);
    }

    public static Notification newSystemNoty(String content, String sourceURL, User user) {
        return newNoty("系统消息", content, sourceURL, user);
    }

    public static Notification newSystemNoty(String content, String sourceURL) {
        return newSystemNoty(content, sourceURL, null);
    }

    public static Notification newNotyMe(String title, String content, String sourceURL) {
        return newNoty(title, content, sourceURL, User.current());
    }

    /**
     * 更改 通知的状态
     *
     * @param state
     */
    public void changState(S state) {
        this.state = state;
        this.save();
    }

    /**
     * 将通知 变成 已读状态
     *
     * @param ids
     */
    public static void markAsRead(List<Long> ids) {
        DB.execute(String.format("UPDATE Notification set state='%s' WHERE %s",
                Notification.S.CHECKED.name(), SqlSelect.whereIn("id", ids)));
    }

    /**
     * 获取下一个通知.(每次一个)
     * 首先查找 Queue Cache, 如果 Queue 中用, 则返回 Queue 中的.
     * 如果 Queue Cache 没缓存, 重新初始化 Queue, 再返回 Queue 中的
     *
     * @return
     */
    public static F.Option<Notification> next(User user) {
        if(user == null) return F.Option.None();

        if(!USER_QUEUE_CACHE.containsKey(user.username)) {
            Notification.initUserNotificationQueue(user);
        }
        Notification note = USER_QUEUE_CACHE.get(user.username).poll();
        if(note == null) return F.Option.None();
        else {
            Notification managedNote = Notification.findById(note.id);
            managedNote.state = S.CHECKED;
            return F.Option.Some(managedNote.<Notification>save());
        }
    }

    public static void initUserNotificationQueue(User user) {
        if(!USER_QUEUE_CACHE.containsKey(user.username)) {
            synchronized(USER_QUEUE_CACHE) {
                if(USER_QUEUE_CACHE.containsKey(user.username)) return; // double check
                BlockingQueue<Notification> blockingQueue = new LinkedBlockingQueue<>();
                List<Notification> notifications = user.unNotifiedNotification();
                for(Notification note : notifications) {
                    blockingQueue.add(note);
                }
                USER_QUEUE_CACHE.put(user.username, blockingQueue);//不限制 Notification Queue 容量
            }
        }
    }

    public static void clearUserNotificationQueue(User user) {
        USER_QUEUE_CACHE.remove(user.username);
    }
}
