package jobs;

import play.jobs.Job;

/**
 * 用来补充性质的更新已经在系统中的 Feedback 的信息;
 * 会加载所有有 OsTicketId 的 Feedback , 然后对她们进行更新:
 * 1. 如果发现 Feedback 消失了, 则记录此 Feedback 被处理完了
 * 2. 同时可以更新 Feedback 记录的最新时间(包含时分秒)
 * User: wyattpan
 * Date: 8/9/12
 * Time: 3:44 PM
 */
public class FeedbackInfoFetchJob extends Job {
    @Override
    public void doJob() {
    }
}
