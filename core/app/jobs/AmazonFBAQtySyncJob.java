package jobs;

import helper.GTs;
import helper.Webs;
import models.market.JobRequest;
import models.market.Selling;
import models.market.SellingQTY;
import models.product.Product;
import models.product.Whouse;
import org.apache.commons.lang.StringUtils;
import org.apache.commons.lang.math.NumberUtils;
import play.Logger;
import play.jobs.Job;
import play.libs.IO;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * 针对 Amazon FBA 仓库的库存的同步
 * User: wyattpan
 * Date: 2/7/12
 * Time: 11:32 AM
 */
public class AmazonFBAQtySyncJob extends Job implements JobRequest.AmazonJob {

    @Override
    public void doJob() throws Exception {
        /**
         * 1. 从所有 Whouse 中找出 FBA 的 Whouse.
         * 2. 通过 Whouse 关联的 Account 获取账号的  accessKey 等参数向 FBA 申请库存情况列表
         * 3. 下载回列表后进行系统内更新
         */
        List<Whouse> whs = Whouse.find("type=?", Whouse.T.FBA).fetch();
        for(Whouse wh : whs) {
            if(wh.account == null) {
                Logger.warn("Whouse [" + wh.name + "] is FBA but is not bind an Account right now!!");
            } else {
                if(wh.account.closeable) {
                    Logger.warn("Whouse [%s], Account [%s] is closed.", wh.name, wh.account.uniqueName);
                    return;
                } else {
                    JobRequest job = JobRequest.checkJob(wh.account, this, wh.account.marketplaceId());
                    if(job == null) continue;
                    job.request();
                }
            }
        }
        Logger.info("AmazonFBAQtySyncJob checks Job(step1).");

        // 3. 更新状态的 Job
        JobRequest.updateState(type());
        Logger.info("AmazonOrderFetchJob step2 done!");

        // 4. 获取 ReportId
        JobRequest.updateReportId(type());
        Logger.info("AmazonOrderFetchJob step3 done!");

        // 5. 下载 report 文件
        JobRequest.downLoad(type());
        Logger.info("AmazonOrderFetchJob step4 done!");

        // 6. 处理下载好的文件
        JobRequest.dealWith(type(), this);
        Logger.info("AmazonOrderFetchJob step5 done!");
    }

    @Override
    public void callBack(JobRequest jobRequest) {
        Whouse wh = Whouse.find("account=?", jobRequest.account).first();
        List<SellingQTY> sqtys = AmazonFBAQtySyncJob.fbaCSVParseSQTY(new File(jobRequest.path), wh);
        for(SellingQTY sqty : sqtys) {
            // 解析出来的 SellingQTY, 如果系统中拥有则进行更新, 否则绑定到 Selling 身上
            if(!sqty.isPersistent()) {
                String sid = Selling.sid(sqty.msku(), jobRequest.account.type, jobRequest.account);
                if(filter(sid)) continue;
                try {
                    sqty.attach2Selling(sqty.msku(), wh);
                } catch(Exception e) {
                    String warmsg = "FBA CSV Report hava Selling[" + sid + "] that system can not be found!";
                    Logger.warn(warmsg);
                    Webs.systemMail(warmsg, warmsg + "<br/>\r\n" + Webs.E(e) +
                            ";<br/>\r\n需要通过 Amazon 与系统内的 Selling 进行同步, 处理掉丢失的 Product 与 Selling, 然后再重新进行 FBA 库存的解析.");
                }
            } else {
                sqty.save();
            }
        }
    }

    /**
     * 需要过滤的一些 Amazon FBA 中的遗留 SELLINGID
     */
    public final static Map<String, String> FILTER = GTs.MapBuilder
            .map("71KDT-BPUL-2S,2|A_DE|2", "德国账号 FBA 中无法删除")
            .build();

    /**
     * 是否过滤掉?
     *
     * @param sellingId
     * @return
     */
    public static boolean filter(String sellingId) {
        return FILTER.containsKey(sellingId);
    }


    @Override
    public JobRequest.T type() {
        return JobRequest.T.MANAGE_FBA_INVENTORY_ARCHIVED;
    }

    @Override
    public int intervalHours() {
        return 4; // 4 小时
    }

    /**
     * 从 FBA 的 CSV report 文件中解析出系统中存在或者不存在的 SellingQTY, 但不做保存到数据库的处理.
     *
     * @param file
     * @return
     */
    public static List<SellingQTY> fbaCSVParseSQTY(File file, Whouse wh) {
        List<String> lines = IO.readLines(file);
        lines.remove(0);

        List<SellingQTY> qtys = new ArrayList<SellingQTY>();
        for(String line : lines) {
            String[] vals = StringUtils.splitPreserveAllTokens(line, "\t");

            // 如果属于 UnUsedSKU 那么则跳过这个解析
            if(Product.unUsedSKU(vals[0])) continue;

            String sqtyId = String.format("%s_%s", vals[0].toUpperCase(), wh.id);
            SellingQTY qty = SellingQTY.findById(sqtyId);
            if(qty == null) qty = new SellingQTY(sqtyId);

            qty.inbound = NumberUtils.toInt(vals[16]);
            qty.qty = NumberUtils.toInt(vals[10]);
            qty.unsellable = NumberUtils.toInt(vals[11]);
            qty.pending = NumberUtils.toInt(vals[12]);

            qtys.add(qty);
        }

        return qtys;
    }
}
