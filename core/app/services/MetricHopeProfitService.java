package services;

import models.market.M;
import models.market.OrderItem;
import models.procure.ProcureUnit;
import models.procure.Shipment.T;
import org.joda.time.DateTime;
import play.libs.F;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

/**
 * Created by IntelliJ IDEA.
 * User: cary
 * Date: 14-3-17
 * Time: 上午11:22
 */
public class MetricHopeProfitService {

    public String sku;
    public M market;
    public T shiptype;
    public MetricProfitService profit;

    public MetricHopeProfitService(
            String sku, M market, T shiptype) {
        this.sku = sku;
        this.market = market;
        Date begin = DateTime.now().withDayOfYear(1).toDate();
        Date end = DateTime.now().withTimeAtStartOfDay().toDate();
        profit = new MetricProfitService(begin, end, this.market, this.sku, null);
        this.shiptype = shiptype;
    }

    /**
     * 期望售价,自动带出最近售价
     */
    public float salePrice() {
        OrderItem orderitem = OrderItem.find(" product.sku=? and selling.market=? order by createDate desc",
                this.sku, this.market).first();
        if(orderitem == null) {
            return 0f;
        } else
            return orderitem.currency.toUSD(orderitem.price);
    }

    /**
     * 期望采购价
     */
    public float procurePrice() {
        List<Object> params = new ArrayList<>();
        StringBuilder sbd = new StringBuilder(
                "SELECT DISTINCT d FROM ProcureUnit d WHERE 1=1 AND");
        sbd.append(" d.sku=?");
        params.add(this.sku);
        sbd.append(" and d.selling.market=?");
        params.add(this.market);
        sbd.append(" order by createDate desc ");
        ProcureUnit p = ProcureUnit.find(sbd.toString(), params.toArray()).first();
        if(p == null) {
            return 0f;
        } else {
            return p.attrs.currency.toUSD(p.attrs.price);
        }
    }

    /**
     * SKU运费单价
     */
    public float shipPrice() {
        F.T3<F.T2<Float, Integer>, F.T2<Float, Integer>, F.T2<Float, Integer>> feeinfo = profit.shipTypePrice();
        F.T2<Float, Integer> seainfo = feeinfo._1;
        F.T2<Float, Integer> airinfo = feeinfo._2;
        F.T2<Float, Integer> expressinfo = feeinfo._3;
        if(this.shiptype == T.SEA) {
            if(seainfo._2 != 0) {
                return seainfo._1 / seainfo._2;
            } else
                return 0f;
        }
        if(this.shiptype == T.AIR) {
            if(airinfo._2 != 0) {
                return airinfo._1 / airinfo._2;
            } else
                return 0f;
        }
        if(this.shiptype == T.EXPRESS) {
            if(expressinfo._2 != 0) {
                return expressinfo._1 / expressinfo._2;
            } else
                return 0f;
        }
        return 0f;
    }

    /**
     * 关税和VAT单价
     *
     * @return
     */
    public float vatPrice() {
        return profit.esVatPrice();
    }

    /**
     * 预计亚马逊费用
     */
    public float amzonfee() {
        return 0f;
    }

    /**
     * 预计FBA费用
     */
    public float fbafee() {
        return 0f;
    }

}
