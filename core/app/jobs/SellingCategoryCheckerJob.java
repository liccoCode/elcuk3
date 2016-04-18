package jobs;

import helper.FLog;
import helper.GTs;
import helper.LogUtils;
import helper.Webs;
import models.Jobex;
import models.embedded.AmazonProps;
import models.market.Selling;
import models.product.Category;
import models.product.Product;
import org.joda.time.DateTime;
import play.Play;
import play.jobs.Job;

import java.util.*;

/**
 * TODO : 还需要吗?
 * <pre>
 * 对 Selling 所处在的 Amazon Node 进行检测
 * 周期:
 * - 轮询周期: 1d
 * - Duration: 0 40 1 1,8.15,22,29 * ?
 * </pre>
 * User: wyattpan
 * Date: 6/6/12
 * Time: 3:45 PM
 * @deprecated
 */
public class SellingCategoryCheckerJob extends Job {

    @Override
    public void doJob() {
        long begin = System.currentTimeMillis();
        if(!Jobex.findByClassName(SellingCategoryCheckerJob.class.getName()).isExcute()) return;
        /**
         * 1. 找到所有的 Category
         * 2. 遍历每一个 Category 下的每一个 Product
         * 3. 找到这个 Product 下所有的 Selling
         *  - 根据这个 Category 所建议的 Amazon Node, 如果没有在 Selling 身上找到这些建议的 Amazon Node 则加入缓存, 以便后面发送邮件
         */

        Map<String, Set<Selling>> invalidSelling = new LinkedHashMap<String, Set<Selling>>();

        List<Category> cats = Category.all().fetch();
        for(Category cat : cats) {
            String key = String
                    .format(String.format("%s | %s", cat.categoryId, cat.settings.amazonNode));
            invalidSelling.put(key, new HashSet<Selling>());

            Map<String, String> allowNodes = cat.settings.amazonNodeMap();
            for(Product pro : cat.products) {
                List<Selling> sellings = Selling.find("merchantSKU like ?", pro.sku + "%").fetch();
                for(Selling sell : sellings) {
                    if(sell.state == Selling.S.DOWN) continue;
                    sell.aps.arryParamSetUP(AmazonProps.T.STR_TO_ARRAY);
                    if(sell.aps.rbns == null || sell.aps.rbns.size() == 0) {
                        invalidSelling.get(key).add(sell);
                    } else {
                        for(String node : sell.aps.rbns) {
                            if(!allowNodes.containsKey(node)) invalidSelling.get(key).add(sell);
                        }
                    }
                }
            }
        }

        Webs.systemMail("SellingCategoryCheckerJob invalid Selling Amazon Node.",
                GTs.render("SellingCategoryCheckerJob",
                        GTs.newMap("invalidMap", invalidSelling).build()));
        if(Play.mode.isDev())
            FLog.fileLog(String.format("SellingCategoryCheckerJob.%s.html",
                    DateTime.now().toString("yyyy-MM-dd.HH.mm.ss")),
                    GTs.render("SellingCategoryCheckerJob",
                            GTs.newMap("invalidMap", invalidSelling).build()), FLog.T.JOBS_ERROR);

        if(LogUtils.isslow(System.currentTimeMillis() - begin,"SellingCategoryCheckerJob")) {
            LogUtils.JOBLOG.info(String
                    .format("SellingCategoryCheckerJob calculate.... [%sms]", System.currentTimeMillis() - begin));
        }
    }
}
