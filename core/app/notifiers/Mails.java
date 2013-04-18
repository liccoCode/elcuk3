package notifiers;

import helper.Webs;
import jobs.promise.ReviewMailCheckPromise;
import models.MailsRecord;
import models.market.*;
import models.procure.Shipment;
import org.apache.commons.lang.StringUtils;
import org.joda.time.DateTime;
import play.Logger;
import play.Play;
import play.exceptions.MailException;
import play.libs.F;
import play.mvc.Mailer;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Future;

/**
 * Created by IntelliJ IDEA.
 * User: wyattpan
 * Date: 2/20/12
 * Time: 11:24 AM
 */
public class Mails extends Mailer {
    // 如果有新增加邮件, 需要向 ElcukRecord.emailOverView 注册
    // Mails 中的不同的邮件类型
    public static final String CLEARANCE = "shipment_clearance";
    public static final String IS_DONE = "shipment_isdone";
    public static final String MORE_OFFERS = "more_offers";
    public static final String REVIEW_UK = "amazon_review_uk";
    public static final String REVIEW_DE = "amazon_review_de";
    public static final String REVIEW_US = "amazon_review_us";
    public static final String FEEDBACK_WARN = "feedback_warnning";
    public static final String REVIEW_WARN = "review_warnning";
    public static final String FNSKU_CHECK = "fnsku_check_warn";

    // ------------------------------ Shipment 邮件  -----------------------

    public static void shipment_clearance(Shipment shipment) {
        String title = String.format("{CLEARANCE}[SHIPMENT] 运输单 [%s] 已经开始清关.", shipment.id);
        MailsRecord mr = null;
        try {
            setSubject(title);
            mailBase();
            addRecipient("p@easyacceu.com");
            mr = new MailsRecord(infos.get(), MailsRecord.T.NORMAL, CLEARANCE);
            send(shipment);
            mr.success = true;
        } catch(Exception e) {
            Logger.warn(title + ":" + Webs.E(e));
        } finally {
            if(mr != null)
                mr.save();
        }
    }

    public static void shipment_isdone(Shipment shipment) {
        String title = String.format("{ARRIVED}[SHIPMENT] 运输单 [%s] 已经抵达,并且签收,需确认.", shipment.id);
        MailsRecord mr = null;
        try {
            setSubject(title);
            mailBase();
            addRecipient("p@easyacceu.com");
            mr = new MailsRecord(infos.get(), MailsRecord.T.NORMAL, IS_DONE);
            send(shipment);
            mr.success = true;
        } catch(Exception e) {
            Logger.warn(title + ":" + Webs.E(e));
        } finally {
            if(mr != null)
                mr.save();
        }
    }


    /**
     * Listing 被人上架了的警告邮件
     */
    public static void moreOfferOneListing(List<ListingOffer> offers, Listing lst) {
        String title = String
                .format("{WARN}[Offer] %s More than one offer in one Listing.", lst.listingId);
        MailsRecord mr = null;
        try {
            setSubject(title);
            mailBase();
            addRecipient("alerts@easyacceu.com");
            mr = new MailsRecord(infos.get(), MailsRecord.T.NORMAL, MORE_OFFERS);
            send(offers, lst);
            mr.success = true;
        } catch(Exception e) {
            Logger.warn(title + ":" + Webs.E(e));
        } finally {
            if(mr != null)
                mr.save();
        }
    }

    // ------------------- Review 邮件 --------------------------------

    /**
     * 给 Amazon UK 的卖家发送邀请留 Review 的邮件;
     *
     * @param order
     */
    public static void amazonUK_REVIEW_MAIL(Orderr order) {
        reviewMailBase(order, REVIEW_UK);
    }

    public static void amazonDE_REVIEW_MAIL(Orderr order) {
        reviewMailBase(order, REVIEW_DE);
    }

    public static void amazonUS_REVIEW_MAIL(Orderr order) {
        reviewMailBase(order, REVIEW_US);
    }

