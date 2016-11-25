package ext;

import helper.Webs;
import models.market.M;
import models.procure.*;
import models.view.dto.AnalyzeDTO;
import models.whouse.Inbound;
import org.apache.commons.lang.StringUtils;
import play.libs.F;
import play.templates.BaseTemplate;
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
                return "#B0BFD6";
            case DELIVERY:
                return "#006ACC";
            case DONE:
                return "#3DA4C2";
            case SHIPPING:
                return "#49A4C6";
            case SHIP_OVER:
                return "#108080";
            case INBOUND:
                return "#F9A021";
            case CLOSE:
            default:
                return "#5BB75B";
        }
    }

    public static String rgb(Inbound.S status) {
        switch(status) {
            case Create:
                return "#B0BFD6";
            case Handing:
                return "#006ACC";
            case End:
                return "#3DA4C2";
            default:
                return "#5BB75B";
        }
    }

    public static String overLong(String value) {
        if(StringUtils.isNotBlank(value)) {
            if(value.length() > 12) {
                return value.substring(0, 11) + "...";
            } else {
                return value;
            }
        } else {
            return "";
        }
    }

    public static String rgb(Deliveryment.S state) {
        switch(state) {
            case PENDING:
                return "#5CB85C";
            case CONFIRM:
                return "#FAA52C";
            case DONE:
                return "#4DB2D0";
            case CANCEL:
            default:
                return "#D14741";
        }
    }

    public static String rgb(Shipment.S state) {
        switch(state) {
            case PLAN:
                return "#3DA4C2";
            case SHIPPING:
                return "#006ACC";
            case CLEARANCE:
                return "#5BB75B";
            case CANCEL:
                return "#F9A021";
            case DONE:
            default:
                return "#108080";
        }
    }

    public static String rgb(Shipment.T type) {
        if(type == null) return "#BD4A48";
        switch(type) {
            case AIR:
                return "#49A4C6";
            case EXPRESS:
                return "#468847";
            case SEA:
                return "#C09853";
            default:
                return "#333333";
        }
    }

    public static String rgb(FBAShipment.S state) {
        if(state == null) return "#333333";
        switch(state) {
            case PLAN:
            case WORKING:
                return "#333333";
            case SHIPPED:
                return "#49A4C6";
            case IN_TRANSIT:
                return "#3DA4C2";
            case DELIVERED:
                return "#108080";
            case CHECKED_IN:
                return "#006ACC";
            case RECEIVING:
                return "#F9A021";
            case CLOSED:
                return "#5BB75B";
            case CANCELLED:
                return "#C09853";
            case DELETED:
                return "#BD4A48";
            default:
                return "#333333";
        }
    }

    public static String rgb(M market) {
        switch(market) {
            case AMAZON_US:
                return "#333333";
            case AMAZON_UK:
                return "#49A4C6";
            case AMAZON_DE:
                return "#3DA4C2";
            case AMAZON_FR:
                return "#108080";
            case AMAZON_IT:
                return "#006ACC";
            case AMAZON_ES:
                return "#F9A021";
            case AMAZON_JP:
                return "#5BB75B";
            default:
                return "#333333";
        }
    }

    public static BaseTemplate.RawData records(FBAShipment fba) {
        String[] lines = StringUtils.splitByWholeSeparator(fba.records, "\n");
        if(lines != null) {
            StringBuilder sb = new StringBuilder();
            sb.append("<table class='table table-bordered table-condensed'>").append("<tr>")
                    .append("<th>时间</th>").append("<th>FNSKU</th>").append("<th>SellingId</th>")
                    .append("<th>数量</th>").append("<th>ShimentId</th>")
                    .append("<th>CenterId</th>")
                    .append("</tr>");
            for(String line : lines) {
                String[] fields = StringUtils.splitByWholeSeparator(line, "\t");
                sb.append("<tr>").append("<td>")
                        .append(StringUtils.join(fields, "</td><td>"))
                        .append("</td></tr>");
            }
            sb.append("</table>");
            return raw(sb.toString());
        } else {
            return raw("");
        }
    }

    /**
     * 判断运输项目的预计发货时间是否超过当前运输单
     *
     * @param itm
     * @return
     */
    public static String overdue(ShipItem itm) {
        if(itm.unit.attrs.planShipDate.getTime() < itm.shipment.dates.planBeginDate.getTime())
            return "#F2DEDE";
        else
            return "#FFFFFF";
    }

    /**
     * 根据差值 返回不同颜色来表示升、降、相等
     *
     * @param diff
     * @return
     */
    public static String rgb(Float diff) {
        if(diff > 0)
            return "#468847";
        else if(diff < 0)
            return "#B94A48";
        return "#0000ff";
    }

    /**
     * 计算 AnalyzeDTO.difference的增长/下降百分比
     *
     * @param dto
     * @return
     */
    public static float percentage(AnalyzeDTO dto) {
        float day7Avg = dto.day7 / 7;
        if(day7Avg == 0 || dto.day1 == 0)
            return 0;
        return Math.abs(day7Avg / dto.day1);
    }

    /**
     * 获得 采购单中商品单价、总价的美元形式
     *
     * @return t2[申报价, 总申报价]
     */
    public static F.T2<Float, Float> amountUSD(ProcureUnit unit) {
        float priceUSD = Webs.scale2PointUp(unit.product.declaredValue);
        float amountUSD = priceUSD * unit.qty();
        return new F.T2<>(priceUSD, amountUSD);
    }
}
