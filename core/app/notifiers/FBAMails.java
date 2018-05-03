package notifiers;

import helper.Webs;
import models.ElcukConfig;
import models.MailsRecord;
import models.procure.FBAShipment;
import models.procure.Shipment;
import play.Logger;
import play.Play;
import play.mvc.Mailer;

import java.util.List;

/**
 * Created by IntelliJ IDEA.
 * User: wyattpan
 * Date: 10/17/12
 * Time: 6:17 PM
 */
public class FBAMails extends Mailer {
    // 如果有新增加邮件, 需要向 ElcukRecord.emailOverView 注册

    public static final String STATE_CHANGE = "shipment_state_change";
    public static final String RECEIVING_CHECK = "shipment_receiving_check";

    /**
     * FBA 的状态改变的时候发送邮件
     * Mailer 的返回值必须为基本类型
     *
     * @param fba
     * @param oldState FBA 的原始状态
     * @param newState FBA 改变的新状态
     * @return
     */
    public static boolean shipmentStateChange(FBAShipment fba, FBAShipment.S oldState, FBAShipment.S newState) {
        setSubject(String.format("{INFO} FBA %s state FROM %s To %s", fba.shipmentId, oldState, newState));
        mailBase();
        addRecipient("s@easya.cc");
        MailsRecord mr = null;
        try {
            mr = new MailsRecord(infos.get(), MailsRecord.T.FBA, STATE_CHANGE);
            send(fba, oldState, newState);
            mr.success = true;
        } catch(Exception e) {
            Logger.warn(Webs.e(e));
            return false;
        } finally {
            mr.save();
        }
        return true;
    }

    /**
     * 对传入的运输单进行到港 3 天前警告
     *
     * @param state
     * @return
     */
    public static boolean shipmentsNotify(List<Shipment> ships, Shipment.S state, ElcukConfig cfg) {
        setSubject("%s 个%s阶段运输单为顺利进行下一阶段提前 3 天提醒", ships.size(), cfg.fullName);
        mailBase();
        addRecipient("s@easya.cc");
        try {
            send(ships, state);
        } catch(Exception e) {
            Logger.error(Webs.e(e));
            return false;
        }
        return true;
    }

    private static void mailBase() {
        setCharset("UTF-8");
        if(Play.mode.isProd()) {
            setFrom(models.OperatorConfig.getVal("addressname") + " " + models.OperatorConfig.getVal("supportemail"));
        } else {
            setFrom("EasyAcc <1733913823@qq.com>"); // 因为在国内 Gmail 老是被墙, 坑爹!! 所以非 产品环境 使用 QQ 邮箱测试.
        }
    }
}
