package jobs.promise;

import helper.Webs;
import models.market.Orderr;
import play.Logger;
import play.Play;
import play.jobs.Job;

import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;

/**
 * 邮件后台发送成功后需要将信息同步到数据库
 * User: wyattpan
 * Date: 10/29/12
 * Time: 9:55 AM
 */
public class ReviewMailCheckPromise extends Job {
    private String orderId;
    private Future<Boolean> sendFlag;

    public ReviewMailCheckPromise(String orderId, Future<Boolean> sendFlag) {
        this.orderId = orderId;
        this.sendFlag = sendFlag;
    }

    @Override
    public void doJob() {
        try {
            boolean mailSuccess = sendFlag.get(10, TimeUnit.SECONDS);

            if(!mailSuccess) {
                Logger.warn("Order %s review email failed.", this.orderId);
            } else {
                Orderr ord = Orderr.findById(this.orderId);
                ord.reviewMailed = true;
                if(Play.mode.isProd())
                    ord.save();
                Logger.info("Order[%s](%s) email send success!", this.orderId, ord.market);
            }
        } catch(Exception e) {
            Logger.warn("Order %s review email failed. [%s]", this.orderId, Webs.E(e));
        }
    }
}
