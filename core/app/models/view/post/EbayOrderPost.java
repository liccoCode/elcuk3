package models.view.post;

import helper.Dates;
import models.market.EbayOrder;
import models.market.M;
import models.market.Orderr;
import org.apache.commons.lang.StringUtils;
import play.libs.F;

import java.util.ArrayList;
import java.util.List;


/**
 * Created by IntelliJ IDEA.
 * User: licco
 * Date: 2017/9/4
 * Time: 上午10:55
 */
public class EbayOrderPost extends Post<EbayOrder> {

    private static final long serialVersionUID = 9006377542594278217L;
    public M market;
    public Orderr.S state = Orderr.S.SHIPPED;
    public String category;
    public int perSize = 20;

    @Override
    public F.T2<String, List<Object>> params() {
        StringBuilder sbd = new StringBuilder("SELECT DISTINCT e FROM EbayOrder e LEFT JOIN e.items i ");
        sbd.append("WHERE e.type = ? ");
        List<Object> params = new ArrayList<>();
        params.add(EbayOrder.T.EBAY);
        sbd.append(" AND e.createDate >= ? AND e.createDate <= ? ");
        params.add(Dates.morning(this.from));
        params.add(Dates.night(this.to));

        if(market != null) {
            sbd.append(" AND e.market = ? ");
            params.add(market);
        }
        if(state != null) {
            sbd.append(" AND e.state = ? ");
            params.add(state);
        }
        if(StringUtils.isNotBlank(this.search)) {
            sbd.append(" AND (i.product.sku = ? or e.orderId = ? )");
            params.add(this.search);
            params.add(this.search);
        }

        if(StringUtils.isNotBlank(this.category)) {
            sbd.append(" AND i.product.category.categoryId = ? ");
            params.add(this.category);
        }

        sbd.append(" ORDER BY e.createDate DESC ");
        return new F.T2<>(sbd.toString(), params);
    }

    @Override
    public List<EbayOrder> query() {
        F.T2<String, List<Object>> params = params();
        this.count = EbayOrder.find(params._1, params._2.toArray()).fetch().size();
        return EbayOrder.find(params._1, params._2.toArray()).fetch(this.page, this.perSize);
    }

    @Override
    public Long getTotalCount() {
        return this.count;
    }

    @Override
    public Long count(F.T2<String, List<Object>> params) {
        return this.count;
    }

}
