import helper.Caches;
import helper.HTTP;
import play.jobs.Job;
import play.jobs.OnApplicationStop;

/**
 * Created by IntelliJ IDEA.
 * User: wyattpan
 * Date: 3/14/12
 * Time: 12:07 PM
 */
@OnApplicationStop
public class Shutdown extends Job {
    @Override
    public void doJob() {
        Caches.clearRunningCacheKey();
        HTTP.stop();
    }
}
