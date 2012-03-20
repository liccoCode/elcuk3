package finance;

import helper.Currency;
import org.junit.Test;
import play.test.UnitTest;

/**
 * Created by IntelliJ IDEA.
 * User: wyattpan
 * Date: 3/19/12
 * Time: 10:58 AM
 */
public class CurrencyTest extends UnitTest {
    @Test
    public void testCurrencyUpdate() {
        Currency.updateCRY();
    }
}
