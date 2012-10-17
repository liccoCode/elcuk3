package notifiers;

import helper.Webs;
import models.procure.FBAShipment;
import play.Logger;
import play.Play;
import play.mvc.Mailer;

/**
 * Created by IntelliJ IDEA.
 * User: wyattpan
 * Date: 10/17/12
 * Time: 6:17 PM
 */
public class FBAMails extends Mailer {
    /**
     * FBA 的状态改变的时候发送邮件
     *
     * @param fba
     * @param oldState FBA 的原始状态
     * @param newState FBA 改变的新状态
     * @return
     */
    public static /*Mailer 的返回值必须为基本类型*/boolean shipmentStateChange(FBAShipment fba, FBAShipment.S oldState, FBAShipment.S newState) {
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
