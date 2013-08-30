package models;

import com.google.gson.annotations.Expose;
import org.apache.commons.lang.StringUtils;
import play.data.validation.Required;
import play.db.jpa.GenericModel;

import javax.persistence.*;
import java.util.Date;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * 用户的提醒消息(push)
 * User: wyattpan
 * Date: 10/26/12
 * Time: 11:55 AM
 */
@Entity
@org.hibernate.annotations.Entity(dynamicUpdate = true)
public class Notification extends GenericModel {
    public static final int SERVICE = 1;
    public static final int PROCURE = 2;
    public static final int SHIPPER = 3;
    public static final int PM = 4;

    public Notification() {
        this.createAt = new Date();
    }

    public Notification(User user, String title, String content) {
        this();
        this.user = user;
        this.title = title;
        this.content = content;
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
     * 创建时间
     */
    @Expose
    public Date createAt;

    /**
     * 什么是否被通知了
     */
    public Date notifyAt;

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
            new Notification(u, title, content).save();
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
        return notify;
    }

    /**
     * 向某些用户 通知信息
     */
    public static void notifiesToUsers(String title, String content, User... users) {
        for(User u : users) {
            new Notification(u, title, content).save();
        }
    }

    /**
     * 通知某一个组(1: service group, 2: procure group, 3: shipper group, 4: PM group)
     *
     * @param group   1: service group, 2: procure group, 3: shipper group, 4: PM group
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
        }
    }

    /**
     * 系统消息
     *
     * @param group   1: service group, 2: procure group, 3: shipper group, 4: PM group
     * @param content
     */
    public static void notifies(String content, int... group) {
        notifies(null, content, group);
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

    public static void changState(List<Long> ids) {
        for(Long id : ids) {
            Notification notification = Notification.findById(id);
            notification.changState(Notification.S.CHECKED);
        }
    }
}
