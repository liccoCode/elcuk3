package jobs;

import helper.Webs;
import models.Jobex;
import play.Logger;
import play.Play;
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
@Every("3s")
public class JobDriver extends Job {
    @Override
    public void doJob() throws Exception {
        if(Play.mode.isDev()) {
            Logger.debug(String.format("In Dev mode JobDriver is not running..."));
            return;
        }
        Logger.debug("JobDriver Start...");
        int all = 0;
        int success = 0;
        int skip = 0;
        int close = 0;
        try {
            List<Jobex> jobs = Jobex.findAll();
            all = jobs.size();
            for(Jobex job : jobs) {
                if(job.close) {
                    Logger.debug("Job %s is closed", job.className);
                    close++;
                    continue;
                }
                if(job.duration == null || job.duration.trim().isEmpty()) {
                    Logger.error("Job %s duration is invalid!", job.className);
                    skip++;
                    continue;
                }
                long now = System.currentTimeMillis();
                if(TimeUnit.MILLISECONDS.toSeconds(now - job.lastUpdateTime) >= Time.parseDuration(job.duration)) {
                    try {
                        job.newInstance().now();
                        job.lastUpdateTime = now;
                        job.save();
                        success++;
                    } catch(Exception e) {
                        Logger.error("Job %s: %s", job.className, Webs.E(e));
                    }
                } else {
                    Logger.debug("Skip Job %s", job.className);
                }
            }
        } finally {
            Logger.debug("JobDriver Stop...");
            Logger.info("All %s, Success %s, Skip %s, Closed %s", all, success, skip, close);
        }
    }
}
