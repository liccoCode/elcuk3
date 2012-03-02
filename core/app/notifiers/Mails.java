package notifiers;

import models.market.Listing;
import models.market.ListingOffer;
import models.market.Orderr;
import org.apache.commons.lang.StringUtils;
import play.Logger;
import play.Play;
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

    /**
     * 给 Amazon UK 的卖家发送邀请留 Review 的邮件;
     *
     * @param order
     */
    public static void amazonUK_REVIEW_MAIL(final Orderr order) {
        if(StringUtils.isBlank(order.email)) {
            Logger.warn("Order[" + order.orderId + "] do not have Email Address!");
            return;
        }
        setSubject("Thanks for purchasing from Easyacc On Amazon");
        mailBase();
        if(Play.mode.isProd()) {
            addRecipient(order.email);
        } else {
            addRecipient("wppurking@gmail.com");
        }

        final Future<Boolean> future = send(order);

        new Thread(new MailsHelper.MAIL_CALLBACK_1(future, order, 2, 'f')).start();
    }

    private static void mailBase() {
        if(Play.mode.isProd()) {
            setFrom("EasyAcc <support@easyacceu.com>");
        } else {
            setFrom("EasyAcc <1733913823@qq.com>"); // 因为在国内 Gmail 老是被墙, 坑爹!! 所以非 产品环境 使用 QQ 邮箱测试.
        }
    }

}
