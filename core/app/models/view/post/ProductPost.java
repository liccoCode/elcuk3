package models.view.post;

import controllers.Login;
import models.product.Category;
import models.product.Product;
import org.apache.commons.lang.StringUtils;
import play.db.helper.SqlSelect;
import play.libs.F;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Created by IntelliJ IDEA.
 * User: wyattpan
 * Date: 10/15/12
 * Time: 5:56 PM
 */
public class ProductPost extends Post<Product> {

    private static final long serialVersionUID = 9125924809280823169L;
    public String state = "Active";

    public boolean scope = false;

    public List<String> categories = new ArrayList<>();

    public ProductPost() {
        this.perSize = 25;
    }

    @Override
    public Long count(F.T2<String, List<Object>> params) {
        return (long) Product.find(params._1, params._2.toArray()).fetch().size();
    }

    @Override
    public Long getTotalCount() {
        return this.count();
    }

    @Override
    public F.T2<String, List<Object>> params() {
        StringBuilder sbd = new StringBuilder("SELECT DISTINCT p FROM Product p")
                .append(" LEFT JOIN p.productAttrs a")
                .append(" LEFT JOIN p.sellings s")
                .append(" WHERE 1=1");
        List<Object> params = new ArrayList<>();

        if(categories.size() > 0) {
            sbd.append(" AND p.category.id IN ").append(SqlSelect.inlineParam(categories));
        }

        if(StringUtils.isNotBlank(this.search)) {
            String word = this.word();
            sbd.append("AND (").append(" p.sku LIKE ? OR p.origin_sku LIKE ? ");
            if(this.scope)
                sbd.append(" OR p.abbreviation LIKE ?").append("OR p.locates LIKE ?")
                        .append("OR p.sellingPoints LIKE ?").append(" OR s.asin LIKE ?")
                        .append(" OR s.aps.title LIKE ? ").append(" OR s.aps.keyFetures LIKE ? ")
                        .append(" OR s.aps.productDesc LIKE ? ").append(" OR s.aps.searchTerms LIKE ? ");
            sbd.append(" OR a.value LIKE ?").append(" OR s.fnSku LIKE ?").append(")");
            if(this.scope)
                for(int i = 0; i < 12; i++) params.add(word);
            else
                for(int i = 0; i < 4; i++) params.add(word);
        }

        if(StringUtils.isNotBlank(this.state)) {
            sbd.append(" AND p.state IN ");
            if(StringUtils.equalsIgnoreCase(this.state, "Active")) {
                sbd.append(SqlSelect.inlineParam(Arrays.asList(Product.S.NEW, Product.S.SELLING)));
            } else {
                sbd.append(SqlSelect.inlineParam(Collections.singletonList(Product.S.DOWN)));
            }
        }
        String username = Login.currentUserName();
        List<String> categoryList = Category.categories(username).stream().map(category -> category.categoryId)
                .collect(Collectors.toList());
        if(categoryList != null && categoryList.size() > 0) {
            sbd.append(" AND p.category.categoryId IN ").append(SqlSelect.inlineParam(categoryList));
        } else {
            categoryList = new ArrayList<>();
            categoryList.add("-1");
            sbd.append(" AND p.category.categoryId IN ").append(SqlSelect.inlineParam(categoryList));
        }
        return new F.T2<>(sbd.toString(), params);
    }

    @Override
    public List<Product> query() {
        F.T2<String, List<Object>> params = params();
        this.count = this.count(params);
        return Product.find(params._1, params._2.toArray()).fetch(this.page, this.perSize);
    }

    public Product pickup() {
        F.T2<String, List<Object>> params = params();
        return Product.find(params._1, params._2.toArray()).first();
    }
}
