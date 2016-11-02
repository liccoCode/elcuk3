package helper;

import models.market.M;
import play.Logger;
import play.db.DB;
import play.utils.FastRuntimeException;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.*;

/**
 * Created by IntelliJ IDEA.
 * User: wyatt
 * Date: 1/23/13
 * Time: 10:24 AM
 */
public class Promises {
    public static final M[] MARKETS = {M.AMAZON_DE, M.AMAZON_US, M.AMAZON_CA, M.AMAZON_UK, M.AMAZON_FR, M.AMAZON_ES,
            M.AMAZON_IT, M.AMAZON_JP};
    private static final ExecutorService threadPool = Executors.newFixedThreadPool(8);

    /**
     * Fork 多个 Job 根据 Callback.doJobWithResult(m) 去执行计算;
     * 如果需要访问 DB, 则请使用 DBCallback 进行.
     * <p>
     * WANING: 只适用于获取数据操作,不支持涉及到持久化的操作.
     *
     * @param callback
     * @param <T>
     * @return
     */
    public static <T> List<T> forkJoin(final Callback<T> callback) {
        List<T> vos = new ArrayList<>();
        // 通过 Job 异步 fork 加载不同时段的数据
        List<FutureTask<T>> futures = new ArrayList<>();
        long begin = System.currentTimeMillis();
        Logger.info("[%s:#%s] Start Fork to fetch Analyzes Sellings.", callback.id(), begin);
        try {
            List iterators;
            if(callback instanceof CallbackWithContext<?>) {
                iterators = ((CallbackWithContext) callback).getContext();
            } else {
                iterators = Arrays.asList(Promises.MARKETS);
            }
            for(final Object param : iterators) {
                //WARNING::  这里的 new Callable<T>... 在 java8 中可以被简写成 () -> 但是这种写法在 play 1.4.x 中无法通过 precompile!
                FutureTask<T> task = new FutureTask<>(new Callable<T>() {
                    @Override
                    public T call() throws Exception {
                        try {
                            return callback.doJobWithResult(param);
                        } finally {
                            if(callback instanceof DBCallback<?>) {
                                ((DBCallback) callback).close();
                            }
                        }
                    }
                });
                threadPool.submit(task);
                futures.add(task);
            }
            try {
                for(FutureTask<T> task : futures) {
                    //获取执行结果，如果在指定时间内，还没获取到结果，就直接返回 null
                    vos.add(task.get(45, TimeUnit.MINUTES));
                }
            } catch(Exception e) {
                throw new FastRuntimeException(
                        String.format("[%s] 因为 %s 问题, 请然后重新尝试搜索.", callback.id(), Webs.E(e)));
            }
        } finally {
            Logger.info("[%s:#%s] End of Fork fetch. Passed: %s ms",
                    callback.id(),
                    begin,
                    (System.currentTimeMillis() - begin));
        }
        return vos;
    }

    /**
     * 非 DB 的 Fork 计算
     *
     * @param <T>
     */
    public interface Callback<T> {
        T doJobWithResult(Object param);

        /**
         * 用来标记执行线程的
         *
         * @return
         */
        String id();
    }

    /**
     * 带有 Connection 链接的 Callback;
     * 利用 DBPlugin, 从 DB 的 datasource 中直接获取新的 db 链接.
     *
     * @param <T>
     */
    public static abstract class DBCallback<T> implements Callback<T> {

        private static ThreadLocal<Connection> connHolder = new ThreadLocal<>();


        public Connection getConnection() {
            if(connHolder.get() == null) {
                try {
                    connHolder.set(DB.getDataSource().getConnection());
                } catch(SQLException e) {
                    throw new FastRuntimeException(e);
                }
            }
            return connHolder.get();
        }

        public void close() {
            try {
                if(connHolder.get() != null) {
                    connHolder.get().close();
                    connHolder.set(null);
                }
            } catch(SQLException e) {
                throw new FastRuntimeException(e);
            }
        }
    }

    public static abstract class CallbackWithContext<T> implements Callback<T> {
        /**
         * 用于 each 的 Context
         *
         * @return
         */
        public abstract List getContext();
    }
}
