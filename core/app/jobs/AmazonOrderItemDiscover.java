package jobs;

import com.amazonservices.mws.orders.MarketplaceWebServiceOrdersException;
import helper.LogUtils;
import helper.Webs;
import models.Jobex;
import models.market.Account;
import models.market.OrderItem;
import models.market.Orderr;
import mws.MWSOrders;
import org.apache.commons.lang.StringUtils;
import org.joda.time.DateTime;
import play.Logger;
import play.db.DB;
import play.jobs.Job;

import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.sql.Timestamp;
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
 * @deprecated
 */
public class AmazonOrderItemDiscover extends Job<List<OrderItem>> {
    @Override
    public void doJob() {
        long begin = System.currentTimeMillis();
        if(!Jobex.findByClassName(AmazonOrderItemDiscover.class.getName()).isExcute()) return;
        List<Account> accounts = Account.openedSaleAcc();
        for(Account acc : accounts) {
            // 只搜索 1 个月内的
            // TODO 性能有问题
            List<Orderr> orderrs = Orderr.find(
                    "SIZE(items)=0 AND account=? AND createDate>=? AND state!=? ORDER BY createDate",
                    acc, DateTime.now().minusMonths(1).toDate(), Orderr.S.CANCEL
            ).fetch(30);

            if(orderrs.size() <= 0) {
                Logger.info("%s orderItem clear.", acc.uniqueName);
            } else {
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
                Logger.info("Discover %s  %s OrderItems.", acc.uniqueName, allOrderItems.size());
            }
        }
        if(LogUtils.isslow(System.currentTimeMillis() - begin,"AmazonOrderItemDiscover")) {
            LogUtils.JOBLOG.info(String
                    .format("AmazonOrderItemDiscover calculate.... [%sms]", System.currentTimeMillis() - begin));
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
        PreparedStatement psmt = null;
        try {
            psmt = DB.getConnection().prepareStatement(
                    "INSERT INTO OrderItem(id, createDate, discountPrice, price, currency," +
                            " listingName, quantity, order_orderId, product_sku, selling_sellingId," +
                            " usdCost, market, promotionIDs, giftWrap)" +
                            " VALUES(?,?,?,?,?,?,?,?,?,?,?,?,?,?)"
            );

            int i = 1;
            StringBuilder errors = new StringBuilder("");
            for(OrderItem orderItem : orderItems) {
                if(!orderItemValidate(orderItem, errors)) continue;

                psmt.setString(i++, orderItem.id);
                psmt.setTimestamp(i++, new Timestamp(orderItem.order.paymentDate == null ?
                        orderItem.createDate.getTime() : orderItem.order.paymentDate.getTime())
                );
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
                psmt.addBatch();
                i = 1;
            }
            int[] results = psmt.executeBatch();
            Logger.info("Batch insert %s OrderItem. [%s](%s)",
                    orderItems.size(), Webs.intArrayString(results), results.length
            );
            if(errors.length() > 0) {
                Webs.systemMail("[发现] OrderItem 的时候, 有如下 OrderItem 没有正常存入数据库",
                        "<h4>检查不存在的 Product!</h4> <br>" + errors.toString());
            }
        } catch(SQLException e) {
            Logger.error(Webs.S(e));
        } finally {
            try {
                if(psmt != null) psmt.close();
            } catch(Exception e) {
                e.printStackTrace();
            }
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
        PreparedStatement psmt = null;
        try {
            psmt = DB.getConnection().prepareStatement(
                    "UPDATE OrderItem SET discountPrice=?, price=?, currency=?," +
                            " listingName=?, quantity=?, usdCost=?," +
                            " market=?, promotionIDs=?, giftWrap=?," +
                            " createDate=?" +
                            " WHERE id=?"
            );

            int i = 1;
            StringBuilder errors = new StringBuilder("");
            List<String> orderIds = new ArrayList<String>();
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
                psmt.setTimestamp(i++, new Timestamp(orderItem.order.paymentDate == null ?
                        orderItem.createDate.getTime() : orderItem.order.paymentDate.getTime())
                );
                psmt.setString(i, orderItem.id);
                psmt.addBatch();
                i = 1;

                orderIds.add(orderItem.id);
            }
            int[] results = psmt.executeBatch();
            Logger.info("Batch inser %s OrderItem. [%s](%s)",
                    orderItems.size(), Webs.intArrayString(results), results.length
            );
            if(errors.length() > 0) {
                Webs.systemMail("[更新] OrderItem 的时候, 有如下 OrderItem 没有正常存入数据库",
                        "<h4>检查不存在的 Product!</h4> <br>" +
                                errors.toString() + "<br><br>" +
                                "All OrderItems: " + StringUtils.join(orderIds, "<br>")
                );
            }
        } catch(SQLException e) {
            Logger.error(Webs.S(e));
        } finally {
            try {
                if(psmt != null) psmt.close();
            } catch(Exception e) {
                e.printStackTrace();
            }
        }
    }

    private static boolean orderItemValidate(OrderItem orderItem, StringBuilder errors) {
        if(orderItem.product == null || orderItem.selling == null) {
            errors.append("Order(").append(orderItem.order.orderId)
                    .append(") have no product or selling [").append(orderItem.memo).append("]")
                    .append(" Market[").append(orderItem.market).append("]")
                    .append(" Product[").append(orderItem.product).append("]")
                    .append(" Selling[").append(orderItem.selling).append("]")
                    .append(" memo[").append(orderItem.memo).append("]")
                    .append("<br><br>");
            return false;
        }
        return true;
    }
}
