package notifiers;

import helper.Webs;
import models.market.*;
import models.procure.Shipment;
import org.apache.commons.lang.StringUtils;
import play.Logger;
import play.Play;
import play.exceptions.MailException;
import play.libs.F;
import play.mvc.Mailer;

import java.util.List;
import java.util.concurrent.Future;

/**
 * Created by IntelliJ IDEA.
 * User: wyattpan
 * Date: 2/20/12
 * Time: 11:24 AM
 */
public class Mails extends Mailer {
    // ------------------------------ Shipment 邮件  -----------------------

    public static void shipment_clearance(Shipment shipment) {
        String title = String.format("{CLEARANCE}[SHIPMENT] 运输单 [%s] 已经开始清关.", shipment.id);
        try {
            setSubject(title);
            mailBase();
            addRecipient("p@easyacceu.com");
            send(shipment);
        } catch(Exception e) {
            Logger.warn(title + ":" + Webs.E(e));
        }
    }

    public static void shipment_isdone(Shipment shipment) {
        String title = String.format("{ARRIVED}[SHIPMENT] 运输单 [%s] 已经抵达,并且签收,需确认.", shipment.id);
        try {
            setSubject(title);
            mailBase();
            addRecipient("p@easyacceu.com");
            send(shipment);
        } catch(Exception e) {
            Logger.warn(title + ":" + Webs.E(e));
        }
    }


    /**
     * Listing 被人上架了的警告邮件
     */
    public static void moreOfferOneListing(List<ListingOffer> offers, Listing lst) {
        String title = String.format("{WARN}[Offer] %s More than one offer in one Listing.", lst.listingId);
        try {
            setSubject(title);
            mailBase();
            addRecipient("alerts@easyacceu.com");
            send(offers, lst);
        } catch(Exception e) {
            Logger.warn(title + ":" + Webs.E(e));
        }
    }

    /**
     * 给 Amazon UK 的卖家发送货物已经发送的邮件;
     * //TODO 暂时还没有适合的内容给我.
     *
     * @param order
     */
    public static void amazonUK_SHIPPED_MAIL(final Orderr order) {
        if(StringUtils.isBlank(order.email)) {
            Logger.warn("Order[" + order.orderId + "] do not have Email Address!");
            return;
        }
        setSubject("Thanks for purchasing from Easyacc on Amazon");
        mailBase();
        if(Play.mode.isProd()) {
            addRecipient(order.email);
        } else {
            addRecipient("wppurking@gmail.com");
        }
        final Future<Boolean> future = send(order);

        new Thread(new MailsHelper.MAIL_CALLBACK_1(future, order, 1, 'f')).start();
    }


    // ------------------- Review 邮件 --------------------------------

    /**
     * 给 Amazon UK 的卖家发送邀请留 Review 的邮件;
     *
     * @param order
     */
    public static void amazonUK_REVIEW_MAIL(Orderr order) {
        reviewMailBase(order);
    }

    public static void amazonDE_REVIEW_MAIL(Orderr order) {
        reviewMailBase(order);
    }

    /**
     * 发送 Review 邮件的基本方法, 其他的仅仅是套用一个方法对应一个模板
     *
     * @param order
     */
    private static void reviewMailBase(Orderr order) {
        if(!MailsHelper.check(order)) return;

        String title = order.reviewMailTitle();
        if(StringUtils.isBlank(title)) {
            Logger.error("!!!! Order[" + order.orderId + "] Mail can not be send !!!!");
            return;
        }
        setSubject(title);
        mailBase();
        if(Play.mode.isProd()) addRecipient(order.email);
        else addRecipient("wppurking@gmail.com");

        try {
            final Future<Boolean> future = send(order, title);

            new Thread(new MailsHelper.MAIL_CALLBACK_1(future, order, 2, 'f')).start();
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
        send(f);
        // send 方法没有抛出异常则表示邮件发送成功
        f.mailedTimes = (f.mailedTimes == null ? 1 : f.mailedTimes + 1);
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
        send(r, title, sbr);
        // send 方法没有抛出异常则表示邮件发送成功
        r.mailedTimes = (r.mailedTimes == null ? 1 : r.mailedTimes + 1);
    }

    public static void fnSkuCheckWarn(List<F.T4<String, String, String, String>> unfindSelling) {
        setSubject("{WARN}[FBA] 如下 Selling 在更新 Selling.fnSku 时无法在系统中找到.");
        mailBase();
        addRecipient("alerts@easyacceu.com");
        send(unfindSelling);
    }


    // ----------------------------------------------
    private static void mailBase() {
        setCharset("UTF-8");
        if(Play.mode.isProd()) {
            setFrom("EasyAcc <support@easyacceu.com>");
        } else {
            setFrom("EasyAcc <1733913823@qq.com>"); // 因为在国内 Gmail 老是被墙, 坑爹!! 所以非 产品环境 使用 QQ 邮箱测试.
        }
    }

}
