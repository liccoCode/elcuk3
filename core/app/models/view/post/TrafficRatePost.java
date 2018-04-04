package models.view.post;

import helper.Caches;
import helper.Dates;
import models.market.M;
import models.view.report.TrafficRate;
import org.apache.commons.lang.StringUtils;
import org.joda.time.DateTime;
import play.cache.Cache;
import play.libs.F;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

/**
 * Created by IntelliJ IDEA.
 * User: wyattpan
 * Date: 9/3/12
 * Time: 4:32 PM
 */
public class TrafficRatePost extends Post<TrafficRate> {

    private static final long serialVersionUID = -2081286181455788781L;
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
        if(cacheElement != null && cacheElement.size()>0) return cacheElement;
        StringBuilder sbd = new StringBuilder();
        List<Object> params = new ArrayList<>();
        sbd.append("sellDate").append(">=?").append(" AND ")
                .append("sellDate<=?");
        params.add(Dates.morning(this.from));
        params.add(Dates.night(this.to));
        if(StringUtils.isNotBlank(this.SellingId)) {
            sbd.append(" AND sellingId like ?");
            params.add(this.SellingId + "%");
        }
        if(this.market != null) {
            sbd.append(" AND market=? ");
            params.add(this.market);
        }
        sbd.append(" ORDER BY sellingId,sellDate");
        List<TrafficRate> traffics =  TrafficRate.find(sbd.toString(), params.toArray()).fetch();
        Cache.set(cacheKey, traffics, "4h");
        return traffics;
    }
}
