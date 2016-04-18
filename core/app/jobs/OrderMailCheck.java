package jobs;

import helper.LogUtils;
import helper.Webs;
import models.Jobex;
import models.market.*;
import notifiers.Mails;
import org.apache.commons.lang.StringUtils;
import org.joda.time.DateTime;
import play.Logger;
import play.jobs.Job;

import java.util.List;

/**
 * 检查所有订单, 确认是否需要发送邮件;
 * // 每隔 20 分钟时检查一次
 * 周期:
 * - 轮询周期: 10mn
 * - Duration: 30mn
 * User: wyattpan
 * Date: 2/28/12
 * Time: 10:02 AM
 * @deprecated
 */
public class OrderMailCheck extends Job {

    @Override
    public void doJob() throws Exception {
        long begin = System.currentTimeMillis();
        if(!Jobex.findByClassName(OrderMailCheck.class.getName()).isExcute()) return;
        DateTime dt = DateTime.parse(DateTime.now().toString("yyyy-MM-dd")); // 仅仅保留 年月日

        Logger.info("Start OrderMailCheck Check REVIEW_MAIL...");
        /**
         * Check 需要发送邀请 Review 的邮件的订单
         */
        // TODO 性能有问题
        List<Orderr> needReview = Orderr
                .find("state=? AND reviewMailed=false AND createDate<=? AND createDate>=? AND market IN (?,?,?) " +
                        "ORDER BY createDate",
                        Orderr.S.SHIPPED,
                        // 只在 46 天前到 12 天前的订单中寻找需要发送 Review 的
                        dt.plusDays(-12).toDate(),
                        dt.plusDays(-46).toDate(),
                        M.AMAZON_DE, M.AMAZON_UK, M.AMAZON_US
                ).fetch(150); // 不能一次性太多了...
        Logger.info(String.format("Load %s Orders From %s To %s.",
                needReview.size(),
                dt.plusDays(-46).toString("yyyy-MM-dd"),
                dt.plusDays(-12).toString("yyyy-MM-dd")));
        /**
         * 1. 加载出来所有需要检查的 all
         * 2. 检查过的邮件 checked 
         * 3. 成功发送了的邮件 send
         * 4. 已经发送了的邮件
         * 5. 检查了但没有发送的
         *  a. not EasyAcc
         *  b. score <= 3
         *  c. not UK
         *  d. noEmail
         */

        int sendUk = 0;
        int sendDe = 0;
        int sendUs = 0;

        int notEasyAcc = 0;
        int below3 = 0;
        int noMarket = 0;
        int noOrderItems = 0;
        int noEmail = 0;
        for(Orderr ord : needReview) {
            // check: 仅仅发送 EasyAcc 开头的标题的产品的邮件.
            boolean ctn = true;
            for(OrderItem oi : ord.items) {
                if(!Listing.isSelfBuildListing(oi.listingName)) ctn = false;
            }
            if(!ctn) {
                Logger.debug(String.format("Skip %s, because of [Not EasyAcc]", ord.orderId));
                notEasyAcc++;
                continue;
            }

            // check: Orderr 的 feedback <= 3 的不发送
            Feedback fbk = Feedback.findById(ord.orderId);
            if(fbk != null && fbk.score <= 3) {
                Logger.debug(String.format("Skip %s, because of [Score below 3]", ord.orderId));
                //TODO 这个数量逐渐增大, 如何处理??
                below3++;
                continue;
            }

            // check: Order 没有 Email
            if(StringUtils.isBlank(ord.email)) {
                Logger.warn("Order[" + ord.orderId + "] do not have Email Address!");
                noEmail++;
                continue;
            }

            if(ord.items.size() <= 0) {
                Logger.warn("Order %s Do not have orderItem yet.", ord.orderId);
                noOrderItems++;
                continue;
            }

            if(ord.market == M.AMAZON_US) {
                // 这里为了将邮件模板区分开, 使用了不同的方法名
                Mails.amazonUS_REVIEW_MAIL(ord);
                sendUs++;
            } else if(ord.market == M.AMAZON_UK) {
                Mails.amazonUK_REVIEW_MAIL(ord);
                sendUk++;
            } else if(ord.market == M.AMAZON_DE) {
                Mails.amazonDE_REVIEW_MAIL(ord);
                sendDe++;
            } else if(ord.market == M.AMAZON_FR) {
                //ignore, 法国市场现在不发送
                ord.reviewMailed = true;
                ord.save();
            } else {
                noMarket++;
                Logger.info("Uncatched Region..." + ord.market);
            }
            //使用 Amazon SES 后可以将这个频率调高点, 每秒 5 封
            Thread.sleep(400);
        }
        String logInfo = String.format("%s From %s:" +
                " Send(%s uk| %s de | %s us)," +
                " [NotEasyAcc(%s), Below3(%s), NoMarket(%s), NoEmail(%s), NoOrderItems(%s)], Total(%s)",
                dt.plusDays(-46).toString("yyyy-MM-dd"), dt.plusDays(-12).toString("yyyy-MM-dd"),
                sendUk, sendDe, sendUs,
                notEasyAcc, below3, noMarket, noEmail, noOrderItems, needReview.size());
        Logger.info(logInfo);

        // 如果没有市场的 review 邮件有 10 封没有发出则需要提示我进行处理
        if(noMarket >= 10) {
            Webs.systemMail("有超过 10 个订单没有市场, 需要处理.", logInfo);
        }

        int totalUnMailed = notEasyAcc + below3 + noMarket + noEmail + noOrderItems;
        // 没有发送邮件的比率超过 3 成进行检查
        if((totalUnMailed / (needReview.size() <= 0 ? 10000 : needReview.size()) >= 0.3) &&
                (sendDe + sendUk + sendUs > 20)/*发送的要大于 20 封, 否则提醒邮件太多.*/) {
            Webs.systemMail("没有发送的邮件数量超过 3 成, 需要进行检查.", logInfo);
        }
        Logger.info("End OrderMailCheck Check REVIEW_MAIL...");
        if(LogUtils.isslow(System.currentTimeMillis() - begin,"OrderMailCheck")) {
            LogUtils.JOBLOG
                    .info(String.format("OrderMailCheck calculate.... [%sms]", System.currentTimeMillis() - begin));
        }
    }
}
