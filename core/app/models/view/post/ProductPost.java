package models.view.post;

import models.product.Product;
import org.apache.commons.lang.StringUtils;
import play.db.helper.SqlSelect;
import play.libs.F;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Created by IntelliJ IDEA.
 * User: wyattpan
 * Date: 10/15/12
 * Time: 5:56 PM
 */
public class ProductPost extends Post<Product> {
    public final Pattern SKU = Pattern.compile("([0-9a-zA-Z]+-[0-9a-zA-Z]+-?[0-9a-zA-Z]*)");
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
        F.T3<Boolean, String, List<Object>> specialSearch = skuSearch();
        if(specialSearch._1) {
            return new F.T2<>(specialSearch._2, specialSearch._3); //针对 SKU 的唯一搜索
        }

        StringBuilder sbd = new StringBuilder("SELECT DISTINCT p FROM Product p LEFT JOIN p.productAttrs a WHERE 1=1");
        List<Object> params = new ArrayList<>();
        if(StringUtils.isNotBlank(this.search) && !specialSearch._1) {
            String word = this.word();
            sbd.append("AND (")
                    .append(" p.sku LIKE ?")
                    .append(" OR a.value LIKE ?")
                    .append(")");
            for(int i = 0; i < 2; i++) params.add(word);
        }

        if(StringUtils.isNotBlank(this.state)) {
            sbd.append(" AND state IN ");
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

    private F.T3<Boolean, String, List<Object>> skuSearch() {
        if(StringUtils.isBlank(this.search))
            return new F.T3<>(false, null, null);

        Matcher matcher = SKU.matcher(this.search);
        if(matcher.find()) {
            String sku = matcher.group(1);
            return new F.T3<>(true, "sku=?",
                    new ArrayList<>(Arrays.asList(sku)));
        }
        return new F.T3<>(false, null, null);
    }
}
