package jobs;

import helper.Dates;
import models.Jobex;
import models.market.Account;
import models.market.JobRequest;
import models.market.M;
import models.market.Orderr;
import org.apache.commons.lang.StringUtils;
import play.Logger;
import play.jobs.Job;
import play.libs.IO;

import java.io.File;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * <pre>
 * 通过 Amazon FBA 来更新订单的信息.
 *  * 周期:
 * - 轮询周期: 10mn
 * - Duration: 30mn
 * - Job Interval: 6h
 * </pre>
 * User: wyattpan
 * Date: 6/11/12
 * Time: 2:44 PM
 */
public class AmazonOrderUpdateJob extends Job implements JobRequest.AmazonJob {
    @Override
    public void doJob() {
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
    }


    /**
     * 每 6 小时才可以创建一次
     *
     * @return
     */
    @Override
    public int intervalHours() {
        return 6;
    }

    @Override
    public void callBack(JobRequest jobRequest) {
        /**
         * TODO 仅仅更新 Orderr 信息
         */
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
    public static Set<Orderr> updateOrderXML(File file, M market) {
        List<String> lines = IO.readLines(file, "UTF-8");
        Set<Orderr> orderrs = new HashSet<Orderr>();
        lines.remove(0); //删除第一行标题
        for(String line : lines) {
            // 在解析 csv 文件的时候会发现有重复的项出现, 不过这没关系,
            String[] vals = StringUtils.splitPreserveAllTokens(line, "\t");
            try {
                if(vals[0].toUpperCase().startsWith("S")) {
                    Logger.info("Skip Self Order[" + vals[0] + "].");
                    continue;
                }
                // 例子数据
                //110-2162328-5179422		DmlbKXMtN	Dprp1L4JR	07664457044546		2013-01-15T12:24:57+00e00	2013-01-17T05:51:50+00:00	2013-01-17T05:06:24+00:00	2013-01-17T16:10:00+00:00	blnpdwtsw5npzjr@marketplace.amazon.com	Diana Gil		80DBK12000-AB	EasyAcc 12000mAh 4 x USB Portable External Battery Pack Charger Power Bank for Tablets: iPad 3, iPad mini; Kindle Fire HD, Google Nexus 7, Nexus 10; S	1	USD	40.99	0.00	3.85	0.00	0.00	0.00	Standard	Diana Gil	163 Lloyd Street	2ND Floor		New Haven	CT	06513	US									0.00	-3.85	SMARTPOST	9102901001301798807420	2013-01-24T04:00:00+00:00	LEX1	AFN	Amazon.com
                Orderr order = new Orderr();
                order.orderId = vals[0];
                order.paymentDate = Dates.parseXMLGregorianDate(vals[7]);
                order.shipDate = Dates.parseXMLGregorianDate(vals[8]);
                order.shippingService = vals[42];
                if(StringUtils.isNotBlank(vals[43])) {
                    order.trackNo = vals[43];
                    order.arriveDate = Dates.parseXMLGregorianDate(vals[44]);
                }
                /**
                 * 这里 Amazon 给的数据并不完整, 需要额外从 OrderInfoFetch 中补全.
                 order.phone = vals[12]; 这个在这份文档中始终没有出现过
                 */
                order.email = vals[10];
                order.buyer = vals[11];
                order.shipLevel = vals[23];
                order.reciver = vals[24];
                order.address = vals[25] + "\r\n" + (vals[26] + " " + vals[27]).trim();
                order.city = vals[28];
                order.province = vals[29];
                order.postalCode = vals[30];
                order.country = vals[31];

                orderrs.add(order);
            } catch(Exception e) {
                Logger.warn("Parse Order[" + vals[0] + "] update Error. [" + e.getMessage() + "]");
            }
        }
        return orderrs;
    }
}
