package jobs;

import models.market.JobRequest;
import models.product.Whouse;
import play.Logger;
import play.jobs.Job;

import java.util.List;

/**
 * 针对 Amazon FBA 仓库的库存的同步
 * User: wyattpan
 * Date: 2/7/12
 * Time: 11:32 AM
 */
public class FBAQtySyncJob extends Job {

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
                JobRequest job = JobRequest.checkJob(wh.account, JobRequest.T.MANAGE_FBA_INVENTORY_ARCHIVED);
                if(job == null) continue;
                job.request();
            }
        }
        Logger.info("FBAQtySyncJob checks Job(step1).");

        // 3. 更新状态的 Job
        List<JobRequest> tobeUpdateState = JobRequest.find("state IN (?,?) AND procressState!='_CANCELLED_' AND type=?",
                JobRequest.S.REQUEST, JobRequest.S.PROCRESS, JobRequest.T.MANAGE_FBA_INVENTORY_ARCHIVED).fetch();
        for(JobRequest job : tobeUpdateState) {
            job.updateState();
        }
        Logger.info("OrderFetchJob step2 done!");

        // 4. 获取 ReportId
        List<JobRequest> tobeFetchReportId = JobRequest.find("state=? AND procressState!='_CANCELLED_' AND type=?",
                JobRequest.S.DONE, JobRequest.T.MANAGE_FBA_INVENTORY_ARCHIVED).fetch();
        for(JobRequest job : tobeFetchReportId) {
            job.updateReportId();
        }
        Logger.info("OrderFetchJob step3 done!");

        // 5. 下载 report 文件
        List<JobRequest> tobeDownload = JobRequest.find("state=? AND type=?",
                JobRequest.S.DOWN, JobRequest.T.MANAGE_FBA_INVENTORY_ARCHIVED).fetch();
        for(JobRequest job : tobeDownload) {
            job.downLoad();
        }
        Logger.info("OrderFetchJob step4 done!");

        // 6. 处理下载好的文件
        List<JobRequest> tobeDeal = JobRequest.find("state=? AND type=?",
                JobRequest.S.END, JobRequest.T.MANAGE_FBA_INVENTORY_ARCHIVED).fetch();
        for(JobRequest job : tobeDeal) {
            job.dealWith();
        }
        Logger.info("OrderFetchJob step5 done!");
    }
}
