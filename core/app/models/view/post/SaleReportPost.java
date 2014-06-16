package models.view.post;

import controllers.Login;
import helper.Caches;
import helper.LogUtils;
import jobs.SaleReportsJob;
import models.User;
import models.market.M;
import models.market.Selling;
import models.product.Category;
import models.view.dto.SaleReportDTO;
import org.apache.commons.lang.StringUtils;
import org.joda.time.DateTime;
import play.cache.Cache;
import play.utils.FastRuntimeException;
import services.MetricSaleReportService;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Created by IntelliJ IDEA.
 * User: mac
 * Date: 14-6-9
 * Time: AM10:17
 */
public class SaleReportPost {
    public Date from;
    public Date to;
    public M market;
    public String search;

    /**
     * 拥有权限的Selling集合
     */
    private List<String> sellingIds;

    public SaleReportPost() {
        DateTime now = DateTime.now().withTimeAtStartOfDay();
        this.from = now.toDate();
        this.to = now.toDate();

    }

    public List<SaleReportDTO> query() {
        User user = User.current();
        String key = Caches.Q.cacheKey(this.from, this.to, (this.market != null ? this.market : "AllMarket"),
                (this.search != null ? this.search : ""), user.username, "SaleReports");

        List<SaleReportDTO> dtos = Cache.get(key, List.class);
        if(dtos == null) {
            if(!SaleReportsJob.isRunning(key)) {
                new SaleReportsJob(this.from, this.to, this.market, this.search, user.username).now();
            }
            throw new FastRuntimeException("已经在后台计算中，请于 10min 后再来查看结果~");
        }
        return dtos;
    }
}
