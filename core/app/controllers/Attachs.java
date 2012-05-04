package controllers;

import helper.Constant;
import models.product.Attach;
import play.libs.Images;
import play.mvc.Controller;
import play.utils.FastRuntimeException;

import java.io.File;

/**
 * 没有限制
 * User: wyattpan
 * Date: 5/4/12
 * Time: 2:53 PM
 */
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
}
