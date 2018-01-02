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
import okhttp3.OkHttpClient;
import okhttp3.Request;
import play.Logger;

import java.io.IOException;
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
        cfg = new Configuration(Zone.zone0());
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
            /** 验证bucket是否存在,若不存在则创建bucket **/
            if(!checkBucketExists(bucket)) {
                createBucket(bucket);
            }

            String upToken = auth.uploadToken(bucket);
            try {
                /** 上传文件并解析结果 **/
                Response response = uploadManager.put(uploadBytes, key, upToken);
                DefaultPutRet putRet = new Gson().fromJson(response.bodyString(), DefaultPutRet.class);

                /** 获取七牛云存储空间url **/
                String bucketUrl = getBucketUrl(bucket);

                /** 返回 七牛云网址url+文件名 **/
                return String.format("%s%s/%s", "http://", bucketUrl, putRet.key);

            } catch(QiniuException ex) {
                Response r = ex.response;
                Logger.info(r.toString());
            }
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
        } catch(QiniuException ex) {
            Logger.info(String.format("bucket:[%s]删除文件:[%s]失败,失败code:[%s],详细信息:[%s]", bucket, fileName, ex.code(),
                    ex.response.toString()));
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
        } catch(QiniuException ex) {
            Logger.info(String.format("bucket:[%s]获取url失败,失败code:[%s],详细信息:[%s]", bucket, ex.code(),
                    ex.response.toString()));
        }
        return null;
    }


    /**
     * 验证 bucket 是否存在
     *
     * @param bucketName
     * @return
     * @throws QiniuException
     */
    public static boolean checkBucketExists(String bucketName) throws QiniuException {
        BucketManager bucketManager = new BucketManager(auth, cfg);
        String[] bs = bucketManager.buckets();
        return Arrays.asList(bs).contains(bucketName);
    }

    /**
     * 创建 Bucket
     *
     * @param bucketName
     */
    public static void createBucket(String bucketName) {
        String path = "/mkbucket/" + bucketName + "/public/0\n";
        String access_token = auth.sign(path);

        String url = "http://rs.qiniu.com/mkbucket/" + bucketName + "/public/0";
        OkHttpClient client = new OkHttpClient();
        Request request = new Request.Builder().url(url)
                .addHeader("Content-Type", "application/x-www-form-urlencoded")
                .addHeader("Authorization", "QBox " + access_token).build();
        okhttp3.Response re = null;
        try {
            re = client.newCall(request).execute();
            if(re.isSuccessful()) {
                Logger.info(String.format("bucket:[%s]创建成功...", bucketName));
            } else {
                Logger.info(String.format("bucket:[%s]创建失败,错误码:[%s]...", bucketName, re.code()));
            }
        } catch(IOException e) {
            Logger.info(String.format("bucket:[%s]创建失败,错误码:[%s]...", bucketName, e.toString()));
        }
    }

}
