package models.view.post;

import models.whouse.Whouse;
import play.libs.F;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by IntelliJ IDEA.
 * User: mac
 * Date: 14-5-14
 * Time: PM3:19
 */
public class WhousePost extends Post<Whouse> {
    public Long cooperatorId;

    public Long userId;

    public Whouse.T type;

    public WhousePost(Whouse.T type) {
        this.type = type;
    }

    @Override
    public List<Whouse> query() {
        F.T2<String, List<Object>> params = params();
        return Whouse.find(params._1, params._2.toArray()).fetch();
    }

    @Override
    public F.T2<String, List<Object>> params() {
        StringBuilder sbd = new StringBuilder("1=1");
        List<Object> params = new ArrayList<>();

        if(this.type != null) {
            sbd.append("AND type=?");
            params.add(this.type);
        }

        if(this.cooperatorId != null) {
            sbd.append("AND cooperator_id=?");
            params.add(this.cooperatorId);
        }

        if(this.userId != null) {
            sbd.append("AND user_id=?");
            params.add(this.userId);
        }
        return new F.T2<>(sbd.toString(), params);
    }
}
