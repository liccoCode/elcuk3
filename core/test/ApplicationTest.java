import org.joda.time.DateTime;
import org.joda.time.DateTimeZone;
import org.junit.Test;
import play.mvc.Http.Response;
import play.test.FunctionalTest;

import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;

public class ApplicationTest extends FunctionalTest {

    @Test
    public void t1() throws UnsupportedEncodingException {
        System.out.println(URLDecoder.decode("sku=NzFBU043LUJQVSw2MTUyMzMyNTEwODQ%3D&asyncUid", "UTF-8"));
    }
}