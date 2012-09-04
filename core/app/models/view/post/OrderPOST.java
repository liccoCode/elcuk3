package models.view.post;

import helper.Dates;
import models.market.Account;
import models.market.M;
import models.market.Orderr;
import org.apache.commons.lang.StringUtils;
import org.apache.commons.lang.math.NumberUtils;
import play.libs.F;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * 订单页面的搜索 POJO, 不进入数据库, 仅仅作为页面的传值绑定
 * User: wyattpan
 * Date: 4/5/12
 * Time: 6:59 PM
 */
public class OrderPOST {
    private static Pattern ORDER_NUM_PATTERN = Pattern.compile("^\\+(\\d+)$");

    public Account account;

    public M market;

    public Orderr.S state;

    public Date from;

    public Date to;

    public P orderBy;

    public String desc;

    public Integer page = 1;

    public Integer size = 100;

    /**
     * 查询字符串
     */
    public String search;

    /**
     * 用来处理 OrderBy 的字段
     */
    public enum P {
        createDate,
        paymentDate,
        shipDate,
        market,
        state
    }

    @SuppressWarnings("unchecked")
    public List<Orderr> query() {
        F.T2<String, Object[]> params = basicParamParse();

        if(params._2.length == 0)
            return Orderr.find(params._1 + "").fetch(this.page, this.size);
        else
            return Orderr.find(params._1 + "", params._2).fetch(this.page, this.size);
    }

    public Long count() {
        F.T2<String, Object[]> params = basicParamParse();

        if(params._2.length == 0)
            return Orderr.count(params._1 + "");
        else
            return Orderr.count(params._1 + "", params._2);
    }

    @SuppressWarnings("unchecked")
    private F.T2<String, Object[]> basicParamParse() {
        StringBuilder sbd = new StringBuilder("1=1 ");
        List params = new ArrayList();
        if(this.account != null && this.account.id != null && this.account.id > 0) {
            sbd.append("AND account=? ");
            params.add(this.account);
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
            this.from = Dates.morning(this.from);
            this.to = Dates.night(this.to);
            sbd.append("AND createDate>=? AND createDate<=? ");
            params.add(this.from);
            params.add(this.to);
        }

        //TODO 现在这里是所有其他字段的模糊搜索, 后续速度不够的时候可以添加模糊搜索的等级.
        if(StringUtils.isNotBlank(this.search)) {
            // 支持 +23 这样搜索订单的购买数大于某个值
            Matcher matcher = ORDER_NUM_PATTERN.matcher(this.search);
            if(matcher.matches()) {
                int orderUnbers = NumberUtils.toInt(matcher.group(1), 1);
                sbd.append("AND (select sum(oi.quantity) from OrderItem oi where oi.order.orderId=orderId)>").append(orderUnbers).append(" ");
            } else {
                this.search = StringUtils.replace(this.search, "'", "''");
                sbd.append("AND (orderId LIKE '%").append(this.search).append("%' OR ").
                        append("address LIKE '%").append(this.search).append("%' OR ").
                        append("address1 LIKE '%").append(this.search).append("%' OR ").
                        append("buyer LIKE '%").append(this.search).append("%' OR ").
                        append("city LIKE '%").append(this.search).append("%' OR ").
                        append("country LIKE '%").append(this.search).append("%' OR ").
                        append("email LIKE '%").append(this.search).append("%' OR ").
                        append("postalCode LIKE '%").append(this.search).append("%' OR ").
                        append("phone LIKE '%").append(this.search).append("%' OR ").
                        append("province LIKE '%").append(this.search).append("%' OR ").
                        append("reciver LIKE '%").append(this.search).append("%' OR ").
                        append("memo LIKE '%").append(this.search).append("%' OR ").
                        append("userid LIKE '%").append(this.search).append("%' OR ").
                        append("trackNo LIKE '%").append(this.search).append("%') ");
            }
        }

        if(this.orderBy != null) {
            sbd.append("ORDER BY ").append(this.orderBy).append(" ").append(StringUtils.isNotBlank(this.desc) ? this.desc : "ASC");
        }
        return new F.T2<String, Object[]>(sbd.toString(), params.toArray());
    }
}
