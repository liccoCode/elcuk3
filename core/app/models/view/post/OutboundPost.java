package models.view.post;

import models.whouse.Outbound;
import play.libs.F;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by licco on 2016/11/30.
 */
public class OutboundPost extends Post<Outbound>{

    public String search;

    public OutboundPost() {
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
    public List<Outbound> query() {
        this.count = this.count();
        F.T2<String, List<Object>> params = params();
        return Outbound.find(params._1, params._2.toArray()).fetch(this.page, this.perSize);
    }

    @Override
    public Long getTotalCount() {
        return this.count();
    }


    @Override
    public Long count(F.T2<String, List<Object>> params) {
        return Outbound.count(params._1, params._2.toArray());
    }



}
