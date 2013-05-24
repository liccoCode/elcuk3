package jobs;

import com.amazonservices.mws.orders.MarketplaceWebServiceOrdersException;
import helper.Webs;
import models.market.Account;
import models.market.OrderItem;
import models.market.Orderr;
import mws.MWSOrders;
import play.Logger;
import play.db.DB;
import play.jobs.Job;

import java.sql.Date;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by IntelliJ IDEA.
 * The ListOrderItems and ListOrderItemsByNextToken operations together share
 * a maximum request quota of 30 and a restore rate of 1 request every 2 seconds.
 * <p/>
 * 每隔 1mn 获取最近 30 个订单. 每 1 分钟 API Quote 会恢复满(30个次)
 * User: wyatt
 * Date: 5/24/13
 * Time: 12:54 PM
 */
public class AmazonOrderItemDiscover extends Job<List<OrderItem>> {
    @Override
    public void doJob() {
        List<Account> accounts = Account.openedSaleAcc();
        for(Account acc : accounts) {
            List<Orderr> orderrs = Orderr.find("SIZE(items)=0 AND account=?", acc).fetch(30);

            List<OrderItem> allOrderItems = new ArrayList<OrderItem>();
            for(Orderr order : orderrs) {
                try {
                    List<OrderItem> orderItems = MWSOrders.listOrderItems(acc, order.orderId);
                    allOrderItems.addAll(orderItems);
                } catch(MarketplaceWebServiceOrdersException e) {
                    Logger.warn("Order %s OrderItem Discover Failed because of [%s]",
                            order.orderId, Webs.S(e));
                }
            }

            this.saveOrderItem(allOrderItems);
        }
    }

    /**
     * 批量保存 OrderItem
     *
     * @param orderItems
     */
    private void saveOrderItem(List<OrderItem> orderItems) {
        try {
            PreparedStatement psmt = DB.getConnection().prepareStatement(
                    "INSERT INTO OrderItem(id, createDate, discountPrice, price, currency," +
                            " listingName, quantity, order_orderId, product_sku, selling_sellingId," +
                            " usdCost, market)" +
                            "VALUES(?,?,?,?,?,?,?,?,?,?,?,?)"
            );

            int i = 1;
            StringBuilder errors = new StringBuilder("");
            for(OrderItem orderItem : orderItems) {
                if(orderItem.product == null || orderItem.selling == null) {
                    errors.append("Order(").append(orderItem.order.orderId)
                            .append(") have no product or selling [")
                            .append(orderItem.memo).append("]<br><br>");
                    continue;
                }
                psmt.setString(i++, orderItem.id);
                psmt.setDate(i++, new Date(orderItem.createDate.getTime()));
                psmt.setFloat(i++, orderItem.discountPrice == null ? 0 : orderItem.discountPrice);
                psmt.setFloat(i++, orderItem.price == null ? 0 : orderItem.price);
                psmt.setString(i++, orderItem.currency == null ? null : orderItem.currency.name());
                psmt.setString(i++, orderItem.listingName);
                psmt.setInt(i++, orderItem.quantity);
                psmt.setString(i++, orderItem.order.orderId);
                psmt.setString(i++, orderItem.product.sku);
                psmt.setString(i++, orderItem.selling.sellingId);
                psmt.setFloat(i++, orderItem.usdCost == null ? 0 : orderItem.usdCost);
                psmt.setString(i, orderItem.order.market.name());
                i = 1;
            }
            psmt.executeBatch();
            Logger.info("Batch inser %s OrderItem.", orderItems.size());
            if(errors.length() > 0) {
                Webs.systemMail("发现 OrderItem 的时候, 有如下 OrderItem 没有正常存入数据库",
                        "<h4>检查不存在的 Product!</h4> <br>" + errors.toString());
            }
        } catch(SQLException e) {
            e.printStackTrace();
        }
    }
}
