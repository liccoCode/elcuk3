package market;

import models.market.Orderr;
import org.junit.Test;
import play.test.UnitTest;

import java.io.File;

/**
 * Created by IntelliJ IDEA.
 * User: wyattpan
 * Date: 1/6/12
 * Time: 4:23 PM
 */
public class OrderParseTest extends UnitTest {
    @Test
    public void testParse() {
        Orderr.parseALLOrderXML(new File("/Users/wyattpan/elcuk-data/2011/10/11/8141580584.xml"));
    }
}
