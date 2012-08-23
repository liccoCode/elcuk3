package jobs;

import helper.FLog;
import helper.HTTP;
import helper.Webs;
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
 * 通过 Http 的方式去 Amazon 后台寻找丢失的信息;
 * 每隔 10 分钟执行一次;
 * User: wyattpan
 * Date: 4/20/12
 * Time: 5:17 PM
 */
public class OrderInfoFetchJob extends Job {
    @Override
    public void doJob() {
        /**
         * 1. 加载 SHIPPED 状态的订单, 并且限制数量;
         */
        int size = 10;
        if(Play.mode.isProd()) size = 30; //调整成 30 个订单一次, 每 10 分钟一次;
        List<Orderr> orders = needCompleteInfoOrders(size);
        for(Orderr ord : orders) {
            try {
                if(ord.crawlUpdateTimes > 4) {
                    Logger.warn("Order|%s| crawl more then 4 times.", ord.orderId);
                    continue;
                }
                String html = OrderInfoFetchJob.fetchOrderDetailHtml(ord);
                OrderInfoFetchJob.orderDetailUserIdAndEmailAndPhone(ord, html).save();
            } catch(Exception e) {
                ord.crawlUpdateTimes++;
                ord.save();
                Logger.warn("Parse Order(%s) Info Error! [%s]", ord.orderId, Webs.E(e));
            }
        }
    }

    public static List<Orderr> needCompleteInfoOrders(int size) {
        /**
         * 1. userid, email, phone, address1 的检查
         * 2. crawlUpdateTimes 抓取次数不能太多的检查
         * 3. 从最老的开始处理.
         * 4. 只需要抓取 SHIPPED 与 REFUNDED 的订单, 因为只有这两个状态才有这些数据
         */
        return Orderr.find("crawlUpdateTimes<4 AND state IN (?,?) AND (userid is null OR email is null OR phone is null OR address1 is null OR address1='') order by createDate",
                Orderr.S.SHIPPED, Orderr.S.REFUNDED).fetch(size);
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
     * 5. address1
     */
    public static Orderr orderDetailUserIdAndEmailAndPhone(Orderr order, String html) {
        Document doc = Jsoup.parse(html);
        order.crawlUpdateTimes++;
        Element lin = doc.select("#_myo_buyerEmail_progressIndicator").first();
        if(lin == null) {
            // 找不到上面的记录的时候, 将这个订单的警告信息记录在 memo 中
            lin = doc.select("#_myoV2PageTopMessagePlaceholder").first();
            order.state = Orderr.S.CANCEL;
            order.memo = lin.text();
        } else {
            Elements smallers = doc.select(".smaller");
            for(Element smaller : smallers) {
                String text = smaller.text().toLowerCase();
                if(StringUtils.contains(text, "erstattung"/*DE*/) || StringUtils.contains(text, "refund"/*uk*/)) {
                    Logger.info("Found Order %s is from %s to %s", order.orderId, order.state, Orderr.S.REFUNDED);
                    order.state = Orderr.S.REFUNDED;
                }
            }
        }

        if(order.state == Orderr.S.SHIPPED || order.state == Orderr.S.REFUNDED) {
            // Email
            if(StringUtils.isBlank(order.email) || !StringUtils.contains(order.email, "@")) {
                String tmp = StringUtils.remove(StringUtils.substringBetween(html, "buyerEmail:", "targetID:").trim(), "\"");
                order.email = StringUtils.remove(tmp, ",");
                if(StringUtils.isNotBlank(order.email)) order.email = order.email.trim();
            }

            // buyerId
            String url = lin.parent().select("a").attr("href");
            String[] args = StringUtils.split(url, "&");
            for(String pa : args) {
                if(!StringUtils.containsIgnoreCase(pa, "buyerID")) continue;
                order.userid = StringUtils.split(pa, "=")[1].trim();
                break;
            }

            // Phone
            if(StringUtils.isBlank(order.phone))
                order.phone = StringUtils.substringBetween(html, "Phone:", "</td>");
            if(StringUtils.isNotBlank(order.phone)) order.phone = order.phone.trim();

            // Address1 (重复地址, 作为参数)
            if(StringUtils.isBlank(order.address1))
                order.address1 = StringUtils.substringBetween(html, "<strong>Shipping Address</strong>", "Phone");
            if(StringUtils.isNotBlank(order.address1)) {
                order.address1 = StringUtils.replace(order.address1, "<br />", "\r\n").trim();
                order.address1 = StringUtils.replace(order.address1, "<br>", "\r\n").trim();
            }

        }
        return order;
    }
}
