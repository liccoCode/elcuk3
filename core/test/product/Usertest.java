package product;

import org.junit.Test;
import play.libs.Crypto;
import play.test.UnitTest;

/**
 * Created by IntelliJ IDEA.
 * User: wyattpan
 * Date: 9/24/12
 * Time: 12:31 PM
 */
public class UserTest extends UnitTest {
    @Test
    public void testPassword() {
        System.out.println("Origin: 123");
        System.out.println(Crypto.encryptAES("123"));
    }
}
