package models;

import play.data.validation.Required;
import play.db.helper.JpqlSelect;
import play.db.jpa.Model;
import play.i18n.Messages;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Lob;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

/**
 * 系统内的操作日志的记录;
 * <p/>
 * User: wyattpan
 * Date: 7/16/12
 * Time: 11:50 AM
 */
@Entity
public class ElcukRecord extends Model {
    /**
     * 用来记录 Model 的 Record
     */
    public interface Log {
        /**
         * 返回需要记录的日志字符串
         *
         * @return
         */
        public String to_log();
    }

    public ElcukRecord(String action, String message) {
        this.action = action;
        this.message = message;
    }

    public ElcukRecord(String action, String message, String fid) {
        this(action, message);
        this.username = User.username();
        this.fid = fid;
    }

    public ElcukRecord(String action, String message, String username, String fid) {
        this(action, message, fid);
        this.username = username;
    }

    public ElcukRecord(String action, String message, String username, String fid, Date createAt) {
        this(action, message, username, fid);
        this.createAt = createAt;
    }

    @Required
    @Lob
    public String message;

    @Required
    @Column(length = 80)
    public String action;

    /**
     * 外键 model 的 IP
     */
    public String fid;

    /**
     * 用户名
     */
    @Required
    @Column(length = 80)
    public String username;

    /**
     * 日志的创建时间, 也就是记录时间
     */
    @Required
    public Date createAt = new Date();

    public static List<ElcukRecord> records(String fid) {
        return ElcukRecord.find("fid=? ORDER BY createAt DESC", fid).fetch();
    }

    public static List<ElcukRecord> records(String fid, String action) {
        return ElcukRecord.find("fid=? AND action=? ORDER BY createAt DESC", fid, action).fetch();
    }

    public static List<ElcukRecord> records(String fid, List<String> actions, int size) {
        List<String> actionMsgs = new ArrayList<String>();
        for(String action : actions) {
            actionMsgs.add(Messages.get(action));
        }
        return ElcukRecord.find("fid=? AND " +
                JpqlSelect.whereIn("action", actionMsgs) +
                " ORDER BY createAt DESC", fid).fetch(size);
    }

    public static List<ElcukRecord> records(List<String> actions, int size) {
        List<String> actionMsgs = new ArrayList<>();
        for(String action : actions) {
            actionMsgs.add(Messages.get(action));
        }
        return ElcukRecord.find(JpqlSelect.whereIn("action", actionMsgs) + " ORDER BY createAt DESC").fetch(size);
    }

    public static JPAQuery fid(String fid) {
        return ElcukRecord.find("fid=? ORDER BY createAt DESC", fid);
    }


    @Override
    public String toString() {
        final StringBuilder sb = new StringBuilder()
                .append("ElcukRecord")
                .append("{message='").append(message).append('\'')
                .append(", action='").append(action).append('\'')
                .append(", fid='").append(fid).append('\'')
                .append(", username='").append(username).append('\'')
                .append(", createAt=").append(createAt)
                .append('}');
        return sb.toString();
    }
}
