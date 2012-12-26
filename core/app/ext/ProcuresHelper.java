package ext;

import models.procure.Deliveryment;
import models.procure.FBAShipment;
import models.procure.ProcureUnit;
import models.procure.Shipment;
import play.templates.JavaExtensions;

import java.util.Date;
import java.util.concurrent.TimeUnit;

/**
 * Created by IntelliJ IDEA.
 * User: wyattpan
 * Date: 8/31/12
 * Time: 5:19 PM
 */
public class ProcuresHelper extends JavaExtensions {

    public static String rgb(ProcureUnit.STAGE stage) {
        switch(stage) {
            case PLAN:
                return "333333";
            case DELIVERY:
                return "006ACC";
            case DONE:
                return "3DA4C2";
            case SHIPPING:
                return "49A4C6";
            case SHIP_OVER:
                return "108080";
            case INBOUND:
                return "F9A021";
            case CLOSE:
            default:
                return "5BB75B";
        }
    }

    public static String rgb(Deliveryment.S state) {
        switch(state) {
            case PENDING:
                return "5CB85C";
            case CONFIRM:
                return "FAA52C";
            case DONE:
                return "4DB2D0";
            case CANCEL:
            default:
                return "D14741";
        }
    }

    public static String rgb(Shipment.S state) {
        switch(state) {
            case PLAN:
                return "3DA4C2";
            case SHIPPING:
                return "006ACC";
            case CLEARANCE:
                return "5BB75B";
            case CANCEL:
                return "F9A021";
            case DONE:
            default:
                return "108080";
        }
    }

    public static String rgb(Shipment.T type) {
        if(type == null) return "BD4A48";
        switch(type) {
            case AIR:
                return "49A4C6";
            case EXPRESS:
                return "468847";
            case SEA:
                return "C09853";
            default:
                return "333333";
        }
    }

    public static String rgb(FBAShipment.S state) {
        if(state == null) return "333333";
        switch(state) {
            case PLAN:
            case WORKING:
                return "333333";
            case SHIPPED:
                return "49A4C6";
            case IN_TRANSIT:
                return "3DA4C2";
            case DELIVERED:
                return "108080";
            case CHECKED_IN:
                return "006ACC";
            case RECEIVING:
                return "F9A021";
            case CLOSED:
                return "5BB75B";
            case CANCELLED:
                return "C09853";
            case DELETED:
                return "BD4A48";
            default:
                return "333333";
        }

    }

    /**
     * 超过或者不足的 ProcureUnit 在页面上的颜色
     *
     * @return
     */
    public static String leekOrOverRgb(ProcureUnit unit) {
        Integer qty = unit.attrs.qty;
        Integer planQty = unit.attrs.planQty;
        if(qty == null) qty = 0;
        if(planQty == null) planQty = 0;
        if(qty - planQty > 0) return "84F000";
        else if(qty - planQty < 0) return "FF7F1C";
        else return "ffffff";
    }

    public static String info(Shipment s) {
        return String.format("[%s:%s] [%s] [%s items] [%s FBAs] [%s Kg] [预计运输: %tF] [预计到达: %tF]",
                s.id, s.type, s.state, s.items.size(), s.fbas.size(), s.totalWeight(), s.planBeginDate, s.planArrivDate);
    }

    /**
     * 根据 end - begin 所计算的时间差, 给与 badge-xxx 提示紧急程度
     *
     * @param begin
     * @param end
     * @return
     */
    public static String badgeSuffix(Date begin, Date end) {
        if(end == null) end = new Date();
        if(begin == null) return "";
        long diffs = end.getTime() - begin.getTime();
        if(diffs < TimeUnit.DAYS.toMillis(2))
            return "badge-success";
        else if(diffs < TimeUnit.DAYS.toMillis(3))
            return "badge-info";
        else if(diffs < TimeUnit.DAYS.toMillis(5))
            return "badge-warning";
        else {
            return "badge-important";
        }
    }
}
