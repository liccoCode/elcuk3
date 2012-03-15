package market;

import models.market.JobRequest;
import org.junit.Test;
import play.test.UnitTest;

import java.util.List;

/**
 * Created by IntelliJ IDEA.
 * User: wyattpan
 * Date: 1/26/12
 * Time: 3:32 PM
 */
public class OrderParseJobTest extends UnitTest {

    //    @Test
    public void testJobRequestOrderParse() {
        List<JobRequest> jobs = JobRequest.find("state=?", JobRequest.S.END).fetch();
        for(JobRequest job : jobs) {
            if(job.path == null || job.path.trim().isEmpty()) continue;
            job.dealWith();
        }
    }

    @Test
    public void testOneOrderParseJobRequest() {
        JobRequest job = JobRequest.findById(1163l);
        job.dealWith();
    }
}
