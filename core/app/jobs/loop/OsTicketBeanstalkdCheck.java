package jobs.loop;

import com.trendrr.beanstalk.BeanstalkClient;
import com.trendrr.beanstalk.BeanstalkDisconnectedException;
import com.trendrr.beanstalk.BeanstalkException;
import com.trendrr.beanstalk.BeanstalkJob;
import helper.Webs;
import jobs.promise.OsTicketSavePromise;
import jobs.promise.OsTicketSyncPromise;
import org.apache.commons.lang.StringUtils;
import org.apache.commons.lang.math.NumberUtils;
import play.Logger;
import play.Play;

import java.util.*;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

/**
 * 系统监听 OsTicket 与系统之间交互的 Beanstalkd 的消息, 并对得到的消息进行处理
 * User: wyattpan
 * Date: 11/8/12
 * Time: 1:30 PM
 */
public class OsTicketBeanstalkdCheck implements Runnable {
    /**
     * Beanstalkd 的默认优先级值
     */
    public static Integer DEFAULT_PRI = 1024;

    // 只允许一个这样的线程存在
    private static final ExecutorService T = Executors.newSingleThreadExecutor();
    private static boolean isBegin = false;

    public synchronized static void begin() {
        if(!OsTicketBeanstalkdCheck.isBegin) {
            OsTicketBeanstalkdCheck.isBegin = true;
            T.submit(OsTicketBeanstalkdCheck.getInstance());
            Logger.info("OsTicketBeanstalkdCheck start...");
        }
    }

    public static OsTicketBeanstalkdCheck getInstance() {
        if(OsTicketBeanstalkdCheck.instance == null)
            OsTicketBeanstalkdCheck.instance = new OsTicketBeanstalkdCheck();
        return OsTicketBeanstalkdCheck.instance;
    }

    public static void stop() {
        T.shutdownNow();
        if(OsTicketBeanstalkdCheck.instance != null)
            OsTicketBeanstalkdCheck.instance.closeClients();
    }

    public static void deleteJob(BeanstalkJob job) throws BeanstalkException {
        BeanstalkClient c = job.getClient();
        try {
            c.deleteJob(job);
        } catch(BeanstalkException e) {
            if(e.getMessage().startsWith("NOT_FOUND")) {
                //ignore..
            } else {
                throw e;
            }
        }
    }

    public static void releaseJob(BeanstalkJob job) throws BeanstalkException {
        BeanstalkClient c = job.getClient();
        try {
            c.release(job, DEFAULT_PRI, 10);
        } catch(BeanstalkException e) {
            if(e.getMessage().startsWith("DELETED") || e.getMessage().startsWith("NOT_FOUND")) {
                //ignore
            } else {
                throw e;
            }

        }
    }

    private static OsTicketBeanstalkdCheck instance;

    /**
     * 因 Beanstalkd 的特性, 所以让每一个 tube 拥有一个 Client 去处理
     */
    private Map<String, BeanstalkClient> tubeClient = new HashMap<String, BeanstalkClient>();
    private List<String> tubes = new ArrayList<String>();
    private boolean running = true;

    private OsTicketBeanstalkdCheck() {
        /**
         * 1. 加载需要监听的 Queue
         * 2. 初始化与 Beanstalkd 的连接池
         * 3. 验证每一个链接正常链接, 否则不运行同步操作
         */
        this.loadTubes();
        this.iniClients();
    }

    private void loadTubes() {
        String queues = Play.configuration.getProperty("beanstlakd.queues");
        if(Play.mode.isDev()) {
            tubes.addAll(Arrays.asList("osticket.new.dev", "osticket.sync.dev"));
        } else {
            if(StringUtils.isBlank(queues))
                tubes.addAll(Arrays.asList("osticket.new", "osticket.sync"));
            else
                tubes.addAll(Arrays.asList(StringUtils.split(queues, ",")));
        }
    }

