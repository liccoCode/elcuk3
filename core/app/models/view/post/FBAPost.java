package models.view.post;

import models.market.M;
import models.procure.FBAShipment;
import org.apache.commons.lang.StringUtils;
import play.libs.F;

import java.util.ArrayList;
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

    public String market = "";
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
        StringBuilder sbd = new StringBuilder("SELECT DISTINCT(fba) FROM FBAShipment fba LEFT JOIN fba.shipItems si WHERE 1=1 ");
        List<Object> params = new ArrayList<Object>();

        M m = M.val(this.market);
        if(m != null) {
            sbd.append("AND fba.account.type=? ");
            params.add(m);
        }

        if(this.states.size() > 0) {
            if(!this.states.contains(null)) {
                sbd.append("AND (");
                for(FBAShipment.S s : this.states) {
                    sbd.append("fba.state=? OR ");
                    params.add(s);
                }
                sbd.delete(sbd.lastIndexOf("OR"), sbd.length()).append(")");
            } else {
                // 清楚选择了 State(所有状态的选项)
                this.states = new ArrayList<FBAShipment.S>();
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
            sbd.append("AND (fba.closeAt - fba.receivingAt)>=3000000 ");
        }

        if(StringUtils.isNotBlank(this.search)) {
            String word = this.word();
            sbd.append("AND fba.shipmentId LIKE '%").append(word).append("%' OR ")
                    .append("si.unit.sku LIKE '%").append(word).append("%' ");
        }
        return new F.T2<String, List<Object>>(sbd.toString(), params);
    }

    @Override
    public List<FBAShipment> query() {
        F.T2<String, List<Object>> params = params();
        return FBAShipment.find(params._1 + " ORDER BY fba.id desc", params._2.toArray()).fetch();
    }
}
