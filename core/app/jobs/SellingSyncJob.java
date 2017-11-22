package jobs;

import helper.Webs;
import jobs.driver.BaseJob;
import models.market.Selling;
import play.Logger;
import play.jobs.Every;

import java.util.List;

/**
 * Created by IntelliJ IDEA.
 * User: licco
 * Date: 2017/11/21
 * Time: 上午10:25
 */
@Every("5mn")
public class SellingSyncJob extends BaseJob {

    public void doit() {
        List<Selling> sellings = Selling.find("sync=? AND state<>? ORDER BY createDate DESC",
                false, Selling.S.DOWN).fetch(10);
        sellings.forEach(selling -> {
            Logger.info("selling:" + selling.sellingId + " 开始执行 syncAmazonInfoFromApi 方法 ");
            try {
                selling.syncAmazonInfoFromApi();
                selling.sync = true;
                selling.save();
            } catch(Exception e) {
                selling.sync = true;
                selling.save();
                Logger.error(Webs.e(e));
            }
        });
    }

}
