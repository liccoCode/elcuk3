package models.view.post;

import models.whouse.Inbound;
import models.whouse.InboundRecord;
import play.libs.F;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by licco on 2016/11/11.
 */
public class InboundPost extends Post<Inbound> {

    public String search;

    public InboundPost() {

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
    public List<Inbound> query() {
        this.count = this.count();
        F.T2<String, List<Object>> params = params();
        return Inbound.find(params._1, params._2.toArray()).fetch(this.page, this.perSize);
    }

    @Override
    public Long getTotalCount() {
        return this.count();
    }


    @Override
    public Long count(F.T2<String, List<Object>> params) {
        return Inbound.count(params._1, params._2.toArray());
    }

}
