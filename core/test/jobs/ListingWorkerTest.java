package jobs;

import org.junit.Test;
import play.test.UnitTest;

/**
 * Created by IntelliJ IDEA.
 * User: wyattpan
 * Date: 5/15/12
 * Time: 10:59 AM
 */
public class ListingWorkerTest extends UnitTest {

    @Test
    public void testReviewWorker() {
        new ListingWorkers.R("B005UCSA46_amazon.co.uk").now();
    }
}
