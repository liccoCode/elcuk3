package jobs;

import helper.AWS;
import models.market.Account;
import models.market.JobRequest;
import play.Logger;
import play.jobs.Job;

import java.util.List;

/**
 * 用来将 Amazon 上的 Selling 数据同步到系统中, 参照 SKU 关联
 * User: wyattpan
 * Date: 4/6/12
 * Time: 4:29 PM
 */
public class AmazonSellingSyncJob extends Job {

    @Override
    public void doJob() {

        /**
         * 1. 找到所有的 Account 并根据所有支持的 MarketPlace 申请同步的文件
         * 2. 下载回列表后进行系统内更新
         */
        List<Account> accs = Account.openedAcc();
        // 只需要两个账号 3 个市场的 Active Listing
        for(Account acc : accs) {
            if("AJUR3R8UN71M4".equals(acc.merchantId)) {
                JobRequest job = JobRequest.checkJob(acc, JobRequest.T.ACTIVE_LISTINGS, AWS.MID.A1F83G8C2ARO7P);
                if(job == null) continue;
                job.request();
                job = JobRequest.checkJob(acc, JobRequest.T.ACTIVE_LISTINGS, AWS.MID.A1PA6795UKMFR9);
                if(job == null) continue;
                job.request();
            } else if("A22H6OV6Q7XBYK".equals(acc.merchantId)) {
                JobRequest job = JobRequest.checkJob(acc, JobRequest.T.ACTIVE_LISTINGS, AWS.MID.A1PA6795UKMFR9);
                if(job == null) continue;
                job.request();
            }
        }

        Logger.info("AmazonSellingSyncJob step1 done!");

        // 3. 更新状态的 Job
        List<JobRequest> tobeUpdateState = JobRequest.find("state IN (?,?) AND procressState!='_CANCELLED_' AND type=?)",
                JobRequest.S.REQUEST, JobRequest.S.PROCRESS, JobRequest.T.ACTIVE_LISTINGS).fetch();
        for(JobRequest job : tobeUpdateState) {
            job.updateState();
        }
        Logger.info("AmazonSellingSyncJob step2 done!");

        // 4. 获取 ReportId
        List<JobRequest> tobeFetchReportId = JobRequest.find("state=? AND procressState!='_CANCELLED_' AND type=?",
                JobRequest.S.DONE, JobRequest.T.ACTIVE_LISTINGS).fetch();
        for(JobRequest job : tobeFetchReportId) {
            job.updateReportId();
        }
        Logger.info("AmazonSellingSyncJob step3 done!");

        // 5. 下载 report 文件
        List<JobRequest> tobeDownload = JobRequest.find("state=? AND type=?",
                JobRequest.S.DOWN, JobRequest.T.ACTIVE_LISTINGS).fetch();
        for(JobRequest job : tobeDownload) {
            job.downLoad();
        }
        Logger.info("AmazonSellingSyncJob step4 done!");

        // 6. 处理下载好的文件
        List<JobRequest> tobeDeal = JobRequest.find("state=? AND type=?",
                JobRequest.S.END, JobRequest.T.ACTIVE_LISTINGS).fetch();
        for(JobRequest job : tobeDeal) {
            job.dealWith();
        }
        Logger.info("AmazonSellingSyncJob step5 done!");
    }
}
