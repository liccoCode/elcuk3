package jobs.works;

import org.joda.time.DateTime;
import play.Logger;
import play.Play;
import play.jobs.Job;

/**
 * 用来更新 Listing 的 Review 信息的线程, 这个线程并不需要执行得那么频繁, 基本上每 24 小时执行一次即可.
 * User: wyattpan
 * Date: 4/18/12
 * Time: 4:57 PM
 */
public class ListingWorkers extends Job {
    public enum T {
        /**
         * 抓取 Listing
         */
        L,
        /**
         * 抓取全部的 Offers
         */
        O,
        /**
         * 抓取 Review
         */
        R
    }

    /**
     * 判断是否创建对应的任务是否在此刻执行进行执行
     *
     * @param t   ListingWorkers.T
     * @param lid 需要处理的 Listing 的 id
     */
    public static void goOrNot(T t, String lid) {
        switch(t) {
            case L:
                new ListingWork(lid, false).now(); // 不需要结果, 但需要为每个结果等待 10s
                break;
            case R:
                if(Play.mode.isProd()) {
                    int hourOfDay = DateTime.now().getHourOfDay();
                    if(hourOfDay >= 3 && hourOfDay <= 5) {
                        new ListingReviewWork(lid).doJob();
                    } else {
                        Logger.debug("Hour Of Day [%s] is not within 3~5", hourOfDay);
                    }
                } else {
                    new ListingReviewWork(lid).doJob();
                }
                break;
        }
    }
}
