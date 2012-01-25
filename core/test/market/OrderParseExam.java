package market;

import models.market.Orderr;
import org.apache.commons.lang.StringUtils;
import org.junit.Test;
import play.libs.IO;
import play.test.UnitTest;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by IntelliJ IDEA.
 * User: wyattpan
 * Date: 1/25/12
 * Time: 3:52 PM
 */
public class OrderParseExam extends UnitTest {
    @Test
    public void findNoOrderItemOrderr() {
        List<String> lines = new ArrayList<String>();
        List<Orderr> orders = Orderr.findAll();
        for(Orderr o : orders) {
            if(o.items == null || o.items.size() == 0)
                lines.add(o.orderId);
        }
        IO.writeContent(StringUtils.join(lines, "\r\n"), new File("/tmp/noOrderItemOrder.txt"));
    }
}
