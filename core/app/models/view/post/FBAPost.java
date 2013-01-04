package models.view.post;

import helper.Dates;
import models.market.M;
import models.procure.FBAShipment;
import org.apache.commons.lang.StringUtils;
import org.joda.time.DateTime;
import play.libs.F;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

/**
 * FBA 查看页面的 Post 请求
 * User: wyattpan
 * Date: 11/29/12
 * Time: 11:39 AM
 */
public class FBAPost extends Post<FBAShipment> {
    public FBAPost() {
    }

    public FBAPost(List<FBAShipment.S> states) {
        this.states = states;
    }

    public Date from = DateTime.now().minusDays(45).toDate();
    public M market;
    public List<FBAShipment.S> states = new ArrayList<FBAShipment.S>();
    public String centerId = "";

    /**
     * 签收超时
     */
    public boolean receiptOverTime = false;

    /**
     * 入库超时
     */
    public boolean receiveOverTime = false;

    @Override
    public F.T2<String, List<Object>> params() {
        StringBuilder sbd = new StringBuilder(
                "SELECT DISTINCT(fba) FROM FBAShipment fba LEFT JOIN fba.shipItems si WHERE 1=1 ");
        List<Object> params = new ArrayList<Object>();

        if(this.from != null && this.to != null) {
            sbd.append("AND fba.createAt>=? AND fba.createAt<=? ");
            params.add(Dates.morning(this.from));
            params.add(Dates.night(this.to));
        }

        if(this.market != null) {
            sbd.append("AND fba.account.type=? ");
            params.add(this.market);
        }

        if(this.states.size() > 0) {
            // Play 会自动添加无法解析的 Enum 为 null
            if(!this.states.contains(null)) {
                sbd.append("AND (");
                for(FBAShipment.S s : this.states) {
                    sbd.append("fba.state=? OR ");
                    params.add(s);
                }
                sbd.delete(sbd.lastIndexOf("OR"), sbd.length()).append(")");
            }
        }

        if(StringUtils.isNotBlank(this.centerId)) {
            sbd.append("AND fba.centerId=? ");
            params.add(this.centerId);
        }

        if(this.receiptOverTime) {
            sbd.append("AND (fba.receivingAt - fba.receiptAt)>=2000000 ");
        }

        if(this.receiveOverTime) {
            // mysql 中两个时间相减一天的时间差为: 1000000
            sbd.append(
                    "AND (((fba.closeAt - fba.receivingAt)>=3000000) OR (fba.receivingAt<=? AND fba.closeAt IS NULL)) ");
            params.add(DateTime.now().minusDays(3).toDate());
        }

        if(StringUtils.isNotBlank(this.search)) {
            String word = this.word();
            sbd.append("AND (")
                    .append("fba.shipmentId LIKE ?")
                    .append("OR si.unit.sku LIKE ?")
                    .append(") ");
            for(int i = 0; i < 2; i++) params.add(word);
        }
        return new F.T2<String, List<Object>>(sbd.toString(), params);
    }

    @Override
    public List<FBAShipment> query() {
        F.T2<String, List<Object>> params = params();
        return FBAShipment.find(params._1 + " ORDER BY fba.id desc", params._2.toArray()).fetch();
    }
}
