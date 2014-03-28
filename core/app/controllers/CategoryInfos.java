package controllers;

import helper.J;
import helper.Webs;
import models.SaleTarget;
import models.User;
import models.product.Category;
import models.view.Ret;
import models.view.highchart.HighChart;
import org.apache.commons.lang.StringUtils;
import org.joda.time.DateTime;
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
        List<Category> cates = User.getTeamCategorys(user);
        Category ca = cates.get(0);
        if(StringUtils.isNotBlank(id)) ca = Category.findById(id);
        render(cates, ca);
    }

    /**
     * 加载某一个 Category 的全年每个月的销售额和已经完成的销售额
     */
    public static void ajaxCategorySalesAmount(String id) {
        try {
            HighChart chart = SaleTarget.ajaxHighChartCategorySalesAmount(id);
            renderJSON(J.json(chart));
        } catch(Exception e) {
            renderJSON(new Ret(Webs.E(e)));
        }
    }

    /**
     * 加载某一个 Category 的全年每个月的利润率和已经完成的利润率
     */
    public static void ajaxCategorySalesProfit(String id) {
        try {
            HighChart chart = SaleTarget.ajaxHighChartCategorySalesProfit(id);
            renderJSON(J.json(chart));
        } catch(Exception e) {
            renderJSON(new Ret(Webs.E(e)));
        }
    }
}
