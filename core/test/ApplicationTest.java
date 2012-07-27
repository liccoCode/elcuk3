import org.joda.time.DateTime;
import org.joda.time.DateTimeZone;
import org.junit.Test;
import play.mvc.Http.Response;
import play.test.FunctionalTest;

public class ApplicationTest extends FunctionalTest {

    //    @Test
    public void testThatIndexPageWorks() {
        Response response = GET("/");
        assertIsOk(response);
        assertContentType("text/html", response);
        assertCharset(play.Play.defaultWebEncoding, response);
    }

    @Test
    public void testDateTime() {
        System.out.println(DateTimeZone.getDefault().getID());
        // Asia/Shanghai
        // US/Central
        System.out.println(DateTime.now(DateTimeZone.forID("Asia/Shanghai")).toString("yyyy-MM-dd HH:mm:ss"));
        System.out.println(DateTime.now(DateTimeZone.forID("US/Central")).toString("yyyy-MM-dd HH:mm:ss"));
    }


    @Test
    public void getDefaultTimeZone() {
        for(Object key : System.getProperties().keySet()) {
            if(key.toString().equals("java.class.path")) continue;
            System.out.println(String.format("%s => %s", key, System.getProperty(key + "")));
        }

        System.out.println(System.getProperty("user.timezone"));
    }
}