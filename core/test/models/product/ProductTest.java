package models.product;

import models.procure.Cooperator;
import org.junit.Test;
import play.libs.Codec;
import play.test.UnitTest;

import java.util.List;

/**
 * Created by IntelliJ IDEA.
 * User: wyattpan
 * Date: 9/3/12
 * Time: 2:38 PM
 */
public class ProductTest extends UnitTest {

    @Test
    public void testCooperators() throws Exception {
        List<Cooperator> cooperators = Cooperator.find("SELECT c FROM Cooperator c, IN(c.cooperItems) ci WHERE ci.sku=? ORDER BY ci.id", "71APNIP-BPU").fetch();
        assertEquals(2, cooperators.size());
        assertEquals("深圳市分寸科技有限公司", cooperators.get(0).fullName);
        assertEquals("深圳市威拓手袋箱包制作有限公司 ", cooperators.get(1).fullName);
    }

    @Test
    public void testBase64() {
        assertEquals("ODAtcXcxYTU2LWJl", Codec.encodeBASE64("80-qw1a56-be"));
        assertEquals("ODAtUVcxQTU2LUJF", Codec.encodeBASE64("80-qw1a56-be".toUpperCase()));
    }
}
