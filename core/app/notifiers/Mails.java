package notifiers;

import models.market.*;
import org.apache.commons.lang.StringUtils;
import play.Logger;
import play.Play;
import play.exceptions.MailException;
import play.mvc.Mailer;

import java.util.concurrent.Future;

/**
 * Created by IntelliJ IDEA.
 * User: wyattpan
 * Date: 2/20/12
 * Time: 11:24 AM
 */
public class Mails extends Mailer {
    /**
     * Listing 被人上架了的警告邮件
     *
     * @param offer 上了我们货架的那个卖家!
     */
    public static void listingOffersWarning(ListingOffer offer) {
        Listing lis = offer.listing;
        setSubject("{WARNNING!}Listing %s find another seller! [%s|%s]", lis.asin, offer.name, offer.offerId);
        mailBase();
        addRecipient("all@easyacceu.com");
        send(lis, offer);
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
        if(f.mailedTimes != null && f.mailedTimes > 3) return;
        setSubject("{WARN}[Feedback] S:%s (Order: %s)", f.score, f.orderId);
        mailBase();
        addRecipient("services@easyacceu.com");
        send(f);
        // send 方法没有抛出异常则表示邮件发送成功
        f.mailedTimes = (f.mailedTimes == null ? 1 : f.mailedTimes + 1);
    }


    /**
     * 系统内部使用的, 拥有 <= 3 分的 Review 的警告邮件;
     * 如果这个 AmazonListingReview 发送邮件的次数大于 3 次, 则不再进行发送.
     *
     * @param r
     */
    public static void listingReviewWarn(AmazonListingReview r) {
        //{WARN}[Review] R: 3.0 A: B004KHXU5Q M:amazon.co.uk
        if(r.mailedTimes != null && r.mailedTimes > 3) return;
        String[] args = StringUtils.split(r.listingId, "_");
        String title = String.format("{WARN}[Review] R:%s A:%s M:%s", r.rating, args[0], args[1]);
        setSubject(title);
        mailBase();
        addRecipient("services@easyacceu.com");
        send(r, title);
        // send 方法没有抛出异常则表示邮件发送成功
        r.mailedTimes = (r.mailedTimes == null ? 1 : r.mailedTimes + 1);
        r.save();
    }


    // ----------------------------------------------
    private static void mailBase() {
        if(Play.mode.isProd()) {
            setFrom("EasyAcc <wyatt@easyacceu.com>");
        } else {
            setFrom("EasyAcc <1733913823@qq.com>"); // 因为在国内 Gmail 老是被墙, 坑爹!! 所以非 产品环境 使用 QQ 邮箱测试.
        }
    }

}
