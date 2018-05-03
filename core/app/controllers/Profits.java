package controllers;


import controllers.api.SystemOperation;
import helper.J;
import models.User;
import models.product.Product;
import models.view.post.SellingRecordPost;
import org.joda.time.DateTime;
import play.libs.F;
import play.mvc.Before;
import play.mvc.Controller;
import play.mvc.With;

import java.util.Collections;
import java.util.List;
import java.util.Map;

/**
 * 利润计算
 * User: cary
 * Date: 3/10/14
 * Time: 2:29 PM
 */
@With({GlobalExceptionHandler.class, Secure.class, SystemOperation.class})
public class Profits extends Controller {

    @Before(only = {"index"})
    public static void setUpIndexPage() {
        User user = User.findById(Login.current().id);
        renderArgs.put("categories", user.categories);
        F.T2<List<String>, List<String>> skusToJson = Product.fetchSkusJson();
        renderArgs.put("skus", J.json(skusToJson._2));
    }

    @Check("profits.index")
    public static void index(SellingRecordPost p) {
        List<Map<String, Object>> profits = Collections.emptyList();
        if(p == null) {
            p = new SellingRecordPost();
            p.from = DateTime.now().minusMonths(1).toDate();
        } else {
            profits = p.queryProfits();
        }
        render(profits, p);
    }


}
