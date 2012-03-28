package noRun;

import models.market.Feedback;
import org.apache.commons.io.FileUtils;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.junit.Test;

import java.io.File;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.util.List;

/**
 * Created by IntelliJ IDEA.
 * User: wyattpan
 * Date: 3/14/12
 * Time: 1:29 PM
 */
public class LoginAmazonTest {
    @Test
    public void testParsePage() throws IOException {
//        String body = FileUtils.readFileToString(new File("/Users/wyattpan/tomcat-logs/homepage.html"));
        String body = FileUtils.readFileToString(new File("/Users/wyattpan/tomcat-logs/afterLogin.html"));
        Document doc = Jsoup.parse(body);
        System.out.println(doc.select("form[name=signin] input").size());
    }

    @Test
    public void parseFeedback() throws IOException {
        List<Feedback> feds = Feedback.parseFeedBackFromHTML(FileUtils.readFileToString(new File("/Users/wyattpan/elcuk2-logs/feedback.p2.html")));
        for(Feedback f : feds) {
            if(f.score >= 3) continue;
            System.out.println(f);
        }
    }

    @Test
    public void testURLEncode() throws UnsupportedEncodingException {
        //http://translate.google.com/?text=
        String ss = "Schutzfolie passt nicht einmal ann&auml;hernd auf ein Sensation XE; Tiete kratzer in der Plastikschale, die das Handy halten soll; Klebestelle zw. Plastik und Kunstleder (oder was das sein sollte) unvollst&auml;ndig was bedeutet, dass wenn ich das Teil benutzt h&auml;tte, sich das &quot;Ledercase&quot; nach wenigen Stunden vom Handy verabschiedet h&auml;tte... zur&uuml;ck an Verk&auml;ufer.";
        System.out.println(URLEncoder.encode(ss));
        System.out.println("==================");
        System.out.println(URLEncoder.encode(ss, "UTF-8"));
    }
}
