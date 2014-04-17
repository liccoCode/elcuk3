package controllers.api;

import jobs.PmDashboard.AbnormalFetchJob;
import jobs.categoryInfo.CategoryInfoFetchJob;
import jobs.PmDashboard.PmDashboardFetchJob;
import jobs.driver.GJob;
import models.view.Ret;
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
     * 周期：一天处理1次  凌晨 8点30执行
     */
    public static void initAbnormalFetchJob() {
        GJob.perform(AbnormalFetchJob.class.getName(), new HashMap<String, Object>());
        renderJSON(new Ret(true,"提交异常信息任务成功!"));
    }

    /**
     * CategoryInfo 数据处理 Job
     * 周期：一天处理1次  凌晨 8点40执行
     */
    public static void initCategoryInfoFetchJob() {
        GJob.perform(CategoryInfoFetchJob.class.getName(), new HashMap<String, Object>());
        renderJSON(new Ret(true,"提交CATEGORY销售分析信息任务成功!"));
    }

    /**
     * 利润目标 数据处理 Job
     * 周期：一天处理1次 ,凌晨 3点执行
     */
    public static void initTargetInfoFetchJob() {
        GJob.perform(PmDashboardFetchJob.class.getName(), new HashMap<String, Object>());
        renderJSON(new Ret(true,"提交PM首页利润目标信息任务成功!"));
    }
}
