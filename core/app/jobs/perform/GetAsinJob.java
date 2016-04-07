package jobs.perform;

import ext.LinkHelper;
import helper.Constant;
import helper.DBUtils;
import helper.HTTP;
import helper.LogUtils;
import jobs.driver.BaseJob;
import jobs.driver.GJob;
import models.User;
import models.market.Account;
import models.market.M;
import models.market.Selling;
import org.apache.commons.io.FileUtils;
import org.apache.commons.lang.StringUtils;
import org.apache.commons.lang3.math.NumberUtils;
import org.joda.time.DateTime;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import play.Play;
import play.utils.FastRuntimeException;

import java.io.File;
import java.util.Map;


/**
 * 用于获取提交 Feed 后获取 Amazon 生成的 ASIN
 * User: wyatt
 * Date: 12/19/13
 * Time: 5:31 PM
 * @deprecated
 */
public class GetAsinJob extends BaseJob {
    // 计数器，记录该Job被执行了多少次
    private int executeCount = 0;

    @SuppressWarnings("unchecked")
    @Override
    public void doit() {
        long begin = System.currentTimeMillis();
        /**
         * 1. 获取账户模拟登陆到 Amazon 的后台
         * 2. 获取 UPS 去搜索对应的Listing, 并获取返回的 html 页面
         * 3. 处理 html 结果(两种情况: 没有搜索到结果 搜索到正确的一条结果)
         */
        if(getContext().get("account.id") == null)
            throw new FastRuntimeException("没有提交 account.id 信息, 不知道使用哪个销售账户去登陆.");
        if(getContext().get("selling.id") == null)
            throw new FastRuntimeException("没有提交 sellingId 信息");
        if(getContext().get("executeCount") != null)
            executeCount = getExecuteCount();
        if(!isExecuteble())
            throw new FastRuntimeException("未找到合适的 ASIN 数据！");

        Account account = Account.findById(NumberUtils.toLong(getContext().get("account.id").toString()));
        Selling selling = Selling.findById(getContext().get("selling.id").toString());
        // ASIN 的长度为 10, UPC 的长度为 16, 当已经是 ASIN 则不再继续去获取一次 ASIN
        if(selling.asin.length() == 10) return;

        M.MID marketId = account.type.amid();
        if(getContext().get("marketId") != null)
            marketId = M.MID.valueOf(getContext().get("marketId").toString());

        Map nextContext = getContext();
        String asin = null;
        try {
            account.changeRegion(marketId.market()); // 模拟登陆去抓取, 需要使用这个支持同账户多市场
            Document doc = Jsoup.parse(HTTP
                    // 需要登陆到账户所在的后台, 不是 Selling 所在的后台
                    .get(account.cookieStore(), LinkHelper.searchAsinByUPCLink(account.type, selling.aps.upc)));
            if(Play.mode.isDev()) {
                try {
                    File file = new File(
                            String.format("%s/deploy/%s", Constant.E_LOGS, marketId + "_" + System.currentTimeMillis()));
                    FileUtils.write(file, doc.outerHtml(), "ISO8859-1");
                } catch(Exception e) {
                    throw new FastRuntimeException(e);
                }
            }
            Elements tables = doc.select("table.manageTable");
            Elements trs = tables.select("tr");
            for(Element tr : trs) {
                String merchantSKU = tr.select("td:eq(4)").text();
                asin = tr.select("td:eq(5)").select("a").text();
                if(asin.length() == 10) {
                    /**
                     * 1. 验证抓取的 SKU 与数据库内的 Selling 对象的 SKU 是否匹配
                     * 2. 更新 Listing 的Id (更改为 [asin]_[market])
                     * 3. 更新 Selling 的 ASIN 值
                     */
                    if(StringUtils.containsIgnoreCase(selling.merchantSKU, merchantSKU)) {
                        selling.asin = asin;
                        selling.save();
                        // 因为外键约束, 所以这里需要关闭 MySQL 的外键检查, 然后再进行 listingId 切换,
                        // 同时需要将直接关联的 Selling.listing_listingId 外键进行更换.
                        // TODO: 因为 Listing 可能会涉及到很多其他的外键关联, 是否考虑将 listingId 更换为 UPC_Market?
                        DBUtils.execute("SET foreign_key_checks=0");
                        String listingId = String.format("%s_%s", asin, selling.market.toString());
                        DBUtils.execute(String.format(
                                "UPDATE Selling SET listing_listingId='%s', state='%s' WHERE sellingId='%s'",
                                listingId, Selling.S.SELLING.name(), selling.sellingId));
                        DBUtils.execute(
                                String.format("UPDATE Listing SET listingId='%s', asin='%s' WHERE listingId='%s'",
                                        listingId, asin,
                                        String.format("%s_%s", selling.aps.upc, selling.market.toString())));
                        DBUtils.execute("SET foreign_key_checks=1");
                        //提示操作人员任务已经处理完成
                        User user = User.findById(NumberUtils.toLong(getContext().get("user.id").toString()));
                        selling.refresh();
                        noty("您提交的上架请求已经处理完成,请检查 ^_^", user);
                        selling.refresh();

                    }
                }
            }

            if(StringUtils.isBlank(asin)) {
                setExecuteCount(++executeCount);
                GJob.perform(GetAsinJob.class.getName(), nextContext, DateTime.now().plusMinutes(2).toDate());
            }
        } catch(Exception e) {
            /**
             * 1. 出现异常，重新添加一个新的job任务
             * 2. 将计数器加 1
             */
            GJob.perform(GetAsinJob.class.getName(), nextContext, DateTime.now().plusMinutes(2).toDate());
            throw new FastRuntimeException(e.getMessage());
        } finally {
            // 还原账户 Region
            account.changeRegion(account.type);
        }
        if(LogUtils.isslow(System.currentTimeMillis() - begin, "GetAsinJob")) {
            LogUtils.JOBLOG.info(String.format("GetAsinJob calculate.... [%sms]", System.currentTimeMillis() - begin));
        }
    }

    public boolean isExecuteble() {
        return this.executeCount < 5;
    }

    @SuppressWarnings("unchecked")
    public void setExecuteCount(int count) {
        getContext().put("executeCount", count);
    }

    public int getExecuteCount() {
        return NumberUtils.toInt(getContext().get("executeCount").toString());
    }

    /**
     * 用于上架成功后提示用户处理完成
     *
     * @param content 内容
     * @param user    提示对象
     */
    public void noty(String content, User user) {
        //Notification.newSystemNoty(content, String.format("%s/feed/%s/show", Constant.ROOT_PATH,
        //        getContext().get("feed.id"))).notifySomeone(user);
    }
}
