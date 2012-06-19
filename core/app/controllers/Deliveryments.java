package controllers;

import helper.Webs;
import models.Ret;
import models.procure.Deliveryment;
import models.product.Attach;
import org.apache.commons.io.FileUtils;
import play.Logger;
import play.mvc.Controller;
import play.mvc.With;

import java.io.File;

/**
 * 采购单控制器
 * User: wyattpan
 * Date: 6/19/12
 * Time: 2:29 PM
 */
@With({FastRunTimeExceptionCatch.class, Secure.class, GzipFilter.class})
public class Deliveryments extends Controller {
    public static void detail(String id) {
        Deliveryment dlmt = Deliveryment.findById(id);
        render(dlmt);
    }

    public static void upload(Attach a) {
        a.setUpAttachName(Attach.P.DELIVERYMENT);
        Logger.info("%s File save to %s.[%s kb]", a.fid, a.location, a.fileSize / 1024);
        try {
            FileUtils.copyFile(a.file, new File(a.location));
            a.save();
        } catch(Exception e) {
            renderJSON(new Ret(Webs.E(e)));
        }
        renderJSON(Webs.exposeGson(a));
    }
}
