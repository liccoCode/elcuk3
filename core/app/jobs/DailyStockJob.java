package jobs;

import jobs.driver.BaseJob;
import models.view.post.StockPost;
import models.whouse.DailyStock;
import play.jobs.On;

import java.util.Date;
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
        StockPost p = new StockPost();
        Map<String, String> total = p.total();
        DailyStock stock = new DailyStock();
        stock.date = new Date();
        stock.qty = Integer.parseInt(total.get("totalQty"));
        stock.totalCNY = Double.parseDouble(total.get("totalCNY"));
        stock.totalUSD = Double.parseDouble(total.get("totalUSD"));
        stock.save();
    }
}
