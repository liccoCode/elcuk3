package controllers;

import models.finance.Apply;
import models.finance.ProcureApply;
import play.mvc.Controller;
import play.mvc.With;

import java.util.List;

/**
 * 所有的请款单控制器
 * User: wyatt
 * Date: 3/26/13
 * Time: 3:53 PM
 */
@With({GlobalExceptionHandler.class, Secure.class})
public class Applys extends Controller {

    @Check("applys.index")
    public static void index() {
        List<Apply> applyes = ProcureApply.find("ORDER BY createdAt DESC").fetch();
        render(applyes);
    }

    /**
     * 采购请款单
     */
    @Check("applys.procure")
    public static void procure(Long id) {
        ProcureApply apply = ProcureApply.findById(id);
        render(apply);
    }

    public static void procureConfirm(Long id) {
        ProcureApply apply = ProcureApply.findById(id);
        apply.confirm = true;
        apply.save();
        render();
    }
}
