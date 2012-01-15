package product;

import org.junit.Test;
import play.test.Fixtures;
import play.test.UnitTest;

/**
 * Created by IntelliJ IDEA.
 * User: wyattpan
 * Date: 1/11/12
 * Time: 5:10 PM
 */
public class CPWSetUp extends UnitTest {
    @Test
    public void setup() {
        Fixtures.deleteDatabase();
        Fixtures.loadModels("product/CPW.yml", "users.yml");
    }
}
