package models.view.post;

import models.product.Product;
import org.apache.commons.lang.StringUtils;
import play.db.helper.SqlSelect;
import play.libs.F;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * Created by IntelliJ IDEA.
 * User: wyattpan
 * Date: 10/15/12
 * Time: 5:56 PM
 */
public class ProductPost extends Post<Product> {
    public String state = "Active";

    public ProductPost() {
        this.perSize = 25;
    }

    @Override
    public Long count(F.T2<String, List<Object>> params) {
        return (long) Product.find(params._1, params._2.toArray()).fetch().size();
    }

    @Override
    public Long getTotalCount() {
        return Product.count();
    }

    @Override
    public F.T2<String, List<Object>> params() {
        StringBuilder sbd = new StringBuilder("SELECT DISTINCT p FROM Product p")
                .append(" LEFT JOIN p.productAttrs a")
                .append(" LEFT JOIN p.listings l")
                .append(" LEFT JOIN l.sellings s")
                .append(" WHERE 1=1");
        List<Object> params = new ArrayList<>();

        if(StringUtils.isNotBlank(this.search)) {
            String word = this.word();
            sbd.append("AND (")
                    .append(" p.sku LIKE ?")
                    .append(" OR a.value LIKE ?")
                    .append(" OR s.fnSku LIKE ?")
                    .append(")");
            for(int i = 0; i < 3; i++) params.add(word);
        }

        if(StringUtils.isNotBlank(this.state)) {
            sbd.append(" AND p.state IN ");
            if(StringUtils.equalsIgnoreCase(this.state, "Active")) {
                sbd.append(SqlSelect.inlineParam(Arrays.asList(Product.S.NEW, Product.S.SELLING)));
            } else {
                sbd.append(SqlSelect.inlineParam(Arrays.asList(Product.S.DOWN)));
            }
        }
        return new F.T2<>(sbd.toString(), params);
    }

    @Override
    public List<Product> query() {
        F.T2<String, List<Object>> params = params();
        this.count = this.count(params);
        return Product.find(params._1, params._2.toArray()).fetch(this.page, this.perSize);
    }
}
