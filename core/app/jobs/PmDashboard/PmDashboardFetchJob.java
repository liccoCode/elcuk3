package jobs.PmDashboard;

import helper.LogUtils;
import jobs.driver.BaseJob;
import org.apache.commons.lang.StringUtils;
import play.cache.Cache;
import play.utils.FastRuntimeException;
import query.PmDashboardCache;

/**
 * PM 首页异常信息处理 Job
 * <p/>
 * 会将所有的异常的数据计算出来缓存到 Redis
 * 然后在controller内根据条件去缓存获取对应的即可
 * <p/>
 * User: mac
 * Date: 14-3-21
 * Time: PM2:03
 * @deprecated
 */
public class PmDashboardFetchJob extends BaseJob {

    @SuppressWarnings("unchecked")
    @Override
    public void doit() {

        if(getContext().get("teamid") == null)
            throw new FastRuntimeException("没有提交 teamid 信息");
        String teamid = getContext().get("teamid").toString();
        String runningname = "pmdashboardfetchjob_running" + teamid;
        if(isRnning(runningname)) return;

        Long id = new Long(teamid);

        Object year = getContext().get("year");
        int calyear = 0;
        if(year!=null) {
            calyear = Integer.parseInt((String)year);
        }

        long begin = System.currentTimeMillis();
        Cache.add(runningname, runningname);
        PmDashboardCache.doTargetCache(id, calyear);
        Cache.delete(runningname);
        if(LogUtils.isslow(System.currentTimeMillis() - begin,"PmDashboardFetchJob")) {
        LogUtils.JOBLOG
            .info(String.format("PmDashboardFetchJob calculate.%s... [%sms]", id,System.currentTimeMillis() - begin));
        }
    }

    public static boolean isRnning(String runningname) {
        return StringUtils.isNotBlank(Cache.get(runningname, String.class));
    }


}
