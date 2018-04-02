package jobs;

import helper.Dates;
import jobs.driver.BaseJob;
import models.view.post.StockPost;
import models.whouse.DailyStock;
import models.whouse.StockRecord;
import play.jobs.On;

import java.util.Arrays;
import java.util.Date;
import java.util.List;
import java.util.Map;

/**
 * Created by IntelliJ IDEA.
 * User: licco
 * Date: 2018/4/2
 * Time: 上午11:36
 */
@On("0 0 23 * * ?")
public class DailyStockJob extends BaseJob {

    @Override
    public void doit() {
        Date now = new Date();
        StockPost p = new StockPost();
        Map<String, String> total = p.total();
        DailyStock stock = new DailyStock();
        stock.date = now;
        stock.qty = Integer.parseInt(total.get("totalQty"));
        stock.totalCNY = Double.parseDouble(total.get("totalCNY"));
        stock.totalUSD = Double.parseDouble(total.get("totalUSD"));
        List<StockRecord> recordList = StockRecord.find("createDate>=? AND createDate<=? AND type IN (?,?,?,?,?)",
                Dates.morning(now), Dates.night(now), StockRecord.T.Inbound, StockRecord.T.Unqualified_Transfer,
                StockRecord.T.Outbound, StockRecord.T.OtherOutbound, StockRecord.T.Refund).fetch();
        stock.inboundQty = recordList.stream()
                .filter(record -> Arrays.asList(StockRecord.T.Inbound, StockRecord.T.Unqualified_Transfer)
                        .contains(record.type)).mapToInt(record -> record.qty).sum();
        stock.outboundQty = recordList.stream().filter(record -> Arrays
                .asList(StockRecord.T.Outbound, StockRecord.T.OtherOutbound, StockRecord.T.Refund).contains(record.type))
                .mapToInt(record -> record.qty).sum();
        stock.save();
    }
}
