package models.view.post;

import jobs.analyze.SellingRecordCaculateJob;
import models.market.SellingRecord;
import org.apache.commons.lang.StringUtils;
import org.joda.time.DateTime;
import play.cache.Cache;
import play.libs.F;
import play.utils.FastRuntimeException;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

/**
 * Created by IntelliJ IDEA.
 * User: wyatt
 * Date: 8/16/13
 * Time: 2:12 PM
 */
public class SellingRecordsPost extends Post<SellingRecord> {
    public SellingRecordsPost() {
        this.perSize = 20;
    }

    public Date dateTime = new Date();

    @Override
    public F.T2<String, List<Object>> params() {
        return new F.T2<String, List<Object>>("", null);
    }

    @SuppressWarnings("unchecked")
    public List<SellingRecord> records() {
        List<SellingRecord> records = Cache.get("sellingRecordCaculateJob", List.class);
        if(records == null || records.size() == 0) {
            String running = Cache.get("sellingRecordCaculateJobRunning", String.class);
            if(StringUtils.isNotBlank(running)) throw new FastRuntimeException("正在计算中, 请等待 10 分钟后重试.");
            new SellingRecordCaculateJob(new DateTime(2013, 7, 2, 0, 0)).now();
        }
        return records;
    }

    @Override
    public List<SellingRecord> query() {
        List<SellingRecord> records = records();

        this.count = records.size();
        List<SellingRecord> afterPager = new ArrayList<SellingRecord>();
        int index = (this.page - 1) * this.perSize;
        int end = index + this.perSize;
        for(; index < end; index++) {
            if(index >= this.count) break;
            afterPager.add(records.get(index));
        }
        return afterPager;
    }
}
