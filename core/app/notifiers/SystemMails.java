package notifiers;

import helper.Dates;
import helper.Webs;
import models.market.AmazonListingReview;
import models.market.Feedback;
import models.market.Selling;
import models.procure.FBAShipment;
import models.product.Product;
import models.view.dto.AnalyzeDTO;
import org.joda.time.DateTime;
import play.Logger;
import play.Play;
import play.libs.F;
import play.mvc.Mailer;

import java.util.Date;
import java.util.List;

/**
 * 系统内使用的邮件发送
 * User: wyattpan
 * Date: 8/23/12
 * Time: 3:10 PM
 */
public class SystemMails extends Mailer {

    /**
     * 每天发送的 Review 提醒邮件
     *
     * @return
     */
    public static boolean dailyReviewMail(List<AmazonListingReview> reviews) {
        setSubject(String.format("{INFO} %s Reviews Overview.", Dates.date2Date(new DateTime().minusDays(1).toDate())));
        mailBase();
        addRecipient("alerts@easyacceu.com", "m@easyacceu.com");
        try {
            send(reviews);
        } catch(Exception e) {
            Logger.warn(Webs.E(e));
            return false;
        }
        return true;
    }

    private static void mailBase() {
        SystemMails.setCharset("UTF-8");
        if(Play.mode.isProd()) {
            SystemMails.setFrom("EasyAcc <support@easyacceu.com>");
        } else {
            SystemMails.setFrom("EasyAcc <1733913823@qq.com>"); // 因为在国内 Gmail 老是被墙, 坑爹!! 所以非 产品环境 使用 QQ 邮箱测试.
        }
    }

    public static boolean dailyFeedbackMail(List<Feedback> feedbacks) {
        setSubject(String.format("{INFO} %s Feedback Overview.", Dates.date2Date(new DateTime().minusDays(1).toDate())));
        mailBase();
        addRecipient("alerts@easyacceu.com", "m@easyacceu.com");
        try {
            send(feedbacks);
        } catch(Exception e) {
            Logger.warn(Webs.E(e));
            return false;
        }
        return true;
    }

    public static boolean productPicCheckermail(List<F.T2<Product, AnalyzeDTO>> productAndSellT2s) {
        setSubject(String.format("{CHECK} %s Product Picture Information Check", Dates.date2Date()));
        mailBase();
        addRecipient("alerts@easyacceu.com");
        try {
            send(productAndSellT2s);
        } catch(Exception e) {
            Logger.warn(Webs.E(e));
            return false;
        }
        return true;
    }

    /**
     * FBA 的状态改变的时候发送邮件
     * @param fba
     * @param oldState FBA 的原始状态
     * @param newState FBA 改变的新状态
     * @return
     */
    public static /*Mailer 的返回值必须为基本类型*/boolean fbaShipmentStateChange(FBAShipment fba, FBAShipment.S oldState, FBAShipment.S newState) {
        setSubject(String.format("{INFO} FBA %s state FROM %s To %s", fba.shipmentId, oldState, newState));
        mailBase();
        addRecipient("alerts@easyacceu.com", "p@easyacceu.com");
        try {
            send(fba, oldState, newState);
        } catch(Exception e) {
            Logger.warn(Webs.E(e));
            return false;
        }
        return true;
    }
}
