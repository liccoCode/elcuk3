package models.view.post;

import com.alibaba.fastjson.JSONObject;
import helper.Constant;
import helper.Dates;
import helper.ES;
import models.market.M;
import models.market.Orderr;
import models.view.dto.OrderReportDTO;
import org.apache.commons.lang.StringUtils;
import org.elasticsearch.index.query.BoolFilterBuilder;
import org.elasticsearch.index.query.FilterBuilder;
import org.elasticsearch.index.query.FilterBuilders;
import org.elasticsearch.index.query.QueryBuilders;
import org.elasticsearch.search.builder.SearchSourceBuilder;
import org.joda.time.DateTime;
import play.Logger;
import play.Play;
import play.db.helper.SqlSelect;
import play.utils.FastRuntimeException;

import java.util.*;
import java.util.regex.Pattern;

/**
 * 订单页面的搜索 POJO, 不进入数据库, 仅仅作为页面的传值绑定
 * User: wyattpan
 * Date: 4/5/12
 * Time: 6:59 PM
 */
public class OrderPOST extends ESPost<Orderr> {

    // 崩溃: 如果使用 from 其类型变为了 Integer, 所以在 Order Post 中改名为 begin
    public Date begin;
    public Date end;

    public OrderPOST() {
        DateTime now = DateTime.now().withTimeAtStartOfDay();
        this.end = now.toDate();
        this.begin = now.minusDays(7).toDate();
        this.perSize = 25;
        this.page = 1;
    }

    public OrderPOST(int perSize) {
        this.perSize = perSize;
    }

    private static Pattern ORDER_NUM_PATTERN = Pattern.compile("^\\+(\\d+)$");

    public Long accountId;

    public M market;

    public Orderr.S state = Orderr.S.SHIPPED;

    public String orderBy = "createDate";

    public String desc = "DESC";

    public Boolean paymentInfo = true;

    public Boolean warnning = false;

    public Boolean promotion = null;

    public String sku;

    public float percent;

    public String invoiceState;

    public List<Orderr> query() {
        SearchSourceBuilder builder = this.params();
        try {
            JSONObject result;
            if(StringUtils.isEmpty(this.sku)) {
                result = ES.search(System.getenv(Constant.ES_INDEX), "order", builder);
            } else {
                result = ES.search(System.getenv(Constant.ES_INDEX), "orderitem", this.skuParams());
            }

            JSONObject hits = result.getJSONObject("hits");
            this.count = hits.getLong("total");
            Set<String> orderIds = new HashSet<>();
            for(Object obj : hits.getJSONArray("hits")) {
                JSONObject hit = (JSONObject) obj;
                orderIds.add(hit.getJSONObject("_source").getString("order_id"));
            }
            if(orderIds.size() <= 0)
                throw new FastRuntimeException("没有结果");
            if(StringUtils.isNotEmpty(invoiceState)) {
                return Orderr.find("invoiceState=? and " + SqlSelect.whereIn("orderId", orderIds), invoiceState).fetch();
            }
            return Orderr.find(SqlSelect.whereIn("orderId", orderIds)).fetch();
        } catch(Exception e) {
            Logger.error(e.getMessage());
            return new ArrayList<>();
        }
    }

    public List<OrderReportDTO> queryForExcel() {
        SearchSourceBuilder builder = this.params();
        try {
            JSONObject result;
            if(StringUtils.isEmpty(this.sku)) {
                result = ES.search(System.getenv(Constant.ES_INDEX), "order", builder);
            } else {
                result = ES.search(System.getenv(Constant.ES_INDEX), "orderitem", this.skuParams());
            }

            JSONObject hits = result.getJSONObject("hits");
            this.count = hits.getLong("total");
            this.perSize = hits.getInteger("total");
            this.page = 1;
            builder = this.params();
            if(StringUtils.isEmpty(this.sku)) {
                result = ES.search(System.getenv(Constant.ES_INDEX), "order", builder);
            } else {
                result = ES.search(System.getenv(Constant.ES_INDEX), "orderitem", this.skuParams());
            }
            hits = result.getJSONObject("hits");
            Set<String> orderIds = new HashSet<>();
            for(Object obj : hits.getJSONArray("hits")) {
                JSONObject hit = (JSONObject) obj;
                orderIds.add(hit.getJSONObject("_source").getString("order_id"));
            }
            if(orderIds.size() <= 0)
                throw new FastRuntimeException("没有结果");
            return OrderReportDTO.query(orderIds);
        } catch(Exception e) {
            Logger.error(e.getMessage());
            return new ArrayList<>();
        }
    }

