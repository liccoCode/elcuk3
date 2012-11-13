package ext;

import helper.Dates;
import models.procure.Deliveryment;
import models.procure.ProcureUnit;
import models.procure.Shipment;
import play.templates.JavaExtensions;

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
                return "3da4c2";
            case DELIVERY:
                return "006acc";
            case DONE:
                return "5bb75b";
            case SHIP_OVER:
                return "108080";
            case CLOSE:
            default:
                return "f9a021";
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
                return "3da4c2";
            case SHIPPING:
                return "006acc";
            case CLEARANCE:
                return "5bb75b";
            case CANCEL:
                return "f9a021";
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
        return String.format("[%s] [%s] [%s items] [%s FBAs] [%s Kg] [预计运输时间: %s]", s.id, s.state, s.items.size(), s.fbas.size(), s.totalWeight(), Dates.date2Date(s.planBeginDate));
    }
}
