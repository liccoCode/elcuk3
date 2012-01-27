package market;

import models.market.OrderItem;
import models.market.Orderr;
import org.apache.commons.lang.StringUtils;
import org.junit.Test;
import play.libs.IO;
import play.test.UnitTest;

import java.io.File;
import java.util.*;
import java.util.concurrent.atomic.AtomicInteger;

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

    @Test
    public void testCalcuQTY() {
        Calendar cal = Calendar.getInstance();
        cal.set(2011, 9, 6);
        Calendar cal2 = Calendar.getInstance();
        cal2.set(2011, 9, 31);
        List<Orderr> orders = Orderr.find("createDate>=? AND createDate<=?", cal.getTime(), cal2.getTime()).fetch();
        Map<String, AtomicInteger> qtyMap = new HashMap<String, AtomicInteger>();
        for(Orderr ord : orders) {
            if(ord.items == null || ord.items.size() == 0) continue;
            for(OrderItem oi : ord.items) {
                if(qtyMap.containsKey(oi.product.sku))
                    qtyMap.get(oi.product.sku).addAndGet(oi.quantity);
                else
                    qtyMap.put(oi.product.sku, new AtomicInteger(oi.quantity));
            }
        }
        StringBuilder sb = new StringBuilder();
        for(String sku : qtyMap.keySet()) {
            sb.append(String.format("%s : %s\r\n", sku, qtyMap.get(sku).get()));
        }
        IO.writeContent(sb.toString(), new File("/tmp/sku_quantity.txt"));
    }
}
