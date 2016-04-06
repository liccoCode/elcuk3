package jobs;

import helper.FLog;
import helper.HTTP;
import helper.LogUtils;
import helper.Webs;
import models.Jobex;
import models.market.Orderr;
import org.apache.commons.lang.StringUtils;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import play.Logger;
import play.Play;
import play.jobs.Job;

import java.util.List;

/**
 * <pre>
 * 通过 Http 的方式去 Amazon 后台寻找丢失的信息;
 * 每隔 10 分钟执行一次;
 * 周期:
 * - 轮询周期: 1mn
 * - Duration: 2mn
 * </pre>
 * User: wyattpan
 * Date: 4/20/12
 * Time: 5:17 PM
 * @deprecated
 */
public class OrderInfoFetchJob extends Job {
    @Override
    public void doJob() {
        long begin = System.currentTimeMillis();
        if(!Jobex.findByClassName(OrderInfoFetchJob.class.getName()).isExcute()) return;
        /**
         * 1. 加载 SHIPPED 状态的订单, 并且限制数量;
         */
        int size = 10;
        if(Play.mode.isProd()) size = 30; //调整成 30 个订单一次, 每 10 分钟一次;
        List<Orderr> orders = OrderInfoFetchJob.needCompleteInfoOrders(size);
        for(Orderr ord : orders) {
            try {
                String html = OrderInfoFetchJob.fetchOrderDetailHtml(ord);
                OrderInfoFetchJob.orderDetailUserIdAndEmailAndPhone(ord, html);
                ord.save();
            } catch(Exception e) {
                Logger.warn("Parse Order(%s) Info Error! email:%s, userId:%s, phone:%s. [%s]",
                        ord.orderId, ord.email, ord.userid, ord.phone, Webs.S(e));
            }
        }
        if(LogUtils.isslow(System.currentTimeMillis() - begin, "OrderInfoFetchJob")) {
            LogUtils.JOBLOG
                    .info(String.format("OrderInfoFetchJob calculate.... [%sms]", System.currentTimeMillis() - begin));
        }
    }

    // TODO 性能有问题
    public static List<Orderr> needCompleteInfoOrders(int size) {
        /**
         * 1. userid, email, phone 的检查
         * 2. 从最老的开始处理.
         * 3. 只需要抓取 SHIPPED 与 REFUNDED 的订单, 因为只有这两个状态才有这些数据
         */
        String sql = "state IN (?,?) AND (userid is null OR email is null OR phone is null)";
        List<Orderr> orders = Orderr.find(sql + " ORDER BY createDate DESC",
                Orderr.S.SHIPPED, Orderr.S.REFUNDED).fetch(size);
        Logger.info("OrderInfoFetchJob fetch %s orders.", orders.size());
        return orders;
    }

    public static String fetchOrderDetailHtml(Orderr ord) {
        String url = ord.account.type.orderDetail(ord.orderId);
        Logger.info("OrderInfo(UserId) [%s].", url);
        String html = HTTP.get(ord.account.cookieStore(), url);
        if(Play.mode.isDev())
            FLog.fileLog(String.format("order.detail.%s.html", ord.orderId), html, FLog.T.HTTP_ERROR);
        return html;
    }

    /**
     * 通过 HTTP 方式到 Amazon 后台进行订单信息的补充:
     * 1. check order state
     * 2. userId
     * 3. email
     * 4. phone
     */
    public static Orderr orderDetailUserIdAndEmailAndPhone(Orderr order, String html) {
        Document doc = Jsoup.parse(html);
        Element lin = doc.select("#_myo_buyerEmail_progressIndicator").first();
        if(lin == null) {
            // 找不到上面的记录的时候, 将这个订单的警告信息记录在 memo 中
            lin = doc.select("#_myoV2PageTopMessagePlaceholder").first();
            if(lin != null && StringUtils.isNotBlank(lin.text()) && StringUtils.contains(lin.text().toLowerCase(),
                    "cancelled") ||
                    (lin != null && StringUtils.contains(lin.text().toLowerCase(), "storniert"))/*德语*/) {
                Logger.info("Order %s state from %s to %s", order.orderId, order.state, Orderr.S.CANCEL);
                order.state = Orderr.S.CANCEL;
                order.memo = lin.text();
            }
        } else {
            Elements smallers = doc.select(".smaller");
            for(Element smaller : smallers) {
                String text = smaller.text().toLowerCase();
                if(StringUtils.contains(text, "erstattung"/*DE*/) ||
                        StringUtils.contains(text, "refund"/*uk*/)) {
                    Logger.info("Found Order %s is from %s to %s", order.orderId, order.state,
                            Orderr.S.REFUNDED);
                    order.state = Orderr.S.REFUNDED;
                    break;
                }
            }
        }

        if(order.state == Orderr.S.SHIPPED || order.state == Orderr.S.REFUNDED) {
            // Email
            if(StringUtils.isBlank(order.email)) {
                try {
                    String email = doc.select("[buyeremail]").attr("buyeremail");
                    if(StringUtils.isNotBlank(email)) order.email = email.replace("\"", "").trim();
                } catch(NullPointerException e) {
                    // 避免 email 解析出现 null point
                }
            }

            // buyerId
            String url = doc.select("tr.list-row table[class=data-display] a").attr("href");
            String[] args = StringUtils.split(url, "&");
            for(String pa : args) {
                try {
                    if(!StringUtils.containsIgnoreCase(pa, "buyerID")) continue;
                    order.userid = StringUtils.split(pa, "=")[1].trim();
                } catch(NullPointerException e) {
                    // 避免 userid 解析出 null point
                }
                break;
            }

            // Phone
            if(StringUtils.isBlank(order.phone)) {
                try {
                    Elements tables = doc.select("table.data-display");
                    if(tables.size() >= 2) {
                        String tableHtml = tables.get(1).outerHtml();
                        if(StringUtils.contains(tableHtml, "Phone:")) {
                            String phone = StringUtils.substringBetween(tableHtml, "Phone:", "</td>");
                            if(StringUtils.isNotBlank(phone)) order.phone = phone.trim();
                        } else {
                            // amazon 没有提供 phone,  那么不再一直抓取 phone
                            order.phone = "";
                        }
                    }
                } catch(NullPointerException e) {
                    // 避免 phone 解析出 null pint
                }
            }
        }
        return order;
    }
}
