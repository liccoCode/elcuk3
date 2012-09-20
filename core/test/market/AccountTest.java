package market;

import helper.Constant;
import helper.HTTP;
import helper.Webs;
import models.market.Account;
import models.market.M;
import models.market.Selling;
import org.apache.commons.io.FileUtils;
import org.apache.http.message.BasicNameValuePair;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.junit.Before;
import org.junit.Test;
import play.test.UnitTest;

import java.io.File;
import java.io.IOException;
import java.util.Arrays;
import java.util.concurrent.TimeUnit;

/**
 * Created by IntelliJ IDEA.
 * User: Wyatt
 * Date: 12-1-7
 * Time: 上午6:31
 */
public class AccountTest extends UnitTest {
    Account acc;
    Selling sell;

    @Before
    public void setAcc() {
        acc = Account.findById(131l);

        sell = new Selling();
        sell.merchantSKU = "80-qw1a56-be";
        sell.asin = "B005JSG7GE";
        sell.market = acc.type;
        sell.account = acc;
        sell.sellingId = sell.sid();
        Selling dbSell = Selling.findById(sell.sellingId);
        if(dbSell != null) sell = dbSell;
    }

    @Test
    public void readAccount() throws ClassNotFoundException, IOException {
        try {
            // 登陆并且缓存登陆
            //testSellerCentralLogIn
            Webs.dev_login(acc);
        } catch(Exception e) {
            e.printStackTrace();
            //ignore
        }
    }

    //    @Test
    public void testBriefFlatFinance() {
        // 测试下载销售记录文件
        acc.briefFlatFinance(M.AMAZON_US);
    }

    //    @Test
    public void testsellerCentralHomePage() throws IOException {
        // 测试登陆 Amazon 后台
        String html = HTTP.get(acc.cookieStore(), acc.type.sellerCentralHomePage());
        assertTrue(Account.isLoginEnd(Jsoup.parse(html)));
    }

    //    @Test
    public void testamazonSiteLogin() {
        // 测试登陆 Amazon 前台
        assertTrue(acc.loginAmazonSize(M.AMAZON_US));
    }

    //    @Test
    public void testFnSkuLabel() throws IOException {
        // 测试下载 FBA label
        FileUtils.writeByteArrayToFile(new File(Constant.HOME + "/X000DDLRY3.pdf"), HTTP.postDown(acc.cookieStore(), acc.type.fnSkuDownloadLink(), Arrays.asList(
                new BasicNameValuePair("qty.0", "27"), // 一页打 44 个
                new BasicNameValuePair("fnSku.0", "X000DDLRY3"),
                new BasicNameValuePair("mSku.0", "80-qw1a56-be"),
                new BasicNameValuePair("labelType", "ItemLabel_A4_27")
        )));
        File file = new File(Constant.HOME + "/X000DDLRY3.pdf");
        assertTrue(file.exists());
        // 这个文件至少在 3k 以上
        assertTrue(file.length() > 3000);
        // 文件最后更新时间在 10 s 的误差内
        assertTrue(Math.abs(System.currentTimeMillis() - file.lastModified()) < TimeUnit.SECONDS.toMillis(10));
    }

    //    @Test
    public void testlistingEditPage() throws IOException {
        // 测试访问修改 Listing 页面
        String html = HTTP.get(acc.cookieStore(), M.listingEditPage(sell));
        Document doc = Jsoup.parse(html);
        assertNotNull(doc.select("form[name=productForm]"));
    }

    //    @Test
    public void testSyncFromAmazon() {
        // 测 Amazon 同步回数据
        sell.syncFromAmazon();
    }

    //    @Test
    public void testlistingPostPage() {
        // 更新 Listing
        sell.deploy();
    }

    @Test
    public void testuploadImageLink() {
        // 上传图片
    }

    //    @Test
    public void testremoveImageLink() {
        // 删除图片
    }


    //    @Test
    public void testsaleSellingLink() {
        // 上架 Listing 第一步, 选择 Category
    }

    //    @Test
    public void testsaleSellingPostLink() {
        // 上架 Listing 第二步, 上架
    }
}
