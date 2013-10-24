package elasticsearch;

import helper.ES;
import models.market.M;
import models.market.Orderr;
import org.elasticsearch.action.search.SearchResponse;
import org.elasticsearch.action.search.SearchType;
import org.elasticsearch.index.query.BoolFilterBuilder;
import org.elasticsearch.index.query.FilterBuilders;
import org.elasticsearch.index.query.QueryBuilders;
import org.joda.time.DateTime;
import org.junit.Before;
import org.junit.Test;
import play.test.UnitTest;

import java.util.Map;

import static org.hamcrest.CoreMatchers.is;

/**
 * Created by IntelliJ IDEA.
 * User: wyatt
 * Date: 10/24/13
 * Time: 4:00 PM
 */
public class OrderSearchTest extends UnitTest {
    @Before
    public void setUp() {
        ES.init();
    }

    @Test
    public void testClient() {
        Map<String, Object> source = ES.get("elcuk2", "order", "203-8671889-8524331");
        assertThat(source.get("orderId").toString(), is("203-8671889-8524331"));

        // utc -> +8
        assertThat(new DateTime(source.get("createDate").toString()).toString("yyyy-MM-dd HH:mm:ss"),
                // 2013-09-24 22:55:55
                is("2013-09-25 06:55:55"));
    }

    @Test
    public void testSearch() {
        String search = "80DBK12000";

        BoolFilterBuilder boolFilter = FilterBuilders.boolFilter()
                .must(FilterBuilders.rangeFilter("createDate")
                        .from(DateTime.parse("2013-10-17T00:00:00Z").toDate())
                        .to(DateTime.parse("2013-10-24T00:00:00Z").toDate()));

        boolFilter.should(FilterBuilders.termFilter("market", M.AMAZON_DE.name().toLowerCase()));
        boolFilter.should(FilterBuilders.termFilter("state", Orderr.S.SHIPPED.name().toLowerCase()));

        SearchResponse resp = ES.client().prepareSearch("elcuk2")
                .setTypes("order")
                .setSearchType(SearchType.QUERY_THEN_FETCH)
                .setQuery(QueryBuilders
                        .queryString(search)
                        .field("sids")
                        .field("buyer")
                        .field("email")
                        .field("address")
                        .field("orderId"))
                .setFilter(boolFilter)
                .setFrom(0).setSize(25)
                .execute().actionGet();
        assertThat(resp.getHits().totalHits(), is(783l));
        assertThat(resp.getHits().getHits().length, is(25));
    }
}
