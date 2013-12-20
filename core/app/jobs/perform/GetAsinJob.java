package jobs.perform;

import ext.LinkHelper;
import helper.HTTP;
import jobs.driver.BaseJob;
import jobs.driver.GJob;
import models.market.Account;
import models.market.Listing;
import models.market.Selling;
import org.apache.commons.lang3.math.NumberUtils;
import org.elasticsearch.common.joda.time.DateTime;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import play.utils.FastRuntimeException;
import java.util.Map;


/**
 * 用于获取提交 Feed 后获取 Amazon 生成的 ASIN
 * User: wyatt
 * Date: 12/19/13
 * Time: 5:31 PM
 */
public class GetAsinJob extends BaseJob {

    @SuppressWarnings("unchecked")
    @Override
    public void doit() {
        // 计数器，记录该Job被执行了多少次
        int excuteCount = 0;

        /**
         * 1. 获取账户模拟登陆到 Amazon 的后台
         * 2. 获取 UPS 去搜索对应的Listing, 并获取返回的 html 页面
         * 3. 处理 html 结果(两种情况: 没有搜索到结果 搜索到正确的一条结果)
         */
        if (getContext().get("account.id") == null)
            throw new FastRuntimeException("没有提交 account.id 信息, 不知道使用哪个销售账户去登陆.");
        if (getContext().get("sellingId") == null)
            throw new FastRuntimeException("没有提交 sellingId 信息");
        if (getContext().get("excuteCount") != null) {
            excuteCount = NumberUtils.toInt(getContext().get("excuteCount").toString());
            if ("3".contains(getContext().get("excuteCount").toString())) {
                throw new FastRuntimeException("未找到合适的 ASIN 数据！");
            }
        }

        Account account = Account.findById(NumberUtils.toLong(getContext().get("account.id").toString()));
        String sellingId = getContext().get("sellingId").toString();
        Selling selling = Selling.findById(sellingId);
        Document doc = Jsoup.parse(HTTP.get(account.cookieStore(), LinkHelper.searchAsinByUPCLink(selling)));
        Map nextContext = getContext();
        String asin = null;
        try {
            Elements trResults = doc.select(".data-display manageTable").select("tr");
            for (Element tr : trResults) {
                asin = tr.select("td.content > a").first().text();
                if (asin.length() == 10) {
                    /**
                     * 1. 更新 Listing 的Id (更改为 [asin]_[market])
                     * 2. 更新 Selling 的 ASIN 值
                     */
                    selling.asin = asin;
                    Listing lst = Listing.findById(Listing.lid(selling.asin, selling.market));
                    if (lst != null) {
                        lst.asin = asin;
                        lst.save();
                    }
                    selling.save();
                }
            }
        } catch (Exception e) {
            /**
             * 1. 出现异常，重新添加一个新的job任务
             * 2. 将计数器加1
             */
            ++excuteCount;
            nextContext.put("excuteCount", excuteCount);
            GJob.perform(GetAsinJob.class.getName(), nextContext, DateTime.now().plusMinutes(2).toDate());
            throw new FastRuntimeException("解析html文档时发生异常");
        }
        if (asin == null || "".contains(asin.trim())) {
            ++excuteCount;
            nextContext.put("excuteCount", excuteCount);
            GJob.perform(GetAsinJob.class.getName(), nextContext, DateTime.now().plusMinutes(2).toDate());
        }
    }
}
