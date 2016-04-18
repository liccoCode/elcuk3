package jobs;

import helper.LogUtils;
import jobs.works.ListingWork;
import models.Jobex;
import org.apache.commons.lang.StringUtils;
import play.Logger;
import play.Play;
import play.jobs.Job;


/**
 * 与 ListingSchedulJob 配合使用
 * Listing 抓取的驱动者, 负责控制 Lisitng 抓取的速度与服务器的选择.
 * 周期:
 * - 轮询周期: 1s
 * - Duration: 3s(后面会调整快)
 * User: wyattpan
 * Date: 2/9/12
 * Time: 7:04 PM
 * @deprecated
 */
public class ListingDriverlJob extends Job {
    // 消费者
    private static final Integer THREADS = 2;//一次任务抓取几个 Listing

    /**
     * 一个 Listing 的抓取需要几秒来处理
     */
    private static final Integer SPEED = 3;

    @Override
    public void doJob() throws Exception {
        long begin = System.currentTimeMillis();
        // 因为 Every 1s 产生的 log 太麻烦, 但 log 我又不能关闭, 需要查看东西, 所以真要在测试环境使用这个执行的时候, 需要注释掉这一行
        if(Play.mode.isDev()) return;
        if(!Jobex.findByClassName(ListingDriverlJob.class.getName()).isExcute()) return;
        /**
         * 根据:
         * 1. 并发数量 n
         * 2. 处理速度 s
         * 计算每一秒需要多少线程, 与控制速度
         */
        int works = THREADS * SPEED;
        for(int i = 0; i < works; i++) {
            // 抓取 Listing 通过需要抓取 offers
            String listingId = ListingSchedulJob.peekListingId();
            if(StringUtils.isBlank(listingId)) continue;
            new ListingWork(ListingSchedulJob.peekListingId(), true).now();
        }
        Logger.info("After Work, Queue size left: %s", ListingSchedulJob.queueSize());
        if(LogUtils.isslow(System.currentTimeMillis() - begin,"ListingDriverlJob")) {
            LogUtils.JOBLOG
                    .info(String.format("ListingDriverlJob calculate.... [%sms]", System.currentTimeMillis() - begin));
        }
    }


}
