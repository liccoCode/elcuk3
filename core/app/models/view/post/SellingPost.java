package models.view.post;

import controllers.Login;
import models.market.M;
import models.market.Selling;
import models.product.Category;
import org.apache.commons.lang.StringUtils;
import play.db.helper.SqlSelect;
import play.libs.F;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Created by IntelliJ IDEA.
 * User: licco
 * Date: 15/8/4
 * Time: 5:39 PM
 */
public class SellingPost extends Post<Selling> {

    private static final long serialVersionUID = 466049732872654863L;
    public String analyzeResult;
    public M market;
    public Selling.SC sellingCycle;
    public String systemUp;
    public String keywords;
    public String categoryid;
    public int perSize = 25;
    public Selling.PS pirateState;

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
            sql.append(" AND s.product.category.categoryId = ? ");
            params.add(categoryid);
        }
        if(pirateState != null) {
            sql.append(" AND pirateState = ? ");
            params.add(pirateState);
        }
        if(StringUtils.isNotBlank(keywords)) {
            sql.append(" AND (s.sellingId LIKE ? OR s.asin LIKE ? OR s.product.sku LIKE ? ) ");
            params.add("%" + keywords + "%");
            params.add("%" + keywords + "%");
            params.add("%" + keywords + "%");
        }
        String username = Login.currentUserName();
        List<String> categoryList = Category.categories(username).stream().map(category -> category.categoryId)
                .collect(Collectors.toList());
        if(categoryList != null && categoryList.size() > 0) {
            sql.append(" AND s.product.category.categoryId in ").append(SqlSelect.inlineParam(categoryList));
        } else {
            categoryList = new ArrayList<>();
            categoryList.add("-1");
            sql.append(" AND s.product.category.categoryId in ").append(SqlSelect.inlineParam(categoryList));
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
