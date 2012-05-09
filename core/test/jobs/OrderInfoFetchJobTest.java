package jobs;

import helper.Constant;
import helper.HTTP;
import models.market.Orderr;
import org.apache.commons.io.FileUtils;
import org.apache.commons.lang.StringUtils;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.junit.Test;
import play.Logger;
import play.test.UnitTest;

import java.io.File;
import java.io.IOException;

/**
 * Created by IntelliJ IDEA.
 * User: wyattpan
 * Date: 5/9/12
 * Time: 3:12 PM
 */
public class OrderInfoFetchJobTest extends UnitTest {

    /**
     * 0. 登陆
     * 1. 下载文件
     * 2. 解析
     */
//    @Test 1
    public void orderDetail() throws IOException {
        Orderr ord = Orderr.findById("203-6007669-8343509");
        String url = ord.account.type.orderDetail(ord.orderId);
        Logger.info("OrderInfo(UserId) [%s].", url);
        String html = HTTP.get(url);
        FileUtils.writeStringToFile(new File(Constant.HOME + "/elcuk2-data/" + ord.orderId + "_email.html"), html);
        Document doc = Jsoup.parse(html);
        ord.orderDetailUserIdAndEmail(doc);
    }

    @Test
    public void testDownloadFile() throws IOException {
        String html = FileUtils.readFileToString(new File("/Users/wyattpan/elcuk2-data/203-6007669-8343509_email.html"));
        int head = StringUtils.indexOfIgnoreCase(html, "buyerEmail:");
        int end = StringUtils.indexOfIgnoreCase(html, "targetID:");
        String sub = html.substring(head + 14, end).trim();
        System.out.println("[" + sub.substring(0, sub.length() - 2) + "]");
    }

    @Test
    public void testOrderDetailUserId() throws IOException {
        String html = FileUtils.readFileToString(new File("/Users/wyattpan/elcuk2-data/203-6007669-8343509_email.html"));
        Document doc = Jsoup.parse(html);
        Orderr ord = Orderr.findById("203-6007669-8343509");

        ord.orderDetailUserIdAndEmail(doc);
        assertEquals("547hqxp7bxk5tzk@marketplace.amazon.co.uk", ord.email);
        assertEquals("A1BUFNIMGMLXIU", ord.userid);
        ord.save();
    }
}
