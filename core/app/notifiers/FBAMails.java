package notifiers;

import helper.Webs;
import models.MailsRecord;
import models.embedded.ERecordBuilder;
import models.procure.FBAShipment;
import play.Logger;
import play.Play;
import play.mvc.Mailer;

import java.util.ArrayList;
import java.util.Set;

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
        MailsRecord mr=MailsRecord.findByTitle(infos.get().get("subject").toString());
        mr.addParams(infos.get().get("from").toString(),(ArrayList<String>)infos.get().get("recipients"),STATE_CHANGE,MailsRecord.T.FBA);
        try {
            send(fba, oldState, newState);
            new ERecordBuilder().mail()
                    .msgArgs(infos.get().get("from").toString(), "p@easyacceu.com")
                    .fid(STATE_CHANGE)
                    .save();
        } catch(Exception e) {
            Logger.warn(Webs.E(e));
            mr.success=false;
            return false;
        }finally {
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
        MailsRecord mr=MailsRecord.findByTitle(infos.get().get("subject").toString());
        mr.addParams(infos.get().get("from").toString(),(ArrayList<String>)infos.get().get("recipients"),NOT_RECEING,MailsRecord.T.FBA);
        try {
            send(fba);
            new ERecordBuilder().mail()
                    .msgArgs(infos.get().get("from").toString(), "p@easyacceu.com")
                    .fid(NOT_RECEING)
                    .save();
        } catch(Exception e) {
            mr.success=false;
            Logger.warn(Webs.E(e));
            return false;
        }finally {
            mr.save();
        }
        return true;
    }

    /**
     * FBA 正在入库的检查邮件
     *
     * @param fbas
     */
    public static boolean itemsReceivingCheck(Set<FBAShipment> fbas) {
        setSubject("{WARN} 总共 %s 个 FBA 入库时间过长, 需检查", fbas.size());
        mailBase();
        addRecipient("alerts@easyacceu.com", "p@easyacceu.com");

        MailsRecord mr=MailsRecord.findByTitle(infos.get().get("subject").toString());
        mr.addParams(infos.get().get("from").toString(),(ArrayList<String>)infos.get().get("recipients"),RECEIVING_CHECK,MailsRecord.T.FBA);
        try {
            send(fbas);
            new ERecordBuilder().mail()
                    .msgArgs(infos.get().get("from").toString(), "p@easyacceu.com")
                    .fid(RECEIVING_CHECK)
                    .save();
        } catch(Exception e) {
            mr.success=false;
            Logger.warn(Webs.E(e));
            return false;
        }finally {
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
