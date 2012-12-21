package models.view.post;

import helper.Dates;
import models.market.M;
import models.market.Orderr;
import org.apache.commons.lang.StringUtils;
import org.apache.commons.lang.math.NumberUtils;
import org.joda.time.DateTime;
import play.libs.F;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * 订单页面的搜索 POJO, 不进入数据库, 仅仅作为页面的传值绑定
 * User: wyattpan
 * Date: 4/5/12
 * Time: 6:59 PM
 */
public class OrderPOST extends Post<Orderr> {
    public OrderPOST() {
        this.from = new DateTime().withZone(Dates.timeZone(null)).minusDays(7).toDate();
        this.to = new DateTime().withZone(Dates.timeZone(null)).toDate();
        this.perSize = 25;
    }

    public OrderPOST(int perSize) {
        this.perSize = perSize;
    }

    private static Pattern ORDER_NUM_PATTERN = Pattern.compile("^\\+(\\d+)$");

    public Long accountId;

    public M market;

    public Orderr.S state;

    public String orderBy = "createDate";

    public String desc = "DESC";

    public Boolean paymentInfo = true;

    @SuppressWarnings("unchecked")
    public List<Orderr> query() {
        F.T2<String, List<Object>> params = params();
        this.count = this.count(params);

        return Orderr.find(params._1, params._2.toArray()).fetch(this.page, this.perSize);
    }

    @Override
    public Long count(F.T2<String, List<Object>> params) {
        return Orderr.count(params._1, params._2.toArray());
    }

    @Override
    public F.T2<String, List<Object>> params() {
        StringBuilder sbd = new StringBuilder("1=1 ");
        List<Object> params = new ArrayList<Object>();
        if(this.accountId != null) {
            sbd.append("AND account.id=? ");
            params.add(this.accountId);
        }

        if(this.market != null) {
            sbd.append("AND market=? ");
            params.add(this.market);
        }

        if(this.state != null) {
            sbd.append("AND state=? ");
            params.add(this.state);
        }


        if(this.from != null && this.to != null) {
            sbd.append("AND createDate>=? AND createDate<=? ");
            params.add(Dates.morning(this.from));
            params.add(Dates.night(this.to));
        }

        if(this.paymentInfo != null) {
            if(this.paymentInfo)
                sbd.append("AND SIZE(fees)>0 ");
            else if(!this.paymentInfo)
                sbd.append("AND SIZE(fees)<=0 ");
        }

        //TODO 现在这里是所有其他字段的模糊搜索, 后续速度不够的时候可以添加模糊搜索的等级.
        if(StringUtils.isNotBlank(this.search)) {
            // 支持 +23 这样搜索订单的购买数大于某个值
            Matcher matcher = ORDER_NUM_PATTERN.matcher(this.search);
            if(matcher.matches()) {
                int orderUnbers = NumberUtils.toInt(matcher.group(1), 1);
                sbd.append("AND (select sum(oi.quantity) from OrderItem oi where oi.order.orderId=orderId)>").append(orderUnbers).append(" ");
            } else {
                String search = this.word();
                sbd.append("AND (orderId LIKE ? OR ").
                        append("address LIKE ? OR ").
                        append("address1 LIKE ? OR ").
                        append("buyer LIKE ? OR ").
                        append("city LIKE ? OR ").
                        append("country LIKE ? OR ").
                        append("email LIKE ? OR ").
                        append("postalCode LIKE ? OR ").
                        append("phone LIKE ? OR ").
                        append("province LIKE ? OR ").
                        append("reciver LIKE ? OR ").
                        append("memo LIKE ? OR ").
                        append("userid LIKE ? OR ").
                        append("trackNo LIKE ?) ");
                for(int i = 0; i < 14; i++) params.add(search);
            }
        }

        if(StringUtils.isNotBlank(this.orderBy)) {
            sbd.append("ORDER BY ").append(this.orderBy).append(" ").append(StringUtils.isNotBlank(this.desc) ? this.desc : "ASC");
        }
        return new F.T2<String, List<Object>>(sbd.toString(), params);
    }

}
