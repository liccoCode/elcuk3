package models.view.post;

import helper.Dates;
import models.market.M;
import models.market.SellingRecord;
import models.procure.Deliveryment;
import org.apache.commons.lang.StringUtils;
import org.joda.time.DateTime;
import play.libs.F;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

/**
 * Created by IntelliJ IDEA.
 * User: wyattpan
 * Date: 9/3/12
 * Time: 4:32 PM
 */
public class SellingRecordPost extends Post<SellingRecord> {

    private static final long serialVersionUID = -2081286181455788781L;
    /**
     * 在 ProcureUnits中，planView 和noPlaced 方法 需要调用 index，必须重写，否则总是构造方法中的时间
     */
    public Date from;
    public Date to;
    public int perSize = 50;

    /**
     * 市场
     */
    public M market;

    public String SellingId;


    @Override
    public F.T2<String, List<Object>> params() {
        StringBuilder sbd = new StringBuilder("SELECT s FROM SellingRecord s WHERE  1=1 AND ");
        List<Object> params = new ArrayList<>();
        /** 时间参数 **/
        sbd.append(" s.date>=? AND s.date<=?");
        params.add(Dates.morning(this.from));
        params.add(Dates.night(this.to));

        if(this.search != null && !"".equals(this.search.trim())) {
            String word = String.format("%%%s%%", StringUtils.replace(search.trim(), "'", "''"));
            sbd.append(" AND s.product_sku like ? ");
            params.add(word);
        }
        if(this.market != null) {
            sbd.append(" AND s.market=? ");
            params.add(this.market);
        }
        sbd.append(" ORDER BY s.id");
        return new F.T2<>(sbd.toString(), params);
    }


    public SellingRecordPost() {
        this.from = DateTime.now().minusDays(3).toDate();
        this.to = new Date();
    }


    public List<SellingRecord> query() {
        F.T2<String, List<Object>> params = params();
        this.count = SellingRecord.find(params._1, params._2.toArray()).fetch().size();
        String sql = params._1 + " ";
        return SellingRecord.find(sql, params._2.toArray()).fetch(this.page, this.perSize);
    }

    public List<SellingRecord> queryForExcel() {
        F.T2<String, List<Object>> params = params();
        String sql = params._1 + " ";
        return SellingRecord.find(sql, params._2.toArray()).fetch();
    }

    public Long getTotalCount() {
        return this.count;
    }

}
