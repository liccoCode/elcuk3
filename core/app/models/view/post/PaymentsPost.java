package models.view.post;


import helper.Currency;
import play.libs.F;
import org.joda.time.DateTime;
import helper.Dates;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import models.finance.Payment;


/**
 * Created by IntelliJ IDEA.
 * User: DyLanM
 * Date: 13-8-5
 * Time: 上午10:09
 */
public class PaymentsPost extends Post<Payment>{

    public PaymentsPost() {
           DateTime now = DateTime.now(Dates.timeZone(null));
           this.from = now.minusDays(5).toDate();
           this.to = now.toDate();
           this.dateType = DateType.update;
     }



    /**
      * 由于在 Action Redirect 的时候, 需要保留参数, 而 Play 并没有保留, 所以只能多写一次
      */
     public Date from;
     public Date to;

     public DateType dateType;

    public Payment.S state;

    public Long cooperId;

    public Date paymentDate;

    public Currency actualCurrency;

    public String actualAccountNumber = "";

    public enum DateType {

        create {
          @Override
          public String label(){
              return "创建时间";
             }
        },
        update {
          @Override
          public String label(){
              return "更新时间";
          }
        };

        public abstract String label();

    }



    @Override
    public F.T2<String, List<Object>> params() {


        StringBuffer sql = new StringBuffer(" 1=1 ");
        List<Object> params = new ArrayList<Object>();

        if( this.dateType != null )
        {
             if( this.dateType == DateType.create )
             {
                 sql.append(" AND createdAt >=?  AND createdAt <=? ");
             }
             else
             {
                 sql.append(" AND updateAt >=? AND updateAt <=? ");
             }
            params.add( Dates.morning(this.from) );
            params.add( Dates.night(this.to) );
        }

        if( this.state != null )
        {
            sql.append(" AND state =?");
            params.add(this.state);
        }

        if( this.cooperId != null )
        {
            sql.append(" AND cooperator.id =? ");
            params.add(this.cooperId);
        }

        if( this.paymentDate != null )
        {
            sql.append("AND paymentDate =?");
            params.add( this.paymentDate );
        }

        if( this.actualCurrency != null )
        {
            sql.append("AND actualCurrency =?");
            params.add(this.actualCurrency);
        }

        if( !actualAccountNumber.equals(""))
        {
            sql.append(" AND actualAccountNumber = ?");
            params.add(this.actualAccountNumber);
        }


        return new F.T2<String, List<Object>>(sql.toString(), params);
    }

    public List<Payment> query() {
           F.T2<String, List<Object>> params = params();
           return Payment.find( params._1 + " ORDER BY createdAt DESC", params._2.toArray() ).fetch();
       }

}
