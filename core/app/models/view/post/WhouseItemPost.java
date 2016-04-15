package models.view.post;

import models.whouse.Whouse;
import models.whouse.WhouseItem;
import org.apache.commons.lang3.StringUtils;
import play.libs.F;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by IntelliJ IDEA.
 * User: duan
 * Date: 4/12/16
 * Time: 5:38 PM
 */
public class WhouseItemPost extends Post<WhouseItem> {
    public Whouse whouse;

    public WhouseItemPost() {
        this.perSize = 20;
        this.page = 1;
    }

    @Override
    public F.T2<String, List<Object>> params() {
        StringBuilder sbd = new StringBuilder("1=1");
        List<Object> params = new ArrayList<>();

        if(this.whouse != null && this.whouse.id != null) {
            sbd.append(" AND whouse=?");
            params.add(this.whouse);
        }

        if(StringUtils.isNotBlank(this.search)) {
            sbd.append(" AND stockObjId LIKE ?");
            params.add(this.word());
        }
        sbd.append(" order by whouse.id");
        return new F.T2<>(sbd.toString(), params);
    }

    @Override
    public List<WhouseItem> query() {
        this.count = this.count();
        F.T2<String, List<Object>> params = params();
        return WhouseItem.find(params._1, params._2.toArray()).fetch(this.page, this.perSize);
    }

    @Override
    public Long count(F.T2<String, List<Object>> params) {
        return WhouseItem.count(params._1, params._2.toArray());
    }

    @Override
    public Long getTotalCount() {
        return this.count();
    }
}
