import org.junit.Test;
import play.test.UnitTest;

/**
 * Created by IntelliJ IDEA.
 * User: mac
 * Date: 13-3-26
 * Time: 下午3:51
 */
public class UserTest extends UnitTest {

    @Test
    public void testMath() {
        System.out.println(Math.ceil((float) 50 / 3));
        int a = (int) Math.ceil((float) 50 / 3);
        assertEquals(17, a);
    }
}
