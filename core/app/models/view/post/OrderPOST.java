package models.view.post;

import com.alibaba.fastjson.JSONObject;
import helper.ES;
import models.market.M;
import models.market.Orderr;
import org.apache.commons.lang.StringUtils;
import org.elasticsearch.index.query.BoolFilterBuilder;
import org.elasticsearch.index.query.FilterBuilders;
import org.elasticsearch.index.query.QueryBuilders;
import org.elasticsearch.search.builder.SearchSourceBuilder;
import org.joda.time.DateTime;
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
        this.end = DateTime.now().toDate();
        this.begin = DateTime.now().minusDays(7).toDate();
        this.perSize = 25;
        this.page = 1;
    }

    public OrderPOST(int perSize) {
        this.perSize = perSize;
    }

    private static Pattern ORDER_NUM_PATTERN = Pattern.compile("^\\+(\\d+)$");

    public Long accountId;

    public M market;

    public Orderr.S state;

    public String orderBy = "createDate";

    public String desc = "DESC";

    public Boolean paymentInfo = true;

    public Boolean warnning = false;

    public Boolean promotion = null;


    @SuppressWarnings("unchecked")
    public List<Orderr> query() {
        SearchSourceBuilder builder = this.params();
        System.out.println(builder);
        try {
            JSONObject result = ES.search("elcuk2", "order", builder);
            JSONObject hits = result.getJSONObject("hits");
            this.count = hits.getLong("total");
            Set<String> orderIds = new HashSet<String>();
            for(Object obj : hits.getJSONArray("hits")) {
                JSONObject hit = (JSONObject) obj;
                orderIds.add(hit.getJSONObject("_source").getString("orderId"));
            }
            if(orderIds.size() <= 0)
                throw new FastRuntimeException("没有结果");
            return Orderr.find(SqlSelect.whereIn("orderId", orderIds)).fetch();
        } catch(Exception e) {
            return new ArrayList<Orderr>();
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
        BoolFilterBuilder boolFilter = FilterBuilders.boolFilter()
                .must(FilterBuilders.rangeFilter("createDate")
                        .from(this.begin)
                        .to(this.end));


        SearchSourceBuilder builder = new SearchSourceBuilder();
//        builder..prepareSearch("elcuk2")
//                .setTypes("order")
        builder.query(QueryBuilders
                .queryString(StringUtils.isBlank(this.search) ? "*" : this.search)
                .field("sids")
                .field("buyer")
                .field("email")
                .field("address")
                .field("orderId")
                .field("userid")
                .field("trackNo"))
                .filter(boolFilter)
                .explain(true)
                .from(this.getFrom())
                .size(this.perSize);


        if(this.market != null) {
            boolFilter.should(FilterBuilders.termFilter("market", this.market.name().toLowerCase()));
        }
        if(this.state != null) {
            boolFilter.should(FilterBuilders.termFilter("state", this.state.name().toLowerCase()));
        }
        if(this.accountId != null) {
            boolFilter.should(FilterBuilders.termFilter("account_id", this.accountId));
        }
        return builder;
    }

    /*
    @Override
    public F.T2<String, List<Object>> para2ms() {
        StringBuilder sbd = new StringBuilder(" FROM Orderr o");
        sbd.append(" LEFT JOIN o.items oi ");
        sbd.append(" WHERE 1=1");
        List<Object> params = new ArrayList<Object>();
        if(this.accountId != null) {
            sbd.append("AND o.account.id=? ");
            params.add(this.accountId);
        }

        if(this.from != null && this.to != null) {
            sbd.append("AND o.createDate>=? AND o.createDate<=? ");
//             * 如果选择了某一个市场, 则需要将时间也匹配上时区; 例:
//             * 1. 搜索 2013-01-17 日美国市场的订单
//             * 2. 转换语义: 搜索北京时间 2013-01-17 16:00:00 ~ 2013-01-18 16:00:00 的美国市场的订单
//             * 3. 转换后搜索
            if(this.market != null) {
                params.add(this.market.withTimeZone(this.from).toDate());
                params.add(this.market.withTimeZone(Dates.night(this.to)).toDate());
            } else {
                params.add(Dates.morning(this.from));
                params.add(Dates.night(this.to));
            }
        }

        if(this.market != null) {
            sbd.append("AND o.market=? ");
            params.add(this.market);
        }

        if(this.state != null) {
            sbd.append("AND o.state=? ");
            params.add(this.state);
        }

        if(this.warnning != null) {
            sbd.append("AND o.warnning=?");
            params.add(this.warnning);
        }

        if(this.promotion != null) {
            if(this.promotion)
                sbd.append("AND orderId in(select order.orderId from SaleFee where type.name=? OR type.parent.name=?)");
            else
                sbd.append(
                        "AND orderId not in(select order.orderId from SaleFee where type.name=? OR type.parent.name=?)");
            params.add("promorebates"); //促销费用
            params.add("promorebates");
        }

        if(StringUtils.isNotBlank(this.search)) {
            // 支持 +23 这样搜索订单的购买数大于某个值
            Matcher matcher = ORDER_NUM_PATTERN.matcher(this.search);
            if(matcher.matches()) {
                int orderUnbers = NumberUtils.toInt(matcher.group(1), 1);
                sbd.append("AND (select sum(oi.quantity) from OrderItem oi")
                        .append(" WHERE oi.order.orderId=orderId)>")
                        .append(orderUnbers).append(" ");
            } else {
                String search = this.word();
                sbd.append("AND (o.orderId LIKE ? OR ").
                        append("o.address LIKE ? OR ").
                        append("o.address1 LIKE ? OR ").
                        append("o.buyer LIKE ? OR ").
                        append("o.city LIKE ? OR ").
                        append("o.country LIKE ? OR ").
                        append("o.email LIKE ? OR ").
                        append("o.postalCode LIKE ? OR ").
                        append("o.phone LIKE ? OR ").
                        append("o.province LIKE ? OR ").
                        append("o.reciver LIKE ? OR ").
                        append("o.memo LIKE ? OR ").
                        append("o.userid LIKE ? OR ").
                        append("o.trackNo LIKE ? OR ").
                        append("oi.product.sku LIKE ?) ");
                for(int i = 0; i < 15; i++) params.add(search);
            }
        }

        if(this.paymentInfo != null) {
            if(this.paymentInfo)
                sbd.append("AND SIZE(o.fees)>0 ");
            else if(!this.paymentInfo)
                sbd.append("AND SIZE(o.fees)<=0 ");
        }

        if(StringUtils.isNotBlank(this.orderBy)) {
            sbd.append("ORDER BY o.").append(this.orderBy).append(" ")
                    .append(StringUtils.isNotBlank(this.desc) ? this.desc : "ASC");
        }

        return new F.T2<String, List<Object>>(sbd.toString(), params);
    }
            */

}
