package jobs;

import org.joda.time.DateTime;
import play.libs.F;

import java.util.*;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * 下载最近 90 天的入库记录, 用于进行入库跟踪
 * 周期:
 * - 轮询周期: 20mn
 * - Duration: 20mn
 * - Job Interval: 1h
 * User: wyatt
 * Date: 5/14/13
 * Time: 4:20 PM
 *
 * @deprecated
 */
public class AmazonFBAInventoryReceivedJob {

    public static class Rows {
        public List<String> records = new ArrayList<>();
        public Map<String, AtomicInteger> mskus = new HashMap<>();

        public int qty(String msku) {
            if(mskus.get(msku) == null) {
                return 0;
            } else {
                return mskus.get(msku).get();
            }
        }

        /**
         * 获取 Records 中的最早时间
         *
         * @return
         */
        public F.Option<Date> getEarliestDate() {
            return getEarliestDate(this.records);
        }

        public static F.Option<Date> getEarliestDate(List<String> records) {
            if(records == null || records.size() == 0) return F.Option.None();
            List<Date> dates = new ArrayList<>();
            for(String record : records) {
                String dateStr = record.split("||")[0];
                dates.add(DateTime.parse(dateStr).toDate());
            }
            Collections.sort(dates);
            return F.Option.Some(dates.get(0));
        }
    }
}
