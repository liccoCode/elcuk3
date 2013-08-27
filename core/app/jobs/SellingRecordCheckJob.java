package jobs;

import models.Jobex;
import models.market.Account;
import models.market.SellingRecord;
import org.joda.time.DateTime;
import play.Logger;
import play.jobs.Job;

import java.util.List;
import java.util.Set;

/**
 * <pre>
 * 在检查所有 Selling 的 SellingRecord, 如果没有进行计算;
 * 每天的 00:30 计算昨天的数据
 * 周期:
 * - 轮询周期: 5mn
 * - Duration: 0 40 0,23 * * ?
 * </pre>
 * User: wyattpan
 * Date: 5/29/12
 * Time: 4:03 PM
 */
public class SellingRecordCheckJob extends Job {

    public DateTime fixTime;

    @Override
    public void doJob() {
        if(!Jobex.findByClassName(SellingRecordCheckJob.class.getName()).isExcute()) return;
        /**
         * 0. 一 fixTime 为基准时间
         * 1. 下载 5~0(5) 天前的 SellingRecord 数据.
         */
        if(fixTime == null) fixTime = DateTime.now();
        //------- 抓取 Amazon 的数据 (由于抓取 Amazon 的数据是一个整体, 所以最后处理) --------
        /**
         * 找到所有 Amazon 市场的 SellingRecord 数据
         * PS: 只能抓取到两天前的 PageView 数据
         */
        for(int i = -5; i <= 0; i++) {
            SellingRecordCheckJob.amazonNewestRecords(fixTime.plusDays(i));
        }
    }

    /**
     * 抓取 Amazon 某一天的 Selling Record 数据
     *
     * @param fixTime
     */
    public static void amazonNewestRecords(DateTime fixTime) {
        List<Account> accs = Account.openedSaleAcc();
        Set<SellingRecord> records = null;
        // 现在写死, 只有 2 个账户, UK 需要抓取 uk, de; DE 只需要抓取 de
        for(Account acc : accs) {
            records = SellingRecord.newRecordFromAmazonBusinessReports(acc, acc.type, fixTime.toDate());
            Logger.info("Fetch Account(%s) %s records", acc.prettyName(), records.size());
            if(records.size() <= 0) continue;
            // 直接这样处理,因为通过 SellingRecord.newRecordFromAmazonBusinessReports 出来的方法已经存在与 Session 缓存中了.
            for(SellingRecord record : records) {
                record.save();
            }
        }
    }
}
