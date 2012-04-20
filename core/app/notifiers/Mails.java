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
        setSubject("Feedback Warnning! Score: %s (Order: %s)", f.score, f.orderId);
        mailBase();
        addRecipient("services@easyacceu.com");
        send(f);
    }


    /**
     * 系统内部使用的, 拥有 <= 3 分的 Review 的警告邮件
     *
     * @param r
     */
    public static void listingReviewWarn(AmazonListingReview r) {
        String title = String.format("ListingReview Warnning! Rating: %s Listing: %s", r.rating, r.listingId);
        setSubject(title);
        mailBase();
        addRecipient("services@easyacceu.com");
        send(r, title);
    }


    // ----------------------------------------------
    private static void mailBase() {
        if(Play.mode.isProd()) {
            setFrom("EasyAcc <support@easyacceu.com>");
        } else {
            setFrom("EasyAcc <1733913823@qq.com>"); // 因为在国内 Gmail 老是被墙, 坑爹!! 所以非 产品环境 使用 QQ 邮箱测试.
        }
    }

}