    /**
     * 发送 Review 邮件的基本方法, 其他的仅仅是套用一个方法对应一个模板
     *
     * @param order
     */
<<<<<<< HEAD
    private static void reviewMailBase(Orderr order, String tmp) {
=======
    private static void reviewMailBase(Orderr order, String template) {
>>>>>>> hotfix/#213
        if(StringUtils.isBlank(order.email)) {
            Logger.warn("Order[" + order.orderId + "] do not have Email Address!");
            return;
        }
        // 避免系统内删除而 Amazon 还存在的 Selling 出现问题.
        for(OrderItem oi : order.items) {
            if(oi.selling == null) {
                return;
            }
        }

        String title = order.reviewMailTitle();
        if(StringUtils.isBlank(title)) {
            Logger.error("!!!! Order[" + order.orderId + "] Mail can not be send !!!!");
            return;
        }
        setSubject(title);
        mailBase();
        if(Play.mode.isProd()) addRecipient(order.email);
        else addRecipient("wppurking@gmail.com");
        MailsRecord mr = null;
        try {
<<<<<<< HEAD
            mr = new MailsRecord(infos.get(), MailsRecord.T.NORMAL, tmp);
=======
            mr = new MailsRecord(infos.get(), MailsRecord.T.NORMAL, template);
>>>>>>> hotfix/#213
            final Future<Boolean> future = send(order, title);
            new ReviewMailCheckPromise(order.orderId, future, mr).now();
        } catch(MailException e) {
            Logger.warn("Order[" + order.orderId + "] Send Error! " + e.getMessage());
        }
    }

    // -----------------------------------------------------------------

    /**
     * 系统内部使用, 当抓取到的 Feedback 为
     *
     * @param f
     */
    public static void feedbackWarnning(Feedback f) {
        if(f.mailedTimes != null && f.mailedTimes >= 2) return;

        setSubject("{WARN}[Feedback] S:%s (Order: %s)", f.score, f.orderId);
        mailBase();
        addRecipient("services@easyacceu.com");
        MailsRecord mr = null;
        try {
            mr = new MailsRecord(infos.get(), MailsRecord.T.NORMAL, FEEDBACK_WARN);
            send(f);
            f.mailedTimes = (f.mailedTimes == null ? 1 : f.mailedTimes + 1);
            mr.success = true;
        } catch(Exception e) {
<<<<<<< HEAD

=======
            Logger.warn("Feedback[" + f.feedback + "] Send Error! " + e.getMessage());
>>>>>>> hotfix/#213
        } finally {
            if(mr != null)
                mr.save();
        }


    }


    /**
     * 系统内部使用的, 拥有 <= 4 分的 Review 的警告邮件;
     * 如果这个 AmazonListingReview 发送邮件的次数大于 1 次, 则不再进行发送.
     *
     * @param r
     */
    public static void listingReviewWarn(AmazonListingReview r) {
        //{WARN}[Review] R: 3.0 A: B004KHXU5Q M:amazon.co.uk
        if(r.mailedTimes != null && r.mailedTimes >= 1) return;
        List<Orderr> ords = r.relateOrder();
        StringBuilder sbr = new StringBuilder();
        for(Orderr ord : ords) sbr.append(ord.orderId).append(",");
        String[] args = StringUtils.split(r.listingId, "_");
        String title = String.format("{WARN}[Review] R:%s A:%s M:%s", r.rating, args[0], args[1]);
        setSubject(title);
        mailBase();
        addRecipient("services@easyacceu.com");
        MailsRecord mr = null;
        try {
            mr = new MailsRecord(infos.get(), MailsRecord.T.NORMAL, REVIEW_WARN);
            send(r, title, sbr);
            r.mailedTimes = (r.mailedTimes == null ? 1 : r.mailedTimes + 1);
            mr.success = true;
        } catch(Exception e) {
<<<<<<< HEAD

=======
            Logger.warn("AmazonListingReview[" + r.alrId + "] Send Error! " + e.getMessage());
>>>>>>> hotfix/#213
        } finally {
            if(mr != null)
                mr.save();
        }
        // send 方法没有抛出异常则表示邮件发送成功

    }

    public static void fnSkuCheckWarn(List<F.T4<String, String, String, String>> unfindSelling) {
        setSubject("{WARN}[FBA] 如下 Selling 在更新 Selling.fnSku 时无法在系统中找到.");
        mailBase();
        addRecipient("alerts@easyacceu.com");
        MailsRecord mr = null;
        try {
            mr = new MailsRecord(infos.get(), MailsRecord.T.NORMAL, FNSKU_CHECK);
            send(unfindSelling);
            mr.success = true;
        } catch(Exception e) {
<<<<<<< HEAD

=======
            Logger.warn("unfindSelling WARN Send Error! " + e.getMessage());
>>>>>>> hotfix/#213
        } finally {
            if(mr != null)
                mr.save();
        }

    }


    // ----------------------------------------------
    private static void mailBase() {
        setCharset("UTF-8");
        if(Play.mode.isProd()) {
            setFrom("EasyAcc <support@easyacceu.com>");
        } else {
            setFrom("EasyAcc <support@easyacceu.com>"); // 因为在国内 Gmail 老是被墙, 坑爹!! 所以非 产品环境 使用 QQ 邮箱测试.
        }
    }

}
