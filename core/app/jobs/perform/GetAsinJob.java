package jobs.perform;

import ext.LinkHelper;
import helper.HTTP;
import jobs.driver.BaseJob;
import models.market.Account;
import models.market.Selling;
import org.apache.commons.lang3.math.NumberUtils;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import play.utils.FastRuntimeException;


/**
 * 用于获取提交 Feed 后获取 Amazon 生成的 ASIN
 * User: wyatt
 * Date: 12/19/13
 * Time: 5:31 PM
 */
public class GetAsinJob extends BaseJob {
    @Override
    public void doit() {
        /**
         * 1. 获取账户模拟登陆到 Amazon 的后台
         * 2. 获取 UPS 去搜索对应的Listing, 并获取返回的 html 页面
         * 3. 处理 html 结果(三种情况: 没有搜索到结果 搜索到多条结果 搜索到正确一条结果)
         */
        if(getContext().get("account.id") == null)
            throw new FastRuntimeException("没有提交 account.id 信息, 不知道使用哪个销售账户去登陆.");
        if(getContext().get("sellingId") == null)
            throw new FastRuntimeException("没有提交 sellingId 信息");

        Account account = Account.findById(NumberUtils.toLong(getContext().get("account.id").toString()));
        String sellingId = getContext().get("sellingId").toString();
        Selling selling = Selling.findById(sellingId);
        Document doc = Jsoup.parse(HTTP.get(account.cookieStore(), LinkHelper.searchAsinByUPCLink(selling)));

    }
}
