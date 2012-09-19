package models.market;

import helper.AWS;
import play.Logger;
import play.db.jpa.Model;
import play.utils.FastRuntimeException;

import javax.persistence.Entity;
import javax.persistence.EnumType;
import javax.persistence.Enumerated;
import javax.persistence.OneToOne;
import java.util.Date;
import java.util.List;
import java.util.concurrent.TimeUnit;

/**
 * 任何非同步的请求, 比如向 Amazon 的定时请求订单
 * User: wyattpan
 * Date: 1/23/12
 * Time: 5:29 PM
 */
@Entity
public class JobRequest extends Model {
    public interface AmazonJob {
        /**
         * 回掉的处理函数
         *
         * @param jobRequest
         */
        public void callBack(JobRequest jobRequest);

        /**
         * 表明是哪一种 JobRequest
         *
         * @return
         */
        public T type();

        /**
         * 返回创建这个 Job 的间隔时间
         *
         * @return
         */
        public int intervalHours();
    }


    public enum T {
        /**
         * 用来发现 Amazon 上通过 FBA 新发现的订单
         */
        ALL_FBA_ORDER_FETCH,
        /**
         * 从 Amazon 上抓取会已经发送出去的订单
         */
        ALL_FBA_ORDER_SHIPPED,
        /**
         * 用来同步系统中库存与 FBA 库存的量
         */
        MANAGE_FBA_INVENTORY_ARCHIVED,

        /**
         * 在 Amazon 上活动的 Listing, 这份日志用于同步 SKU 与 Amazon 上的 Listing
         */
        ACTIVE_LISTINGS;

        public String toString() {
            switch(this) {
                case ACTIVE_LISTINGS:
                    return "_GET_MERCHANT_LISTINGS_DATA_";
                case ALL_FBA_ORDER_SHIPPED:
                    return "_GET_AMAZON_FULFILLED_SHIPMENTS_DATA_";
                case MANAGE_FBA_INVENTORY_ARCHIVED:
                    return "_GET_FBA_MYI_ALL_INVENTORY_DATA_";
                case ALL_FBA_ORDER_FETCH:
                default:
                    return "_GET_XML_ALL_ORDERS_DATA_BY_ORDER_DATE_";
            }
        }

    }

    /**
     * 当前这一项任务的处理状态
     */
    public enum S {
        /**
         * CheckJob 创建一个新的
         */
        NEW,
        /**
         * 向网站请求了 Report
         */
        REQUEST,
        /**
         * 网站在处理中, 还需要继续请求
         */
        PROCRESS,
        /**
         * Amazon 处理完成了, 可以进入请求 ReportId 了
         */
        DONE,
        /**
         * 拥有 ReportId 了, 可以进行下载了
         */
        DOWN,
        /**
         * 下载完成了, 结束;
         */
        END,
        /**
         * 这个任务接受了进行关闭
         */
        CLOSE
    }

    @OneToOne
    public Account account;

    public Date requestDate;

    public Date lastUpdateDate;


    @Enumerated(EnumType.STRING)
    public T type;

    @Enumerated(EnumType.STRING)
    public S state = S.NEW;

    // ----- logic relate ----
    public String requestId;

    public String reportId;

    public String procressState;

    @Enumerated(EnumType.STRING)
    public M.MID marketplaceId;

    public String path;

    /*这份 Report 请求的数据的时间段*/
    public Date startDate;
    public Date endDate;


    /**
     * 根据 Account 检查是否需要有不同类型的 Job 创建;
     *
     * @param acc
     * @param ajob
     * @param mid  需要使用哪一个市场的数据;
     * @return
     */
    public static JobRequest checkJob(Account acc, AmazonJob ajob, M.MID mid) {
        return newJob(ajob.intervalHours(), ajob.type(), acc, mid);
    }

    /**
     * 找到最新的指定类型的 JobRequest
     *
     * @param type
     * @param acc
     * @return
     */
    public static JobRequest newEstJobRequest(T type, Account acc, S state) {
        return JobRequest.find("account=? AND type=? AND state=? ORDER BY id DESC", acc, type, state).first();
    }

