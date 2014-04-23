package jobs;

import jobs.driver.DriverJob;
import play.Logger;
import play.Play;
import play.exceptions.UnexpectedException;
import play.jobs.Job;
import play.jobs.JobsPlugin;
import play.libs.Expression;
import play.libs.Time;

import java.util.concurrent.TimeUnit;

/**
 * 在 Dev 环境下, 自动启动 JobsPlugin 导致在代码修改 rleoad 的时候, 正常的 request 会与
 * 这些 Job 的执行在 Play.detectChanges() 时发生方法同步, 当 job 过多的时候并且执行时间过长的时候
 * , request 总会被同步阻塞着, 无法立马 reload , 所以将系统中的 job 全部调整为手动启动.
 * User: wyatt
 * Date: 3/27/13
 * Time: 11:50 AM
 */
public class JobsSetup {
    private static int jobs = 0;

    public static void init() {
        boolean isprodjob = isProdJob();
        boolean isdevJob = isDevJob();

        /**
         * 因cookie信息不是序列化，无法保存到redis,则将此job运行
         */
        every(KeepSessionJob.class, "29mn");
        if(isprodjob || isdevJob) {
            // 手动的将所有的需要的 Job 启动

            //TODO 所有的 Job 全部转移到 Crontab 中, 通过页面给予生成 crontab 然后泵更新 crontab 配置文件

            // Order Deal 5 step
            every(AmazonOrderDiscover.class, "1mn");
            every(AmazonOrderItemDiscover.class, "30s");
            every(AmazonOrderUpdateJob.class, "1h");
            every(AmazonOrderFetchJob.class, "1h");
            every(OrderInfoFetchJob.class, "1mn");

            // Amazon Review Job 的处理
            // TODO 还有两个未知的家伙:  ReviewPromise, ReviewMailCheckPromise
            every(AmazonReviewCrawlJob.class, "1mn");
            every(AmazonReviewCheckJob.class, "1mn");

            // Feedback Job 处理
            every(FeedbackCrawlJob.class, "30mn");
            every(FeedbackCheckJob.class, "5mn");

            every(OrderMailCheck.class, "10mn");
            every(AmazonFBACapaticyWatcherJob.class, "30mn");
            every(AmazonFBAQtySyncJob.class, "5mn");
            every(AmazonSellingSyncJob.class, "1h");
            every(AmazonFBAInventoryReceivedJob.class, "20mn");
            every(CheckerProductCheckJob.class, "1d");
            every(FAndRNotificationJob.class, "1h");
            every(AmazonFinanceCheckJob.class, "1mn");
            every(ListingDriverlJob.class, "1s");
            every(ListingSchedulJob.class, "1mn");
            every(SellingCategoryCheckerJob.class, "1d");
            every(SellingRecordCheckJob.class, "5mn");
            every(ShipmentSyncJob.class, "5mn");
            new DriverJob().now();
            Logger.info("JobPlguin setup %s jobs.", JobsSetup.jobs);
        }
    }

    /**
     * JobsPlguin 中 @Every 处理方式, 拿出来手动处理.
     *
     * @param expression
     * @param clazz
     */
    public static void every(Class<?> clazz, String expression) {
        try {
            Job job = (Job) clazz.newInstance();
            JobsPlugin.scheduledJobs.add(job);
            String value = expression;
            if(value.startsWith("cron.")) {
                value = Play.configuration.getProperty(value);
            }
            value = Expression.evaluate(value, value).toString();
            if(!"never".equalsIgnoreCase(value)) {
                JobsPlugin.executor.scheduleWithFixedDelay(job, Time.parseDuration(value),
                        Time.parseDuration(value), TimeUnit.SECONDS);
            }
            jobs++;
        } catch(InstantiationException ex) {
            throw new UnexpectedException("Cannot instanciate Job " + clazz.getName());
        } catch(IllegalAccessException ex) {
            throw new UnexpectedException("Cannot instanciate Job " + clazz.getName());
        }
    }

    /**
     * 正式环境，并且设置了执行job则可执行
     */
    public static boolean isProdJob() {
        if(Play.mode.isProd() && "true".equals(Play.configuration.getProperty("jobs.prod"))) {
            return true;
        } else {
            return false;
        }
    }

    /**
     * 开发环境，并且设置了执行job则可执行
     *
     * @return
     */
    public static boolean isDevJob() {
        if(Play.mode.isDev() && "true".equals(Play.configuration.getProperty("jobs.dev"))) {
            return true;
        } else {
            return false;
        }
    }

}
