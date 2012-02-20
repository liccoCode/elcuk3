package notifiers;

import models.market.Listing;
import models.market.ListingOffer;
import play.Play;
import play.mvc.Mailer;

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
        if(Play.mode.isProd()) {
            setFrom("EasyAcc <support@easyacceu.com>");
        } else {
            setFrom("EasyAcc <1733913823@qq.com>"); // 因为在国内 Gmail 老是被墙, 坑爹!! 所以非 产品环境 使用 QQ 邮箱测试.
        }
        addRecipient("all@easyacceu.com");
        send(lis, offer);
    }
}
