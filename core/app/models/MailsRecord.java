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
import play.Logger;
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

    /**
     * 邮件名称
     */
    @Expose
    public String title;


    /**
     * 模板
     */
    @Expose
    public String template;


    //需不需要用枚举呢?????
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
     * 收件人  用 ',' 号隔开
     */
    @Expose
    public String recipients;

    public MailsRecord(String title) {
        this.title=title;
    }

    /**
     * 条件查询
     * @param from 开始时间
     * @param to 结束时间
     * @param type  类型
     * @param success
     * @param group 特定收件人
     * @return
     */
    @Cached("5mn")
    public static HighChart ajaxRecordBy(Date from, Date to, String type, boolean success, String group) {
        DateTime _from = new DateTime(Dates.morning(from));
        DateTime _to = new DateTime(Dates.night(to)).plusDays(1);
        T t ;
        //查询当做条件时,也必须 使用枚举类型而不能用String?????
        if(type.equals(T.NORMAL.toString())){
             t=T.NORMAL;
        }else if(type.equals(T.SYSTEM.toString())){
            t=T.SYSTEM;
        }else
            t=T.FBA;

        List<MailsRecord> records=getMailsRecords(_from.toDate(), _to.toDate(),t, success, group);

        HighChart lines = new HighChart().startAt(_from.getMillis());
        DateTime travel = _from.plusDays(0); // copy 一个新的

        //初始化不同邮件类型 模板的使用次数
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


        while(travel.getMillis()<_to.getMillis()){
            String travelStr=travel.toString("yyyy-MM-dd");

            //总数量
            float totalCount=0;
            for(MailsRecord record : records){
                DateTime tmp=new DateTime(record.createDt);
                if(tmp.toString("yyyy-MM-dd").equals(travelStr)){
                    totalCount+=1;
                    counts.put(record.template,counts.get(record.template)+1);
                }
            }
            lines.line("all_records").add(totalCount);
            Iterator<String> ite=counts.keySet().iterator();
            while(ite.hasNext()){
                String key=ite.next();
                lines.line(key).add(counts.get(key));
                counts.put(key,0f);
            }
            travel = travel.plusDays(1);
        }
        return lines;
    }

    /**
     * 查询记录
     * @param from
     * @param to
     * @param type
     * @param success
     * @param group
     * @return
     */
    private static List<MailsRecord> getMailsRecords(Date from, Date to, T type, boolean success, String group) {
        String cacheKey = Caches.Q.cacheKey(from, to, type, success, group);
        List<MailsRecord> records= Cache.get(cacheKey, List.class);
        if (records!=null) return records;
        Logger.info("缓存中无记录,开始查询......");
        String querystr="type=? and success=? and createDt between ? and ?";
        if(!StringUtils.isBlank(group)){
            querystr+=" and recipients like ?";
            records=MailsRecord.find(querystr,type,success,from,to,"%"+group+"%").fetch();
        }else
            records=MailsRecord.find(querystr,type,success,from,to).fetch();
        if(records!=null)
            Cache.add(cacheKey, records);
        return records;
    }



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
       this.recipients="";
       for(String recipient :recipients)
           //这里最后会多出个逗号,由于没有从数据库拿出来用的需要,暂时这样
           this.recipients  +=recipient+",";
       this.template=tmp;
       this.type=type;
   }





}
