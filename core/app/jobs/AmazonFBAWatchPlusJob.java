package jobs;

import com.amazonservices.mws.FulfillmentInboundShipment._2010_10_01.FBAInboundServiceMWSException;
import helper.FBA;
import helper.GTs;
import helper.Webs;
import models.Jobex;
import models.market.Account;
import models.procure.FBAShipment;
import models.procure.ShipItem;
import models.procure.Shipment;
import play.Logger;
import play.jobs.Every;
import play.jobs.Job;
import play.libs.F;

import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Map;

/**
 * FBA Shipment Items 的跟踪
 * 周期:
 * - 轮询周期: 1h
 * - Duration: TODO 使用 cron
 * User: wyattpan
 * Date: 10/17/12
 * Time: 2:29 PM
 */
@Every("20mn")
public class AmazonFBAWatchPlusJob extends Job {
    /**
     * NOTE:
     * Fulfillment Outbound Shipment API section together share a maximum request quota of 30 and a restore rate of two requests every second.
     */

    @Override
    public void doJob() {
        if(!Jobex.findByClassName(AmazonFBAWatchPlusJob.class.getName()).isExcute()) return;
        List<Account> accounts = Account.openedSaleAcc();
        for(Account acc : accounts) {
            List<FBAShipment> fbas = FBAShipment.find("account=? AND state NOT IN (?,?) ORDER BY lastWatchAmazonItemsAt",
                    acc, FBAShipment.S.PLAN, FBAShipment.S.CANCELLED).fetch(30);

            for(FBAShipment fba : fbas) {
                AmazonFBAWatchPlusJob.listFBAShipmentItems(fba);
            }
        }
    }

    /**
     * 跟踪 Amazon FBA ShipmentItems
     *
     * @param fbaShipment
     */
    public static void listFBAShipmentItems(FBAShipment fbaShipment) {
        try {
            /**
             * 1. 找到 Amazon 上这个 ShipmentId 的所有产品的数量
             *  - 将这份数据使用模板,记录在 FBAShipmetn 身上一份
             * 2. 找到系统内相同 ShipmentId 的 Shipment 的所有 ShipItems
             * 3. 比对两份数据并进行处理;
             *  - 如果系统内同一个 msku 为分开的 ShipItem 则取最高的那一个
             */
            Map<String, F.T2<Integer, Integer>> fbaItems = FBA.listShipmentItems(fbaShipment.shipmentId, fbaShipment.account);
            fbaShipment.itemsOnAmazonWithHTML = GTs.render("itemsOnAmazonWithHTML", GTs.newMap("fbaItems", fbaItems).build());
            fbaShipment.save();

            List<ShipItem> shipItems = ShipItem.find("shipment.fbaShipment=?", fbaShipment).fetch();
            Collections.sort(shipItems, new SortShipItemQtyDown());
            for(ShipItem item : shipItems) {
                F.T2<Integer, Integer> fbaItm = fbaItems.get(item.unit.selling.merchantSKU);
                // 找到后删除 Map 中的, 避免 ShipItems 中的重复处理
                fbaItems.remove(item.unit.selling.merchantSKU);
                if(fbaItm == null) {
                    // TODO Amazon 上有系统中没有, 该做什么? 提醒? 现在很多数据都与 Amazon 上不一样, 邮件提醒会疯掉.
                } else {
                    item.recivedQty = fbaItm._1;
                }
                item.save();
            }
        } catch(FBAInboundServiceMWSException e) {
            Logger.warn("AmazonFBAWatchPlusJob.listFBAShipmentItems Error. %s", Webs.E(e));
        }
    }

    public static class SortShipItemQtyDown implements Comparator<ShipItem> {
        @Override
        public int compare(ShipItem o1, ShipItem o2) {
            return o2.qty - o1.qty;
        }
    }
}
