package jobs;

import helper.LogUtils;
import models.Jobex;
import models.market.Account;
import models.market.JobRequest;
import models.procure.FBAShipment;
import org.apache.commons.lang.StringUtils;
import org.apache.commons.lang.math.NumberUtils;
import org.joda.time.DateTime;
import play.Logger;
import play.jobs.Job;
import play.libs.F;
import play.libs.IO;

import java.io.File;
import java.util.*;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * 下载最近 90 天的入库记录, 用于进行入库跟踪
 * 周期:
 * - 轮询周期: 20mn
 * - Duration: 20mn
 * - Job Interval: 1h
 * User: wyatt
 * Date: 5/14/13
 * Time: 4:20 PM
 * @deprecated
 */
public class AmazonFBAInventoryReceivedJob extends Job implements JobRequest.AmazonJob {
    @Override
    public void doJob() throws Exception {
        long begin = System.currentTimeMillis();
        if(!Jobex.findByClassName(AmazonFBAInventoryReceivedJob.class.getName()).isExcute()) return;
        // 对每一个用户都是如此
        List<Account> accs = Account.openedSaleAcc();
        for(Account acc : accs) {
            JobRequest job = JobRequest.checkJob(acc, this, acc.marketplaceId());
            if(job != null) job.request();
        }

        Logger.info("AmazonFBAInventoryReceivedJob step1 done!");

        // 3. 更新状态的 Job
        JobRequest.updateState(type());
        Logger.info("AmazonFBAInventoryReceivedJob step2 done!");

        // 4. 获取 ReportId
        JobRequest.updateReportId(type());
        Logger.info("AmazonFBAInventoryReceivedJob step3 done!");

        // 5. 下载 report 文件
        JobRequest.downLoad(type());
        Logger.info("AmazonFBAInventoryReceivedJob step4 done!");

        // 6. 处理下载好的文件
        JobRequest.dealWith(type(), this);
        Logger.info("AmazonFBAInventoryReceivedJob step5 done!");
        if(LogUtils.isslow(System.currentTimeMillis() - begin,"AmazonFBAInventoryReceivedJob")) {
            LogUtils.JOBLOG.info(String
                    .format("AmazonFBAInventoryReceivedJob calculate.... [%sms]", System.currentTimeMillis() - begin));
        }
    }

    @Override
    public void callBack(JobRequest jobRequest) {
        Map<String, Rows> rowsMap = fileToRows(new File(jobRequest.path));
        /**
         * 1. 解析 file 文件成为 Rows 数据结构
         * 2. 根据 FBAShipment 进行一一判断, 同时将 records 记录到 FBAShipment 身上
         */
        List<FBAShipment> fbas = FBAShipment.findBySHipmentIds(rowsMap.keySet());
        for(FBAShipment fba : fbas) {
            fba.syncFromAmazonInventoryRows(rowsMap.get(fba.shipmentId));
        }
    }

    /**
     * 将文本文件转换为 Map[FBA,Rows] 结构
     *
     * @param file
     * @return
     */
    public Map<String, Rows> fileToRows(File file) {
        List<String> lines = IO.readLines(file);
        lines.remove(0);
        Map<String, Rows> fbas = new HashMap<String, Rows>();
        for(String line : lines) {
            // 0: date, 1: fnsku, 2:msku, 3:product_name, 4:qty, 5:fba shipmentId, 6:center_id
            String[] args = StringUtils.splitPreserveAllTokens(line, "\t");
            String shipmentId = args[5];
            String msku = args[2].toUpperCase();
            int qty = NumberUtils.toInt(args[4]);

            Rows rows = fbas.get(shipmentId);
            if(rows == null) {
                rows = new Rows();
                fbas.put(shipmentId, rows);
            }

            if(rows.mskus.containsKey(msku)) {
                rows.mskus.get(msku).addAndGet(qty);
            } else {
                rows.mskus.put(msku, new AtomicInteger(qty));
            }
            rows.records.add(StringUtils.join(
                    Arrays.asList(args[0], args[1], args[2], args[4], args[5], args[6]),
                    "\t"));
        }
        return fbas;
    }

    @Override
    public JobRequest.T type() {
        return JobRequest.T.GET_FBA_FULFILLMENT_INVENTORY_RECEIPTS_DATA;
    }

    @Override
    public int intervalHours() {
        return 1;
    }

    public static class Rows {
        public List<String> records = new ArrayList<String>();
        public Map<String, AtomicInteger> mskus = new HashMap<String, AtomicInteger>();

        public int qty(String msku) {
            if(mskus.get(msku) == null) {
                return 0;
            } else {
                return mskus.get(msku).get();
            }
        }

        /**
         * 获取 Records 中的最早时间
         *
         * @return
         */
        public F.Option<Date> getEarliestDate() {
            return getEarliestDate(this.records);
        }

        public static F.Option<Date> getEarliestDate(List<String> records) {
            if(records == null || records.size() == 0) return F.Option.None();
            List<Date> dates = new ArrayList<Date>();
            for(String record : records) {
                String dateStr = record.split("\t")[0];
                dates.add(DateTime.parse(dateStr).toDate());
            }
            Collections.sort(dates);
            return F.Option.Some(dates.get(0));
        }
    }
}
