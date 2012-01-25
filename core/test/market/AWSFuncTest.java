package market;

import jobs.OrderFetchJob;
import models.market.Account;
import org.junit.Test;
import play.test.FunctionalTest;

import java.util.HashMap;
import java.util.Map;

/**
 * Created by IntelliJ IDEA.
 * User: wyattpan
 * Date: 1/24/12
 * Time: 12:47 PM
 */
public class AWSFuncTest extends FunctionalTest {
    //    @Before
    public void setAcc() {
        Map<String, String> params = new HashMap<String, String>();
        params.put("a.type", Account.M.AMAZON_UK.name());
        params.put("a.username", "easyacc.eu@gmail.com");
        params.put("a.password", "6XC$X5oY!jj");
        params.put("a.token", "3e3TWsDOt6KBfubRzEIRWZuhSuxa+aRGWvnnjJuf");
        params.put("a.accessKey", "AKIAI6EBPJLG64HWDBGQ");
        params.put("a.merchantId", "AJUR3R8UN71M4");
        POST("/accounts/u", params);
    }

    @Test
    public void downResource() {
        /**
         * 1. 初始化 Account;
         * 2. 检查 JobR
         * 3. 创建 Job
         * 4. 下载 Job
         */
        new OrderFetchJob().now();
        /*
        try {
            Thread.sleep(15000);
            Logger.info("Sleep 10 s...");
        } catch(InterruptedException e) {
            e.printStackTrace();
        }
        new OrderFetchJob().now();

        try {
            Thread.sleep(15000);
        } catch(InterruptedException e) {
            e.printStackTrace();
        }
        new OrderFetchJob().now();

        try {
            Thread.sleep(15000);
        } catch(InterruptedException e) {
            e.printStackTrace();
        }
        new OrderFetchJob().now();
        */
    }

}
