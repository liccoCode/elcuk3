package models.view.post;

import controllers.Login;
import helper.DBUtils;
import models.User;
import models.procure.Cooperator;
import models.product.Category;
import org.apache.commons.lang.StringUtils;
import play.db.helper.JpqlSelect;
import play.db.helper.SqlSelect;
import play.i18n.Messages;
import play.libs.F;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * Created by IntelliJ IDEA.
 * User: licco
 * Date: 2017/5/19
 * Time: 下午2:10
 */
public class CooperatorPost extends Post<Cooperator> {

    private static final long serialVersionUID = 4613786316373015576L;

    public Cooperator.T type;
    public boolean visible = true;

    @Override
    public F.T2<String, List<Object>> params() {
        List<Object> params = new ArrayList<>();
        StringBuilder sql = new StringBuilder("SELECT DISTINCT c FROM Cooperator c LEFT JOIN c.cooperItems i ");
        sql.append(" LEFT JOIN i.product t ");
        sql.append(" LEFT JOIN i.material m WHERE  1 = 1 ");

        if(StringUtils.isNotEmpty(search)) {
            sql.append(" AND (t.sku like ? OR c.fullName like ? OR c.name like ? or m.code like ?)");
            params.add("%" + search + "%");
            params.add("%" + search + "%");
            params.add("%" + search + "%");
            params.add("%" + search + "%");
        }
        User user = Login.current();
        if(!user.isHaveCrossCop()) {
            sql.append(" AND c.projectName = ? ");
            params.add(user.projectName);
        }
        sql.append(" AND c.visible = ? ");
        params.add(visible);
        sql.append(" AND c.type = ? ");
        params.add(type);

        String username = Login.currentUserName();
        List<String> categoryList = Category.categories(username).stream().map(category -> category.categoryId)
                .collect(Collectors.toList());
        if(categoryList != null && categoryList.size() > 0) {
            sql.append(" AND (t.category.categoryId IN ").append(SqlSelect.inlineParam(categoryList));
            sql.append(" OR t.sku is null)");
        } else {
            categoryList = new ArrayList<>();
            categoryList.add("-1");
            sql.append(" AND i.product.category.categoryId IN ").append(SqlSelect.inlineParam(categoryList));
        }

        sql.append(" ORDER BY c.name ASC ");
        return new F.T2<>(sql.toString(), params);
    }

    public List<Cooperator> query() {
        F.T2<String, List<Object>> params = params();
        this.count = this.count(params);
        return Cooperator.find(params._1, params._2.toArray()).fetch(this.page, this.perSize);
    }

    public Long getTotalCount() {
        return this.count;
    }

    @Override
    public Long count(F.T2<String, List<Object>> params) {
        return (long) Cooperator.find(params._1, params._2.toArray()).fetch().size();
    }

    public List<Map<String, Object>> logs() {
        StringBuilder sbd = new StringBuilder("SELECT e.* ,ci.sku ,m.`code`,c.`fullName` from ElcukRecord e ");
        sbd.append(" LEFT join CooperItem ci  on e.`fid` = ci.id ");
        sbd.append(" left join Cooperator c on ci.`cooperator_id` = c.id ");
        sbd.append(" left join Material m on m.id = ci.`material_id` ");
        sbd.append(" WHERE 1=1 AND  ");
        List<String> actionMsgs = Arrays.asList("cooperators.savecooperitem",
                "cooperators.updatecooperitem",
                "cooperators.cooperitemdelete").
                stream().map(action -> Messages.get(action)).collect(Collectors.toList());
        sbd.append(JpqlSelect.whereIn("action", actionMsgs));
        sbd.append("AND createAt>DATE_SUB(CURDATE(), INTERVAL ? MONTH) ORDER BY createAt DESC");
        List<Map<String, Object>> rows = DBUtils.rows(sbd.toString(), new Object[]{3});
        return rows;
    }

}
