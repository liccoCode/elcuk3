package jobs;

import com.amazonservices.mws.orders.MarketplaceWebServiceOrdersException;
import helper.Webs;
import models.Jobex;
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
 * 第二步获取 OrderItem 信息
 * <p/>
 * The ListOrderItems and ListOrderItemsByNextToken operations together share
 * a maximum request quota of 30 and a restore rate of 1 request every 2 seconds.
 * <p/>
 * 每隔 1mn 获取最近 30 个订单. 每 1 分钟 API Quote 会恢复满(30个次)
 * - 轮询周期: 30s
 * - Duration: 1mn
 * User: wyatt
 * Date: 5/24/13
 * Time: 12:54 PM
 */
public class AmazonOrderItemDiscover extends Job<List<OrderItem>> {
    @Override
    public void doJob() {
        if(!Jobex.findByClassName(AmazonOrderItemDiscover.class.getName()).isExcute()) return;
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

            AmazonOrderItemDiscover.saveOrderItem(allOrderItems);
        }
    }

    public static void saveOrderItemByOrders(List<Orderr> orderrs) {
        List<OrderItem> orderItems = new ArrayList<OrderItem>();
        for(Orderr orderr : orderrs) {
            orderItems.addAll(orderr.items);
        }
        saveOrderItem(orderItems);
    }

    /**
     * 批量保存 OrderItem
     *
     * @param orderItems
     */
    public static void saveOrderItem(List<OrderItem> orderItems) {
        try {
            PreparedStatement psmt = DB.getConnection().prepareStatement(
                    "INSERT INTO OrderItem(id, createDate, discountPrice, price, currency," +
                            " listingName, quantity, order_orderId, product_sku, selling_sellingId," +
                            " usdCost, market, promotionIDs, giftWrap)" +
                            "VALUES(?,?,?,?,?,?,?,?,?,?,?,?,?,?)"
            );

            int i = 1;
            StringBuilder errors = new StringBuilder("");
            for(OrderItem orderItem : orderItems) {
                if(!orderItemValidate(orderItem, errors)) continue;

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
                psmt.setString(i++, orderItem.order.market.name());
                psmt.setString(i++, orderItem.promotionIDs);
                psmt.setFloat(i, orderItem.giftWrap == null ? 0 : orderItem.giftWrap);
                i = 1;
            }
            psmt.executeBatch();
            Logger.info("Batch inser %s OrderItem.", orderItems.size());
            if(errors.length() > 0) {
                Webs.systemMail("[发现] OrderItem 的时候, 有如下 OrderItem 没有正常存入数据库",
                        "<h4>检查不存在的 Product!</h4> <br>" + errors.toString());
            }
        } catch(SQLException e) {
            e.printStackTrace();
        }
    }

    public static void updateOrderItemByOrders(List<Orderr> orderrs) {
        List<OrderItem> orderItems = new ArrayList<OrderItem>();
        for(Orderr orderr : orderrs) {
            orderItems.addAll(orderr.items);
        }
        updateOrderItem(orderItems);
    }

    public static void updateOrderItem(List<OrderItem> orderItems) {
        try {
            PreparedStatement psmt = DB.getConnection().prepareStatement(
                    "UPDATE OrderItem SET discountPrice=?, price=?, currency=?," +
                            " listingName=?, quantity=?, usdCost=?," +
                            " market=?, promotionIDs=?, giftWrap=?" +
                            " WHERE id=?"
            );

            int i = 1;
            StringBuilder errors = new StringBuilder("");
            for(OrderItem orderItem : orderItems) {
                if(!orderItemValidate(orderItem, errors)) continue;

                psmt.setFloat(i++, orderItem.discountPrice == null ? 0 : orderItem.discountPrice);
                psmt.setFloat(i++, orderItem.price == null ? 0 : orderItem.price);
                psmt.setString(i++, orderItem.currency == null ? null : orderItem.currency.name());
                psmt.setString(i++, orderItem.listingName);
                psmt.setInt(i++, orderItem.quantity);
                psmt.setFloat(i++, orderItem.usdCost == null ? 0 : orderItem.usdCost);
                psmt.setString(i++, orderItem.market.name());
                psmt.setString(i++, orderItem.promotionIDs);
                psmt.setFloat(i++, orderItem.giftWrap == null ? 0 : orderItem.giftWrap);
                psmt.setString(i, orderItem.id);
                i = 1;
            }
            psmt.executeBatch();
            Logger.info("Batch inser %s OrderItem.", orderItems.size());
            if(errors.length() > 0) {
                Webs.systemMail("[更新] OrderItem 的时候, 有如下 OrderItem 没有正常存入数据库",
                        "<h4>检查不存在的 Product!</h4> <br>" + errors.toString());
            }
        } catch(SQLException e) {
            e.printStackTrace();
        }
    }

    private static boolean orderItemValidate(OrderItem orderItem, StringBuilder errors) {
        if(orderItem.product == null || orderItem.selling == null) {
            errors.append("Order(").append(orderItem.order.orderId)
                    .append(") have no product or selling [").append(orderItem.memo).append("]")
                    .append("Product[").append(orderItem.product).append("]")
                    .append("Selling[").append(orderItem.selling).append("]")
                    .append("<br><br>");
            return false;
        }
        return true;
    }
}
