package jobs.analyze;

import helper.Caches;
import models.view.post.SkuProfitPost;
import models.view.report.SkuProfit;
import org.apache.commons.lang.StringUtils;
import play.Logger;
import play.cache.Cache;
import play.jobs.Job;

import java.util.List;

/**
 * Created by licco on 15/12/23.
 */
public class SkuSaleProfitJob extends Job {

    private String RUNNING = "SkuSaleProfitJobRunning";
    private SkuProfitPost post;

    public SkuSaleProfitJob(SkuProfitPost p) {
        this.post = p;
    }

    @Override
    public void doJob() throws Exception {
        String sku_key = "";
        String market_key = "";
        String categories_key = "";
        if(post.sku != null) sku_key = post.sku;
        if(post.pmarket != null) market_key = post.pmarket;
        if(post.categories != null) categories_key = post.categories.toLowerCase();

        String key = Caches.Q.cacheKey("skuprofitpost", post.begin, post.end, categories_key, sku_key, market_key);
        if(isRunning(key)) return;
        Cache.add(key + RUNNING, key + RUNNING);
        //从ES查找SKU的利润
        List<SkuProfit> profits = post.query();
        Cache.add(key, profits, "8h");
        Cache.delete(key + RUNNING);
        Logger.info("skuSaleProfitJob执行结束......");
    }

    /**
     * 确保同一时间只有一个 analyzes 正在计算
     *
     * @return
     */
    public boolean isRunning(String key) {
        return StringUtils.isNotBlank(Cache.get(key + RUNNING, String.class));
    }


}
