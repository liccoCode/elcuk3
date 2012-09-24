package jobs;

import models.market.Listing;
import org.joda.time.DateTime;
import play.Logger;
import play.jobs.Job;

import java.util.List;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

/**
 * 更新系统中的所有 Listing 信息; 与 ListingDriverlJob 配合为典型的 "生产者<->消费者"模式;
 * <p/>
 * 拥有一个更新队列, 这个任务为同时触发 创建需要更新的 Listing 与 消耗队列中的 Listing 的工作;
 * 不让线程自我不停的运行是因为通过此任务可以停止 Listing 的更新
 * <p/>
 * User: wyattpan
 * Date: 12/29/11
 * Time: 12:39 AM
 */
public class ListingSchedulJob extends Job {
    // 生产者
    private Lock lock = new ReentrantLock();

    @Override
    public void doJob() throws Exception {
        /**
         * 1. check init Listing queue
         * 2. check is need reload queue?
         */
    }
}
