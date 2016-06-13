package models.view.post;

import models.User;
import org.apache.commons.lang.StringUtils;
import play.libs.F;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by licco on 16/5/23.
 */
public class UserPost extends Post<User> {

    public int perSize = 20;

    @Override
    public F.T2<String, List<Object>> params() {
        List<Object> params = new ArrayList<>();
        StringBuilder sql = new StringBuilder("SELECT DISTINCT s FROM User s Where 1=1 ");
        if(StringUtils.isNotBlank(this.search)) {
            sql.append(" AND s.username like ? ");
            params.add("%" + this.search + "%");
        }
        return new F.T2<>(sql.toString(), params);
    }

    public List<User> query() {
        F.T2<String, List<Object>> params = params();
        this.count = this.count(params);
        return User.find(params._1, params._2.toArray()).fetch(this.page, this.perSize);
    }

    public Long count(F.T2<String, List<Object>> params) {
        return (long) User.find(params._1, params._2.toArray()).fetch().size();
    }


    public Long getTotalCount() {
        F.T2<String, List<Object>> params = params();
        return (long) User.find(params._1, params._2.toArray()).fetch().size();
    }

}
