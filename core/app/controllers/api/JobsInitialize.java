package controllers.api;

import jobs.PmDashboard.AbnormalFetchJob;
import jobs.categoryInfo.CategoryInfoFetchJob;
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
     * 周期：一天处理1到两次
     */
    public static void initAbnormalFetchJob() {
        GJob.perform(AbnormalFetchJob.class.getName(), new HashMap<String, Object>());
        Cache.add(AbnormalFetchJob.RUNNING, AbnormalFetchJob.RUNNING);
    }

    /**
     * CategoryInfo 数据处理 Job
     * 周期：一天处理1到两次
     */
    public static void initCategoryInfoFetchJob() {
        GJob.perform(CategoryInfoFetchJob.class.getName(), new HashMap<String, Object>());
        Cache.add(CategoryInfoFetchJob.RUNNING, CategoryInfoFetchJob.RUNNING);
    }
}
