package query.vo;

import models.market.M;
import models.market.Orderr;

import java.util.Date;

/**
 * 定义一个 VO, 使用 Map 麻烦...
 * User: wyatt
 * Date: 1/23/13
 * Time: 11:25 AM
 */
public class OrderrVO {
    public String orderId;
    public M market;
    public Date createDate;
    public Long account_id;
    public Orderr.S state;
}
