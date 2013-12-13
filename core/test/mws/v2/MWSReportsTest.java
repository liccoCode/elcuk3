package mws.v2;

import factory.FactoryBoy;
import models.market.Account;
import models.market.JobRequest;
import org.junit.Before;
import org.junit.Test;
import play.test.UnitTest;
import util.DateHelper;

import java.io.File;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.CoreMatchers.notNullValue;

/**
 * 测试新版本的 Report API
 * User: wyatt
 * Date: 12/13/13
 * Time: 4:43 PM
 */
public class MWSReportsTest extends UnitTest {
    @Before
    public void setUp() {
        FactoryBoy.deleteAll();
    }

    //    @Test
    public void requestReport() {
        Account account = FactoryBoy.create(Account.class, "de");
        JobRequest job = new JobRequest(account, JobRequest.T.ALL_FBA_ORDER_SHIPPED, DateHelper.beforeDays(10));

        new MWSReports().requestReport(job);
        assertThat(job.requestId, is(notNullValue()));
        assertThat(job.state, is(JobRequest.S.REQUEST));
        //7130341850
        System.out.println(job);
    }

    @Test
    public void reportChecker() {
        Account account = FactoryBoy.create(Account.class, "de");
        JobRequest job = new JobRequest(account, JobRequest.T.ALL_FBA_ORDER_SHIPPED, DateHelper.beforeDays(10));
        job.requestId = "7130341850";

        new MWSReports().checkReportRequest(job);
        assertThat(job.state, is(JobRequest.S.DONE));
        assertThat(job.reportId, is(notNullValue()));
        System.out.println(job);
    }

    public void downloadReport() {
        Account account = FactoryBoy.create(Account.class, "de");
        JobRequest job = new JobRequest(account, JobRequest.T.ALL_FBA_ORDER_SHIPPED, DateHelper.beforeDays(10));
        job.requestId = "7130341850";
        job.reportId = "";

        new MWSReports().checkReportRequest(job);
        assertThat(new File(job.path).exists(), is(true));
        assertThat(job.state, is(JobRequest.S.END));
        System.out.println(job.path);
    }
}
