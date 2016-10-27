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

        if(isprodjob || isdevJob) {
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
        } catch(InstantiationException | IllegalAccessException ex) {
            throw new UnexpectedException("Cannot instanciate Job " + clazz.getName());
        }
    }

    /**
     * 正式环境，并且设置了执行job则可执行
     */
    public static boolean isProdJob() {
        return Play.mode.isProd() && "true".equals(Play.configuration.getProperty("jobs.prod"));
    }

    /**
     * 开发环境，并且设置了执行job则可执行
     */
    public static boolean isDevJob() {
        return Play.mode.isDev() && "true".equals(Play.configuration.getProperty("jobs.dev"));
    }

}
