package jobs;

import helper.Dates;
import helper.LogUtils;
import helper.Webs;
import models.Jobex;
import models.market.Account;
import models.market.JobRequest;
import models.market.M;
import models.market.Orderr;
import org.apache.commons.lang.StringUtils;
import play.Logger;
import play.db.DB;
import play.jobs.Job;
import play.libs.IO;

import java.io.File;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.List;

/**
 * <pre>
 * 通过 Amazon FBA 来更新订单的信息.
 *  * 周期:
 * - 轮询周期: 1h
 * - Duration: 2h
 * - Job Interval: 24h
 * </pre>
 * User: wyattpan
 * Date: 6/11/12
 * Time: 2:44 PM
 * @deprecated
 */
public class AmazonOrderUpdateJob extends Job implements JobRequest.AmazonJob {
    @Override
    public void doJob() {
        long begin = System.currentTimeMillis();
        if(!Jobex.findByClassName(AmazonOrderUpdateJob.class.getName()).isExcute()) return;
        // 对每一个用户都是如此
        List<Account> accs = Account.openedSaleAcc();

        // 1,2. 需要创建新的 Job
        for(Account acc : accs) {
            JobRequest job = JobRequest.checkJob(acc, this, acc.marketplaceId());
            if(job != null) job.request();
        }
        Logger.info("AmazonOrderUpdateJob step1 done!");

        // 3. 更新状态的 Job
        JobRequest.updateState(type());
        Logger.info("AmazonOrderUpdateJob step2 done!");

        // 4. 获取 ReportId
        JobRequest.updateReportId(type());

        Logger.info("AmazonOrderUpdateJob step3 done!");

        // 5. 下载 report 文件
        JobRequest.downLoad(type());
        Logger.info("AmazonOrderUpdateJob step4 done!");

        // 6. 处理下载好的文件
        JobRequest.dealWith(type(), this);
        Logger.info("AmazonOrderUpdateJob step5 done!");
        if(LogUtils.isslow(System.currentTimeMillis() - begin,"AmazonOrderUpdateJob")) {
            LogUtils.JOBLOG.info(String
                    .format("AmazonOrderUpdateJob calculate.... [%sms]", System.currentTimeMillis() - begin));
        }
    }


    /**
     * 每 6 小时才可以创建一次
     *
     * @return
     */
    @Override
    public int intervalHours() {
        return 24;
    }

    @Override
    public void callBack(JobRequest jobRequest) {
        /**
         * 补充更新订单信息
         */
        List<Orderr> orderSet = AmazonOrderUpdateJob.updateOrderXML(new File(jobRequest.path), jobRequest.account.type);
        AmazonOrderUpdateJob.updateShippedOrder(orderSet);
    }

    @Override
    public JobRequest.T type() {
        return JobRequest.T.ALL_FBA_ORDER_SHIPPED;
    }