    /**
     * 具体的检查某个类型的 JobRequest 是否需要进行创建.
     *
     * @param interval 创建下一个 JobRequest 的时间间隔.
     * @param type     这个 JobRequest 的类型
     * @param acc      哪一个账户
     * @return
     */
    private static JobRequest newJob(int interval, T type, Account acc, M.MID mid) {
        if(!acc.type.name().startsWith("AMAZON"))
            throw new FastRuntimeException("Only Amazon Account can have ALL_FBA_ORDER_SHIPPED JOB!");
        JobRequest job = JobRequest.find("account=? AND type=? AND marketplaceId=? ORDER BY requestDate DESC", acc, type, mid).first();

        //先判断 Job 不为空的情况
        if(job == null || (System.currentTimeMillis() - job.requestDate.getTime()) > TimeUnit.HOURS.toMillis(interval)) {
            JobRequest njob = new JobRequest();
            njob.account = acc;
            njob.requestDate = njob.lastUpdateDate = new Date();
            njob.state = S.NEW;
            njob.type = type;
            njob.marketplaceId = mid;
            return njob;
        }
        return null;
    }

    /**
     * 检查这些状态是否是位可以提交的任务
     *
     * @return
     */
    private boolean checkAvailableType() {
        switch(this.type) {
            case ALL_FBA_ORDER_FETCH:
            case ALL_FBA_ORDER_SHIPPED:
            case MANAGE_FBA_INVENTORY_ARCHIVED:
            case ACTIVE_LISTINGS:
                return true;
            default:
                return false;
        }
    }

    /**
     * 发出请求
     */
    public void request() {
        if(checkAvailableType()) {
            Logger.debug("(step1)JobRequest request " + this.type + " REQUEST Job.");
            try {
                AWS.requestReport_step1(this);
            } catch(Exception e) {
                Logger.warn("JobRequest Request Report Error. " + e.getMessage());
            }
        }
    }

    /**
     * 更新 Job 状态
     */
    public static void updateState(T type) {
        List<JobRequest> tobeUpdateState = JobRequest.find("state IN (?,?) AND procressState!='_CANCELLED_' AND type=?",
                JobRequest.S.REQUEST, JobRequest.S.PROCRESS, type).fetch();
        for(JobRequest job : tobeUpdateState) {
            if(job.checkAvailableType()) {
                Logger.debug("(step2)JobRequest request " + job.type + " UPDATE_STATE Job.");
                try {
                    AWS.requestState_step2(job);
                } catch(Exception e) {
                    Logger.warn("JobRequest Update State Error. " + e.getMessage());
                }
            }
        }
    }

    /**
     * 获取 ReportId
     */
    public static void updateReportId(T type) {
        List<JobRequest> tobeFetchReportId = JobRequest.find("state=? AND procressState!='_CANCELLED_' AND type=?",
                JobRequest.S.DONE, type).fetch();
        for(JobRequest job : tobeFetchReportId) {
            if(job.checkAvailableType()) {
                Logger.debug("JobRequest request " + job.type + " UPDATE_REPORTID Job.");
                try {
                    AWS.requestReportId_step3(job);
                } catch(Exception e) {
                    Logger.warn("JobRequest Update Report Error. " + e.getMessage());
                }
            }
        }
    }

    /**
     * 修在文件
     */
    public static void downLoad(T type) {
        List<JobRequest> tobeDownload = JobRequest.find("state=? AND type=?", JobRequest.S.DOWN, type).fetch();
        for(JobRequest job : tobeDownload) {
            if(job.state != S.DOWN) return;
            if(job.checkAvailableType()) {
                Logger.debug("JobRequest request " + job.type + " DOWNLOAD Job.");
                try {
                    AWS.requestReportDown_step4(job);
                } catch(Exception e) {
                    Logger.warn("JobRequest DownLoad Error. " + e.getMessage());
                }
            }
        }
    }

    /**
     * 处理下载好的文件
     */
    public static void dealWith(T type, AmazonJob amazon) {
        List<JobRequest> tobeDeal = JobRequest.find("state=? AND type=?", JobRequest.S.END, type).fetch();
        for(JobRequest job : tobeDeal) {
            amazon.callBack(job);
            job.state = S.CLOSE;
            job.save();
        }
    }
}
