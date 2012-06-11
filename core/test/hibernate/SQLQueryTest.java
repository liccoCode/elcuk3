package hibernate;

import org.junit.Test;
import play.db.helper.SqlSelect;
import play.test.UnitTest;

/**
 * Created by IntelliJ IDEA.
 * User: wyattpan
 * Date: 6/11/12
 * Time: 3:51 PM
 */
public class SQLQueryTest extends UnitTest {
    @Test
    public void testSQLQuery() {
        System.out.println(new SqlSelect().select("createDate, price, quantity, currency, usdCost").from("OrderItem").limit(20).toString());
    }
}