    /**
     * 解析 Amazon 的 Shipped Order 邮件,对系统内已经存在的订单进行更新
     *
     * @param file
     * @return
     */
    public static List<Orderr> updateOrderXML(File file, M market) {
        List<String> lines = IO.readLines(file, "UTF-8");
        List<Orderr> orderrs = new ArrayList<Orderr>();
        lines.remove(0); //删除第一行标题
        for(String line : lines) {
            // 在解析 csv 文件的时候会发现有重复的项出现, 不过这没关系,
            String[] vals = StringUtils.splitPreserveAllTokens(line, "\t");
            try {
                if(vals[0].toUpperCase().startsWith("S")) {
                    Logger.info("Skip Self Order[" + vals[0] + "].");
                    continue;
                }
                //amazon-order-id	merchant-order-id	shipment-id	shipment-item-id	amazon-order-item-id	merchant-order-item-id	purchase-date	payments-date	shipment-date	reporting-date	buyer-email	buyer-name	buyer-phone-number	sku	product-name	quantity-shipped	currency	item-price	item-tax	shipping-price	shipping-tax	gift-wrap-price	gift-wrap-tax	ship-service-level	recipient-name	ship-address-1	ship-address-2	ship-address-3	ship-city	ship-state	ship-postal-code	ship-country	ship-phone-number	bill-address-1	bill-address-2	bill-address-3	bill-city	bill-state	bill-postal-code	bill-country	item-promotion-discount	ship-promotion-discount	carrier	tracking-number	estimated-arrival-date	fulfillment-center-id	fulfillment-channel	sales-channel
                // 例子数据
                //110-2162328-5179422		DmlbKXMtN	Dprp1L4JR	07664457044546		2013-01-15T12:24:57+00e00	2013-01-17T05:51:50+00:00	2013-01-17T05:06:24+00:00	2013-01-17T16:10:00+00:00	blnpdwtsw5npzjr@marketplace.amazon.com	Diana Gil		80DBK12000-AB	EasyAcc 12000mAh 4 x USB Portable External Battery Pack Charger Power Bank for Tablets: iPad 3, iPad mini; Kindle Fire HD, Google Nexus 7, Nexus 10; S	1	USD	40.99	0.00	3.85	0.00	0.00	0.00	Standard	Diana Gil	163 Lloyd Street	2ND Floor		New Haven	CT	06513	US									0.00	-3.85	SMARTPOST	9102901001301798807420	2013-01-24T04:00:00+00:00	LEX1	AFN	Amazon.com
                Orderr order = new Orderr();
                order.orderId = vals[0];
                order.shipDate = Dates.parseXMLGregorianDate(vals[8]);
                order.shippingService = vals[42];
                if(StringUtils.isNotBlank(vals[43])) {
                    order.trackNo = vals[43];
                    order.arriveDate = Dates.parseXMLGregorianDate(vals[44]);
                }
                order.email = vals[10];
                order.buyer = vals[11];
                order.reciver = vals[24];

                List<String> address = new ArrayList<String>();
                for(int i = 25; i <= 27; i++) {
                    if(StringUtils.isBlank(vals[i])) continue;
                    address.add(vals[i].trim());
                }
                order.address = StringUtils.join(address, ", ");

                List<String> billingAddress = new ArrayList<String>();
                for(int i = 33; i <= 39; i++) {
                    if(StringUtils.isBlank(vals[i])) continue;
                    billingAddress.add(vals[i].trim());
                }
                order.address1 = StringUtils.join(billingAddress, ", ");

                orderrs.add(order);
            } catch(Exception e) {
                Logger.warn("Parse Order[" + vals[0] + "] update Error. [" + e.getMessage() + "]");
            }
        }
        return orderrs;
    }

    private static void updateShippedOrder(List<Orderr> fbaShippedOrderrs) {
        PreparedStatement psmt = null;
        try {
            psmt = DB.getConnection()
                    .prepareStatement("UPDATE Orderr SET shipDate=?, shippingService=?, trackNo=?, arriveDate=?," +
                            " email=?, buyer=?, reciver=?, address=?, address1=?" +
                            " WHERE orderId=?");
            int i = 1;
            for(Orderr orderr : fbaShippedOrderrs) {
                psmt.setTimestamp(i++, orderr.shipDate == null ? null : new Timestamp(orderr.shipDate.getTime()));
                psmt.setString(i++, orderr.shippingService);
                psmt.setString(i++, orderr.trackNo);
                psmt.setTimestamp(i++, orderr.arriveDate == null ? null : new Timestamp(orderr.arriveDate.getTime()));
                psmt.setString(i++, orderr.email);
                psmt.setString(i++, orderr.buyer);
                psmt.setString(i++, orderr.reciver);
                psmt.setString(i++, orderr.address);
                psmt.setString(i++, orderr.address1);
                psmt.setString(i, orderr.orderId);
                psmt.addBatch();
                i = 1;
            }
            int[] results = psmt.executeBatch();
            Logger.info("UpdateShippedOrderrs %s. Results: [%s](%s)", fbaShippedOrderrs.size(),
                    Webs.intArrayString(results), results.length);
        } catch(SQLException e) {
            e.printStackTrace();
        } finally {
            try {
                if(psmt != null) psmt.close();
            } catch(Exception e) {
                e.printStackTrace();
            }
        }
    }
}
