package controllers;

import models.finance.ProcureApply;
import play.mvc.Controller;

/**
 * 所有的请款单控制器
 * User: wyatt
 * Date: 3/26/13
 * Time: 3:53 PM
 */
public class Applys extends Controller {
    /**
     * 采购请款单
     */
    public static void procure(Long id) {
        ProcureApply apply = ProcureApply.findById(id);
        render(apply);
    }
}
