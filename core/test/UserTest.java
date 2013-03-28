import models.User;
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
    public void testLogin(){
        User usr=new User("rose","123");
        usr.update();
    }
}
