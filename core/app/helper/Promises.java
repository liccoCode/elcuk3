package helper;

import models.market.M;
import play.Logger;
import play.jobs.Job;
import play.libs.F;
import play.utils.FastRuntimeException;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;

/**
 * Created by IntelliJ IDEA.
 * User: wyatt
 * Date: 1/23/13
 * Time: 10:24 AM
 */
public class Promises {
    public static final M[] MARKETS = {M.AMAZON_DE, M.AMAZON_US, M.AMAZON_UK, M.AMAZON_FR};

    /**
     * Fork 多个 Job 根据 Callback.doJobWithResult(m) 去执行计算;
     * ps:
     * M:market 就没有进行抽象为 Context 了, 有需要再重构
     *
     * @param callback
     * @param <T>
     * @return
     */
    public static <T> List<T> forkJoin(final Callback<T> callback) {
        List<T> vos = new ArrayList<T>();
        // 通过 Job 异步 fork 加载不同时段的数据
        /**
         * FIXME 这类型的代码在下面, 这些需要进行重构到一起
         * 1. 此处
         * 2. OrderItem.categoryPercent
         * 3. OrderItem.skuOrMskuAccountRelateOrderItem
         */
        List<F.Promise<List<T>>> voPromises = new ArrayList<F.Promise<List<T>>>();
        long begin = System.currentTimeMillis();
        Logger.info("[%s:#%s] Start Fork to fetch Analyzes Sellings.", callback.id(), begin);
        try {
            for(final M m : Promises.MARKETS) {
                voPromises.add(new Job<List<T>>() {
                    @Override
                    public List<T> doJobWithResult() throws Exception {
                        return callback.doJobWithResult(m);
                    }
                }.now());
            }
            for(F.Promise<List<T>> voP : voPromises) {
                vos.addAll(voP.get(1, TimeUnit.MINUTES));
            }
        } catch(Exception e) {
            throw new FastRuntimeException(
                    String.format("[%s] 因为 %s 问题, 请然后重新尝试搜索.", callback.id(), Webs.E(e)));
        } finally {
            Logger.info("[%s:#%s] End of Fork fetch. Passed: %s ms",
                    callback.id(),
                    begin,
                    (System.currentTimeMillis() - begin));
        }
        return vos;
    }

    public interface Callback<T> {
        public List<T> doJobWithResult(M m);

        /**
         * 用来标记执行线程的
         *
         * @return
         */
        public String id();
    }
}
