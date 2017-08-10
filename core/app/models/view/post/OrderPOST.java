package models.view.post;

import com.alibaba.fastjson.JSONObject;
import helper.Constant;
import helper.Dates;
import helper.ES;
import models.market.M;
import models.market.Orderr;
import models.view.dto.OrderReportDTO;
import org.apache.commons.lang.StringUtils;
import org.elasticsearch.index.query.BoolQueryBuilder;
import org.elasticsearch.index.query.ExistsQueryBuilder;
import org.elasticsearch.index.query.QueryBuilders;
import org.elasticsearch.search.builder.SearchSourceBuilder;
import org.joda.time.DateTime;
import play.Play;
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

    private static final long serialVersionUID = -6583404252425758018L;
    // 崩溃: 如果使用 from 其类型变为了 Integer, 所以在 Order Post 中改名为 begin
    public Date begin;
    public Date end;

    public OrderPOST() {
        DateTime now = DateTime.now().withTimeAtStartOfDay();
        this.end = now.toDate();
        this.begin = now.minusDays(7).toDate();
        this.perSize = 25;
        this.page = 1;
        this.count = 0;
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

    public String category;

    public enum F {
        order_id {
            @Override
            public String label() {
                return "Order Id";
            }
        },
        sku {
            @Override
            public String label() {
                return "SKU";
            }
        },
        promotion_ids {
            @Override
            public String label() {
                return "Promotion IDs";
            }
        },
        buyer {
            @Override
            public String label() {
                return "Buyer";
            }
        },
        email {
            @Override
            public String label() {
                return "Email";
            }
        },
        userid {
            @Override
            public String label() {
                return "User Id";
            }
        };

        public abstract String label();
    }

    public String field;

    public List<Orderr> query() {
        JSONObject result;
        if(!Objects.equals(field, F.sku.name()) && StringUtils.isEmpty(this.category)) {
            result = ES.search(System.getenv(Constant.ES_INDEX), "order", this.params());
        } else {
            result = ES.search(System.getenv(Constant.ES_INDEX), "orderitem", this.skuParams());
        }
        Set<String> orderIds = new HashSet<>();
        Optional<JSONObject> topHits = Optional.ofNullable(result.getJSONObject("hits"));
        topHits.map(hits -> hits.getJSONArray("hits"))
                .ifPresent(hits -> hits.stream().map(hit -> (JSONObject) hit)
                        .map(hit -> hit.getJSONObject("_source"))
                        .map(field -> field.getString("order_id"))
                        .filter(orderId -> !orderId.isEmpty())
                        .forEach(orderIds::add)
                );
        topHits.ifPresent(hits -> this.count = hits.getLong("total"));
        if(orderIds.isEmpty()) return Collections.emptyList();
        return Orderr.find("orderId IN (:orderIds)").bind("orderIds", orderIds).fetch();
    }

    public List<OrderReportDTO> queryForExcel() {
        JSONObject result;
        if(StringUtils.isEmpty(this.sku) && StringUtils.isEmpty(this.category)) {
            result = ES.search(System.getenv(Constant.ES_INDEX), "order", this.params());
        } else {
            result = ES.search(System.getenv(Constant.ES_INDEX), "orderitem", this.skuParams());
        }
        Set<String> orderIds = new HashSet<>();
        Optional.ofNullable(result.getJSONObject("hits"))
                .map(hits -> hits.getJSONArray("hits"))
                .ifPresent(hits -> hits.stream().map(hit -> (JSONObject) hit)
                        .map(hit -> hit.getJSONObject("_source"))
                        .map(fields -> fields.getString("order_id"))
                        .filter(orderId -> !orderId.isEmpty())
                        .forEach(orderIds::add)
                );
        if(orderIds.size() <= 0) throw new FastRuntimeException("没有结果");
        return Orderr.find("orderId IN (:orderIds)").bind("orderIds", orderIds).fetch();
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
        BoolQueryBuilder boolQuery = QueryBuilders.boolQuery();
        if(this.promotion != null) {
            ExistsQueryBuilder existsQuery = QueryBuilders.existsQuery("promotion_ids");
            if(this.promotion) {
                boolQuery.must(existsQuery);
            } else {
                boolQuery.mustNot(existsQuery);
            }
        }
        if(this.market != null) {
            boolQuery.must(QueryBuilders.termQuery("market", this.market.name().toLowerCase()))
                    .must(QueryBuilders.rangeQuery("date") // ES: date -> createDate
                            // 市场变更, 具体查询时间也需要变更
                            .from(Dates.morning(this.market.withTimeZone(this.begin).toDate())).includeLower(true)
                            .to(Dates.night(this.market.withTimeZone(this.end).toDate())).includeUpper(true));
        } else {
            boolQuery.must(QueryBuilders.rangeQuery("date")
                    .from(Dates.morning(this.begin)).includeLower(true)
                    .to(Dates.night(this.end)).includeUpper(true));
        }
        if(this.state != null) {
            boolQuery.must(QueryBuilders.termQuery("state", this.state.name().toLowerCase()));
        }
        if(this.accountId != null) {
            boolQuery.must(QueryBuilders.termQuery("account_id", this.accountId));
        }
        if(StringUtils.isNotBlank(this.invoiceState)) {
            boolQuery.must(QueryBuilders.termQuery("invoice_state", this.invoiceState));
        }
        if(StringUtils.isNotBlank(this.search)) {
            boolQuery.must(QueryBuilders.matchPhraseQuery(this.field, this.search));
        }
        return new SearchSourceBuilder()
                .postFilter(boolQuery)
                .from(this.getFrom())
                .size(this.perSize)
                .explain(Play.mode.isDev());
    }

    public SearchSourceBuilder skuParams() {
        BoolQueryBuilder boolQuery = QueryBuilders.boolQuery();
        if(this.market != null) {
            boolQuery.must(QueryBuilders.termQuery("market", this.market.name().toLowerCase()))
                    .must(QueryBuilders.rangeQuery("date") // ES: date -> createDate
                            // 市场变更, 具体查询时间也需要变更
                            .from(Dates.morning(this.market.withTimeZone(this.begin).toDate())).includeLower(true)
                            .to(Dates.night(this.market.withTimeZone(this.end).toDate())).includeUpper(true));
        } else {
            boolQuery.must(QueryBuilders.rangeQuery("date")
                    .from(Dates.morning(this.begin)).includeLower(true)
                    .to(Dates.night(this.end)).includeUpper(true));
        }


        if(this.state != null) {
            boolQuery.must(QueryBuilders.termQuery("state", this.state.name().toLowerCase()));
        }
        if(StringUtils.isNotBlank(this.sku)) {
            boolQuery.must(QueryBuilders.matchPhraseQuery("sku", ES.parseEsString(sku).toLowerCase()));
        }
        if(StringUtils.isNotBlank(this.category)) {
            boolQuery.must(QueryBuilders.prefixQuery("sku", this.category));
        }
        if(StringUtils.isNotBlank(this.invoiceState)) {
            boolQuery.must(QueryBuilders.termQuery("invoice_state", this.invoiceState));
        }
        return new SearchSourceBuilder()
                .query(QueryBuilders.queryStringQuery(this.search()))
                .postFilter(boolQuery)
                .from(this.getFrom())
                .size(this.perSize)
                .explain(Play.mode.isDev());
    }

}
