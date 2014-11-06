package controllers.api;

import jobs.PmDashboard.PmDashboardFetchJob;
import jobs.driver.GJob;
import models.market.Orderr;
import models.view.Ret;
import org.apache.commons.lang.StringUtils;

import play.mvc.Controller;
import play.mvc.With;

import java.util.HashMap;
import java.util.Map;

/**
 * 检查订单是否存在
 * <p/>
 * User: mac
 * Date: 14-11-6
 * Time: AM9:51
 */
@With(APIChecker.class)
public class OrderCheck extends Controller {

    /**
     * 检查订单是否存在
     */
    public static void order() {
        String orderid = request.params.get("orderid");
        Orderr order = Orderr.findById(orderid);
        if(order != null) {
            renderJSON(new Ret(true, "存在订单!"));
        } else
            renderJSON(new Ret(false, "不存在订单!"));
    }
}
