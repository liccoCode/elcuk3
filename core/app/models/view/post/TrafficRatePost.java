package models.view.post;

import helper.Caches;
import helper.Dates;
import helper.Webs;
import models.market.M;
import org.apache.commons.lang.StringUtils;
import org.joda.time.DateTime;
import play.cache.Cache;

import java.util.*;

import models.market.SellingRecord;

import models.view.report.TrafficRate;
import play.db.helper.SqlSelect;
import play.libs.F;

/**
 * Created by IntelliJ IDEA.
 * User: wyattpan
 * Date: 9/3/12
 * Time: 4:32 PM
 */
public class TrafficRatePost extends Post<TrafficRate> {


    /**
     * 在 ProcureUnits中，planView 和noPlaced 方法 需要调用 index，必须重写，否则总是构造方法中的时间
     */
    public Date from;
    public Date to;

    /**
     * 市场
     */
    public M market;


    public String SellingId;


    @Override
    public F.T2<String, List<Object>> params() {
        // no use
        throw new UnsupportedOperationException("AnalyzePost 不需要调用 params()");
    }


    public TrafficRatePost() {
        this.from = DateTime.now().minusDays(3).toDate();
        this.to = new Date();
    }


    public List<TrafficRate> query() {
        String cacheKey = Caches.Q.cacheKey("trafficRate", this.market, this.from, this.to, this.SellingId);
        List<TrafficRate> cacheElement = Cache.get(cacheKey, List.class);
        if(cacheElement != null) return cacheElement;

        StringBuilder sbd = new StringBuilder();
        List<Object> params = new ArrayList<Object>();
        sbd.append("date").append(">=?").append(" AND ")
                .append("date<=?");
        params.add(Dates.morning(this.from));
        params.add(Dates.night(this.to));

        if(StringUtils.isNotBlank(this.SellingId)) {
            sbd.append(" AND selling.sellingId like ?");
            params.add(this.SellingId + "%");
        }
        if(this.market != null) {
            sbd.append(" AND market=? ");
            params.add(this.market);
        }
        sbd.append(" ORDER BY selling.sellingId,date");

        List<SellingRecord> dateMixRecords = SellingRecord
                .find(sbd.toString(), params.toArray()).fetch();

        List<TrafficRate> traffics = new ArrayList<TrafficRate>();
        for(SellingRecord rcd : dateMixRecords) {
            try {
                TrafficRate traffic = new TrafficRate();
                traffic.sellingId = rcd.selling.sellingId;
                traffic.sessions = rcd.sessions;
                traffic.pageViews = rcd.pageViews;
                traffic.sellDate = rcd.date;
                traffic.orders = rcd.orders;
                traffic.units = rcd.units;
                traffic.market = rcd.market;
                traffic.turnRatio = Webs.scalePointUp(3, (float) rcd.orders / (rcd.sessions == 0 ? 1 : rcd.sessions));
                traffics.add(traffic);
            } catch(Exception e) {

            }
        }

        Cache.set(cacheKey, traffics, "4h");
        return traffics;
    }
}
