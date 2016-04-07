package jobs.promise;

import helper.Webs;
import models.MailsRecord;
import models.embedded.ERecordBuilder;
import models.market.M;
import models.market.Orderr;
import notifiers.Mails;
import play.Logger;
import play.Play;
import play.jobs.Job;

import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

/**
 * 邮件后台发送成功后需要将信息同步到数据库
 * User: wyattpan
 * Date: 10/29/12
 * Time: 9:55 AM
 * @deprecated
 */
public class ReviewMailCheckPromise extends Job {
    private String orderId;
    private Future<Boolean> sendFlag;
    private MailsRecord mailsRecord;

    public ReviewMailCheckPromise(String orderId, Future<Boolean> sendFlag, MailsRecord mailsRecord) {
        this.orderId = orderId;
        this.sendFlag = sendFlag;
        this.mailsRecord = mailsRecord;
    }

    @Override
    public void doJob() {
        try {
            boolean mailSuccess = sendFlag.get(30, TimeUnit.SECONDS);
            if(!mailSuccess) {
                Logger.warn("Order %s review email failed.", this.orderId);
            } else {
                Orderr ord = Orderr.findById(this.orderId);
                ord.reviewMailed = true;
                if(Play.mode.isProd()) {
                    ord.save();
                    mailsRecord.success = true;
                }
                Logger.info("Order[%s](%s) email send success!", this.orderId, ord.market);
                this.orderRecordMail(true);
            }
        } catch(TimeoutException e) {
            this.orderRecordMail(false);
            Logger.warn("Order %s review email maybe send success. [%s]", this.orderId, Webs.E(e));
        } catch(Exception e) {
            Logger.warn("Order %s review email failed. [%s]", this.orderId, Webs.E(e));
        } finally {
            if(mailsRecord != null)
                mailsRecord.save();
        }
    }

    private void orderRecordMail(boolean log) {
        Orderr ord = Orderr.findById(this.orderId);
        ord.reviewMailed = true;
        if(Play.mode.isProd()) {
            ord.save();
            new ERecordBuilder().mail().msgArgs("support@easyacceu.com", ord.email)
                    .fid(fid(ord)).save();
        }
        if(log) Logger.info("Order[%s](%s) email send success!", this.orderId, ord.market);
    }

    private String fid(Orderr order) {
        if(order.market == M.AMAZON_DE)
            return Mails.REVIEW_DE;
        else if(order.market == M.AMAZON_UK)
            return Mails.REVIEW_UK;
        else if(order.market == M.AMAZON_US)
            return Mails.REVIEW_US;
        else
            return "amazon_review";
    }
}
