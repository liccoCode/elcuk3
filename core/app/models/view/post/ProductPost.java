package models.view.post;

import models.product.Product;
import org.apache.commons.lang.StringUtils;
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

    public ProductPost() {
        this.perSize = 25;
    }

    @Override
    public Long count(F.T2<String, List<Object>> params) {
        return Product.count(params._1, params._2.toArray());
    }

    @Override
    public F.T2<String, List<Object>> params() {
        F.T3<Boolean, String, List<Object>> specialSearch = skuSearch();
        if(specialSearch._1)
            return new F.T2<String, List<Object>>(specialSearch._2, specialSearch._3);

        StringBuilder sbd = new StringBuilder("1=1");
        List<Object> params = new ArrayList<Object>();
        if(StringUtils.isNotBlank(this.search)) {
            sbd.append(" AND sku LIKE ?");
            params.add(this.search + "%");
        }

        return new F.T2<String, List<Object>>(sbd.toString(), params);
    }

    @Override
    public List<Product> query() {
        F.T2<String, List<Object>> params = params();
        this.count = this.count(params);

        return Product.find(params._1, params._2.toArray()).fetch(this.page, this.perSize);
    }

    private F.T3<Boolean, String, List<Object>> skuSearch() {
        if(StringUtils.isBlank(this.search))
            return new F.T3<Boolean, String, List<Object>>(false, null, null);

        Matcher matcher = SKU.matcher(this.search);
        if(matcher.find()) {
            String sku = matcher.group(1);
            return new F.T3<Boolean, String, List<Object>>(true, "sku=?",
                    new ArrayList<Object>(Arrays.asList(sku)));
        }
        return new F.T3<Boolean, String, List<Object>>(false, null, null);
    }
}
