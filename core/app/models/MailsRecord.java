package models;

import com.google.gson.annotations.Expose;
import helper.Cached;
import helper.Caches;
import helper.Dates;
import models.view.highchart.HighChart;
import notifiers.FBAMails;
import notifiers.Mails;
import notifiers.SystemMails;
import org.apache.commons.lang.StringUtils;
import org.hibernate.annotations.DynamicUpdate;
import org.joda.time.DateTime;
import play.cache.Cache;
import play.db.helper.SqlQuery;
import play.db.jpa.Model;

import javax.persistence.Entity;
import javax.persistence.EnumType;
import javax.persistence.Enumerated;
import java.util.*;

/**
 * Created by IntelliJ IDEA.
 * User: rose
 * Date: 13-4-2
 * Time: 下午3:16
 */
@Entity
@DynamicUpdate
public class MailsRecord extends Model {

    private static final long serialVersionUID = 922525941133922956L;
    /**
     * 邮件名称
     */
    @Expose
    public String title;


    /**
     * 模板
     */
    @Expose
    public String templateName;


    public enum T {
        NORMAL,
        FBA,
        SYSTEM
    }

    @Expose
    @Enumerated(EnumType.STRING)
    public T type;

    @Expose
    public Date createdAt = new Date();

    /**
     * 是否发送成功
     */
    @Expose
    public boolean success;

    @Expose
    /**
     * 发件人
     */
    public String sender;

    /**
     * 收件人  多个用 ',' 号隔开
     */
    @Expose
    public String recipients;

    public MailsRecord(Map<String, Object> infos, T type, String tmpName) {
        this.title = infos.get("subject").toString();
        this.sender = infos.get("from").toString();
        List<String> list = (List<String>) infos.get("recipients");
        this.recipients = StringUtils.join(list, ",");
        this.templateName = tmpName;
        this.type = type;
    }


    /**
     * 条件查询
     *
     * @param from    开始时间
     * @param to      结束时间
     * @param type    类型
     * @param success
     * @param group   特定收件人
     * @return
     */
    @Cached("5mn")
    public static HighChart ajaxRecordBy(Date from, Date to, T type, List<String> templates, boolean success,
                                         String group) {
        DateTime _from = new DateTime(Dates.morning(from));
        DateTime _to = new DateTime(Dates.night(to));
        List<MailsRecord> records = getMailsRecords(_from.toDate(), _to.toDate(), type, templates, success, group);
        HighChart lines = new HighChart().startAt(_from.getMillis());
        DateTime travel = _from.plusDays(0); // copy 一个新的
        //初始化不同邮件类型 模板的使用次数
        Map<String, Float> counts = new HashMap<>();
        if(templates != null) {
            for(String t : templates) {
                counts.put(t, 0f);
            }
        } else {
            if(type.equals(T.NORMAL)) {
                counts.put(Mails.CLEARANCE, 0f);
                counts.put(Mails.REVIEW_US, 0f);
                counts.put(Mails.REVIEW_DE, 0f);
                counts.put(Mails.REVIEW_UK, 0f);
                counts.put(Mails.MORE_OFFERS, 0f);
                counts.put(Mails.FEEDBACK_WARN, 0f);
                counts.put(Mails.FNSKU_CHECK, 0f);
                counts.put(Mails.IS_DONE, 0f);
                counts.put(Mails.REVIEW_WARN, 0f);
            } else if(type.equals(T.FBA)) {
                counts.put(FBAMails.RECEIVING_CHECK, 0f);
                counts.put(FBAMails.STATE_CHANGE, 0f);
            } else if(type.equals(T.SYSTEM)) {
                counts.put(SystemMails.DAILY_REVIEW, 0f);
                counts.put(SystemMails.DAILY_FEEDBACK, 0f);
                counts.put(SystemMails.SKU_PIC_CHECK, 0f);
            }
        }
        while(travel.getMillis() < _to.getMillis()) {
            String travelStr = travel.toString("yyyy-MM-dd");
            //总数量
            float totalCount = 0;
            for(MailsRecord record : records) {
                DateTime tmpDT = new DateTime(record.createdAt);
                if(tmpDT.toString("yyyy-MM-dd").equals(travelStr)) {
                    totalCount += 1;
                    counts.put(record.templateName, counts.get(record.templateName) + 1);
                }
            }
            lines.series("all_records").add(travel.toDate(), totalCount);
            for(String key : counts.keySet()) {
                lines.series(key).add(travel.toDate(), counts.get(key));
                counts.put(key, 0f);
            }
            travel = travel.plusDays(1);
        }
        return lines;
    }

    /**
     * 查询记录
     *
     * @param from
     * @param to
     * @param type
     * @param success
     * @param group
     * @return
     */
    private static List<MailsRecord> getMailsRecords(Date from, Date to, T type, List<String> templates, boolean success,
                                                     String group) {
        String cacheKey = Caches.Q.cacheKey(from, to, type, success, group, templates);
        List<MailsRecord> records = Cache.get(cacheKey, List.class);
        if(records != null) return records;
        synchronized(MailsRecord.class) {
            StringBuilder querystr = new StringBuilder("type=? and createdAt between ? and ? and success=?");
            List<Object> paras = new ArrayList<>();
            paras.add(type);
            paras.add(from);
            paras.add(to);
            paras.add(success);
            if(StringUtils.isNotBlank(group)) {
                querystr.append(" and recipients like ?");
                paras.add("%" + group + "%");
            }
            if(templates != null) {
                querystr.append(" and ");
                querystr.append(SqlQuery.whereIn("templateName", templates));
            }
            records = MailsRecord.find(querystr.toString(), paras.toArray()).fetch();
            if(records != null) {
                Cache.add(cacheKey, records);
            }
        }
        return records;
    }

}
