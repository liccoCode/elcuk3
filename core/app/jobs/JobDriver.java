package jobs;

import models.Jobex;
import play.Logger;
import play.jobs.Every;
import play.jobs.Job;
import play.libs.Time;

import java.util.List;
import java.util.concurrent.TimeUnit;

/**
 * Created by IntelliJ IDEA.
 * User: wyattpan
 * Date: 12/29/11
 * Time: 1:04 AM
 */
@Every("5s")
public class JobDriver extends Job {
    @Override
    public void doJob() throws Exception {
        Logger.info("JobDriver Start...");
        List<Jobex> jobs = Jobex.findAll();
        for(Jobex job : jobs) {
            if(job.close) {
                Logger.debug("Job %s is closed", job.className);
                continue;
            }
            if(job.duration == null || job.duration.trim().isEmpty()) {
                Logger.error("Job %s duration is invalid!", job.className);
                continue;
            }
            long now = System.currentTimeMillis();
            if(TimeUnit.MILLISECONDS.toSeconds(now - job.lastUpdateTime) >= Time.parseDuration(job.duration)) {
                job.newInstance().now();
                job.lastUpdateTime = now;
                job.save();
            } else Logger.debug("Skip Job %s", job.className);
        }
        Logger.info("JobDriver Stop...");
    }
}
