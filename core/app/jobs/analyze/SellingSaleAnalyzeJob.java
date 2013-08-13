package jobs.analyze;

import models.view.post.AnalyzePost;
import play.Logger;
import play.cache.Cache;
import play.jobs.Job;

/**
 * 周期:
 * 轮询: 每个 8h 一次
 * 为系统后台任务
 * <p/>
 * User: wyatt
 * Date: 8/13/13
 * Time: 3:06 PM
 */
public class SellingSaleAnalyzeJob extends Job {
    @Override
    public void doJob() {
        /**
         * 1. 北京时间最近 1 周
         * 2. 两种类型 sid/sku
         */
        // 清理掉原来的
        Cache.delete(AnalyzePost.AnalyzeDTO_SID_CACHE);
        Cache.delete(AnalyzePost.AnalyzeDTO_SKU_CACHE);

        AnalyzePost post = new AnalyzePost();
        post.type = "sid";
        long begin = System.currentTimeMillis();
        post.analyzes();
        Logger.info("SellingSaleAnalyzeJob calculate Sellings.... [%sms]", System.currentTimeMillis() - begin);

        post.type = "sku";
        begin = System.currentTimeMillis();
        post.analyzes();
        Logger.info("SellingSaleAnalyzeJob calculate SKU.... [%sms]", System.currentTimeMillis() - begin);
    }
}
