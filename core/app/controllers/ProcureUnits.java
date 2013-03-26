package controllers;

import models.procure.ProcureUnit;
import play.mvc.Controller;

/**
 * Created by IntelliJ IDEA.
 * User: wyatt
 * Date: 3/26/13
 * Time: 5:56 PM
 */
public class ProcureUnits extends Controller {
    /**
     * 预付款申请
     *
     * @param id
     */
    public static void billingPrePay(Long id, Long applyId) {
        ProcureUnit unit = ProcureUnit.findById(id);
        unit.billingPrePay();
        Applys.procure(applyId);
    }

    /**
     * 尾款申请
     *
     * @param id
     */
    public static void billingTailPay(Long id, Long applyId) {

    }
}
