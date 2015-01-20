package models.view.post;

import play.libs.F;
import java.util.*;

import models.ReportRecord;

/**
 * 订单页面的搜索 POJO, 不进入数据库, 仅仅作为页面的传值绑定
 * User: wyattpan
 * Date: 4/5/12
 * Time: 6:59 PM
 */
public class ReportPost extends Post<ReportRecord> {


    public ReportPost() {
        this.perSize = 25;
        this.page = 1;
        this.month = 0;
    }

    public ReportPost(int perSize) {
        this.perSize = perSize;
    }

    public int year;
    public int month;

    public ReportRecord.RT reporttype;


    @SuppressWarnings("unchecked")
    public List<ReportRecord> query() {
        F.T2<String, List<Object>> params = params();
        return ReportRecord.find(params._1 + " ORDER BY createAt DESC", params._2.toArray()).fetch();
    }

    @Override
    public Long count(F.T2<String, List<Object>> params) {
        return ReportRecord.count("SELECT COUNT(*) FROM ReportRecord WHERE " + params._1,
                params._2.toArray()
        );
    }

    public Long getTotalCount() {
        return this.count;
    }

    @Override
    public F.T2<String, List<Object>> params() {
        StringBuilder sbd = new StringBuilder();
        List<Object> params = new ArrayList<Object>();
        sbd.append(" 1=1 ");
        if(this.year != 0) {
            sbd.append("AND year=?");
            params.add(this.year);
        }
        if(this.month != 0) {
            sbd.append("AND month=?");
            params.add(this.month);
        }
        if(this.reporttype != null) {
            sbd.append("AND reporttype=?");
            params.add(this.reporttype);
        }
        return new F.T2<String, List<Object>>(sbd.toString(), params);
    }

}
