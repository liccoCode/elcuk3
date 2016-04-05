package jobs.promise;

import play.jobs.Job;

import java.util.Date;

/**
 * Created by IntelliJ IDEA.
 * User: wyattpan
 * Date: 10/30/12
 * Time: 5:25 PM
 *
 * @deprecated
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
//        DateTime dt = new DateTime(this.begin);
//        for(int i = 0; i <= this.days; i++) {
//            SellingRecordCheckJob.amazonNewestRecords(dt.plusDays(i));
//        }
    }
}
