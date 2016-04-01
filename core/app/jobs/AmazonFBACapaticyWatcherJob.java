package jobs;

import helper.Dates;
import helper.GTs;
import helper.HTTP;
import helper.LogUtils;
import models.Jobex;
import models.market.Account;
import models.whouse.Whouse;
import org.apache.commons.lang.StringUtils;
import org.apache.commons.lang.math.NumberUtils;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import play.Logger;
import play.jobs.Job;

import java.util.List;

/**
 * 每隔 4h 检查去 Amazon 同步一次库存容量情况
 * User: wyattpan
 * Date: 9/29/12
 * Time: 10:30 AM
 */
public class AmazonFBACapaticyWatcherJob extends Job {
    @Override
    public void doJob() {
        long begin = System.currentTimeMillis();
        if(!Jobex.findByClassName(AmazonFBACapaticyWatcherJob.class.getName()).isExcute()) return;
        List<Whouse> whouses = Whouse.find("type=?", Whouse.T.FBA).fetch();
        for(Whouse whouse : whouses) {
            if(whouse.account == null) {
                Logger.warn("Whouse %s[%s] is FBA but do not have an relative accout!",
                        whouse.name(), whouse.id);
                continue;
            }

            whouse.account.changeRegion(whouse.account.type);
            String html = HTTP
                    .get(whouse.account.cookieStore(), whouse.account.type.fbaCapacityPage());
            Logger.info("Watch %s FBA.", whouse.name());
            Document doc = Jsoup.parse(html);
            if(!Account.isLoginEnd(doc)) {
                Logger.warn("Account %s is not login, skip this one.", whouse.account.prettyName());
                continue;
            }
            whouse.capaticyContent = fbaCapacityWidgetDiv(doc, whouse);
            whouse.save();
        }
        if(LogUtils.isslow(System.currentTimeMillis() - begin,"AmazonFBACapaticyWatcherJob")) {
            LogUtils.JOBLOG.info(String
                    .format("AmazonFBACapaticyWatcherJob calculate.... [%sms]", System.currentTimeMillis() - begin));
        }
    }


    public String fbaCapacityWidgetDiv(Document doc, Whouse whouse) {
        /**
         * 将 FBA 中的库存 Standard-Size 与 OverSize 的上限与实际解析出来, 并且结合 Bootstrap2.x 给与进度条.
         */
        Element standardSizeEl = doc.select("#fba-capacity-widget-type-sortable").first();
        Element overSizeEl = doc.select("#fba-capacity-widget-type-non-sortable").first();

        int standardSizeLimit = NumberUtils
                .toInt(StringUtils
                        .split(standardSizeEl.select(".fba-capacity-widget-bar span").get(2).text().trim())[0]);
        int standardSize = NumberUtils.toInt(
                standardSizeEl.select(".fba-capacity-widget-bar .fba-capacity-widget-utilization-integer").text()
                        .trim());
        int overSizeLimit = NumberUtils
                .toInt(StringUtils.split(overSizeEl.select(".fba-capacity-widget-bar span").get(2).text().trim())[0]);
        int overSize = NumberUtils
                .toInt(overSizeEl.select(".fba-capacity-widget-bar .fba-capacity-widget-utilization-integer").text()
                        .trim());

        return GTs.render("fbaCapacity", GTs.newMap("standardSize", standardSize)
                .put("standardSizeLimit", standardSizeLimit)
                .put("overSize", overSize)
                .put("overSizeLimit", overSizeLimit)
                .put("whouse", whouse)
                .put("date", Dates.date2DateTime())
                .build());
    }
}
