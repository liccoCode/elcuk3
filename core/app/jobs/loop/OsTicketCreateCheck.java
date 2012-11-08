package jobs.loop;

import com.trendrr.beanstalk.BeanstalkClient;
import com.trendrr.beanstalk.BeanstalkException;
import com.trendrr.beanstalk.BeanstalkJob;
import helper.Webs;
import jobs.promise.OsTicketSavePromise;
import org.apache.commons.lang.math.NumberUtils;
import play.Logger;
import play.Play;

import java.util.concurrent.Executors;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

/**
 * 这里的是无限循环的线程, 从项目启动一直运行到项目结束
 * User: wyattpan
 * Date: 11/8/12
 * Time: 1:30 PM
 */
public class OsTicketCreateCheck implements Runnable {

    // 只允许一个这样的线程存在
    private static final ThreadPoolExecutor T = (ThreadPoolExecutor) Executors.newFixedThreadPool(1);

    public static void begin() {
        if(T.getActiveCount() < 1) {
            T.submit(OsTicketCreateCheck.INSTANCE);
            Logger.info("OsTicketCreateCheck start...");
        }
    }

    public static void stop() {
        OsTicketCreateCheck.INSTANCE.bk.close();
        T.shutdownNow();
        T.getQueue().clear();
    }

    private static OsTicketCreateCheck INSTANCE = new OsTicketCreateCheck();

    private BeanstalkClient bk;

    private OsTicketCreateCheck() {
        bk = new BeanstalkClient(
                Play.configuration.getProperty("beanstalkd.url", "r.easya.cc"),
                NumberUtils.toInt(Play.configuration.getProperty("beanstalkd.port", "11300")),
                // 做一次预防, 避免测试环境影响生产环境的 osticket 队列
                Play.mode.isProd() ? "osticket" : "osticket_dev");
    }

    @Override
    public void run() {
        /**
         * 一直循环, 找到一个 Ticket 处理一个, 删除这个 Job
         * - 如果系统中存在, 删除 Job
         * - 如果系统中不存在, 添加后删除 Job
         */
        while(true) {
            BeanstalkJob job = null;
            try {
                //{title=Return policy enquiry from Amazon customer Nazia iqbal, createAt=2012-11-08 13:56:00, ticketId=763353}
                job = bk.reserve(null);
                new OsTicketSavePromise(job).now().get(10, TimeUnit.SECONDS);
            } catch(Exception e) {
                //ignore
                Logger.warn(Webs.E(e));
            } finally {
                if(job != null)
                    try {
                        bk.deleteJob(job);
                    } catch(BeanstalkException e) {
                        //ignore
                    }
            }
        }
    }
}
