package jobs;

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
        if(Play.mode.isProd() ||
                (Play.mode.isDev() && "true".equals(Play.configuration.getProperty("jobs.dev")))) {
            // 手动的将所有的需要的 Job 启动

            // Order Deal 5 step
            every(AmazonOrderDiscover.class, "1mn");
            every(AmazonOrderItemDiscover.class, "30s");
            every(AmazonOrderUpdateJob.class, "1h");
            every(AmazonOrderFetchJob.class, "1h");
            every(OrderInfoFetchJob.class, "1mn");

            // Amazon Review Job 的处理
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
            every(KeepSessionJob.class, "5mn");
            every(ListingDriverlJob.class, "1s");
            every(ListingSchedulJob.class, "1mn");
            every(SellingCategoryCheckerJob.class, "1d");
            every(SellingRecordCheckJob.class, "5mn");
            every(ShipmentSyncJob.class, "5mn");

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
}
