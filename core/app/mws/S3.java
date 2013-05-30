package mws;

import helper.Constant;
import org.jets3t.service.S3Service;
import org.jets3t.service.S3ServiceException;
import org.jets3t.service.ServiceException;
import org.jets3t.service.impl.rest.httpclient.RestS3Service;
import org.jets3t.service.model.S3Object;
import org.jets3t.service.security.AWSCredentials;
import play.Logger;
import play.libs.IO;
import play.utils.FastRuntimeException;

import java.io.File;
import java.io.InputStream;
import java.util.HashMap;
import java.util.Map;

/**
 * Created by IntelliJ IDEA.
 * User: wyatt
 * Date: 11/24/12
 * Time: 10:42 PM
 */
public class S3 {
    private S3Service service;
    /**
     * Elcuk2 在 Amazon 上的 bucket Name 与 Password
     */
    public static final String ELCUK2_BUCKET = "elcuk2";

    private static S3 I;


    private S3() {
        try {
            service = new RestS3Service(
                    AWSCredentials.load(ELCUK2_BUCKET, new File("conf/s3.enc")));
        } catch(ServiceException e) {
            Logger.error("!!!!!!! Amazon S3 Can not init !!!!!!!!");
            System.exit(0);
        }
    }

    /**
     * 让 Play 启动的时候也启动,初始化 Amazon S3
     * 也为了让代码能够及早执行
     */
    public static void init() {
        if(S3.I == null) {
            S3.I = new S3();
            Logger.info("Amazon S3 init success");
        } else {
            Logger.warn("No need init S3 onece more.");
        }

    }

    public static S3 I() {
        if(S3.I == null) S3.init();
        return I;
    }


    public File upload(String directory, File file) {
        return this.upload(directory, file, new HashMap<String, String>());
    }

    public File upload(String directory, File file, Map<String, String> metas) {
        S3Object obj = null;
        try {
            if(!directory.endsWith("/")) directory += "/";
            obj = new S3Object(file);
            obj.setKey(directory + file.getName());
            for(Map.Entry<String, String> entry : metas.entrySet()) {
                obj.addMetadata(entry.getKey(), entry.getValue());
            }
        } catch(Exception e) {
            // can not be happed...
        }
        try {
            service.putObject(ELCUK2_BUCKET, obj);
        } catch(S3ServiceException e) {
            throw new FastRuntimeException(e);
        }
        return file;
    }

    /**
     * 下载 Amazon S3 上的文件, 返回下载后的临时文件; ps: 下载已经完成.
     *
     * @param directory
     * @param fileName
     * @return
     */
    public File download(String directory, String fileName) {
        if(!directory.endsWith("/")) directory += "/";
        try {
            S3Object obj = service.getObject(ELCUK2_BUCKET, directory + fileName);
            File tmp = new File(String.format("%s/%s", Constant.TMP, fileName));
            IO.write(obj.getDataInputStream(), tmp);
            return tmp;
        } catch(Exception e) {
            throw new FastRuntimeException(e);
        }
    }

    /**
     * 下载 Amazon S3 上的文件, 返回文件输入留; ps: 下载持续中...
     *
     * @param directory
     * @param fileName
     * @return
     */
    public InputStream stream(String directory, String fileName) {
        if(!directory.endsWith("/")) directory += "/";
        try {
            S3Object obj = service.getObject(ELCUK2_BUCKET, directory + fileName);
            return obj.getDataInputStream();
        } catch(Exception e) {
            throw new FastRuntimeException(e);
        }
    }
}
