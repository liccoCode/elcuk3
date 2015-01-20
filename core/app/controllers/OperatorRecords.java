package controllers;

import models.market.OperatorConfig;
import play.mvc.Controller;
import play.mvc.With;

import java.util.List;

/**
 * 运营报表控制器
 * Created by IntelliJ IDEA.
 * User: mac
 * Date: 15-1-20
 * Time: AM11:13
 */
@With({GlobalExceptionHandler.class, Secure.class})
public class OperatorRecords extends Controller {

    /**
     * 报表相关参数设置
     */
    @Check("paymentunits.destroy")
    public static void config() {
        List<OperatorConfig> configurations = OperatorConfig.findAll();
        render(configurations);
    }
}