    @Override
    public Long count(SearchSourceBuilder searchBuilder) {
        return this.count;
    }

    public Long getTotalCount() {
        return this.count;
    }

    @Override
    public SearchSourceBuilder params() {
        BoolFilterBuilder boolFilter = FilterBuilders.boolFilter();

        SearchSourceBuilder builder = new SearchSourceBuilder();
        builder.query(QueryBuilders
                .queryString(this.search())
                .field("selling_ids")
                .field("buyer")
                .field("email")
                .field("address")
                .field("order_id")
                .field("userid")
                .field("track_no")
                .field("upc")
                .field("asin")
                .field("promotion_ids")
        ).postFilter(boolFilter).from(this.getFrom()).size(this.perSize).explain(Play.mode.isDev());

        if(this.promotion != null) {
            FilterBuilder boolBuilder;
            if(this.promotion) {
                boolBuilder = FilterBuilders.missingFilter("promotion_ids").nullValue(true);
            } else {
                boolBuilder = FilterBuilders.existsFilter("promotion_ids");
            }
            boolFilter.mustNot(boolBuilder);
        }

        if(this.market != null) {
            boolFilter.must(FilterBuilders.termFilter("market", this.market.name().toLowerCase()))
                    .must(FilterBuilders.rangeFilter("date") // ES: date -> createDate
                            // 市场变更, 具体查询时间也需要变更
                            .from(Dates.morning(this.market.withTimeZone(this.begin).toDate())).includeLower(true)
                            .to(Dates.night(this.market.withTimeZone(this.end).toDate())).includeUpper(true));
        } else {
            boolFilter.must(FilterBuilders.rangeFilter("date").from(Dates.morning(this.begin)).includeLower(true)
                    .to(Dates.night(this.end)).includeUpper(true));
        }


        if(this.state != null) {
            boolFilter.must(FilterBuilders.termFilter("state", this.state.name().toLowerCase()));
        }
        if(this.accountId != null) {
            boolFilter.must(FilterBuilders.termFilter("account_id", this.accountId));
        }
        return builder;
    }

    public SearchSourceBuilder skuParams() {
        BoolFilterBuilder boolFilter = FilterBuilders.boolFilter();

        SearchSourceBuilder builder = new SearchSourceBuilder();
        builder.query(QueryBuilders
                .queryString(this.search())
                .field("selling_id")
                .field("order_id"))
                .postFilter(boolFilter).from(this.getFrom()).size(this.perSize).explain(Play.mode.isDev());

        if(this.market != null) {
            boolFilter.must(FilterBuilders.termFilter("market", this.market.name().toLowerCase()))
                    .must(FilterBuilders.rangeFilter("date") // ES: date -> createDate
                            // 市场变更, 具体查询时间也需要变更
                            .from(Dates.morning(this.market.withTimeZone(this.begin).toDate())).includeLower(true)
                            .to(Dates.night(this.market.withTimeZone(this.end).toDate())).includeUpper(true));
        } else {
            boolFilter.must(FilterBuilders.rangeFilter("date").from(Dates.morning(this.begin)).includeLower(true)
                    .to(Dates.night(this.end)).includeUpper(true));
        }


        if(this.state != null) {
            boolFilter.must(FilterBuilders.termFilter("state", this.state.name().toLowerCase()));
        }
        if(this.sku != null) {
            boolFilter.must(FilterBuilders.termFilter("sku", ES.parseEsString(sku).toLowerCase()));
        }

        return builder;
    }

}
