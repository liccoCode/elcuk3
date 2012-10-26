package models;

import com.google.gson.annotations.Expose;
import org.apache.commons.lang.StringUtils;
import play.data.validation.Required;
import play.db.jpa.Model;
import play.libs.F;

import javax.persistence.Entity;
import javax.persistence.ManyToOne;
import javax.persistence.PrePersist;
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
public class Notification extends Model {
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

    public static void notificationAll(String title, String content) {
        List<User> users = User.openUsers();
        for(User u : users)
            new Notification(u, title, content).save();
    }

    /**
     * 创建一个 Notification
     *
     * @param user
     * @return
     */
    public static Notification create(User user, String content) {
        Notification notify = new Notification(user, null, content);
        return notify.save();
    }

    /**
     * 获取下一个通知.(每次一个)
     *
     * @return
     */
    public static F.Option<Notification> next(User user) {
        Notification notify = Notification.find("user=? AND notifyAt IS NULL", user).first();
        if(notify == null)
            return F.Option.None();
        else {
            notify.notifyAt = new Date();
            notify.save();
            return F.Option.Some(notify);
        }
    }
}
