package controllers;

import helper.Constant;
import helper.J;
import helper.Webs;
import models.product.Attach;
import models.view.Ret;
import org.apache.commons.io.FileUtils;
import play.Logger;
import play.libs.Images;
import play.mvc.Controller;
import play.mvc.With;
import play.utils.FastRuntimeException;

import java.io.File;
import java.util.List;

/**
 * 没有限制
 * User: wyattpan
 * Date: 5/4/12
 * Time: 2:53 PM
 */
@With({GlobalExceptionHandler.class})
public class Attachs extends Controller {

    public static void image(Attach a, Integer w, Integer h) {
        Attach attach = Attach.findAttach(a);
        if(attach != null) {
            if(w == null || h == null)
                renderBinary(attach.file);
            else {
                File resizedImag = new File(String.format("%s/%s", Constant.TMP, attach.fileName));
                Images.resize(attach.file, resizedImag, w, h, true);
                renderBinary(resizedImag);
            }
        } else
            throw new FastRuntimeException("No File Found.");
    }

    /**
     * 给 Product 关联图片
     *
     * @param a
     */
    public static void upload(Attach a) {
        a.setUpAttachName();
        Logger.info("%s File save to %s.[%s kb]", a.fid, a.location, a.fileSize / 1024);
        try {
            FileUtils.copyFile(a.file, new File(a.location));
            a.save();
        } catch(Exception e) {
            renderJSON(new Ret(Webs.E(e)));
        }
        renderJSON(J.G(a));
    }

    public static void rm(Attach a) {
        Attach attach = Attach.findAttach(a);
        if(attach == null) renderJSON(new Ret("系统中不存在."));
        try {
            attach.rm();
        } catch(Exception e) {
            renderJSON(new Ret(Webs.E(e)));
        }
        renderJSON(new Ret());
    }

    public static void images(String fid) {
        List<Attach> imgs = Attach.find("fid=?", fid).fetch();
        renderJSON(J.G(imgs));
    }
}
