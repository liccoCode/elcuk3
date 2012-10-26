package jobs;

import models.Jobex;
import models.market.Feedback;
import models.market.Listing;
import models.market.OrderItem;
import models.market.Orderr;
import notifiers.Mails;
import org.apache.commons.lang.StringUtils;
import org.joda.time.DateTime;
import play.Logger;
import play.jobs.Every;
import play.jobs.Job;

import java.util.List;

/**
 * 检查所有订单, 确认是否需要发送邮件;
 * // 每隔 20 分钟时检查一次
 * 周期:
 * - 轮询周期: 15mn
 * - Duration: 4h
 * User: wyattpan
 * Date: 2/28/12
 * Time: 10:02 AM
 */
@Every("15mn")
public class OrderMailCheck extends Job {

    @Override
    public void doJob() throws Exception {
        if(!Jobex.findByClassName(OrderMailCheck.class.getName()).isExcute()) return;
        Logger.debug("OrderMailCheck Check SHIPPED_MAIL...");
        DateTime dt = DateTime.parse(DateTime.now().toString("yyyy-MM-dd")); // 仅仅保留 年月日
        /**
         * 1. Check 将需要发送 "货物已经发送了的邮件加载出来进行发送"
         *
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
         */


        Logger.debug("OrderMailCheck Check REVIEW_MAIL...");
        /**
         * 2. Check 需要发送邀请 Review 的邮件的订单
         */
        List<Orderr> needReview = Orderr.find("state=? AND createDate<=? AND createDate>=? ORDER BY createDate",
                Orderr.S.SHIPPED,
                // 只在 46 天前到 12 天前的订单中寻找需要发送 Review 的
                DateTime.parse(dt.plusDays(-12).toString("yyyy-MM-dd")).toDate(),
                DateTime.parse(dt.plusDays(-46).toString("yyyy-MM-dd")).toDate()
        ).fetch();
        Logger.info(String.format("Load %s Orders From %s To %s.",
                needReview.size(),
                dt.plusDays(-46).toString("yyyy-MM-dd"),
                dt.plusDays(-12).toString("yyyy-MM-dd")));
        /**
         * 1. 加载出来所有需要检查的 all
         * 2. 检查过的邮件 checked 
         * 3. 成功发送了的邮件 send
         * 4. 已经发送了的邮件 mailed
         * 5. 检查了但没有发送的
         *  a. not EasyAcc
         *  b. score <= 3
         *  c. not UK
         *  d. noEmail
         */

        int mailed = 0;
        int checked = 0;

        int sendUk = 0;
        int sendDe = 0;

        int notEasyAcc = 0;
        int below3 = 0;
        int noMarket = 0;
        int noEmail = 0;
        for(Orderr ord : needReview) {
            checked++;
            // check: 仅仅发送 EasyAcc 开头的标题的产品的邮件.
            boolean ctn = true;
            for(OrderItem oi : ord.items) if(!Listing.isSelfBuildListing(oi.listingName)) ctn = false;
            if(!ctn) {
                Logger.debug(String.format("Skip %s, because of [Not EasyAcc]", ord.orderId));
                notEasyAcc++;
                continue;
            }

            // check: Orderr 的 feedback <= 3 的不发送
            Feedback fbk = Feedback.findById(ord.orderId);
            if(fbk != null && fbk.score <= 3) {
                Logger.debug(String.format("Skip %s, because of [Score below 3]", ord.orderId));
                below3++;
                continue;
            }

            // check: Order 没有 Email
            if(StringUtils.isBlank(ord.email)) {
                Logger.warn("Order[" + ord.orderId + "] do not have Email Address!");
                noEmail++;
                continue;
            }

            char e = ord.emailed(2);
            if(e == 'f' || e == 'F') {
                mailed++;
                Logger.debug("Order[" + ord.orderId + "] has mailed [REVIEW_MAIL]");
            } else {
                if((sendUk + sendDe) >= 100) {
                    Logger.info("UK + DE send 100 mails, skip this one.");
                    break; // 暂时每一次只发送 100 封, 因为量不大
                }
                switch(ord.market) {
                    case AMAZON_UK:
                        sendUk++;
                        Mails.amazonUK_REVIEW_MAIL(ord);
                        break;
                    case AMAZON_DE:
                        sendDe++;
                        Mails.amazonDE_REVIEW_MAIL(ord);
                        break;
                    //TODO 增加对 US 的邀请 Review 邮件
                    default:
                        noMarket++;
                        Logger.info("Uncatched Region..." + ord.market);
                }
                Thread.sleep(500);//每封邮件不能发送那么快
            }
        }
        Logger.info(String.format("%s From %s: Send(%s uk| %s de), [NotEasyAcc(%s), Below3(%s), NoMarket(%s), NoEmail(%s)], Mailed(%s), Checked(%s), Total(%s)",
                dt.plusDays(-46).toString("yyyy-MM-dd"), dt.plusDays(-12).toString("yyyy-MM-dd"), sendUk, sendDe, notEasyAcc, below3, noMarket, noEmail, mailed, checked, needReview.size()));
    }
}
