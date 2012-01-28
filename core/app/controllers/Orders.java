package controllers;

import helper.Webs;
import models.market.Account;
import models.market.Orderr;
import org.apache.commons.io.FileUtils;
import org.apache.commons.lang.StringUtils;
import play.Logger;
import play.mvc.Controller;
import play.mvc.With;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Created by IntelliJ IDEA.
 * User: Wyatt
 * Date: 12-1-6
 * Time: 下午4:02
 */
@With({Secure.class, GzipFilter.class})
public class Orders extends Controller {

    public static void o_index(Integer p, Integer s) {
        Webs.fixPage(p, s);
        List<Orderr> orders = Orderr.find("ORDER BY createDate DESC").fetch(p, s);
        Long count = Orderr.count();
        render(orders, count, p, s);
    }

    public static void ini(String m) {
        List<File> files = new ArrayList<File>(FileUtils.listFiles(new File(System.getProperty("user.home") + "/elcuk-data/2012/back/" + m), new String[]{"xml", "csv"}, true));
        Account acc = Account.findById(1l);
        for(File file : files) {
            Logger.info("Parse: " + file.getAbsolutePath());
            List<Orderr> orders;
            Map<String, Orderr> orderrMap = new HashMap<String, Orderr>();
            Map<String, Orderr> oldOrderrMap = new HashMap<String, Orderr>();
            try {
                if(file.getName().contains("csv")) {
                    orders = new ArrayList<Orderr>(Orderr.parseUpdateOrderXML(file, Account.M.AMAZON_UK)); // 1. 解析出 XML 中所有的 Order 的最新信息
                } else {
                    orders = Orderr.parseAllOrderXML(file, Account.M.AMAZON_UK); // 1. 解析出 XML 中所有的 Order 的最新信息
                }
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
                if(file.getName().contains("csv")) {
                    for(Orderr newOrd : orders) {
                        if(oldOrderrMap.containsKey(newOrd.orderId)) continue;
                        Logger.warn("Update Order [" + newOrd.orderId + "] is not exist.");
                    }
                } else {

                    for(Orderr newOrd : orders) { // 3. 将数据库中没有加载到的 Order 给新保存
                        if(oldOrderrMap.containsKey(newOrd.orderId)) continue;
                        newOrd.account = Account.find("uniqueName=?", "amazon.co.uk_easyacc.eu@gmail.com").first();
                        newOrd.save();
                        Logger.info("Save Order: " + newOrd.orderId);
                    }
                }
            } catch(Exception e) {
                Logger.error(file.getName() + " 不是 AllOrders.xml 但不管::" + e.getMessage());
            }
        }
    }
}
