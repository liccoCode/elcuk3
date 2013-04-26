package jobs.promise;

import helper.FBA;
import helper.GTs;
import helper.Webs;
import models.procure.FBAShipment;
import models.procure.ShipItem;
import play.Logger;
import play.db.helper.JpqlSelect;
import play.jobs.Job;
import play.libs.F;

import java.util.*;

/**
 * FBA Shipment Items 的跟踪
 * User: wyattpan
 * Date: 10/17/12
 * Time: 2:29 PM
 */
public class AmazonFBAWatchPlusPromise extends Job {

    private List<FBAShipment> fbas;

    public AmazonFBAWatchPlusPromise(List<FBAShipment> fbas) {
        this.fbas = fbas;
    }

    /**
     * NOTE:
     * Fulfillment Outbound Shipment API section together share a maximum request quota of 30 and a restore rate of two requests every second.
     */

    @Override
    public void doJob() {
        if(this.fbas == null) this.fbas = new ArrayList<FBAShipment>();
        this.syncFBAShipmentItems();
    }

    /**
     * 同步 FBAs
     */
    public void syncFBAShipmentItems() {
        for(FBAShipment fba : this.reloadContext()) {
            this.syncFBAShipmentItems(fba);
            try {
                // 每处理一个暂停 500ms, 避免超过 2 req/s 的 limit
                Thread.sleep(500);
            } catch(InterruptedException e) {
                //ignore
            }
        }
    }

    /**
     * 每一个 Promise 的 Context 需要自己的 JPA Transaction
     *
     * @return
     */
    private List<FBAShipment> reloadContext() {
        if(this.fbas == null || this.fbas.size() == 0) return new ArrayList<FBAShipment>();
        List<Long> fbaId = new ArrayList<Long>();
        for(FBAShipment fba : fbas) {
            fbaId.add(fba.id);
        }
        return FBAShipment.find("id IN " + JpqlSelect.inlineParam(fbaId)).fetch();
    }

    /**
     * 跟踪 Amazon FBA ShipmentItems
     *
     * @param fbaShipment
     */
    private void syncFBAShipmentItems(FBAShipment fbaShipment) {
        try {
            /**
             * 1. 找到 Amazon 上这个 ShipmentId 的所有产品的数量
             *  - 将这份数据使用模板,记录在 FBAShipmetn 身上一份
             * 2. 找到系统内相同 ShipmentId 的 Shipment 的所有 ShipItems
             * 3. 比对两份数据并进行处理;
             *  - 如果系统内同一个 msku 为分开的 ShipItem 则取最高的那一个
             */
            Map<String, F.T2<Integer, Integer>> fbaItems = FBA
                    .listShipmentItems(fbaShipment.shipmentId, fbaShipment.account);
            fbaShipment.itemsOnAmazonWithHTML = GTs
                    .render("itemsOnAmazonWithHTML", GTs.newMap("fbaItems", fbaItems).build());
            fbaShipment.lastWatchAmazonItemsAt = new Date();
            fbaShipment.save();

            List<ShipItem> shipItems = ShipItem.sameFBAShipItems(fbaShipment.shipmentId);
            Collections.sort(shipItems, new SortShipItemQtyDown());
            // 使用 copy 为了在删除 map 中元素的时候不影响原有数据
            Map<String, F.T2<Integer, Integer>> fbaItemsCopy = new HashMap<String, F.T2<Integer, Integer>>(
                    fbaItems);

            // 处理 FBA 中一个个 msku , 而系统中拥有多个 运输项目 对应一个 FBA 中的 msku 的情况
            for(ShipItem item : shipItems) {
                F.T2<Integer, Integer> fbaItm = fbaItemsCopy.get(item.unit.selling.merchantSKU);
                // 找到后删除 Map 中的, 避免 ShipItems 中的重复处理
                fbaItemsCopy.remove(item.unit.selling.merchantSKU);
                if(fbaItm == null) {
                    // TODO Amazon 上有系统中没有, 该做什么? 提醒? 现在很多数据都与 Amazon 上不一样, 邮件提醒会疯掉.
                } else {
                    // 当 Amazon FBA 中的接收到的数量大于实际可接收的数量的时候, 需要将检查是否有相同的 msku 存在同一分运输单中, 需要分散到相同 msku 不同 ShipItem 上去.
                    if(fbaItm._1 > item.recivedQty) {
                        List<ShipItem> sameItemList = new ArrayList<ShipItem>();
                        sameItemList.add(item);
                        // 找到相同的 ShipItems
                        for(ShipItem sameItem : shipItems) {
                            if(!sameItem.id.equals(item.id) && sameItem.unit.selling.merchantSKU
                                    .equals(item.unit.selling.merchantSKU))
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
        } catch(Exception e) {
            Logger.warn("AmazonFBAWatchPlusPromise.syncFBAShipmentItems Error. %s", Webs.E(e));
        }
    }


    public static class SortShipItemQtyDown implements Comparator<ShipItem> {
        @Override
        public int compare(ShipItem o1, ShipItem o2) {
            return o2.qty - o1.qty;
        }
    }
}
