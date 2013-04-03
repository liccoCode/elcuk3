package models;

import com.google.gson.annotations.Expose;
import play.db.jpa.GenericModel;

import javax.persistence.*;
import java.util.ArrayList;
import java.util.Date;

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
