package notifiers;

import helper.Webs;
import models.MailsRecord;
import models.market.*;
import org.apache.commons.lang.StringUtils;
import play.Logger;
import play.Play;
import play.libs.F;
import play.mvc.Mailer;

import java.util.List;

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

    /**
     * Listing 被人上架了的警告邮件
     */
    public static void moreOfferOneListing(List<ListingOffer> offers, Listing lst) {
        String title = String.format("{WARN}[Offer] %s More than one offer in one Listing.", lst.listingId);
        MailsRecord mr = null;
        try {
            setSubject(title);
            mailBase();
            addRecipient("alerts@easya.cc");
            mr = new MailsRecord(infos.get(), MailsRecord.T.NORMAL, MORE_OFFERS);
            send(offers, lst);
            mr.success = true;
        } catch(Exception e) {
            Logger.warn(title + ":" + Webs.e(e));
        } finally {
            if(mr != null)
                mr.save();
        }
    }

    /**
     * 系统内部使用, 当抓取到的 Feedback 为
     *
     * @param f
     */
    public static void feedbackWarnning(Feedback f) {
        if(f.mailedTimes != null && f.mailedTimes >= 2) return;
        setSubject("{WARN}[Feedback] S:%s (Order: %s)", f.score, f.orderId);
        mailBase();
        addRecipient("service@easya.cc");
        MailsRecord mr = null;
        try {
            mr = new MailsRecord(infos.get(), MailsRecord.T.NORMAL, FEEDBACK_WARN);
            send(f);
            f.mailedTimes = (f.mailedTimes == null ? 1 : f.mailedTimes + 1);
            mr.success = true;
        } catch(Exception e) {
            Logger.warn("Feedback[" + f.feedback + "] Send Error! " + e.getMessage());
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
        addRecipient("service@easya.cc");
        MailsRecord mr = null;
        try {
            mr = new MailsRecord(infos.get(), MailsRecord.T.NORMAL, REVIEW_WARN);
            send(r, title, sbr);
            r.mailedTimes = (r.mailedTimes == null ? 1 : r.mailedTimes + 1);
            mr.success = true;
        } catch(Exception e) {
            Logger.warn("AmazonListingReview[" + r.alrId + "] Send Error! " + e.getMessage());
        } finally {
            if(mr != null)
                mr.save();
        }
        // send 方法没有抛出异常则表示邮件发送成功

    }

    public static void fnSkuCheckWarn(List<F.T4<String, String, String, String>> unfindSelling) {
        setSubject("{WARN}[FBA] 如下 Selling 在更新 Selling.fnSku 时无法在系统中找到.");
        mailBase();
        addRecipient("alerts@easya.cc");
        MailsRecord mr = null;
        try {
            mr = new MailsRecord(infos.get(), MailsRecord.T.NORMAL, FNSKU_CHECK);
            send(unfindSelling);
            mr.success = true;
        } catch(Exception e) {
            Logger.warn("unfindSelling WARN Send Error! " + e.getMessage());
        } finally {
            if(mr != null)
                mr.save();
        }
    }

    private static void mailBase() {
        setCharset("UTF-8");
        if(Play.mode.isProd()) {
            setFrom(models.OperatorConfig.getVal("addressname") + " " + models.OperatorConfig.getVal("supportemail"));
        } else {
            setFrom("EasyAcc <support@easyacceu.com>"); // 因为在国内 Gmail 老是被墙, 坑爹!! 所以非 产品环境 使用 QQ 邮箱测试.
        }
    }

}
