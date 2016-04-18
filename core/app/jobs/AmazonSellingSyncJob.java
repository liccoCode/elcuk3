package jobs;

import helper.LogUtils;
import helper.Webs;
import models.Jobex;
import models.market.*;
import models.product.Product;
import org.apache.commons.io.FileUtils;
import org.apache.commons.lang.StringUtils;
import org.apache.commons.lang.math.NumberUtils;
import play.Logger;
import play.jobs.Job;
import play.libs.F;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

/**
 * <pre>
 * 用来将 Amazon 上的 Selling 数据同步到系统中, 参照 SKU 关联;
 * 这个任务一般会是手动执行.
 * 周期:
 * - 轮询周期: 1h
 * - Duration: 24h
 * - Job Interval: 10h
 * </pre>
 * User: wyattpan
 * Date: 4/6/12
 * Time: 4:29 PM
 * @deprecated
 */
public class AmazonSellingSyncJob extends Job implements JobRequest.AmazonJob {

    @Override
    public void doJob() {
        long begin = System.currentTimeMillis();
        if(!Jobex.findByClassName(AmazonSellingSyncJob.class.getName()).isExcute()) return;
        /**
         * 1. 找到所有的 Account 并根据所有支持的 MarketPlace 申请同步的文件
         * 2. 下载回列表后进行系统内更新
         */
        List<Account> accs = Account.openedSaleAcc();
        // 只需要三个账号 3 个市场的 Active Listing
        for(Account acc : accs) {
            JobRequest job = JobRequest.checkJob(acc, this, acc.marketplaceId());
            if(job == null) continue;
            job.request();
        }

        Logger.info("AmazonSellingSyncJob step1 done!");

        // 3. 更新状态的 Job
        JobRequest.updateState(type());
        Logger.info("AmazonSellingSyncJob step2 done!");

        // 4. 获取 ReportId
        JobRequest.updateReportId(type());
        Logger.info("AmazonSellingSyncJob step3 done!");

        // 5. 下载 report 文件
        JobRequest.downLoad(type());
        Logger.info("AmazonSellingSyncJob step4 done!");

        // 6. 处理下载好的文件
        JobRequest.dealWith(type(), this);
        Logger.info("AmazonSellingSyncJob step5 done!");
        if(LogUtils.isslow(System.currentTimeMillis() - begin,"AmazonSellingSyncJob")) {
            LogUtils.JOBLOG.info(String
                    .format("AmazonSellingSyncJob calculate.... [%sms]", System.currentTimeMillis() - begin));
        }
    }

    @Override
    public void callBack(JobRequest jobRequest) {
        F.T2<List<Selling>, List<Listing>> sellAndListingTuple = AmazonSellingSyncJob
                .dealSellingFromActiveListingsReport(new File(jobRequest.path), jobRequest.account,
                        jobRequest.marketplaceId.market());
        for(Listing lst : sellAndListingTuple._2) lst.save();
        for(Selling sell : sellAndListingTuple._1) sell.save();
    }

    @Override
    public JobRequest.T type() {
        return JobRequest.T.ACTIVE_LISTINGS;
    }

    /**
     * 每 10 个小时才允许创建同步一次 Selling
     *
     * @return
     */
    @Override
    public int intervalHours() {
        return 10;
    }


