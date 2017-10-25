package helper;

import com.google.gson.Gson;
import com.qiniu.common.QiniuException;
import com.qiniu.common.Zone;
import com.qiniu.http.Response;
import com.qiniu.storage.BucketManager;
import com.qiniu.storage.Configuration;
import com.qiniu.storage.UploadManager;
import com.qiniu.storage.model.DefaultPutRet;
import com.qiniu.util.Auth;


/**
 * Created by IntelliJ IDEA.
 * User: Even
 * Date: 17/10/13
 * Time: AM9:52
 */
public class QiniuUtils {

    private QiniuUtils() {
    }

//    private static final String accessKey = System.getenv("AWS_SQS_QUEUE");
//    private static final String secretKey = System.getenv("AWS_SQS_QUEUE");
//    private static final String bucket = System.getenv("AWS_SQS_QUEUE");

    private static final String accessKey = "yaaXtE79fydbE-ar7Mro7cjBQUzYqx9FmEZylEVN";
    private static final String secretKey = "BMwtTVa5g9-_DsgJfBd5XXNYmUp4OXNav0kNoavm";
    private static final String bucket = "elcuk2";


    private static Configuration cfg;
    private static Auth auth;

    public static synchronized void init() {
        //构造一个带指定Zone对象的配置类
        cfg = new Configuration(Zone.zone2());
        auth = Auth.create(accessKey, secretKey);
    }


    /**
     * 上传文件
     *
     * @param fileName
     * @param uploadBytes
     * @throws Exception
     */
    public static void upload(String fileName, byte[] uploadBytes) throws Exception {
        UploadManager uploadManager = new UploadManager(cfg);
        //默认不指定key的情况下，以文件内容的hash值作为文件名
        String key = fileName;
        try {
            String upToken = auth.uploadToken(bucket);
            try {
                Response response = uploadManager.put(uploadBytes, key, upToken);
                //解析上传成功的结果
                DefaultPutRet putRet = new Gson().fromJson(response.bodyString(), DefaultPutRet.class);
            } catch(QiniuException ex) {
                Response r = ex.response;
                System.err.println(r.toString());
                try {
                    System.err.println(r.bodyString());
                } catch(QiniuException ex2) {
                    //ignore
                }
            }
        } catch(Exception ex) {
            ex.printStackTrace();
        }
    }


    /**
     * 删除文件
     *
     * @param fileName
     * @throws Exception
     */
    public static void delete(String fileName) {
        BucketManager bucketManager = new BucketManager(auth, cfg);
        try {
            bucketManager.delete(bucket, fileName);
        } catch(QiniuException ex) {
            //如果遇到异常，说明删除失败
            System.err.println(ex.code());
            System.err.println(ex.response.toString());
        }
    }


    /**
     * 删除文件
     * @throws Exception
     */
    public static String  getBucketUrl() {
        BucketManager bucketManager = new BucketManager(auth, cfg);
        try {
            String[] urls = bucketManager.domainList(bucket);
            if(urls.length >0) return urls[0];
        } catch(QiniuException ex) {
            //如果遇到异常，说明删除失败
            System.err.println(ex.code());
            System.err.println(ex.response.toString());
        }
        return  null;
    }
    

}
