package jobs;

import helper.Dates;
import models.Jobex;
import models.Notification;
import models.User;
import models.market.Orderr;
import models.market.Selling;
import models.market.SellingRecord;
import org.joda.time.DateTime;
import play.jobs.Job;

import java.util.List;

/**
 * SellingRecord 的生成, 每天所有的 Selling 都需要有一份 SellingRecord 数据
 * 周期:
 * - 轮询周期: 1h
 * - Duration: 10 0 0 * * ?(每天开始后第 10 s)
 * User: wyattpan
 * Date: 11/8/12
 * Time: 4:24 PM
 */
public class SellingRecordGenerateJob extends Job {

    public DateTime fixTime;

    public SellingRecordGenerateJob() {
        this.fixTime = DateTime.now();
    }

    public SellingRecordGenerateJob(DateTime fixTime) {
        if(fixTime == null)
            this.fixTime = new DateTime();
        else
            this.fixTime = fixTime;
    }

    @Override
    public void doJob() {
        if(!Jobex.findByClassName(SellingRecordGenerateJob.class.getName()).isExcute()) return;
        long begin = System.currentTimeMillis();
        List<Selling> sellings = Selling.all().fetch();
        for(Selling s : sellings) {
            SellingRecord record = new SellingRecord(s, fixTime.toDate());
            if(SellingRecord.count("id=?", record.id) > 0)
                record = SellingRecord.findById(record.id);
            record.orderCanceld = (int) Orderr
                    .count("state=? AND createDate=?", Orderr.S.CANCEL, fixTime.toDate());
            record.rating = s.avgRating();
            record.reviewSize = s.listing.listingReviews.size();
            record.salePrice = s.aps.salePrice;
            record.save();
        }
        Notification.notifies(User.findByUserName("wyatt"),
                String.format(
                        "SellingRecordGenerateJob 任务完成, 总共耗时: %s s; 总共创建 %s 个 SellingRecord.[%s]",
                        (System.currentTimeMillis() - begin) / 1000, sellings.size(),
                        Dates.date2DateTime(fixTime.toDate())));
    }
}
