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
        return Product.count(params._1, params._2.toArray());
    }

    @Override
    public Long getTotalCount() {
        return Product.count();
    }

    @Override
    public F.T2<String, List<Object>> params() {
        StringBuilder sbd = new StringBuilder("1=1");
        List<Object> params = new ArrayList<Object>();

        String sku = skuSearch();
        if(StringUtils.isNotBlank(this.state)) {
            if(StringUtils.isNotBlank(sku)) { //如果匹配上了 SKU 的话直接使用主键匹配，否则的话使用模糊查询 囧
                sbd.append("AND sku=?");
                params.add(sku);
            } else {
                sbd.append(" AND sku LIKE ?");
                params.add(this.search + "%");
            }
            sbd.append(" AND state IN ");
            if(StringUtils.equalsIgnoreCase(this.state, "Active")) {
                sbd.append(SqlSelect.inlineParam(Arrays.asList(Product.S.NEW, Product.S.SELLING)));
            } else {
                sbd.append(SqlSelect.inlineParam(Arrays.asList(Product.S.DOWN)));
            }
        }
        return new F.T2<String, List<Object>>(sbd.toString(), params);
    }

    @Override
    public List<Product> query() {
        F.T2<String, List<Object>> params = params();
        this.count = this.count(params);

        return Product.find(params._1, params._2.toArray()).fetch(this.page, this.perSize);
    }

    private String skuSearch() {
        if(StringUtils.isBlank(this.search)) return "";
        Matcher matcher = SKU.matcher(this.search);
        if(matcher.find()) return matcher.group(1);
        return "";
    }
}
