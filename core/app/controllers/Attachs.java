package controllers;

import controllers.api.SystemOperation;
import helper.J;
import helper.QiniuUtils;
import helper.Webs;
import models.product.Attach;
import models.view.Ret;
import org.apache.commons.io.FileUtils;
import org.apache.commons.lang3.StringUtils;
import play.Logger;
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
            File file = new File(attach.location);
            renderBinary(file);
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
            String bucket = String.format("erp-%s", models.OperatorConfig.getVal("brandname")).toLowerCase();
            String fileName = String.format("%s/%s", a.p.name(), a.fileName);
            String url = QiniuUtils.upload(fileName, bucket, a.getBytes());
            a.qiniuLocation = url;
            a.save();
        } catch(Exception e) {
            renderJSON(new Ret(Webs.e(e)));
        }
        renderJSON(J.g(a));
    }

    public static void uploadForBase64(Attach.P p, String fid, String base64File, String originName) {
        Attach a = new Attach(p, fid);
        try {
            a.file = Attach.generateImageByBase64(base64File, originName);
            a.setUpAttachName();
            FileUtils.copyFile(a.file, new File(a.location));
            a.save();
        } catch(Exception e) {
            renderJSON(new Ret(Webs.e(e)));
        }
        renderJSON(new Ret("上传成功!"));
    }

    public static void rm(Attach a) {
        Attach attach = Attach.findAttach(a);
        if(attach == null) renderJSON(new Ret("系统中不存在."));
        try {
            String bucket = String.format("erp-%s", models.OperatorConfig.getVal("brandname")).toLowerCase();
            String fileName = String.format("%s/%s", attach.p.name(), attach.fileName);
            QiniuUtils.delete(fileName, bucket);
            attach.rm();

        } catch(Exception e) {
            renderJSON(new Ret(Webs.e(e)));
        }
        renderJSON(new Ret());
    }

    public static void images(String fid, String p) {
        // 如果 fid 能够唯一定位则可以只使用 fid, 如果 fid 无法直接定位, 则需要借助
        List<Attach> imgs = Attach.attaches(fid, p);
        StringBuilder buff = new StringBuilder();
        buff.append("[");
        for(Attach img : imgs) {
            buff.append("{").append("\"").append("id").append("\"").append(":").append("\"").append(img.id)
                    .append("\"").append(",").append("\"").append("qiniuLocation").append("\"").append(":").append("\"")
                    .append(img.qiniuLocation).append("\"").append(",").append("\"").append("fileName").append("\"")
                    .append(":").append("\"").append(img.fileName).append("\"")
                    .append(",").append("\"").append("outName").append("\"").append(":").append("\"")
                    .append(img.outName).append("\"").append(",").append("\"")
                    .append("originName").append("\"").append(":").append("\"")
                    .append(img.originName)
                    .append("\"").append("},");
        }
        buff.append("]");
        renderJSON(StringUtils.replace(buff.toString(), "},]", "}]"));
    }


    public static void explorerImages(String fid) {
        List<java.util.Map<String, String>> imgs = Attach.attachImages(fid);
        renderJSON(J.g(imgs));
    }


    public static void productSync() {
        File file = new File("/var/www/kod/data/User/elcuk2/home/SKU/");
        File[] listFiles = file.listFiles();
        for(int i = 0; i < listFiles.length; i++) {
            File sku = listFiles[i];
            Logger.info(String.format("当前进度 %s/%s,路径:[%s]", i + 1, listFiles.length, sku.getAbsolutePath()));
            if(sku.isDirectory()) {
                File[] atta = sku.listFiles();
                if(atta != null && atta.length > 0) {
                    for(File a : atta) {
                        //这里将列出所有的文件
                        if(!a.isDirectory() && a.getName().indexOf(".") > -1) {
                            Attach attach = new Attach();
                            attach.file = a;
                            attach.fileName = a.getName();
                            attach.p = Attach.P.SKU;
                            attach.fid = sku.getName();
                            attach.setUpAttachName();

                            String fileName = String.format("%s/%s", attach.p.name(), attach.fileName);
                            String bucket = String.format("erp-%s", models.OperatorConfig.getVal("brandname")).toLowerCase();
                            Logger.info(String.format("开始上传七牛云附件,附件:[%s]",a.getName()));
                            attach.qiniuLocation = QiniuUtils.upload(fileName, bucket, attach.getBytes());
                            attach.save();
                            Logger.info(String.format("完成上传七牛云附件,url:[%s]",attach.qiniuLocation));
                        }
                    }
                }

            }
        }
    }

}
