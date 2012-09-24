package jobs;

import jobs.works.ListingWorkers;
import play.Logger;
import play.jobs.Job;

import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.atomic.AtomicBoolean;


/**
 * 与 ListingSchedulJob 配合使用
 * User: wyattpan
 * Date: 2/9/12
 * Time: 7:04 PM
 */
public class ListingDriverlJob extends Job {
    /**
     * 进行更新使用的队列
     */
    public static LinkedBlockingQueue<String> QUEUE = new LinkedBlockingQueue<String>();

    /**
     * 用来控制线程此线程是否可以继续运行的, 如果任务全部结束, 那么则可以运行, 否则将被跳过这任务
     */
    public static final AtomicBoolean flag = new AtomicBoolean(true);

    private static final Integer CRAW_LISTING_NUM = 3;//一次任务抓取几个 Listing


    @Override
    public void doJob() throws Exception {
        /**
         * 1. 检查是否可以运行这次任务
         * 2. 从队列中依次拿出任务并根据参数进行速度调节
         * 3. 将任务分发到不同的子 Job 中去
         */
        if(!flag.get()) {
            Logger.info("Last Time Job is not DONE yet...");
            return; // FLAG 为 false 则跳过这次任务
        }


        flag.set(false);
        for(int i = 0; i < CRAW_LISTING_NUM; i++) {
            String listingId = QUEUE.poll();
            if(listingId == null) {
                Logger.debug("Wow! Queue is empty!");
                continue;
            }
            // --- Listing 抓取
            ListingWorkers.goOrNot(ListingWorkers.T.L, listingId);
            // --- Review 抓取
            ListingWorkers.goOrNot(ListingWorkers.T.R, listingId);
        }
        flag.set(true);
        Logger.debug("ListingDriverlJob.Queue left " + QUEUE.size());
    }


}
