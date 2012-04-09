package jobs;

import models.market.Account;
import models.market.JobRequest;
import play.Logger;
import play.jobs.Job;

import java.util.List;

/**
 * 每隔一段时间到 Amazon 上进行订单的抓取
 * //TODO 在处理好 Product, Listing, Selling 的数据以后再编写
 * User: Wyatt
 * Date: 12-1-8
 * Time: 上午5:59
 */
public class OrderFetchJob extends Job {
    @Override
    public void doJob() throws Exception {
        // 对每一个用户都是如此
        List<Account> accs = Account.openedAcc();
        /**
         * 1. 检查对应的市场是否需要进行创建新的 Job, 需要则创建, 否则返回 null
         * 2. 处理需要进行发送请求的 Job;
         * 3. 获取需要更新状态的 Job, 并对这些 Job 进行状态更新;
         * 4. 获取需要获取 ReportId 的 Job, 并将这些 Job 进行 ReportId 更新;
         * 5. 获取需要获取 Report 文件的 Job, 并将这些 Job 进行 Report 文件下载.
         */

        // 1,2. 需要创建新的 Job
        for(Account acc : accs) {
            for(JobRequest.T type : JobRequest.T.values()) {
                JobRequest job = JobRequest.checkJob(acc, type, acc.marketplaceId());
                if(job == null) continue;
                job.request();
            }
        }
        Logger.info("OrderFetchJob step1 done!");

        // 3. 更新状态的 Job
        List<JobRequest> tobeUpdateState = JobRequest.find("state IN (?,?) AND procressState!='_CANCELLED_' AND type IN (?,?)",
                JobRequest.S.REQUEST, JobRequest.S.PROCRESS, JobRequest.T.ALL_FBA_ORDER_FETCH, JobRequest.T.ALL_FBA_ORDER_SHIPPED).fetch();
        for(JobRequest job : tobeUpdateState) {
            job.updateState();
        }
        Logger.info("OrderFetchJob step2 done!");

        // 4. 获取 ReportId
        List<JobRequest> tobeFetchReportId = JobRequest.find("state=? AND procressState!='_CANCELLED_' AND type IN (?,?)",
                JobRequest.S.DONE, JobRequest.T.ALL_FBA_ORDER_FETCH, JobRequest.T.ALL_FBA_ORDER_SHIPPED).fetch();
        for(JobRequest job : tobeFetchReportId) {
            job.updateReportId();
        }
        Logger.info("OrderFetchJob step3 done!");

        // 5. 下载 report 文件
        List<JobRequest> tobeDownload = JobRequest.find("state=? AND type IN (?,?)",
                JobRequest.S.DOWN, JobRequest.T.ALL_FBA_ORDER_FETCH, JobRequest.T.ALL_FBA_ORDER_SHIPPED).fetch();
        for(JobRequest job : tobeDownload) {
            job.downLoad();
        }
        Logger.info("OrderFetchJob step4 done!");

        // 6. 处理下载好的文件
        List<JobRequest> tobeDeal = JobRequest.find("state=? AND type IN (?,?)",
                JobRequest.S.END, JobRequest.T.ALL_FBA_ORDER_FETCH, JobRequest.T.ALL_FBA_ORDER_SHIPPED).fetch();
        for(JobRequest job : tobeDeal) {
            job.dealWith();
        }
        Logger.info("OrderFetchJob step5 done!");
    }
}
