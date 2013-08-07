package models.view.post;

import helper.Currency;
import helper.Dates;
import models.finance.Apply;
import org.joda.time.DateTime;
import play.libs.F;
import models.finance.ProcureApply;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

/**
 * Created by IntelliJ IDEA.
 * User: wyatt
 * Date: 4/2/13
 * Time: 3:25 PM
 */
public class ApplyPost extends Post<Apply> {

    public ApplyPost(){
        DateTime now = DateTime.now(Dates.timeZone(null));
        this.from = now.minusDays(5).toDate();
        this.to = now.toDate();
        this.dateType = DateType.UPDATE;
    }

    public Date from;
    public Date to;

    public DateType dateType;

    public Long suppliers;


    public enum DateType{

        CREATE{
            @Override
            public String label(){
                return "创建时间";
            }
        },
        UPDATE{
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

       if( this.dateType != null ){
           if( this.dateType == DateType.CREATE ){
               sql.append(" AND createdAt >=?  AND createdAt <=? ");
           }
           else{
               sql.append(" AND updateAt >=? AND updateAt <=? ");
           }
           params.add( Dates.morning(this.from) );
           params.add( Dates.night(this.to) );
       }

       if( this.suppliers != null ){
           sql.append(" AND cooperator.id =? ");
           params.add(this.suppliers);
       }

       if( this.search != null && !"".equals( this.search.trim() ) ){
           sql.append(" AND serialNumber like ?");
           params.add( this.word() );
       }

        return new F.T2<String, List<Object>>(sql.toString(), params);
    }

    public List<Apply> query() {
        F.T2<String, List<Object>> params = params();
      return  ProcureApply.find( params._1 +"ORDER BY createdAt DESC" , params._2.toArray()).fetch();
    }
}
