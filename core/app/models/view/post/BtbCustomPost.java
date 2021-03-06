package models.view.post;

import models.procure.BtbCustom;
import org.apache.commons.lang.StringUtils;
import play.libs.F;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by licco on 16/1/20.
 */
public class BtbCustomPost extends Post<BtbCustom>{

    private static final long serialVersionUID = -1348061646691032722L;
    public String keywords;

    @Override
    public F.T2<String, List<Object>> params() {
        List<Object> params = new ArrayList<>();
        StringBuilder sql = new StringBuilder("SELECT DISTINCT s FROM BtbCustom s WHERE isDel = false ");
        if(StringUtils.isNotEmpty(keywords)) {
            sql.append(" AND s.customName like ? ");
            params.add("%"+keywords+"%");
        }
        sql.append(" ORDER BY s.id DESC");
        return new F.T2<>(sql.toString(), params);
    }

    public List<BtbCustom> query() {
        F.T2<String, List<Object>> params = params();
        this.count = this.count(params);
        return BtbCustom.find(params._1, params._2.toArray()).fetch(this.page, this.perSize);
    }


    public Long getTotalCount() {
        F.T2<String, List<Object>> params = params();
        return (long) BtbCustom.find(params._1, params._2.toArray()).fetch().size();
    }

    @Override
    public Long count(F.T2<String, List<Object>> params) {
        return (long) BtbCustom.find(params._1, params._2.toArray()).fetch().size();
    }


}
