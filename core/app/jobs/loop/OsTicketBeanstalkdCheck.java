package jobs.loop;

import com.trendrr.beanstalk.BeanstalkClient;
import com.trendrr.beanstalk.BeanstalkException;
import com.trendrr.beanstalk.BeanstalkJob;
import com.trendrr.beanstalk.BeanstalkPool;
import helper.Webs;
import jobs.promise.OsTicketSavePromise;
import jobs.promise.OsTicketSyncPromise;
import org.apache.commons.lang.StringUtils;
import org.apache.commons.lang.math.NumberUtils;
import play.Logger;
import play.Play;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.Executors;
import java.util.concurrent.ThreadPoolExecutor;

/**
 * 系统监听 OsTicket 与系统之间交互的 Beanstalkd 的消息, 并对得到的消息进行处理
 * User: wyattpan
 * Date: 11/8/12
 * Time: 1:30 PM
 */
public class OsTicketBeanstalkdCheck implements Runnable {

    // 只允许一个这样的线程存在
    private static final ThreadPoolExecutor T = (ThreadPoolExecutor) Executors
            .newFixedThreadPool(1);

    public static void begin() {
        if(T.getActiveCount() < 1) {
            T.submit(OsTicketBeanstalkdCheck.INSTANCE);
            Logger.info("OsTicketBeanstalkdCheck start...");
        }
    }

    public static void stop() {
        T.shutdownNow();
        T.getQueue().clear();
    }

    public static void deleteJob(String tube, BeanstalkJob job) {
        try {
            //TODO 需要将链接调整为相同的
            job.getClient().watchTube(tube);
            job.getClient().deleteJob(job);
            job.getClient().close();
        } catch(BeanstalkException e) {
            //ignore
        }
    }

    private static OsTicketBeanstalkdCheck INSTANCE = new OsTicketBeanstalkdCheck();

    private BeanstalkPool pool = null;
    private List<String> tubes = new ArrayList<String>();
    private boolean running = true;

    private OsTicketBeanstalkdCheck() {
        /**
         * 1. 加载需要监听的 Queue
         * 2. 初始化第一个与 Beanstalkd 的链接.(采用类似 Reactor pattern, 主线程不会阻塞, 所以一个足够)
         * 3. 初始化第二个与 Beanstalkd 的链接, 将与 beanstlakd 的处理同步进行.
         */
        String queues = Play.configuration.getProperty("beanstlakd.queues");
        if(Play.mode.isDev()) {
            tubes.addAll(Arrays.asList("osticket.new.dev", "osticket.sync.dev"));
        } else {
            if(StringUtils.isBlank(queues))
                tubes.addAll(Arrays.asList("osticket.new", "osticket.sync"));
            else
                tubes.addAll(Arrays.asList(StringUtils.split(queues, ",")));
        }

        this.pool = new BeanstalkPool(
                Play.configuration.getProperty("beanstalkd.url", "r.easya.cc"),
                NumberUtils.toInt(Play.configuration.getProperty("beanstalkd.port", "11300")),
                // 暂时还不会达到每秒 5 个任务那么多
                5);
        try {
            for(int i = 0; i < this.pool.getPoolSize(); i++) {
                BeanstalkClient c = this.pool.getClient();
                c.ignoreTube("default");
                c.close();
            }
        } catch(BeanstalkException e) {
            // 无法链接, 直接停掉应用, 认为检查
            Logger.error("Beanstalkd Conn is error! Please Check it and restart app.");
            this.running = false;
        }

        Logger.info("Begin watching tubes: %s", StringUtils.join(this.tubes, ","));
    }

    /**
     * 不断自循环, 找到一个任务就派发出去
     */
    public void watching() {
        for(String tube : this.tubes) {
            // 因为一个 Conn 需要处理多个 Tube, 所以阻塞时间越短越好.(这里只能设置 1s - -||)
            BeanstalkJob job = null;
            BeanstalkClient c = null;
            try {
                c = this.pool.getClient();
                c.watchTube(tube);
                Logger.info("Wating tube %s ...", tube);
                job = c.reserve(1);
            } catch(BeanstalkException e) {
                //ignore.. just retry
            } catch(Exception e) {
                // 在这里发生 BeanstalkDisconnectedException 表示链接有问题; 邮件提醒
                Webs.systemMail("Beanstalkd 连接出现问题.",
                        "通过 e.easya.cc 链接 r.easya.cc 11300 端口的 beanstalkd 连接出现问题, " +
                                "请查看每个服务器的 /etc/hosts 文件, 检查 ip 正常, 检查 iptables 是否开放 11300 端口给源 " +
                                "ip; " + Webs.E(e));
            }
            if(job != null)
                this.dispatch(tube, job);
            else {
                if(c != null)
                    c.close();
            }
        }
    }

    /**
     * 派发到不同的线程去 Play Job 去执行
     *
     * @param tube Beanstalkd 所使用的 tube
     * @param job  需要处理的任务
     */
    private void dispatch(String tube, BeanstalkJob job) {
        tube = tube.toLowerCase();
        //TODO 这里就写成固定的对两个 Queue 的处理, 如果需要更加通用, 再重新进行抽象处理
        // osticket.new
        if(StringUtils.startsWith(tube, "osticket") && StringUtils.contains(tube, "new")) {
            new OsTicketSavePromise(tube, job).now();
        } else if(StringUtils.startsWith(tube, "osticket") && StringUtils.contains(tube, "sync")) {
            new OsTicketSyncPromise(tube, job).now();
        }
    }


    @Override
    public void run() {
        // 如果不运行, 直接结束线程
        if(!this.running) return;
        while(true) {
            this.watching();
        }
    }

    // TODO BeanstalkJob 的任务一些东西 API 不够丰富, 需要进行一些封装再使用
}
