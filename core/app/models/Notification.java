package models;

import com.google.gson.annotations.Expose;
import org.apache.commons.lang.StringUtils;
import play.data.validation.Required;
import play.db.DB;
import play.db.helper.SqlSelect;
import play.db.jpa.GenericModel;

import javax.persistence.*;
import java.util.Date;
import java.util.List;

/**
 * 用户的提醒消息(push)
 * User: wyattpan
 * Date: 10/26/12
 * Time: 11:55 AM
 */
@Entity
@org.hibernate.annotations.Entity(dynamicUpdate = true)
public class Notification extends GenericModel {
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
        for(User user : users) {
            if(user.equals(this.user)) continue;
            Notification.newNotyDiffUser(this, user).save();
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
}
