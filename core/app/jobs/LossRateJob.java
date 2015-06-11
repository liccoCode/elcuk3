package jobs;

import helper.*;
import jobs.driver.BaseJob;
import models.market.M;
import models.view.report.LossRate;
import org.apache.commons.lang.StringUtils;
import play.cache.Cache;
import play.libs.F;
import services.MetricProfitService;
import services.MetricSaleReportService;

import java.math.BigDecimal;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Map;

/**
 * Created by licco on 15/6/10.
 */
public class LossRateJob extends BaseJob {

    private MetricSaleReportService service = new MetricSaleReportService();

    public Date from;
    public Date to;
    public F.T2<String, List<Object>> params;

    public LossRateJob(Date from, Date to, F.T2<String, List<Object>> params) {
        this.from = from;
        this.to = to;
        this.params = params;
    }

    public String buildKey() {
        return Caches.Q.cacheKey(this.from, this.to, "LossRateJob");
    }

    public static boolean isRunning(String key) {
        return StringUtils.isNotBlank(Cache.get(key + "_running", String.class));
    }

    public void doit() {
        long begin = System.currentTimeMillis();
        String runningKey = buildKey() + "_running";
        Cache.add(runningKey, runningKey);
        List<Map<String, Object>> rows = DBUtils.rows(params._1, Dates.morning(this.from), Dates.night(this.to));
        List<LossRate> lossrate = new ArrayList<LossRate>();
        DecimalFormat df = new DecimalFormat("0.00");
        for(Map<String, Object> row : rows) {
            LossRate loss = new LossRate();
            loss.sku = (String) row.get("sku");
            loss.fba = (String) row.get("shipmentid");
            loss.qty = (Integer) row.get("qty");
            loss.lossqty = (Integer) row.get("lossqty");
            if(row.get("currency") != null) {
                loss.currency = Currency.valueOf(row.get("currency").toString());
                loss.price = (Float) row.get("price");
                loss.totallossprice = Float.parseFloat(df.format(loss.currency.toUSD(loss.price) * loss.lossqty));
            }
            loss.market = M.valueOf(row.get("market").toString());
            MetricProfitService service = new MetricProfitService(this.from, this.to, loss.market, loss.sku, null);
            loss.totalShipmentprice = Float.parseFloat(
                    df.format((service.esShipPrice() + service.esVatPrice()) * loss.lossqty));
            Object compenusdamt = row.get("compenusdamt");
            if(compenusdamt != null)
                loss.compenusdamt = Float.parseFloat(df.format(row.get("compenusdamt")));
            else
                loss.compenusdamt = 0f;

            loss.payrate = new BigDecimal(loss.compenusdamt).divide(new BigDecimal(loss.totallossprice +
                    loss.totalShipmentprice), 4, 4).multiply(new BigDecimal(100)).setScale(2, BigDecimal.ROUND_HALF_UP);
            lossrate.add(loss);
        }
        Cache.add(buildKey(), lossrate, "4h");
        Cache.delete(runningKey);
        LogUtils.JOBLOG.info(String.format("LossRateJob execute with key: %s, Calculate time: %s", buildKey(),
                System.currentTimeMillis() - begin));
    }

}
