package jobs;

import models.market.Orderr;
import notifiers.Mails;
import org.joda.time.DateTime;
import play.Logger;
import play.jobs.Job;

import java.util.List;

/**
 * 检查所有订单, 确认是否需要发送邮件;
 * // 每隔 20 分钟时检查一次
 * User: wyattpan
 * Date: 2/28/12
 * Time: 10:02 AM
 */
public class OrderMailCheck extends Job {

    @Override
    public void doJob() throws Exception {
        Logger.info("OrderMailCheck Check SHIPPED_MAIL...");
        /**
         * 1. Check 将需要发送 "货物已经发送了的邮件加载出来进行发送"
         */
        DateTime dt = DateTime.now();
        List<Orderr> orders = Orderr.find("state=? AND createDate>=? AND createDate<=?",
                Orderr.S.SHIPPED,
                // 只在 20 天内的订单中寻找没有发送 SHIPPED 邮件的
                DateTime.parse(dt.plusDays(-20).toString("yyyy-MM-dd")).toDate(),
                dt.toDate()
        ).fetch();
        for(Orderr ord : orders) {
            char e = ord.emailed(1);
            if(e == 'f' || e == 'F') {
                Logger.debug("Order[" + ord.orderId + "] has mailed [SHIPPED_MAIL]");
            } else {
                //Mails.amazonUK_SHIPPED_MAIL(ord);
            }
        }


        Logger.info("OrderMailCheck Check REVIEW_MAIL...");
        /**
         * 2. Check 需要发送邀请 Review 的邮件的订单
         */
        List<Orderr> needReview = Orderr.find("state=? AND createDate<=? AND createDate>=?",
                Orderr.S.SHIPPED,
                // 只在 46 天前到 5 天前的订单中寻找需要发送 Review 的
                DateTime.parse(dt.plusDays(-7).toString("yyyy-MM-dd")).toDate(),
                DateTime.parse(dt.plusDays(-46).toString("yyyy-MM-dd")).toDate()
        ).fetch();
        Logger.info(String.format("Load %s Orders From %s To %s.",
                needReview.size(),
                dt.plusDays(-46).toString("yyyy-MM-dd"),
                dt.plusDays(-26).toString("yyyy-MM-dd")));
        int i = 0;
        int mailed = 0;
        for(Orderr ord : needReview) {
            if(i % 20 == 0) {//每发送了 20 封邮件则等待 5s 后再发送.
                Thread.sleep(5000);
            }
            char e = ord.emailed(2);
            if(e == 'f' || e == 'F') Logger.debug("Order[" + ord.orderId + "] has mailed [REVIEW_MAIL]");
            else {
                if(i >= 160) break; // 暂时每一次只发送 160 封, 因为量不大
                Mails.amazonUK_REVIEW_MAIL(ord);
                mailed++;
                Thread.sleep(350);//每封邮件不能发送那么快
            }
            i++;
        }
        Logger.info(String.format("Mailed %s Orders", mailed));

    }
}