    /**
     * 处理 Amazon 的 Active Listing Report 文档, 如果有新 Listing/Selling 则与系统进行同步处理.
     * 如果系统中有的, Amazon 上没有, 则先不做处理.
     *
     * @return Amazon 上拥有, 但系统中没有的 Selling
     */
    public static F.T2<List<Selling>, List<Listing>> dealSellingFromActiveListingsReport(File file,
                                                                                         Account acc,
                                                                                         M market) {
        //TODO 这个方法需要进行修改
        F.T2<List<Selling>, List<Listing>> sellAndListingTuple = new F.T2<List<Selling>, List<Listing>>(
                new ArrayList<Selling>(), new ArrayList<Listing>());
        List<String> lines = null;
        try {
            lines = FileUtils.readLines(file);
        } catch(IOException e) {
            Logger.warn("File [%s] IO Error!", file.getAbsolutePath());
            return sellAndListingTuple;
        }

        lines.remove(0); // 删除第一行的标题

        for(String line : lines) {
            String[] args = StringUtils.splitPreserveAllTokens(line, "\t");

            /**
             * 1. 解析出 Listing, 并且将 Listing 绑定到对应的 Product 身上
             *  a. 注意需要先查找系统中是否有对应的 Listing, 如果有则不做处理
             *  b. 如果没有对应的 Listing 那么则创建一个新的 Listing 并且保存下来;(Listing 的详细信息等待抓取线程自己去进行更新)
             *
             * 2. 创建 Selling, 因为这份文件是自己的, 所以接触出来的 Listing 数据就是自己的 Selling
             *  a. 注意需要先查找系统中是否有, 有的话则不做处理
             *  b. 没有的话则创建 Selling 并且绑定 Listing
             */

            //de: item-name	item-description	listing-id	seller-sku	price	quantity	open-date	image-url	item-is-marketplace	product-id-type	zshop-shipping-fee	item-note	item-condition	zshop-category1	zshop-browse-path	zshop-storefront-feature	asin1	asin2	asin3	will-ship-internationally	expedited-shipping	zshop-boldface	product-id	bid-for-featured-placement	add-delete	pending-quantity	fulfillment-channel
            //uk: item-name	item-description	listing-id	seller-sku	price	quantity	open-date	image-url	item-is-marketplace	product-id-type	zshop-shipping-fee	item-note	item-condition	zshop-category1	zshop-browse-path	zshop-storefront-feature	asin1	asin2	asin3	will-ship-internationally	expedited-shipping	zshop-boldface	product-id	bid-for-featured-placement	add-delete	pending-quantity	fulfillment-channel
            String t_asin = args[16].trim();
            String t_msku = args[3].trim().toUpperCase();
            String t_title = args[0].trim();
            String t_price = args[4].trim();
            String t_fulfilchannel = args[26].trim();
            try {

                // 如果属于 UnUsedSKU 那么则跳过这个解析
                if(Product.unUsedSKU(t_msku)) continue;
                // 过滤掉 msku 含有 ,2 的, 这些不需要
                if(t_msku.contains(",2")) continue;

                String lid = Listing.lid(t_asin, market);
                Listing lst = Listing.findById(lid);
                Product prod = Product.findByMerchantSKU(t_msku);
                if(prod == null) {
                    String warnMsg =
                            "[Warnning!] Listing[" + lid + "] Missing Product[" + t_msku + "].";
                    Logger.warn(warnMsg);
                    Webs.systemMail(warnMsg,
                            String.format("Listing %s Missing Product %s.", lid, t_msku));
                    continue;// 如果 Product 不存在, 需要跳过这个 Listing!
                }

                if(lst != null) Logger.info("Listing[%s] is exist.", lid);
                else {
                    lst = new Listing();
                    lst.listingId = lid;
                    lst.market = market;
                    lst.asin = t_asin;
                    lst.product = prod;
                    lst.title = t_title;
                    lst.displayPrice = NumberUtils.toFloat(t_price);
                    lst.lastUpdateTime = System.currentTimeMillis();
                    sellAndListingTuple._2.add(lst);
                }

                String sid = Selling.sid(t_msku, market, acc);
                Selling selling = Selling.findById(sid);
                if(selling != null) Logger.info("Selling[%s] is exist.", sid);
                else {
                    selling = new Selling();
                    selling.sellingId = sid;
                    selling.asin = lst.asin;
                    selling.aps.condition_ = "NEW";
                    selling.market = market;
                    selling.merchantSKU = t_msku;

                    selling.aps.title = lst.title;
                    selling.account = acc;
                    selling.shippingPrice = 0f;
                    selling.aps.standerPrice = lst.displayPrice;
                    selling.ps = 2f;
                    selling.state = Selling.S.SELLING;
                    selling.listing = lst;
                    sellAndListingTuple._1.add(selling);
                }
            } catch(Exception e) {
                String warMsg =
                        "Skip Add one Listing/Selling. asin[" + t_asin + "_" + market.toString() +
                                "]";
                Logger.warn(line);
                Webs.systemMail(warMsg, String.format("%s <br/>%n%s", line, Webs.E(e)));
            }
        }
        return sellAndListingTuple;
    }
}
