package hibernate;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.serializer.SerializerFeature;
import helper.JPAs;
import models.market.Account;
import models.market.OrderItem;
import org.joda.time.DateTime;
import org.junit.Test;
import play.db.helper.JpqlSelect;
import play.db.helper.SqlSelect;
import play.test.UnitTest;

import java.util.List;
import java.util.Map;

/**
 * Created by IntelliJ IDEA.
 * User: wyattpan
 * Date: 7/3/12
 * Time: 11:32 AM
 */
public class JPQLTest extends UnitTest {

    @Test
    public void testOrderItemGroupByCategory() {
        Account acc = new Account();
        acc.id = 1l;
        SqlSelect select = new JpqlSelect()
                .select("oi.product.sku as sku, oi.order.orderId as orderId, oi.quantity as qty")
                .from("OrderItem oi")
                .where("oi.order.account=?").param(acc)
                .where("oi.createDate>=?").param(DateTime.parse("2012-06-01").toDate());
        List<Map> rows = JPAs.createQueryMap(select)./*setMaxResults(10).*/getResultList();
        Map row = rows.get(0);
        System.out.println(row.get("sku") + "::" + row.get("orderId") + "::" + row.get("qty"));
        System.out.println("Rows: " + rows.size());
    }

    @Test
    public void testItemGroupByCategory() {
        String json = JSON.toJSONString(OrderItem.itemGroupByCategory(DateTime.parse("2012-06-01").toDate(), DateTime.parse("2012-07-01").toDate(), Account.<Account>findById(1l)),
                SerializerFeature.PrettyFormat);
        System.out.println(json);
    }
}
