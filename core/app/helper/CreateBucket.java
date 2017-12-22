package helper;

import com.qiniu.util.Auth;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;

import java.io.IOException;

/**
 * Created by IntelliJ IDEA.
 * User: Even
 * Date: 17/10/13
 * Time: PM5:41
 */
public class CreateBucket {



    private static final String accessKey = "JgQLOVYJKRuj8JlFRu2jbZ5T9MU4UR12LshWzmKR";
    private static final String secretKey = "u1om-WYy_0pJYkqszjJJou2minprsXcirM_GJqXQ";
    private static final String bucketName = "EASYACC-MATERIALPURCHASES";

    Auth auth = Auth.create(accessKey, secretKey);

    /**
     * 创建空间
     */
    public void createBucket() {
        String path = "/mkbucketv2/" + bucketName + "/public/0";
        String access_token = auth.sign(path);
        System.out.println(access_token);

        String url = "http://rs.qiniu.com/mkbucketv2/" + bucketName + "/public/0";


        OkHttpClient client = new OkHttpClient();
        Request request = new Request.Builder().url(url)
                .addHeader("Content-Type", "application/x-www-form-urlencoded")
                .addHeader("Authorization", "QBox " + access_token).build();
        Response re = null;
        try {
            re = client.newCall(request).execute();
            if(re.isSuccessful() == true) {
                System.out.println(re.code());
                System.out.println(re.toString());
            } else {
                System.out.println(re.code());
            }
        } catch(IOException e) {
            e.printStackTrace();
        }
    }

    /**
     * 测试
     *
     * @param args
     */
    public static void main(String[] args) {
        new CreateBucket().createBucket();
    }


}
