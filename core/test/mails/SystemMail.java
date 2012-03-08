package mails;

import helper.Webs;
import org.junit.Test;
import play.test.UnitTest;

/**
 * Created by IntelliJ IDEA.
 * User: wyattpan
 * Date: 3/8/12
 * Time: 1:19 PM
 */
public class SystemMail extends UnitTest {
    @Test
    public void sendSuccessble() {
        Webs.systemMail("SYSTEM MAIL TEST!", "dfjkjkdjfkdjfkjdjfkj");
    }
}
