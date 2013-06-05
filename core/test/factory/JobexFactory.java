package factory;

import factory.annotation.Factory;
import models.Jobex;

import java.util.concurrent.TimeUnit;

/**
 * Created by IntelliJ IDEA.
 * User: wyatt
 * Date: 6/5/13
 * Time: 11:10 AM
 */
public class JobexFactory extends ModelFactory<Jobex> {
    @Override
    public Jobex define() {
        Jobex job = new Jobex();
        job.close = false;
        job.devRun = true;
        job.duration = "1s";
        return job;
    }

    @Factory(name = "financeCheck")
    public Jobex financeCheck() {
        Jobex job = new Jobex();
        job.className = "jobs.AmazonFinanceCheckJob";
        job.close = false;
        job.devRun = true;
        job.duration = "1s";
        job.lastUpdateTime = System.currentTimeMillis() - TimeUnit.SECONDS.toMillis(10);
        return job;
    }
}
