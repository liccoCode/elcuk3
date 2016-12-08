package models.view.post;

import helper.Dates;
import models.whouse.Refund;
import org.joda.time.DateTime;
import play.libs.F;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by licco on 2016/11/28.
 */
public class RefundPost extends Post<Refund> {

    public String search;

    public RefundPost() {
        DateTime now = DateTime.now(Dates.timeZone(null));
        this.from = now.minusDays(5).toDate();
        this.to = now.toDate();
        this.perSize = 25;
        this.page = 1;
    }

    @Override
    public F.T2<String, List<Object>> params() {
        StringBuilder sbd = new StringBuilder("1=1");
        List<Object> params = new ArrayList<>();

        sbd.append(" ORDER BY createDate DESC");
        return new F.T2<>(sbd.toString(), params);
    }

    @Override
    public List<Refund> query() {
        this.count = this.count();
        F.T2<String, List<Object>> params = params();
        return Refund.find(params._1, params._2.toArray()).fetch(this.page, this.perSize);
    }

    @Override
    public Long getTotalCount() {
        return this.count();
    }


    @Override
    public Long count(F.T2<String, List<Object>> params) {
        return Refund.count(params._1, params._2.toArray());
    }


}
