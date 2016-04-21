package controllers.api;

import jobs.PmDashboard.AbnormalFetchJob;
import jobs.PmDashboard.PmDashboardFetchJob;
import jobs.categoryInfo.CategoryInfoFetchJob;
import jobs.driver.GJob;
import models.view.Ret;
import org.apache.commons.lang.StringUtils;
import play.mvc.Controller;
import play.mvc.With;

import java.util.HashMap;
import java.util.Map;

/**
 * Job初始化入口
 * <p/>
 * User: mac
 * Date: 14-4-2
 * Time: AM9:51
 * @deprecated 系统内无人调用
 */
@With(APIChecker.class)
public class JobsInitialize extends Controller {

    /**
     * PM 首页异常处理 Job
     * 周期：一天处理1次  凌晨 8点30执行
     */
    public static void initAbnormalFetchJob() {
        GJob.perform(AbnormalFetchJob.class.getName(), new HashMap<String, Object>());

        renderJSON(new Ret(true, "提交异常信息任务成功!"));

    }

    /**
     * CategoryInfo 数据处理 Job
     * 周期：一天处理1次  凌晨 8点40执行
     */
    public static void initCategoryInfoFetchJob() {
        GJob.perform(CategoryInfoFetchJob.class.getName(), new HashMap<String, Object>());

        renderJSON(new Ret(true, "提交CATEGORY销售分析信息任务成功!"));

    }

    /**
     * 利润目标 数据处理 Job
     * 周期：一天处理1次 ,凌晨 3点执行
     */
    public static void initTargetInfoFetchJob() {

        String teamid = request.params.get("teamid");
        String year = request.params.get("year");
        Map context = new HashMap();
        if(StringUtils.isNotBlank(teamid)) {
            context.put("teamid", teamid);
            if(StringUtils.isNotBlank(year)) {
                context.put("year", year);
            }
            GJob.perform(PmDashboardFetchJob.class.getName(), context);
            renderJSON(new Ret(true, "提交PM首页利润目标信息任务成功!"));
        } else {
            renderJSON(new Ret(true, "未传team的ID值!"));
        }

    }
}
