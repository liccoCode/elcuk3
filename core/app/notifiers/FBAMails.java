package notifiers;

import helper.Webs;
import models.MailsRecord;
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
    // 如果有新增加邮件, 需要向 ElcukRecord.emailOverView 注册

    public static final String STATE_CHANGE = "shipment_state_change";
    public static final String NOT_RECEING = "shipment_receipt_not_receiving";
    public static final String RECEIVING_CHECK = "shipment_receiving_check";

    /**
     * FBA 的状态改变的时候发送邮件
     *
     * @param fba
     * @param oldState FBA 的原始状态
     * @param newState FBA 改变的新状态
     * @return
     */
    public static /*Mailer 的返回值必须为基本类型*/boolean shipmentStateChange(FBAShipment fba,
                                                                    FBAShipment.S oldState,
                                                                    FBAShipment.S newState) {
        setSubject(String.format("{INFO} FBA %s state FROM %s To %s",
                fba.shipmentId, oldState, newState));
        mailBase();
        addRecipient("p@easyacceu.com");
        MailsRecord mr = null;
        try {
            mr = new MailsRecord(infos.get(), MailsRecord.T.FBA, STATE_CHANGE);
            send(fba, oldState, newState);
            mr.success = true;
        } catch(Exception e) {
            Logger.warn(Webs.E(e));
            return false;
        } finally {
            mr.save();
        }
        return true;
    }

    /**
     * Amazon 签收了, 但是没有入库邮件提醒
     *
     * @return
     */
    public static boolean receiptButNotReceiving(FBAShipment fba) {
        setSubject("{WARN} FBA %s 签收了,但超过 2 天还没有开始入库.", fba.shipmentId);
        mailBase();
        addRecipient("alerts@easyacceu.com", "p@easyacceu.com");
        MailsRecord mr = null;
        try {
            mr = new MailsRecord(infos.get(), MailsRecord.T.FBA, NOT_RECEING);
            send(fba);
            mr.success = true;
        } catch(Exception e) {
            Logger.warn(Webs.E(e));
            return false;
        } finally {
            if(mr != null)
                mr.save();
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
