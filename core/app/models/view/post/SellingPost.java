package models.view.post;

import models.User;
import models.market.M;
import models.market.Selling;
import org.apache.commons.lang.StringUtils;
import play.db.helper.SqlSelect;
import play.libs.F;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by licco on 15/8/4.
 */
public class SellingPost extends Post<Selling> {

    public String analyzeResult;

    public M market;

    public Selling.SC sellingCycle;

    public String systemUp;

    public String keywords;

    public String categoryid;

    @Override
    public F.T2<String, List<Object>> params() {
        List<Object> params = new ArrayList<>();
        StringBuilder sql = new StringBuilder("SELECT DISTINCT s FROM Selling s WHERE 1 = 1 ");

        if(StringUtils.isNotBlank(analyzeResult)) {

            params.add(analyzeResult);
        }
        if(market != null) {
            sql.append(" AND s.market = ? ");
            params.add(market);
        }
        if(sellingCycle != null) {
            sql.append(" AND s.sellingCycle = ? ");
            params.add(sellingCycle);
        }
        if(StringUtils.isBlank(systemUp) || systemUp.equals("是")) {
            systemUp = "是";
            sql.append(" AND s.state IN(?, ?) ");
            params.add(Selling.S.NEW);
            params.add(Selling.S.SELLING);
        } else {
            sql.append(" AND s.state = ? ");
            params.add(Selling.S.DOWN);
        }
        if(StringUtils.isNotBlank(categoryid)) {
            sql.append(" AND s.listing.product.category.categoryId = ? ");
            params.add(categoryid);
        } else {
            List<String> categorys = User.getTeamCategorys(User.current());

            if(categorys != null && categorys.size() > 0) {
                sql.append(" AND s.listing.product.category.categoryId in ").append(SqlSelect.inlineParam(categorys));
            } else {
                categorys = new ArrayList<String>();
                categorys.add("-1");
                sql.append(" AND s.listing.product.category.categoryId in ").append(SqlSelect.inlineParam(categorys));
            }
        }
        if(StringUtils.isNotBlank(keywords)) {
            sql.append(" AND (s.sellingId LIKE ? OR s.asin LIKE ? OR s.listing.product.sku LIKE ? ) ");
            params.add("%" + keywords + "%");
            params.add("%" + keywords + "%");
            params.add("%" + keywords + "%");
        }
        sql.append(" ORDER BY s.createDate DESC ");
        return new F.T2<>(sql.toString(), params);
    }

    public List<Selling> query() {
        F.T2<String, List<Object>> params = params();
        this.count = this.count(params);
        return Selling.find(params._1, params._2.toArray()).fetch(this.page, this.perSize);
    }

    public Long getTotalCount() {
        F.T2<String, List<Object>> params = params();
        return (long) Selling.find(params._1, params._2.toArray()).fetch().size();
    }

    @Override
    public Long count(F.T2<String, List<Object>> params) {
        return (long) Selling.find(params._1, params._2.toArray()).fetch().size();
    }


}