    private void iniClients() {
        /**
         * 使用连接池的问题是因为, 对于处理完成的 Job, 如果需要删除, Beanstalkd 需要原来 reserved 这个
         * Client 的链接去 delete, 否则返回 NOT_FOUND, 所以只能使用 Pool 并且让 Job 一直持有原来
         * Client 的引用
         * refs: https://github.com/kr/beanstalkd/blob/master/doc/protocol.txt#L258
         * (应该没有理解错误吧)
         */
        for(String tube : this.tubes) {
            try {
                BeanstalkClient c = new BeanstalkClient(
                        Play.configuration.getProperty("beanstalkd.url", "r.easya.cc"),
                        NumberUtils.toInt(
                                Play.configuration.getProperty("beanstalkd.port", "11300")
                        ),
                        tube);
                Logger.info("Watching %s tube: ", tube, c.tubeStats());
                this.tubeClient.put(tube, c);
            } catch(BeanstalkException e) {
                // 无法链接, 直接停掉应用, 认为检查
                Logger.error("Beanstalkd Conn is error! Please Check it and restart app.");
                this.running = false;
                break;
            }
        }
    }

    private void closeClients() {
        for(String key : this.tubeClient.keySet()) {
            BeanstalkClient c = this.tubeClient.get(key);
            if(c != null)
                c.close();
        }
    }

    /**
     * 判断当前线程是否可以执行;
     * 还是加上了同步锁, 避免其他问题产生;
     *
     * @return
     */
    private synchronized boolean isRunning() {
        if(!this.running)
            this.closeClients();
        return this.running;
    }

    private void onError(Exception e) {
        this.running = false;
        Webs.systemMail("Beanstalkd 连接出现问题.",
                "通过 e.easya.cc 链接 r.easya.cc 11300 端口的 beanstalkd 连接出现问题, " +
                        "请查看每个服务器的 /etc/hosts 文件, 检查 ip 正常, 检查 iptables 是否开放 11300 端口给源 " +
                        "ip; " + Webs.E(e));
    }

    /**
     * 不断自循环, 找到一个任务就派发出去
     */
    public void watching(String tube) {
        // 因为一个 Conn 需要处理多个 Tube, 所以阻塞时间越短越好.(这里只能设置 1s - -||)
        try {
            BeanstalkClient c = this.tubeClient.get(tube);
            // 不会重复获取, 减缓时间轮询频率
            Logger.info("Reserve %s ....", tube);
            BeanstalkJob job = c.reserve(5);
            if(job != null) {
                Logger.info("Dispatching job from tube %s", tube);
                this.dispatch(tube, job);
            }
        } catch(BeanstalkDisconnectedException e) {
            this.onError(e);
        } catch(BeanstalkException e) {
            //ignore.. just retry
            Logger.warn("[%s] Let beanstalkd pass job from reserve to ready again. %s",
                    tube, Webs.E(e));
        } catch(Exception e) {
            this.onError(e);
        }
    }

    /**
     * 派发不同的 Job 去执行任务, 使用 Job 的原因是为了有事务, 但因为这里的量不大不追求性能, 所以还是简单阻塞 I/O 处理
     *
     * @param tube Beanstalkd 所使用的 tube
     * @param job  需要处理的任务
     */
    private void dispatch(String tube, BeanstalkJob job) throws BeanstalkException {
        //TODO 这里就写成固定的对两个 Queue 的处理, 如果需要更加通用, 再重新进行抽象处理
        // 由于针对 beanstalkd 的操作都是 socket 中的一行 String; 而这些信息不会产生线程之间的共享数据竞争.
        // 如果需要考虑竞争, 那么应该是对共享的 Client 的竞争, 而也由于竞争后不会对 Client 本身造成副作用,
        // 所以没有问题.
        try {
            if(StringUtils.startsWith(tube, "osticket") && StringUtils.contains(tube, "new")) {
                new OsTicketSavePromise(tube, job).now().get(10, TimeUnit.SECONDS);
            } else if(StringUtils.startsWith(tube, "osticket") &&
                    StringUtils.contains(tube, "sync")) {
                new OsTicketSyncPromise(tube, job).now().get(10, TimeUnit.SECONDS);
            }
        } catch(Exception e) {
            throw new BeanstalkException(e);
        }
    }


    @Override
    public void run() {
        // 如果不运行, 直接结束线程
        // 由于系统内与 Beanstalkd 的交互就仅仅一个 Thread 在处理, 所以没有多线程的竞争.
        while(this.isRunning()) {
            for(String tube : this.tubes) {
                this.watching(tube);
            }
        }
    }
}
