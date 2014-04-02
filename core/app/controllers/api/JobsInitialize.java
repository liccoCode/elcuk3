package controllers.api;

import jobs.PmDashboard.AbnormalFetchJob;
import jobs.driver.GJob;
import play.cache.Cache;
import play.mvc.Controller;
import play.mvc.With;

import java.util.HashMap;

/**
 * Job初始化入口
 * <p/>
 * User: mac
 * Date: 14-4-2
 * Time: AM9:51
 */
@With(APIChecker.class)
public class JobsInitialize extends Controller {

    /**
     * PM 首页异常处理 Job
     */
    public static void initAbnormalFetchJob() {
        GJob.perform(AbnormalFetchJob.class.getName(), new HashMap<String, Object>());
        Cache.add(AbnormalFetchJob.RUNNING, AbnormalFetchJob.RUNNING);
    }
}
