package jobs;

import helper.HTTP;
import helper.Webs;
import models.market.Orderr;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
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
        if(Play.mode.isProd()) size = 20; //调整成 20 个订单一次, 每 10 分钟一次;
        List<Orderr> orders = Orderr.find("state=? AND (userid is null OR userid='') order by createDate desc", Orderr.S.SHIPPED).fetch(size);
        for(Orderr ord : orders) {
            try {
                String url = ord.account.type.orderDetail(ord.orderId);
                Logger.info("OrderInfo fetch [%s].", url);
                String html = HTTP.get(url);
                Document doc = Jsoup.parse(html);
                ord.orderInfoParse(doc);
            } catch(Exception e) {
                Logger.warn("Parse Order Info Error! [%s]", Webs.E(e));
            }
        }
    }
}
