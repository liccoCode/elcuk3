package jobs.promise;

import helper.Dates;
import jobs.SellingRecordCheckJob;
import models.Notification;
import models.market.Selling;
import org.joda.time.DateTime;
import play.jobs.Job;

import java.util.Date;
import java.util.List;

/**
 * Created by IntelliJ IDEA.
 * User: wyattpan
 * Date: 10/30/12
 * Time: 5:25 PM
 */
public class SellingRecordFixPromise extends Job {
    private Date begin;
    private int days = 1;

    public SellingRecordFixPromise(Date begin, int days) {
        this.begin = begin;
        this.days = days;
    }

    @Override
    public void doJob() {
        DateTime dt = new DateTime(this.begin);
        for(int i = 0; i <= this.days; i++) {
            SellingRecordCheckJob.amazonNewestRecords(dt.plusDays(i));
        }
        Notification.notifies("Session/PageView 数据",
                String.format("从 %s 开始向后 %s 天的 Session 与 PageView 数据更新完毕.", Dates.date2Date(this.begin), this.days), Notification.PM);
    }
}
