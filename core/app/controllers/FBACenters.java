package controllers;

import controllers.api.SystemOperation;
import helper.J;
import models.procure.FBACenter;
import models.view.Ret;
import play.data.validation.Validation;
import play.mvc.Controller;
import play.mvc.With;

import java.util.List;

/**
 * FBACenter 控制器
 * Created by IntelliJ IDEA.
 * User: duan
 * Date: 11/28/16
 * Time: 2:14 PM
 */
@With({GlobalExceptionHandler.class, Secure.class, SystemOperation.class})
public class FBACenters extends Controller {
    @Check("fbacenters.index")
    public static void index() {
        List<FBACenter> centers = FBACenter.findAll();
        render(centers);
    }

    @Check("fbacenters.index")
    public static void show(Long id) {
        FBACenter center = FBACenter.findById(id);
        notFoundIfNull(center);
        render(center);
    }

    @Check("fbacenters.index")
    public static void update(FBACenter center, Long id) {
        validation.valid(center);
        if(Validation.hasErrors()) {
            center.id = id;
            render("FBACenters/show.html", center);
        } else {
            FBACenter manager = FBACenter.findById(id);
            notFoundIfNull(center);
            manager.update(center);
            flash.success("更新成功!");
            index();
        }
    }

    /**
     * 打开自动同步
     */
    @Check("fbacenters.index")
    public static void enableAutoSync(Long id) {
        FBACenter center = FBACenter.findById(id);
        center.enableAutoSync();
        renderJSON(J.json(new Ret()));
    }

    /**
     * 关闭自动同步
     */
    @Check("fbacenters.index")
    public static void disableAutoSync(Long id) {
        FBACenter center = FBACenter.findById(id);
        center.disableAutoSync();
        renderJSON(J.json(new Ret()));
    }
}
