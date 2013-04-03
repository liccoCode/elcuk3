package models;

import com.google.gson.annotations.Expose;
import helper.Cached;
import helper.Caches;
import helper.Dates;
import models.view.dto.HighChart;
import notifiers.FBAMails;
import notifiers.Mails;
import notifiers.SystemMails;
import org.apache.commons.lang.StringUtils;
import org.joda.time.DateTime;
import play.cache.*;
import play.cache.Cache;
import play.db.jpa.GenericModel;
import play.libs.F;

import javax.persistence.*;
import java.util.*;

/**
 * Created by IntelliJ IDEA.
 * User: rose
 * Date: 13-4-2
 * Time: 下午3:16
 */
@Entity
@org.hibernate.annotations.Entity(dynamicUpdate = true)
public class MailsRecord extends GenericModel {

     @Id
     @GeneratedValue
     @Expose
     public long id;

    @Expose
    public String title;



    @Expose
    public String template;


    public MailsRecord(String title) {
        this.title=title;
    }
    @Cached("5mn")
    public static HighChart ajaxRecordBy(Date from, Date to, String type, boolean success, String group) {
        DateTime _from = new DateTime(Dates.morning(from));
        DateTime _to = new DateTime(Dates.night(to)).plusDays(1);
        List<MailsRecord> records=getMailsRecords(_from.toDate(), _to.toDate(), type, success, group);

        HighChart lines = new HighChart().startAt(_from.getMillis());
        DateTime travel = _from.plusDays(0); // copy 一个新的



        while(travel.getMillis()<_to.getMillis()){
            String travelStr=travel.toString("yyyy-MM-dd");
            float totalCount=0;

            //这里有没有更好的方法??????
            Map<String,Float> counts=new HashMap<String,Float>();
            if(type.equals(T.NORMAL.toString())){
                counts.put(Mails.CLEARANCE,0f);
                counts.put(Mails.REVIEW_US,0f);
                counts.put(Mails.REVIEW_DE,0f);
                counts.put(Mails.REVIEW_UK,0f);
                counts.put(Mails.MORE_OFFERS,0f);
                counts.put(Mails.FEEDBACK_WARN,0f);
                counts.put(Mails.FNSKU_CHECK,0f);
                counts.put(Mails.IS_DONE,0f);
                counts.put(Mails.REVIEW_WARN,0f);
            }else if (type.equals(T.FBA.toString())){
                counts.put(FBAMails.NOT_RECEING,0f);
                counts.put(FBAMails.RECEIVING_CHECK,0f);
                counts.put(FBAMails.STATE_CHANGE,0f);
            }else if (type.equals(T.SYSTEM.toString())){
                counts.put(SystemMails.DAILY_REVIEW,0f);
                counts.put(SystemMails.DAILY_FEEDBACK,0f);
                counts.put(SystemMails.SKU_PIC_CHECK,0f);
            }
            for(MailsRecord record : records){
                DateTime tmp=new DateTime(record.createDt);
                if(tmp.toString("yyyy-MM-dd").equals(travelStr)){
                    totalCount+=1;
                    counts.put(record.template,counts.get(record.template)+1);
                }
            }
            Iterator<String> ite=counts.keySet().iterator();
            lines.line("all_records").add(totalCount);
            while(ite.hasNext()){
                String key=ite.next();
                lines.line(key).add(counts.get(key));
            }
            travel = travel.plusDays(1);
        }
        return lines;
    }

    private static List<MailsRecord> getMailsRecords(Date from, Date to, String type, boolean success, String group) {
        String cacheKey = Caches.Q.cacheKey(from, to, type, success, group);

        List<MailsRecord> records= Cache.get(cacheKey, List.class);
        if (records!=null) return records;
        String HQL="type=? and success=? and createDt between ? and ?";
        if(!StringUtils.isBlank(group))
            HQL+=" and recipients like ?";

        if(StringUtils.isBlank(group))
            records=MailsRecord.find(HQL,type,success,from,to).fetch();
        else
            records=MailsRecord.find(HQL,type,success,from,to,group).fetch();

        if(records!=null)
            Cache.add(cacheKey, records);
        return records;
    }


    public enum T{
        NORMAL{
            @Override
            public String toString() {
                return "NORMAL";
            }
        },
        FBA{
            @Override
            public String toString() {
                return "FBA";
            }
        },
        SYSTEM{
            @Override
            public String toString() {
                return "SYSTEM";
            }
        }
    }

    @Expose
    @Enumerated(EnumType.STRING)
    public T type;

    @Expose
    public Date createDt=new Date();

    @Expose
    public boolean success;

    @Expose
    public String sender;

    @Expose
    public String recipients;

    /**
     * 根据title查找发送失败的邮件记录
     * @param title
     * @return
     */
    public static MailsRecord findFailedByTitle(String title) {
       MailsRecord mr=MailsRecord.find("title=? and success=false",title).first();
       if(mr==null)
           mr=new MailsRecord(title);
        return mr;
    }


   public void addParams(String from, ArrayList<String> recipients, String tmp, T type) {
       this.sender=from;
       for(String recipient :recipients)
           if(this.recipients==null)
               this.recipients=recipient;
           else
               this.recipients+=","+recipient;
       this.template=tmp;
       this.type=type;
   }





}
