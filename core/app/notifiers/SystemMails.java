package notifiers;

import helper.Dates;
import helper.Webs;
import models.MailsRecord;
import models.market.AmazonListingReview;
import models.market.Feedback;
import models.product.Product;
import models.view.dto.AnalyzeDTO;
import org.apache.commons.mail.EmailAttachment;
import org.joda.time.DateTime;
import play.Logger;
import play.Play;
import play.libs.F;
import play.mvc.Mailer;

import java.util.List;

/**
 * 系统内使用的邮件发送
 * User: wyattpan
 * Date: 8/23/12
 * Time: 3:10 PM
 */
public class SystemMails extends Mailer {
    // 如果有新增加邮件, 需要向 ElcukRecord.emailOverView 注册
    public static final String DAILY_REVIEW = "daily_review";
    public static final String DAILY_FEEDBACK = "daily_feedback";
    public static final String SKU_PIC_CHECK = "product_picture_check";

    /**
     * 每天发送的 Review 提醒邮件
     *
     * @return
     */
    public static boolean dailyReviewMail(List<AmazonListingReview> reviews) {
        String title = String
                .format("{INFO} %s Reviews Overview.", Dates.date2Date(new DateTime().minusDays(1).toDate()));
        setSubject(title);
        mailBase();
        addRecipient("alerts@easya.cc", "m@easya.cc");
        MailsRecord mr = null;
        try {
            mr = new MailsRecord(infos.get(), MailsRecord.T.SYSTEM, DAILY_REVIEW);
            send(reviews);
            mr.success = true;
        } catch(Exception e) {
            Logger.warn(Webs.e(e));
            return false;
        } finally {
            if(mr != null)
                mr.save();
        }
        return true;
    }

    private static void mailBase() {
        SystemMails.setCharset("UTF-8");
        if(!Play.mode.isProd()) {
            SystemMails.setFrom(
                    models.OperatorConfig.getVal("addressname") + " " + models.OperatorConfig.getVal("supportemail"));
        } else {
            // 因为在国内 Gmail 老是被墙, 坑爹!! 所以非 产品环境 使用 QQ 邮箱测试.
            SystemMails.setFrom("EasyAcc <1733913823@qq.com>");
        }
    }

    public static boolean dailyFeedbackMail(List<Feedback> feedbacks) {
        String title = String.format("{INFO} %s Feedback Overview.",
                Dates.date2Date(new DateTime().minusDays(1).toDate()));
        setSubject(title);
        mailBase();
        addRecipient("alerts@easya.cc", "m@easya.cc");
        MailsRecord mr = null;
        try {
            mr = new MailsRecord(infos.get(), MailsRecord.T.SYSTEM, DAILY_FEEDBACK);
            send(feedbacks);
            mr.success = true;
        } catch(Exception e) {
            Logger.warn(Webs.e(e));
            return false;
        } finally {
            if(mr != null)
                mr.save();
        }
        return true;
    }

    public static boolean productPicCheckermail(List<F.T2<Product, AnalyzeDTO>> productAndSellT2s) {
        String title = String.format("{CHECK} %s Product Picture Information Check",
                Dates.date2Date());
        setSubject(title);
        mailBase();
        addRecipient("alerts@easya.cc");
        MailsRecord mr = null;
        try {
            mr = new MailsRecord(infos.get(), MailsRecord.T.SYSTEM, SKU_PIC_CHECK);
            send(productAndSellT2s);
            mr.success = true;
        } catch(Exception e) {
            Logger.warn(Webs.e(e));
            return false;
        } finally {
            if(mr != null)
                mr.save();
        }
        return true;
    }

    public static void sendEmail(String title, EmailAttachment attachment) {
        setSubject(title);
        mailBase();
        addAttachment(attachment);
        addRecipient("licco@easya.cc");
        send("hello");
    }
}
