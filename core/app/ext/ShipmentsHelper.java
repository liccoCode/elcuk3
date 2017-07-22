package ext;

import models.ElcukConfig;
import models.User;
import models.procure.Shipment;
import org.joda.time.DateTime;
import play.templates.BaseTemplate;
import play.templates.JavaExtensions;

import java.util.Date;
import java.util.concurrent.TimeUnit;

/**
 * Created by IntelliJ IDEA.
 * User: wyatt
 * Date: 5/16/13
 * Time: 4:30 PM
 */
public class ShipmentsHelper extends JavaExtensions {

    public static Date nextStateDate(Shipment shipment) {
        if(shipment.state == Shipment.S.DONE)
            return shipment.dates.arriveDate;

        DateTime rightDate = new DateTime(shipment.dates.beginDate);
        if(shipment.state == Shipment.S.SHIPPING)
            rightDate = rightDate.plusDays(shipment.config("atport").toInteger());
        else if(shipment.state == Shipment.S.CLEARANCE) {
            rightDate = new DateTime(shipment.dates.atPortDate)
                    .plusDays(shipment.config("clearance").toInteger());
        } else if(shipment.state == Shipment.S.BOOKED) {
            rightDate = new DateTime(shipment.dates.bookDate)
                    .plusDays(shipment.config("book").toInteger());
        } else if(shipment.state == Shipment.S.DELIVERYING) {
            rightDate = new DateTime(shipment.dates.deliverDate)
                    .plusDays(shipment.config("deliver").toInteger());
        } else if(shipment.state == Shipment.S.RECEIPTD) {
            rightDate = new DateTime(shipment.dates.receiptDate)
                    .plusDays(shipment.config("receipt").toInteger());
        } else if(shipment.state == Shipment.S.RECEIVING) {
            rightDate = new DateTime(shipment.dates.inbondDate)
                    .plusDays(shipment.config("inbound").toInteger());
        }
        return rightDate.toDate();
    }

    /**
     * 预计运输单结束时间
     *
     * @param shipment
     * @return
     */
    public static Date predictArriveDate(Shipment shipment) {
        if(shipment.state == Shipment.S.DONE)
            return shipment.dates.arriveDate;
        if(shipment.projectName == User.COR.MengTop)
            return null;

        int totalDays = 0;
        for(String dayType : ElcukConfig.DAY_TYPES.keySet()) {
            totalDays += shipment.config(dayType).toInteger();
        }

        Date lastDate = shipment.dates.beginDate;
        if(lastDate == null) lastDate = shipment.dates.planBeginDate;

        if(shipment.state.ordinal() >= Shipment.S.CLEARANCE.ordinal()) {
            totalDays -= shipment.config("atport").toInteger();
            lastDate = shipment.dates.atPortDate;
        }

        if(shipment.state.ordinal() >= Shipment.S.BOOKED.ordinal()) {
            totalDays -= shipment.config("pick").toInteger();
            lastDate = shipment.dates.bookDate;
        }

        if(shipment.state.ordinal() >= Shipment.S.DELIVERYING.ordinal()) {
            totalDays -= shipment.config("book").toInteger();
            lastDate = shipment.dates.deliverDate;
        }

        if(shipment.state.ordinal() >= Shipment.S.RECEIPTD.ordinal()) {
            totalDays -= shipment.config("deliver").toInteger();
            lastDate = shipment.dates.receiptDate;
        }

        if(shipment.state.ordinal() >= Shipment.S.RECEIVING.ordinal()) {
            totalDays -= shipment.config("receipt").toInteger();
            lastDate = shipment.dates.inbondDate;
        }

        return new DateTime(lastDate).plusDays(totalDays).toDate();
    }

    public static BaseTemplate.RawData betweenDays(Shipment shipment, Shipment.S state) {
        long diff = 0;
        String color = "";
        if(state == Shipment.S.CLEARANCE) {
            if(shipment.dates.atPortDate != null && shipment.dates.beginDate != null) {
                diff = shipment.dates.atPortDate.getTime() - shipment.dates.beginDate.getTime();
            }
        } else if(state == Shipment.S.BOOKED) {
            if(shipment.dates.bookDate != null && shipment.dates.pickGoodDate != null) {
                diff = shipment.dates.bookDate.getTime() - shipment.dates.pickGoodDate.getTime();
            }
        } else if(state == Shipment.S.DELIVERYING) {
            if(shipment.dates.deliverDate != null && shipment.dates.bookDate != null) {
                diff = shipment.dates.deliverDate.getTime() - shipment.dates.bookDate.getTime();
            }
        } else if(state == Shipment.S.RECEIPTD) {
            if(shipment.dates.deliverDate != null && shipment.dates.receiptDate != null) {
                diff = shipment.dates.receiptDate.getTime() - shipment.dates.deliverDate.getTime();
            }
        } else if(state == Shipment.S.RECEIVING) {
            if(shipment.dates.receiptDate != null && shipment.dates.inbondDate != null) {
                diff = shipment.dates.inbondDate.getTime() - shipment.dates.receiptDate.getTime();
            }
        } else if(state == Shipment.S.DONE) {
            if(shipment.dates.arriveDate != null && shipment.dates.inbondDate != null) {
                diff = shipment.dates.arriveDate.getTime() - shipment.dates.inbondDate.getTime();
            }
        }

        if(diff < TimeUnit.DAYS.toMillis(2) && diff > 0) {
            color = "#F9A42B";
        } else if(diff < 0) {
            color = "#BB514C";
        } else {
            color = "#333";
        }

        return raw(String.format("<strong style='color:%s'>%.2f</strong> 天", color,
                diff / ((float) 24 * 3600 * 1000)));
    }
}
