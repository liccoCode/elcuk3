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
    // 消费者
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
         */
    }


}
