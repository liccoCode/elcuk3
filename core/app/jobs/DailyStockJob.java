package jobs;

import helper.Dates;
import jobs.driver.BaseJob;
import models.procure.ProcureUnit;
import models.view.post.ProcurePost;
import models.view.post.StockPost;
import models.whouse.DailyStock;
import models.whouse.StockRecord;
import play.jobs.On;
import play.libs.F;

import java.util.*;

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
        stock.type = DailyStock.T.Whouse;
        stock.save();

        ProcurePost procure = new ProcurePost();
        procure.stages.add(ProcureUnit.STAGE.PLAN);
        procure.stages.remove(ProcureUnit.STAGE.IN_STORAGE);

        Map<String, String> map = procure.total(procure.query());
        DailyStock daily = new DailyStock();
        daily.date = now;
        daily.qty = Integer.parseInt(map.get("totalQty"));
        daily.totalCNY = Double.parseDouble(map.get("totalCNY"));
        daily.totalUSD = Double.parseDouble(map.get("totalUSD"));

        F.T2<String, List<Object>> params = procure.params();
        params._2.add(Dates.morning(new Date()));
        params._2.add(Dates.night(new Date()));
        List<ProcureUnit> details = ProcureUnit.find(params._1 + " AND p.createDate>=? AND p.createDate<=?",
                params._2.toArray()).fetch();
        Integer planQty = details.stream().filter(detail -> detail.parent == null)
                .filter(detail -> Objects.equals(ProcureUnit.STAGE.PLAN, detail.stage))
                .mapToInt(ProcureUnit::qty).sum();
        Integer deliveryQty = details.stream().filter(detail -> detail.parent == null)
                .filter(detail -> Objects.equals(ProcureUnit.STAGE.DELIVERY, detail.stage))
                .mapToInt(ProcureUnit::qty).sum();
        Integer doneQty = details.stream().filter(detail -> detail.parent == null)
                .filter(detail -> Objects.equals(ProcureUnit.STAGE.DONE, detail.stage))
                .mapToInt(ProcureUnit::qty).sum();
        daily.planQty = planQty;
        daily.deliveryQty = deliveryQty;
        daily.doneQty = doneQty;
        daily.save();
    }
}
