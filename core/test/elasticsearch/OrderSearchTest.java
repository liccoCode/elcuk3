package elasticsearch;

import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import helper.Constant;
import helper.ES;
import models.market.M;
import models.market.Orderr;
import models.view.post.OrderPOST;
import org.elasticsearch.index.query.BoolFilterBuilder;
import org.elasticsearch.index.query.FilterBuilders;
import org.elasticsearch.search.builder.SearchSourceBuilder;
import org.joda.time.DateTime;
import org.junit.Test;
import play.test.UnitTest;

import static org.hamcrest.CoreMatchers.*;

/**
 * Created by IntelliJ IDEA.
 * User: wyatt
 * Date: 10/24/13
 * Time: 4:00 PM
 */
public class OrderSearchTest extends UnitTest {

    @Test
    public void testClient() {
        JSONObject obj = ES.get(System.getenv(Constant.ES_INDEX), "order", "203-8671889-8524331");
        JSONObject source = obj.getJSONObject("_source");
        assertThat(source.getString("orderId"), is("203-8671889-8524331"));

        // utc -> +8
        assertThat(new DateTime(source.getString("createDate")).toString("yyyy-MM-dd HH:mm:ss"),
                // 2013-09-24 22:55:55
                is("2013-09-25 06:55:55"));
    }

    @Test
    public void testSearch() throws Exception {
        String search = "80DBK12000";

        BoolFilterBuilder boolFilter = FilterBuilders.boolFilter()
                .must(FilterBuilders.rangeFilter("createDate")
                        .from(DateTime.parse("2013-10-17T00:00:00Z").toDate())
                        .to(DateTime.parse("2013-10-24T00:00:00Z").toDate()));

        boolFilter.should(FilterBuilders.termFilter("market", M.AMAZON_DE.name().toLowerCase()));
        boolFilter.should(FilterBuilders.termFilter("state", Orderr.S.SHIPPED.name().toLowerCase()));

//        SearchSourceBuilder builder = new SearchSourceBuilder()
//                .query(QueryBuilders
//                        .queryString(search)
//                        .field("sids")
//                        .field("buyer")
//                        .field("email")
//                        .field("address")
//                        .field("orderId"))
//                .filter(boolFilter)
//                .from(0)
//                .size(5);
//
//        JSONObject obj = ES.search("elcuk2", "order", builder);
//        assertThat(obj.getJSONObject("hits").getJSONArray("hits").size(), is(5));
    }

    // 需要保证 gengar 服务器上 ES 的 order_bak type 存在
    // TODO: order_index_em.rb -> +: limit 1000 ;  +: order_bak
    @Test
    public void testPromotionSearch() {
        OrderPOST post = new OrderPOST();
        post.search = "";
        post.promotion = true;
        SearchSourceBuilder builder = post.params();

        JSONObject result = ES.search(System.getenv(Constant.ES_INDEX), "order_bak", builder);
        JSONObject hits = result.getJSONObject("hits");
        JSONArray innerHits = hits.getJSONArray("hits");

        assertThat(innerHits.size(), is(22));
        assertThat(innerHits.getJSONObject(0).getJSONObject("_source").getString("promotionIDs"), is(notNullValue()));
    }

    @Test
    public void testNoPromotionSearch() {
        OrderPOST post = new OrderPOST();
        post.search = "";
        post.promotion = false;
        SearchSourceBuilder builder = post.params();

        JSONObject result = ES.search(System.getenv(Constant.ES_INDEX), "order_bak", builder);
        JSONObject hits = result.getJSONObject("hits");
        JSONArray innerHits = hits.getJSONArray("hits");

        assertThat(innerHits.size(), is(25));
        assertThat(innerHits.getJSONObject(0).getJSONObject("_source").getString("promotionIDs"), is(nullValue()));
    }

    @Test
    public void testPromotionStringSearch() {
//        OrderPOST post = new OrderPOST();
//        post.begin = DateTime.parse("2013-01-01T09:33:54.795Z").toDate();
//        post.search = "20% off";
//        SearchSourceBuilder builder = post.params();
//
//        JSONObject result = ES.search("elcuk2", "order_bak", builder);
//        JSONObject hits = result.getJSONObject("hits");
//        JSONArray innerHits = hits.getJSONArray("hits");
//
//        assertThat(innerHits.size(), is(25));
//        assertThat(innerHits.getJSONObject(0).getJSONObject("_source").getString("promotionIDs"),
//                is(containsString("20% off")));
    }
}
