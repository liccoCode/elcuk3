package models.view;

import models.market.Account;
import models.market.M;
import models.market.Orderr;
import org.apache.commons.lang.StringUtils;
import play.libs.F;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

/**
 * 订单页面的搜索 POJO, 不进入数据库, 仅仅作为页面的传值绑定
 * User: wyattpan
 * Date: 4/5/12
 * Time: 6:59 PM
 */
public class OrderPOST {

    public Account account;

    public M market;

    public Orderr.S state;

    public Date from;

    public Date to;

    public P orderBy;

    public String desc;

    public Integer page = 1;

    public Integer size = 35;

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

        if(this.from != null && this.to != null && this.to.getTime() > this.from.getTime()) {
            sbd.append("AND createDate>=? AND createDate<=? ");
            params.add(this.from);
            params.add(this.to);
        }

        //TODO 现在这里是所有其他字段的模糊搜索, 后续速度不够的时候可以添加模糊搜索的等级.
        if(StringUtils.isNotBlank(this.search)) {
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

        if(this.orderBy != null) {
            sbd.append("ORDER BY ").append(this.orderBy).append(" ").append(StringUtils.isNotBlank(this.desc) ? this.desc : "ASC");
        }
        return new F.T2<String, Object[]>(sbd.toString(), params.toArray());
    }
}
