package notifiers;

import models.market.Feedback;
import org.junit.Before;
import org.junit.Test;
import play.test.Fixtures;
import play.test.UnitTest;

import java.util.List;

/**
 * Created by IntelliJ IDEA.
 * User: wyatt
 * Date: 12/30/12
 * Time: 3:25 PM
 */
public class SystemMailsTest extends UnitTest {

    @Before
    public void loadFile() {
        Fixtures.deleteDatabase();
        Fixtures.loadModels("/notifiers/SystemMailsTest.yml");
    }

    @Test
    public void testDailyFeedbackMail() {
        List<Feedback> feedbacks = Feedback.all().fetch(50);
        assertEquals(1, feedbacks.size());
        SystemMails.dailyFeedbackMail(feedbacks);
    }
}
