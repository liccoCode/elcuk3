package jobs;

import helper.FBA;
import helper.GTs;
import helper.Webs;
import models.Jobex;
import models.market.Account;
import models.procure.FBAShipment;
import models.procure.ShipItem;
import play.Logger;
import play.jobs.Every;
import play.jobs.Job;
import play.libs.F;

import java.util.*;

/**
 * FBA Shipment Items 的跟踪
 * 周期:
 * - 轮询周期: 20mn
 * - Duration: 1h
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
            List<FBAShipment> fbas = FBAShipment.find("account=? AND state NOT IN (?,?,?) ORDER BY lastWatchAmazonItemsAt",
                    acc, FBAShipment.S.PLAN, FBAShipment.S.CANCELLED, FBAShipment.S.DELETED).fetch(30);

            for(FBAShipment fba : fbas) {
                AmazonFBAWatchPlusJob.syncFBAShipmentItems(fba);
            }
        }
    }

    /**
     * 跟踪 Amazon FBA ShipmentItems
     *
     * @param fbaShipment
     */
    public static void syncFBAShipmentItems(FBAShipment fbaShipment) {
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
            fbaShipment.lastWatchAmazonItemsAt = new Date();
            fbaShipment.save();

            List<ShipItem> shipItems = ShipItem.sameFBAShipItems(fbaShipment.shipmentId);
            Collections.sort(shipItems, new SortShipItemQtyDown());
            // 使用 copy 为了在删除 map 中元素的时候不影响原有数据
            Map<String, F.T2<Integer, Integer>> fbaItemsCopy = new HashMap<String, F.T2<Integer, Integer>>(fbaItems);

            // 处理 FBA 中一个 msku , 而系统中拥有多个 运输项目 对应一个 FBA 中的 msku 的情况

            for(ShipItem item : shipItems) {
                F.T2<Integer, Integer> fbaItm = fbaItemsCopy.get(item.unit.selling.merchantSKU);
                // 找到后删除 Map 中的, 避免 ShipItems 中的重复处理
                fbaItemsCopy.remove(item.unit.selling.merchantSKU);
                if(fbaItm == null) {
                    // TODO Amazon 上有系统中没有, 该做什么? 提醒? 现在很多数据都与 Amazon 上不一样, 邮件提醒会疯掉.
                } else {
                    // 当接收的 FBA 的数据大于系统内的数量的时候需要做处理. 由于没处理一次 FBA Map 中就少一个, 所以 FBA 中就处理一次.
                    if(fbaItm._1 > item.recivedQty) {
                        List<ShipItem> sameItemList = new ArrayList<ShipItem>();
                        sameItemList.add(item);
                        // 找到相同的 ShipItems
                        for(ShipItem sameItem : shipItems) {
                            if(!sameItem.id.equals(item.id) && sameItem.unit.selling.merchantSKU.equals(item.unit.selling.merchantSKU))
                                sameItemList.add(sameItem);
                        }
                        int fbaQty = fbaItm._1;
                        for(ShipItem itm : sameItemList) {
                            if(fbaQty <= 0) break;
                            // 一个 ShipItem 一个 ShipItem 的满足
                            itm.recivedQty = Math.min(fbaQty, itm.qty);
                            fbaQty -= itm.recivedQty;
                            itm.save();
                        }
                        // 如果处理完了还 > 0, 那么则将这个数据随意 append 到一个 ShipItem 上.
                        if(fbaQty > 0) {
                            sameItemList.get(0).recivedQty += fbaQty;
                            sameItemList.get(0).save();
                        }
                    } else {
                        item.recivedQty = fbaItm._1;
                        item.save();
                    }
                }
            }

            // 在处理了系统内的 ShipItem 后检查
            // TODO 需要考虑相同 FBA Msku 合并 ShipItem 的情况
            fbaShipment.receivingCheck(fbaItems, shipItems);
        } catch(Exception e) {
            Logger.warn("AmazonFBAWatchPlusJob.syncFBAShipmentItems Error. %s", Webs.E(e));
        }
    }

    public static class SortShipItemQtyDown implements Comparator<ShipItem> {
        @Override
        public int compare(ShipItem o1, ShipItem o2) {
            return o2.qty - o1.qty;
        }
    }
}
