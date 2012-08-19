package jobs;

import helper.Dates;
import models.market.Account;
import models.market.JobRequest;
import models.market.M;
import models.market.Orderr;
import org.apache.commons.lang.StringUtils;
import org.joda.time.DateTime;
import play.Logger;
import play.jobs.Job;
import play.libs.IO;

import java.io.File;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * Created by IntelliJ IDEA.
 * User: wyattpan
 * Date: 6/11/12
 * Time: 2:44 PM
 */
public class AmazonOrderUpdateJob extends Job implements JobRequest.AmazonJob {
    @Override
    public void doJob() {
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
         * 1. 将需要更新的数据从 csv 文件中提取出来
         * 2. 遍历所有的订单, 利用 hibernate 的二级缓存, 加载 Orderr 进行保存或者更新
         */
        Set<Orderr> orderSet = AmazonOrderUpdateJob.updateOrderXML(new File(jobRequest.path), jobRequest.account.type);
        for(Orderr order : orderSet) {
            Orderr managed = Orderr.findById(order.orderId);
            if(managed == null) {
                Logger.error("Update Order [%s] is not exist.", order.orderId);
            } else {
                managed.updateAttrs(order);
            }
        }
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
                Orderr order = new Orderr();
                order.orderId = vals[0];
                order.paymentDate = new DateTime(Dates.parseXMLGregorianDate(vals[7]), Dates.timeZone(market)).toDate();
                order.shipDate = new DateTime(Dates.parseXMLGregorianDate(vals[8]), Dates.timeZone(market)).toDate();
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
                order.address = vals[25];
                order.address1 = (vals[26] + " " + vals[27]).trim();
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
