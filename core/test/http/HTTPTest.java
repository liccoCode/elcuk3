package http;

import helper.HTTP;
import org.apache.commons.io.FileUtils;
import org.apache.http.NameValuePair;
import org.apache.http.message.BasicNameValuePair;
import org.junit.Test;
import play.test.UnitTest;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by IntelliJ IDEA.
 * User: wyattpan
 * Date: 6/11/12
 * Time: 4:23 PM
 */
public class HTTPTest extends UnitTest {
    //    @Test
    public void test404Page() throws IOException {
        //http://www.amazon.co.uk/dp/B0042FL0CG
        FileUtils.writeStringToFile(new File("/Users/wyattpan/elcuk2-data/404.html"), HTTP.get("http://www.amazon.co.uk/dp/B0042FL0CG"));
    }

    @Test
    public void googl() {
        List<NameValuePair> param = new ArrayList<NameValuePair>();
        param.add(new BasicNameValuePair("longUrl", "http://e.easyacceu.com/attachs/image?a.fileName=DL|201207|00_31726882.pdf"));
//        param.add(new BasicNameValuePair("key", "AIzaSyC98ClsMbjwD0jp9SGNHMsQ9Twr4G9nqZc"));

        System.out.println(HTTP.postJson("https://www.googleapis.com/urlshortener/v1/url", param));
    }
}
