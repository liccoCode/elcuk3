package models;

import helper.Dates;
import notifiers.FBAMails;
import notifiers.Mails;
import notifiers.SystemMails;
import play.data.validation.Required;
import play.db.jpa.Model;
import query.ElcukRecordQuery;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Lob;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Map;

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


    /**
     * 邮件发送的情况
     *
     * @param from
     * @param to
     * @return
     */
    public static List<Map<String, List<Integer>>> emailOverView(Date from, Date to) {
        List<String> lines = new ArrayList<String>();
        lines.add(Mails.CLEARANCE);
        lines.add(Mails.REVIEW_US);
        lines.add(Mails.REVIEW_UK);
        lines.add(Mails.REVIEW_DE);
        lines.add(Mails.FEEDBACK_WARN);
        lines.add(Mails.REVIEW_WARN);
        lines.add(Mails.IS_DONE);
        lines.add(Mails.FNSKU_CHECK);
        lines.add(Mails.MORE_OFFERS);

        lines.add(SystemMails.DAILY_FEEDBACK);
        lines.add(SystemMails.DAILY_REVIEW);
        lines.add(SystemMails.SKU_PIC_CHECK);

        lines.add(FBAMails.NOT_RECEING);
        lines.add(FBAMails.RECEIVING_CHECK);
        lines.add(FBAMails.STATE_CHANGE);

        List<Map<String, List<Integer>>> mailLines = new ArrayList<Map<String, List<Integer>>>();
        //TODO 需要对加载出来的数据做处理
        for(String lineType : lines) {
            mailLines.add(new ElcukRecordQuery().emails(
                    Dates.morning(from), Dates.night(to), lineType));
        }
        return mailLines;
    }

    public static List<ElcukRecord> records(String fid) {
        return ElcukRecord.find("fid=? ORDER BY createAt DESC", fid).fetch();
    }

    public static List<ElcukRecord> records(String fid, String action) {
        return ElcukRecord.find("fid=? AND action=? ORDER BY createAt DESC", fid, action).fetch();
    }

    public static JPAQuery fid(String fid) {
        return ElcukRecord.find("fid=? ORDER BY createAt DESC", fid);
    }


    @Override
    public String toString() {
        final StringBuilder sb = new StringBuilder();
        sb.append("ElcukRecord");
        sb.append("{message='").append(message).append('\'');
        sb.append(", action='").append(action).append('\'');
        sb.append(", fid='").append(fid).append('\'');
        sb.append(", username='").append(username).append('\'');
        sb.append(", createAt=").append(createAt);
        sb.append('}');
        return sb.toString();
    }
}
