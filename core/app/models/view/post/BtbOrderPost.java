package models.view.post;

import helper.Currency;
import models.market.BtbOrder;
import models.market.BtbOrderItem;
import org.apache.commons.lang.StringUtils;
import org.joda.time.DateTime;
import play.libs.F;

import java.math.BigDecimal;
import java.text.DecimalFormat;
import java.text.NumberFormat;
import java.util.*;

/**
 * Created by licco on 16/1/21.
 */
public class BtbOrderPost extends Post<BtbOrder> {

    public String keywords;

    public String categoryId;

    public Long btbCustomId;

    public int totalQty;

    public List<String> totalSaleCost = new ArrayList<String>();

    public List<String> totalShipCost = new ArrayList<String>();

    public BtbOrderPost() {
        from = DateTime.now().withDayOfYear(1).toDate();
    }

    @Override
    public F.T2<String, List<Object>> params() {
        List<Object> params = new ArrayList<Object>();
        StringBuilder sql = new StringBuilder("SELECT DISTINCT s FROM BtbOrder s LEFT JOIN s.btbOrderItemList i " +
                "WHERE  1 = 1 ");
        if(StringUtils.isNotEmpty(from.toString()) && StringUtils.isNotEmpty(to.toString())) {
            sql.append(" AND s.saleDate > ? AND s.saleDate < ? ");
            params.add(from);
            params.add(to);
        }
        if(StringUtils.isNotEmpty(categoryId)) {
            sql.append(" AND i.product.sku like ? ");
            params.add(categoryId + "%");
        }
        if(btbCustomId != null) {
            sql.append(" AND s.btbCustom.id like ? ");
            params.add(btbCustomId);
        }

        if(StringUtils.isNotEmpty(keywords)) {
            sql.append(" AND (i.product.sku like ? OR s.orderNo like ? )");
            params.add("%" + keywords + "%");
            params.add("%" + keywords + "%");
        }
        sql.append(" GROUP BY s ");
        return new F.T2<String, List<Object>>(sql.toString(), params);
    }

    public List<BtbOrder> query() {
        F.T2<String, List<Object>> params = params();
        this.count = this.count(params);
        return BtbOrder.find(params._1, params._2.toArray()).fetch(this.page, this.perSize);
    }

    public Long getTotalCount() {
        F.T2<String, List<Object>> params = params();
        return new Long(BtbOrder.find(params._1, params._2.toArray()).fetch().size());
    }

    @Override
    public Long count(F.T2<String, List<Object>> params) {
        return new Long(BtbOrder.find(params._1, params._2.toArray()).fetch().size());
    }

    /**
     * 报表合计功能计算
     *
     * @param dtos
     */
    public void totalCost(List<BtbOrder> dtos) {
        Map<Currency, BigDecimal> cost_map = new HashMap<Currency, BigDecimal>();
        Map<Currency, BigDecimal> ship_map = new HashMap<Currency, BigDecimal>();
        for(BtbOrder order : dtos) {
            for(BtbOrderItem item : order.btbOrderItemList) {
                totalQty += item.qty;
                BigDecimal sale = item.price.multiply(new BigDecimal(item.qty));
                if(cost_map.containsKey(item.currency)) {
                    cost_map.put(item.currency, cost_map.get(item.currency).add(sale));
                } else {
                    cost_map.put(item.currency, sale);
                }
            }
            if(order.customShipCost != null) {
                if(cost_map.containsKey(order.customShipUnit)) {
                    cost_map.put(order.customShipUnit, cost_map.get(order.customShipUnit).add(order.customShipCost));
                } else {
                    cost_map.put(order.customShipUnit, order.customShipCost);
                }
            }

            if(order.bankChargesCost != null) {
                if(cost_map.containsKey(order.bankChargesUnit)) {
                    cost_map.put(order.bankChargesUnit, cost_map.get(order.bankChargesUnit).add(order.bankChargesCost));
                } else {
                    cost_map.put(order.bankChargesUnit, order.bankChargesCost);
                }
            }

            if(ship_map.containsKey(order.shipCostUnit)) {
                ship_map.put(order.shipCostUnit, ship_map.get(order.shipCostUnit).add(order.shipCost));
            } else {
                ship_map.put(order.shipCostUnit, order.shipCost);
            }
        }
        Iterator it = cost_map.entrySet().iterator();
        NumberFormat formatter = new DecimalFormat("###,###.##");
        while(it.hasNext()) {
            Map.Entry entry = (Map.Entry) it.next();
            if(entry != null) {
                Currency now = (Currency) entry.getKey();
                totalSaleCost.add(now.symbol() + " " + formatter.format(cost_map.get(now).doubleValue()));
            }
        }
        Iterator iterator = ship_map.entrySet().iterator();
        while(iterator.hasNext()) {
            Map.Entry entry = (Map.Entry) iterator.next();
            if(entry != null) {
                Currency now = (Currency) entry.getKey();
                totalShipCost.add(now.symbol() + " " + formatter.format(ship_map.get(now).doubleValue()));
            }
        }
    }

}
