package market;

import models.market.Account;
import models.market.Orderr;
import models.market.Selling;
import models.product.Product;
import org.apache.commons.io.FileUtils;
import org.apache.commons.lang.StringUtils;
import org.junit.Test;
import play.Logger;
import play.db.jpa.JPA;
import play.test.Fixtures;
import play.test.UnitTest;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Created by IntelliJ IDEA.
 * User: wyattpan
 * Date: 1/6/12
 * Time: 4:23 PM
 */
public class OrderParseTest extends UnitTest {
    //    @Before
    public void setup() {
        Fixtures.delete(Product.class, Selling.class);
        Fixtures.loadModels("Product.yml", "Selling.yml");
    }

    //    @Test
    public void testParse() {
//        List<Orderr> orders = Orderr.parseAllOrderXML(new File("/Users/wyattpan/elcuk-data/2011/10/11/8141580584.xml"), Account.M.AMAZON_UK);
//        List<Orderr> orders = Orderr.parseAllOrderXML(new File("/Users/wyattpan/elcuk-data/2011/12/32/9566254144.xml"), Account.M.AMAZON_UK);
//        List<Orderr> orders = Orderr.parseAllOrderXML(new File("/Users/wyattpan/elcuk-data/2011/12/32/9566893024.xml"), Account.M.AMAZON_UK);
//        List<Orderr> orders = Orderr.parseAllOrderXML(new File("/Users/wyattpan/elcuk-data/2011/12/32/9567535464.xml"), Account.M.AMAZON_UK);
        List<Orderr> orders = Orderr.parseAllOrderXML(new File("/Users/wyattpan/elcuk-data/2012/01/02/9584115364.xml"), Account.M.AMAZON_UK);
        Account acc = Account.findById(1l);
//        List<Orderr> orders = Orderr.parseALLOrderXML(new File("F:/elcuk-data/2011/12/01/9018095104.xml"));
        for(Orderr or : orders) {
            or.account = acc;
            or.merge();
        }
    }

    @Test
    public void parseOrders() {
//        List<File> files = new ArrayList<File>(FileUtils.listFiles(new File("/Users/wyattpan/elcuk-data/2011/12/32/"), new String[]{"xml"}, true));
//        List<File> files = new ArrayList<File>(FileUtils.listFiles(new File("/Users/wyattpan/elcuk-data/2011/12/33/"), new String[]{"xml"}, true));
        List<File> files = new ArrayList<File>(FileUtils.listFiles(new File("/Users/wyattpan/elcuk-data/2012/back/"), new String[]{"xml"}, true));
        Account acc = Account.findById(1l);
        for(File file : files) {
            Logger.info("Parse: " + file.getAbsolutePath());
            List<Orderr> orders = new ArrayList<Orderr>();
            Map<String, Orderr> orderrMap = new HashMap<String, Orderr>();
            Map<String, Orderr> oldOrderrMap = new HashMap<String, Orderr>();
            try {
                orders = Orderr.parseAllOrderXML(file, Account.M.AMAZON_UK); // 1. 解析出 XML 中所有的 Order 的最新信息
                List<String> orderIds = new ArrayList<String>();
                for(Orderr or : orders) {
                    orderrMap.put(or.orderId, or);
                    orderIds.add(or.orderId);
                }
                List<Orderr> managedOrderrs = Orderr.find("orderId IN ('" + StringUtils.join(orderIds, "','") + "')").fetch();
                System.out.println("Managerd Orders: " + managedOrderrs.size());
                for(Orderr or : managedOrderrs) { // 2. 手动从数据库中加载出需要更新的 Order (managed),  然后再将这些处于被管理状态的 Order 进行更新;
                    Orderr newOrder = orderrMap.get(or.orderId);
                    or.updateOrderInfo(newOrder);
                    oldOrderrMap.put(or.orderId, or);
                }
                System.out.println("Save Unsaved Order...");
                for(Orderr newOrd : orders) { // 3. 将数据库中没有加载到的 Order 给新保存
                    if(oldOrderrMap.containsKey(newOrd.orderId)) continue;
                    newOrd.save();
                    Logger.info("Save Order: " + newOrd.orderId);
                }
            } catch(Exception e) {
                Logger.error(file.getName() + " 不是 AllOrders.xml 但不管::" + e.getMessage());
            }
            JPA.em().flush();
        }
    }
}
