package controllers;

import models.User;
import models.product.Category;
import org.apache.commons.lang.StringUtils;
import play.mvc.Controller;
import play.mvc.With;

import java.util.List;

/**
 * Created by IntelliJ IDEA.
 * User: mac
 * Date: 14-3-27
 * Time: AM10:19
 */
@With({GlobalExceptionHandler.class, Secure.class})
public class CategoryInfos extends Controller {

    @Check("categoryinfos.show")
    public static void show(String id) {
        User user = User.findByUserName(Secure.Security.connected());
        List<Category> cates = User.getObjCategorys(user);
        Category ca = cates.get(0);
        if(StringUtils.isNotBlank(id)) ca = Category.findById(id);
        render(cates, ca);
    }
}
