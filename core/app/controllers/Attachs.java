package controllers;

import controllers.api.SystemOperation;
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
@With({GlobalExceptionHandler.class, SystemOperation.class})
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

    public static void uploadForBase64(Attach.P p, String fid, String base64File, String originName) {
        Attach a = new Attach(p, fid);
        try {
            a.file = Attach.generateImageByBase64(base64File, originName);
            a.setUpAttachName();
            FileUtils.copyFile(a.file, new File(a.location));
            a.save();
        } catch(Exception e) {
            renderJSON(new Ret(Webs.E(e)));
        }
        renderJSON(new Ret("上传成功!"));
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

    public static void images(String fid, String p) {
        // 如果 fid 能够唯一定位则可以只使用 fid, 如果 fid 无法直接定位, 则需要借助
        List<Attach> imgs = Attach.attaches(fid, p);
        renderJSON(J.G(imgs));
    }


    public static void explorerImages(String fid) {
        List<java.util.Map<String, String>> imgs = Attach.attachImages(fid);
        renderJSON(J.G(imgs));
    }

}
