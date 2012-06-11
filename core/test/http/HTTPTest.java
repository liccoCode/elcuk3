package http;

import helper.HTTP;
import org.apache.commons.io.FileUtils;
import org.junit.Test;
import play.test.UnitTest;

import java.io.File;
import java.io.IOException;

/**
 * Created by IntelliJ IDEA.
 * User: wyattpan
 * Date: 6/11/12
 * Time: 4:23 PM
 */
public class HTTPTest extends UnitTest {
    @Test
    public void test404Page() throws IOException {
        //http://www.amazon.co.uk/dp/B0042FL0CG
        FileUtils.writeStringToFile(new File("/Users/wyattpan/elcuk2-data/404.html"), HTTP.get("http://www.amazon.co.uk/dp/B0042FL0CG"));
    }
}
