package jobs;

import play.jobs.Job;

/**
 * Amazon 中用于检查 Finance 信息的任务
 * <p/>
 * User: wyatt
 * Date: 6/4/13
 * Time: 10:39 AM
 */
public class AmazonFinanceCheckJob extends Job {

    @Override
    public void doJob() {
        // 1. 寻找需要处理的订单, 并且按照 market 进行分组
        // 2. 派发给 Promise Job 进行出来.
    }
}
