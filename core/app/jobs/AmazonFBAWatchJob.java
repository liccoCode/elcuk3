package jobs;

import helper.FBA;
import helper.Webs;
import jobs.promise.AmazonFBAWatchPlusPromise;
import models.Jobex;
import models.market.Account;
import models.procure.FBAShipment;
import org.apache.commons.lang.StringUtils;
import play.Logger;
import play.jobs.Every;
import play.jobs.Job;
import play.libs.F;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import static models.procure.FBAShipment.S;

/**
 * Amazon FBA 的入库情况跟踪任务;
 * 周期:
 * - 轮询周期: 5mn
 * - Duration: 1h
 * User: wyattpan
 * Date: 10/16/12
 * Time: 11:49 AM
 */
@Every("5mn")
public class AmazonFBAWatchJob extends Job {
    @Override
    public void doJob() {
        if(!Jobex.findByClassName(AmazonFBAWatchJob.class.getName()).isExcute()) return;

        List<Account> accounts = Account.openedSaleAcc();
        for(Account acc : accounts) {
            List<FBAShipment> fbas = FBAShipment.find("account=? AND state NOT IN (?,?,?,?)", acc, S.PLAN, S.CANCELLED, S.CLOSED, S.DELETED).fetch();
            AmazonFBAWatchJob.watchFBAs(acc, fbas);
            new AmazonFBAWatchPlusPromise(fbas).now();
        }
    }

    /**
     * 查看 FBAs; (抽出来方便测试)
     *
     * @param acc
     * @param shipments
     */
    public static void watchFBAs(Account acc, List<FBAShipment> shipments) {
        // 1. 更新 FBA Shipment 状态
        List<String> shipmentIds = new ArrayList<String>();
        for(FBAShipment shipment : shipments) {
            shipmentIds.add(shipment.shipmentId);
            // 再循环内每达到 20 个, 就执行更新(避免翻页), 否则继续向 id list 中添加;
            if(shipmentIds.size() == 20) {
                AmazonFBAWatchJob.amazonFBAState(acc, shipments, shipmentIds);
                try {
                    // 避免超过 Amazon API 的每秒限制
                    Thread.sleep(500);
                } catch(InterruptedException e) {
                    //ignore
                }
                shipmentIds.clear();
            }
        }
        // 最后如果落到外面, shipmentIds 的数量一定小于 50
        AmazonFBAWatchJob.amazonFBAState(acc, shipments, shipmentIds);
    }

    /**
     * 跟踪 Amazon FBA 的状态
     *
     * @param acc
     * @param shipments
     * @param shipmentIds
     */
    private static void amazonFBAState(Account acc, List<FBAShipment> shipments, List<String> shipmentIds) {
        if(shipmentIds.size() > 0) { // 如果没有, 则不需要检查了
            try {
                Map<String, F.T3<String, String, String>> shipmentT3 = FBA.listShipments(shipmentIds, acc);
                for(FBAShipment shipment : shipments) {
                    F.T3<String, String, String> t3 = shipmentT3.get(shipment.shipmentId);
                    if(t3 == null)
                        continue;
                    try {
                        S state = S.valueOf(t3._1);
                        shipment.isNofityState(state);
                        shipment.save();

                        shipment.receiptAndreceivingCheck();
                    } catch(Exception e) {
                        Logger.warn(String.format("AmazonFBAWatchJob state parse STATE %s error.", t3._1));
                    }
                }
            } catch(Exception e) {
                Logger.warn(String.format("Update Account %s FBA Shipment failed. ShipmentIds: %s. %s",
                        acc.prettyName(), StringUtils.join(shipmentIds, ","), Webs.E(e)));
            }
        }
    }
}
