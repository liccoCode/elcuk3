package controllers.product;

import models.product.Product;
import play.data.validation.Valid;
import play.mvc.Controller;

/**
 * Created by IntelliJ IDEA.
 * User: Wyatt
 * Date: 12-1-7
 * Time: 上午11:57
 */
public class Products extends Controller {

    public static void c(@Valid Product p) {
        p.save();
        renderJSON(p);
    }

}
