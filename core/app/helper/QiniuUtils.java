package helper;

import com.google.gson.Gson;
import com.qiniu.common.Zone;
import com.qiniu.http.Response;
import com.qiniu.storage.BucketManager;
import com.qiniu.storage.Configuration;
import com.qiniu.storage.UploadManager;
import com.qiniu.storage.model.DefaultPutRet;
import com.qiniu.util.Auth;
import play.Logger;

import java.util.Arrays;


/**
 * Created by IntelliJ IDEA.
 * User: Even
 * Date: 17/10/13
 * Time: AM9:52
 */
public class QiniuUtils {


    private static final String accessKey = System.getenv("QINIU_ACCESS_KEY");
    private static final String secretKey = System.getenv("QINIU_SECRET_KEY");

    private static Configuration cfg;
    private static Auth auth;

    public static void init() {
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
    public static String upload(String fileName, String bucket, byte[] uploadBytes) {
        init();
        UploadManager uploadManager = new UploadManager(cfg);
        String key = fileName; //默认不指定key的情况下，以文件内容的hash值作为文件名
        try {
            String upToken = auth.uploadToken(bucket);
            /** 上传文件并解析结果 **/
            Response response = uploadManager.put(uploadBytes, key, upToken);
            DefaultPutRet putRet = new Gson().fromJson(response.bodyString(), DefaultPutRet.class);

            /** 获取七牛云存储空间url **/
            String bucketUrl = getBucketUrl(bucket);

            /** 返回 七牛云网址url+文件名 **/
            return String.format("%s%s/%s", "http://", bucketUrl, putRet.key);
        } catch(Exception ex) {
            Logger.info(String.format("七牛云上传失败,详细信息:[%s]", ex.toString()));
        }
        return null;
    }


    /**
     * 删除文件
     *
     * @param fileName
     * @throws Exception
     */
    public static void delete(String fileName, String bucket) {
        try {
            init();
            BucketManager bucketManager = new BucketManager(auth, cfg);
            bucketManager.delete(bucket, fileName);
        } catch(Exception ex) {
            Logger.info(String.format("bucket:[%s]删除文件:[%s]失败", bucket, fileName));
        }
    }


    /**
     * 获取七牛云存储空间url
     *
     * @param bucket 存储空间名称
     * @return
     */
    public static String getBucketUrl(String bucket) {
        BucketManager bucketManager = new BucketManager(auth, cfg);
        try {
            String[] urls = bucketManager.domainList(bucket);
            if(urls.length > 0) return urls[0];
        } catch(Exception ex) {
            Logger.info(String.format("bucket:[%s]获取url失败", bucket));
        }
        return null;
    }


    /**
     * 验证 bucket 是否存在
     *
     * @param bucketName
     * @return
     * @throws Exception
     */
    public static boolean checkBucketExists(String bucketName) throws Exception {
        BucketManager bucketManager = new BucketManager(auth, cfg);
        String[] bs = bucketManager.buckets();
        return Arrays.asList(bs).contains(bucketName);
    }

}
