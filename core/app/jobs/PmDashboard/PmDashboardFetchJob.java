package jobs.PmDashboard;

import helper.DBUtils;
import helper.Dates;
import jobs.driver.BaseJob;
import models.market.Listing;
import models.view.dto.AbnormalDTO;
import org.apache.commons.lang.NumberUtils;
import org.apache.commons.lang.StringUtils;
import org.joda.time.DateTime;
import play.Logger;
import play.cache.Cache;
import play.db.helper.SqlSelect;
import query.PmDashboardCache;
import query.ProductQuery;
import services.MetricProfitService;

import java.util.*;

/**
 * PM 首页异常信息处理 Job
 * <p/>
 * 会将所有的异常的数据计算出来缓存到 Redis
 * 然后在controller内根据条件去缓存获取对应的即可
 * <p/>
 * User: mac
 * Date: 14-3-21
 * Time: PM2:03
 */
public class PmDashboardFetchJob extends BaseJob {
    public static final String RUNNING = "pmdashboardfetchjob_running";
    public static final String AbnormalDTO_CACHE = "pmdashboard_info";

    @SuppressWarnings("unchecked")
    @Override
    public void doit() {
        if(isRnning()) return;
        long begin = System.currentTimeMillis();
        begin = System.currentTimeMillis();
        Cache.add(RUNNING, RUNNING);
        PmDashboardCache.doTargetCache();
        Logger.info("AbnormalFetchJobChart calculate.... [%sms]", System.currentTimeMillis() - begin);
        Cache.delete(RUNNING);
    }

    public static boolean isRnning() {
        return StringUtils.isNotBlank(Cache.get(RUNNING, String.class));
    }

}
