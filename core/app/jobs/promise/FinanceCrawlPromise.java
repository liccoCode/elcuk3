package jobs.promise;

import helper.Dates;
import helper.HTTP;
import models.finance.SaleFee;
import models.market.Account;
import org.apache.commons.lang.StringUtils;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import play.Logger;
import play.jobs.Job;
import play.libs.F;

import java.io.File;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;

/**
 * 用来抓取并且处理 Amazon 每 14 天生成的所有费用的文件.(不会周期运行, 一次性运输任务)
 * User: wyatt
 * Date: 11/24/12
 * Time: 7:50 PM
 */
public class FinanceCrawlPromise extends Job {

    private Account account;
    private Date date;

    private String url = "";

    public FinanceCrawlPromise(Account account, Date date) {
        this.account = account;
        this.date = date;
    }

    @Override
    public void doJob() {
        /**
         * 1. 抓取 Payments 页面的第二个链接;
         * 2. 根据链接下载内容
         * 3. 解析文件并且进入数据库
         */
        String html = HTTP.get(this.account.cookieStore(), this.account.type.pastSettlementsUrl());
        this.pickReportUrl(html);
        F.Option<File> file = HTTP.getDownFile(this.url, this.fileName(), this.account.cookieStore());
        if(!file.isDefined()) {
            //TODO 通知没有成功
            Logger.warn("Amazon Payment Day 14 file download failed. [%s]", this.url);
        }
        // TODO 将文件上传到 S3

        Map<String, List<SaleFee>> feeMap = SaleFee.flatFileFinanceParse(file.get(), this.account);
        this.parseToDB(feeMap);

    }

    /**
     * 将解析出来的 FeeMap 处理并存储进入 DB
     *
     * @param feeMap
     */
    public void parseToDB(Map<String, List<SaleFee>> feeMap) {
        Logger.info("Begin to parse FeeMap to DB...");
        /**
         * 1. 首先将所有的设计到的订单的 SaleFee 对象全部删除;
         * 2. 再根据一个个订单的 SaleFee 重新录入 db
         * 3. 如果没有 orderId 的跳过并日志记录, 通知
         * 4. 如果 SYSTEM orderId 的,直接录入
         */
        for(String orderId : feeMap.keySet()) {
            List<SaleFee> fees = feeMap.get(orderId);
            if(StringUtils.isBlank(orderId)) {
                //ignore
                Logger.warn("---------------------- Empty OrderId: " + orderId + "::" + fees);
            } else {
                for(SaleFee fee : fees) fee.account = this.account;
                if("SYSTEM".equals(orderId)) {
                    SaleFee.batchSaveWithJDBC(fees);
                } else {
                    SaleFee.deleteStateOneSaleFees(orderId);
                    // 这里不再做重复性检查是因为从 Amazon 回来的 day14 已经确保每一份都分开独立, 不会重复的了
                    try {
                        SaleFee.batchSaveWithJDBC(fees);
                    } catch(Exception e) {
                        Logger.warn("Order [%s] is not exist?", orderId);
                    }
                }
            }
        }
        Logger.info("END parse FeeMap to DB...");
    }

    /**
     * 包含 Account 的 report 文明名
     *
     * @return
     */
    public String fileName() {
        if(StringUtils.isBlank(this.url)) return "";
        String tailPart = StringUtils.splitByWholeSeparatorPreserveAllTokens(this.url, "_GET_V2_SETTLEMENT_REPORT_DATA__")[1];
        return String.format("%s.%s", this.account.prettyName(), StringUtils.split(tailPart, "?")[0]);
    }

    /**
     * 获取 URL; 会根据指定的时间去判断, 与指定时间误差在前后 2 天内的才允许下载, 否则不允许下载, 避免出错
     *
     * @return URL 链接与 "" 空字符串
     */
    public String pickReportUrl(String html) {
        Document doc = Jsoup.parse(html);
        Element table = doc.select("#content-main-entities table:eq(1)").first();
        if(table == null) return "";
        Elements trs = table.select("tr[class!=list-row-white]");
        Element targetTr = trs.get(1);
        String period = StringUtils.split(targetTr.select("td > a").text(), "-")[1].trim();
        Date periodSufix = Dates.transactionDate(this.account.type, period);

        // 超过 2 天不进下一步解析了
        if(Math.abs(this.date.getTime() - periodSufix.getTime()) > TimeUnit.DAYS.toMillis(2))
            return "";

        Elements flatFileBtns = targetTr.select("div > a:contains(Download Flat File)");
        if(flatFileBtns.size() == 0)
            return "";
        for(Element btn : flatFileBtns) {
            if("download flat file".equals(btn.attr("name").toLowerCase())) {
                this.url = btn.attr("href");
                return this.url;
            }
        }
        return "";
    }
}
