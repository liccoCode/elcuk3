package jobs.analyze;

import models.view.post.ProfitPost;
import models.view.report.Profit;
import org.apache.commons.lang.StringUtils;
import play.cache.Cache;
import play.jobs.Job;

import java.util.List;

/**
 * 周期:
 * 轮询: 0:20, 7:20, 15:20 三个时间点执行三次
 * 为系统后台任务
 * <p/>
 * User: wyatt
 * Date: 8/13/13
 * Time: 3:06 PM
 */
//@On("0 20 0,7,15 * * ?")
public class SellingProfitSearch extends Job {

    private String RUNNING = "profitsellingsearch_running";
    private ProfitPost post;

    public SellingProfitSearch(ProfitPost p) {
        this.post = p;
    }

    @Override
    public void doJob() {
        String skukey = "";
        String marketkey = "";
        String categorykey = "";
        if(post.sku != null) skukey = post.sku;
        if(post.pmarket != null) marketkey = post.pmarket;
        if(post.category != null) categorykey = post.category.toLowerCase();

        String postkey = helper.Caches.Q.cacheKey("profitpost", post.begin, post.end, categorykey, skukey,
                marketkey);
        if(isRnning(postkey)) return;
        Cache.add(postkey + RUNNING, postkey + RUNNING);
        //从ES查找SKU的利润
        List<Profit> profits = post.query();
        Cache.add(postkey, profits, "8h");
        Cache.delete(postkey + RUNNING);
    }

    /**
     * 确保同一时间只有一个 analyzes 正在计算
     *
     * @return
     */
    public boolean isRnning(String postkey) {
        return StringUtils.isNotBlank(Cache.get(postkey + RUNNING, String.class));
    }
}
