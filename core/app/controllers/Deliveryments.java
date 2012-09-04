package controllers;

import helper.J;
import helper.Webs;
import models.User;
import models.procure.Deliveryment;
import models.procure.Payment;
import models.view.Ret;
import play.Play;
import play.data.validation.Validation;
import play.mvc.Controller;
import play.mvc.Router;
import play.mvc.With;
import play.utils.FastRuntimeException;

import java.util.List;

/**
 * 采购单控制器
 * User: wyattpan
 * Date: 6/19/12
 * Time: 2:29 PM
 */
@With({GlobalExceptionHandler.class, Secure.class, GzipFilter.class})
public class Deliveryments extends Controller {

    /**
     * 从 Procrues#index 页面, 通过选择 ProcureUnit 创建 Deliveryment
     *
     * @param pids
     * @param name
     */
    public static void save(List<Long> pids, String name) {
        validation.required(name);
        if(Validation.hasErrors()) {
            renderJSON(new Ret(Webs.V(Validation.errors())));
        }
        Deliveryment deliveryment = Deliveryment.createFromProcures(pids, name, User.findByUserName(Secure.Security.connected()));
        //TODO 修改为 Deliveryments.show 来查看具体的 Deliveryment
        flash.success("Deliveryment <a href='%s'>%s</a> 创建成功.", Router.getFullUrl("Procures.index"), deliveryment.id);
        renderJSON(new Ret(true, Router.getFullUrl("Procures.index")));
    }

    public static void index() {
        List<Deliveryment> deliveryments = Deliveryment.openDeliveryments();
        render(deliveryments);
    }
}
