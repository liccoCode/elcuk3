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

    public String keywords;

    @Override
    public F.T2<String, List<Object>> params() {
        List<Object> params = new ArrayList<Object>();
        StringBuilder sql = new StringBuilder("SELECT DISTINCT s FROM BtbCustom s WHERE 1 = 1 ");
        if(StringUtils.isNotEmpty(keywords)) {
            sql.append(" AND s.customName like ? ");
            params.add("%"+keywords+"%");
        }
        return new F.T2<String, List<Object>>(sql.toString(), params);
    }

    public List<BtbCustom> query() {
        F.T2<String, List<Object>> params = params();
        this.count = this.count(params);
        return BtbCustom.find(params._1, params._2.toArray()).fetch(this.page, this.perSize);
    }


    public Long getTotalCount() {
        F.T2<String, List<Object>> params = params();
        return new Long(BtbCustom.find(params._1, params._2.toArray()).fetch().size());
    }

    @Override
    public Long count(F.T2<String, List<Object>> params) {
        return new Long(BtbCustom.find(params._1, params._2.toArray()).fetch().size());
    }


}
