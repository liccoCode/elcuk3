package models.market;

import com.amazonservices.mws.FulfillmentInboundShipment._2010_10_01.model.Address;
import com.google.gson.annotations.Expose;
import ext.LinkHelper;
import helper.Constant;
import helper.FLog;
import helper.HTTP;
import helper.Webs;
import org.apache.commons.io.FileUtils;
import org.apache.commons.lang.StringUtils;
import org.apache.http.NameValuePair;
import org.apache.http.client.CookieStore;
import org.apache.http.cookie.Cookie;
import org.apache.http.impl.client.BasicCookieStore;
import org.apache.http.message.BasicNameValuePair;
import org.hibernate.annotations.Cache;
import org.hibernate.annotations.CacheConcurrencyStrategy;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import play.Logger;
import play.Play;
import play.data.validation.Equals;
import play.data.validation.Required;
import play.db.jpa.Model;
import play.libs.F;
import play.utils.FastRuntimeException;

import javax.persistence.*;
import java.io.File;
import java.io.IOException;
import java.util.*;

/**
 * 不同的账户, Market Place 可以相同, 但是 Account 不一定相同.
 * User: wyattpan
 * Date: 1/6/12
 * Time: 4:39 PM
 */
@Entity
@Cache(usage = CacheConcurrencyStrategy.READ_WRITE)
public class Account extends Model {
    private static Map<String, String> MERCHANT_ID;

    /**
     * 必须把每个 Account 对应的 CookieStore 给缓存起来, 否则重新加载的 Account 对象没有登陆过的 CookieStore 了
     */
    private static Map<String, CookieStore> COOKIE_STORE_MAP;

    /**
     * 当使用 MERCHANT_ID 为 final 的时候, 在 OnStartUP 中的 loadModules 初始化报错...
     *
     * @return
     */
    public static Map<String, String> merchant_id() {
        if(MERCHANT_ID == null) {
            MERCHANT_ID = new HashMap<String, String>();
            MERCHANT_ID.put("A2OAJ7377F756P", "Amazon Warehouse Deals"); //UK
            MERCHANT_ID.put("A8KICS1PHF7ZO", "Amazon Warehouse Deals"); //DE
            MERCHANT_ID.put("A2L77EE7U53NWQ", "Amazon Warehouse Deals"); //US
        }
        //TODO 其实市场的以后看到再添加进来
        return MERCHANT_ID;
    }

    public static Map<String, CookieStore> cookieMap() {
        if(COOKIE_STORE_MAP == null) COOKIE_STORE_MAP = new HashMap<String, CookieStore>();
        return COOKIE_STORE_MAP;
    }


    /**
     * 用于限制唯一性的字段; [type]_[username]
     */
    @Column(unique = true, nullable = false)
    @Expose
    public String uniqueName;

    /**
     * 哪一个市场
     */
    @Required
    @Column(nullable = false)
    @Enumerated(EnumType.STRING)
    @Expose
    public M type;

    /**
     * 市场登陆用户名
     */
    @Required
    @Column(nullable = false)
    @Expose
    public String username;

    /**
     * 市场登陆的密码
     */
    @Required
    @Column(nullable = false)
    @Expose
    public String password;

    @Transient
    @Equals(value = "password", message = "account.eq")
    public String confirm;

    /**
     * 不同市场所拥有的钥匙;
     * Secret Key; [Amazon]
     * Toekn; [Ebay]
     */
    @Expose
    public String token;
    @Expose
    public String accessKey;

    /**
     * Amazon MerchantID
     */
    @Expose
    public String merchantId;

    /**
     * 是否可用
     */
    @Expose
    public boolean closeable = false;

    /**
     * 记录这个 Account 是否为登陆操作使用的账号.
     */
    @Expose
    public boolean isSaleAcc = false;


    // 判断账户是否可以点击这几个地区的 Review
    @Expose
    public boolean isAUK = false;

    @Expose
    public boolean isADE = false;

    @Expose
    public boolean isAUS = false;

    /**
     * 将 CookieStore 按照 Account 区分开来以后, 那么在系统中对应的 sellercentral.amazon.co.uk 可以有多个 Account 登陆 , 他们的 Cookie 各不影响
     * 添加 Market 参数, 同一个账号可以再不同市场登陆
     *
     * @return
     */
    public CookieStore cookieStore() {
        return cookieStore(null);
    }

    /**
     * 将 CookieStore 按照 Account 区分开来以后, 那么在系统中对应的 sellercentral.amazon.co.uk 可以有多个 Account 登陆 , 他们的 Cookie 各不影响
     * 添加 Market 参数, 同一个账号可以再不同市场登陆
     *
     * @return
     */
    public CookieStore cookieStore(M market) {
        if(market == null) market = this.type;
        String key = cookieKey(this.id, market);
        if(!cookieMap().containsKey(key))
            cookieMap().put(key, new BasicCookieStore());
        return cookieMap().get(key);
    }

    public String cookie(String name) {
        return cookie(name, null);
    }

    public String cookie(String name, M market) {
        for(Cookie cookie : this.cookieStore(market).getCookies()) {
            if(name.equalsIgnoreCase(cookie.getName()))
                return cookie.getValue();
        }
        return null;
    }

    public void setType(M type) {
        this.type = type;
        if(this.type != null && this.username != null)
            this.uniqueName = String.format("%s_%s", this.type.toString(), this.username);
    }

    public void setUsername(String username) {
        this.username = username;
        if(this.type != null && this.username != null)
            this.uniqueName = String.format("%s_%s", this.type.toString(), this.username);
    }

    /**
     * 销售账号需要登陆的后台系统
     */
    public void loginAmazonSellerCenter() {
        switch(this.type) {
            case AMAZON_UK:
            case AMAZON_DE:
            case AMAZON_FR:
            case AMAZON_US:
                String body = "";
                try {
                    /**
                     * 1. Visit the website, fetch the new Cookie.
                     * 2. With the website params and user/password to login.
                     */
                    body = HTTP.get(this.cookieStore(), this.type.sellerCentralHomePage());

                    if(Play.mode.isDev())
                        FileUtils.writeStringToFile(new File(Constant.HOME + "/elcuk2-logs/" + this.type.name() + ".id_" + this.id + ".homepage.html"), body);

                    Document doc = Jsoup.parse(body);
                    Elements inputs = doc.select("form[name=signin] input");

                    if(inputs.size() == 0) {
                        Logger.info("WebSite [" + this.type.toString() + "] Still have the Session with User [" + this.username + "].");
                        return;
                    }

                    List<NameValuePair> params = new ArrayList<NameValuePair>();
                    for(Element el : inputs) {
                        String att = el.attr("name");
                        if("email".equals(att)) params.add(new BasicNameValuePair(att, this.username));
                        else if("password".equals(att)) params.add(new BasicNameValuePair(att, this.password));
                        else params.add(new BasicNameValuePair(att, el.val()));
                    }
                    body = HTTP.post(this.cookieStore(), this.type.sellerCentralLogIn(), params);
                    if(Play.mode.isDev())
                        FileUtils.writeStringToFile(new File(Constant.HOME + "/elcuk2-logs/" + this.type.name() + ".id_" + this.id + ".afterLogin.html"), body);
                    Element navBar = Jsoup.parse(body).select("#topNavContainer").first();
                    if(navBar != null) Logger.info("%s Seller Central Login Successful!", this.prettyName());
                    else Logger.warn("%s Seller Central Login Failed!", this.prettyName());

                    HTTP.client().getCookieStore().clearExpired(new Date());
                } catch(Exception e) {
                    try {
                        FileUtils.writeStringToFile(new File(Constant.HOME + "/elcuk2-logs/" + this.type.name() + ".id_" + this.id + ".error.html"), body);
                    } catch(IOException e1) {
                        //ignore.
                    }
                    Logger.warn(Webs.E(e));
                }
                break;
            default:
                Logger.warn("Right now, can only login Amazon(UK,DE,FR) Seller Central. " + this.type + " is not support!");
        }
    }

    /**
     * 非销售账号在 Amazon 的前台登陆,<br>
     * 支持同账户登陆多个市场, 例如: 一个账户登陆 Uk/DE
     */
    public boolean loginAmazonSize(M market) {
        switch(this.type) {
            case AMAZON_UK:
            case AMAZON_DE:
            case AMAZON_FR:
            case AMAZON_US:
                /**
                 * 0. clear old cookies
                 * 1. Visit the website, fetch the new Cookie.
                 * 2. With the website params and user/password to login.
                 */
                if(market == null) market = this.type;
                this.cookieStore(market).clear();
                String body = HTTP.get(this.cookieStore(market), market.amazonSiteLogin());
                Document doc = Jsoup.parse(body);
                Elements inputs = doc.select("#ap_signin_form input");

                if(inputs.size() == 0) {
                    Logger.info("WebSite [" + market.toString() + "] Still have the Session with User [" + this.username + "].");
                    FLog.fileLog(String.format("%s.Login.html", this.prettyName()), body, FLog.T.HTTP_ERROR);
                    return false;
                }

                Set<NameValuePair> params = new HashSet<NameValuePair>();
                for(Element el : inputs) {
                    String att = el.attr("name");
                    if("email".equals(att)) params.add(new BasicNameValuePair(att, this.username));
                    else if("password".equals(att)) params.add(new BasicNameValuePair(att, this.password));
                    else if("create".equals(att)) params.add(new BasicNameValuePair(att, "0"));//登陆
                    else params.add(new BasicNameValuePair(att, el.val()));
                }
                body = HTTP.post(this.cookieStore(market), doc.select("#ap_signin_form").first().attr("action"), params);
                boolean isLogin = Account.isLoginFront(Jsoup.parse(body));
                if(Play.mode.isDev())
                    FLog.fileLog(String.format("%s.afterLogin.html", this.prettyName()), body, FLog.T.HTTP_ERROR);
                boolean loginSucc = false;
                if(isLogin) {
                    Logger.info("%s Amazon Site Login Successful!", this.prettyName());
                    loginSucc = true;
                } else {
                    Logger.warn("%s Amazon Site Login Failed!", this.prettyName());
                    FLog.fileLog(String.format("%s.afterLogin.html", this.prettyName()), body, FLog.T.HTTP_ERROR);
                    loginSucc = false;
                }
                HTTP.client().getCookieStore().clearExpired(new Date());
                return loginSucc;
            default:
                Logger.warn("Right now, can only login Amazon(UK,DE,FR) Site." + market + " is not support!");
        }
        return false;
    }

    /**
     * 只有 Amazon EU 才会有的转换区域.
     *
     * @param m
     */
    public void changeRegion(M m) {
        if(this.type == M.AMAZON_US ||
                this.type == M.EBAY_UK) return;
        String url = "Account.changeRegion.";
        try {
            url = this.type.changeRegion(m.amid().name());
            HTTP.get(this.cookieStore(), url);
        } catch(Exception e) {
            throw new FastRuntimeException(String.format("Invoke %s with error.[%s]", url, Webs.E(e)));
        }
    }


    /**
     * 返回这个市场所对应的 MarketPlaceId, 仅仅支持 UK/DE/FR, 其他市场默认返回 UK 的
     *
     * @return
     */
    public M.MID marketplaceId() {
        switch(this.type) {
            case AMAZON_UK:
                return M.MID.A1F83G8C2ARO7P;
            case AMAZON_DE:
                return M.MID.A1PA6795UKMFR9;
            case AMAZON_FR:
                return M.MID.A13V1IB3VIYZZH;
            case AMAZON_US:
                return M.MID.ATVPDKIKX0DER;
            default:
                return M.MID.A1F83G8C2ARO7P;
        }
    }

    /**
     * 此 Account 点击此 Listing 的 like 按钮
     *
     * @param listing
     */
    public F.T2<AmazonLikeRecord, String> clickLike(Listing listing) {
        String sessionId = this.cookie("session-id", listing.market);
        if(sessionId == null) {// 没有 session-id, 即没有登陆, 则尝试登陆一次..
            this.loginAmazonSize(listing.market);
            sessionId = this.cookie("session-id", listing.market);
        }
        String body = HTTP.post(this.cookieStore(listing.market), listing.market.amazonLikeLink(), Arrays.asList(
                new BasicNameValuePair("action", "like"),
                new BasicNameValuePair("itemId", listing.asin),
                new BasicNameValuePair("context", "dp"),
                new BasicNameValuePair("itemType", "asin"),
                new BasicNameValuePair("sessionId", sessionId)

        ));
        boolean success = StringUtils.contains(body, "\"success\":true");
        AmazonLikeRecord likeRecord = new AmazonLikeRecord(listing, this);
        if(success) likeRecord.save();
        else Logger.warn("%s Click %s %s Like Failed.", this.prettyName(), listing.market, listing.asin);
        return new F.T2<AmazonLikeRecord, String>(likeRecord, body);
    }

    /**
     * 此账号点击这个 Review, 点击 Up 或者 Down;
     * 由于 Amazon 不会给与是否点击成功的返回, 所以无法确认是否点击成功, 但仅能知道已经点击了, 具体的信息需要更新 Review 来查看
     *
     * @param isUp   是否点击 Up? 否则点击 Down
     * @param review
     */
    public F.T2<AmazonReviewRecord, String> clickReview(AmazonListingReview review, boolean isUp) {
        /**
         * 1. 访问 Review 页面, 检查是否需要登陆.
         * 2. 解析出登陆后的点击链接.
         * 3. Click 这个链接 ^_^
         */
        F.T3<Boolean, String, String> loginAndClicks = checkLoginAndFetchClickLinks(review);
        if(!loginAndClicks._1) { // 没有登陆则登陆, 只尝试一次登陆!
            synchronized(this.cookieStore(review.listing.market)) {
                this.loginAmazonSize(review.listing.market);
            }
            loginAndClicks = checkLoginAndFetchClickLinks(review);
        }
        String content;
        if(isUp) {
            Logger.info("%s|%s Click link: %s", this.id, this.prettyName(), loginAndClicks._2);
            content = HTTP.get(this.cookieStore(review.listing.market), loginAndClicks._2);
        } else {
            Logger.info("%s|%s Click link: %s", this.id, this.prettyName(), loginAndClicks._3);
            content = HTTP.get(this.cookieStore(review.listing.market), loginAndClicks._3);
        }
        AmazonReviewRecord record = new AmazonReviewRecord(review, this, isUp);
        // 只有后面登陆成功了, 才允许记录 Record
        if(loginAndClicks._1) record.save();
        else Logger.warn("Not Login? %s, %s", this.prettyName(), this.password);
        return new F.T2<AmazonReviewRecord, String>(record, content);
    }

    /**
     * 访问 Review 的页面, 检查是否登陆, 并且解析出点击 Up 与 Down 的两个链接.
     *
     * @param review
     * @return
     */
    private F.T3<Boolean, String, String> checkLoginAndFetchClickLinks(AmazonListingReview review) {
        String html = HTTP.get(this.cookieStore(review.listing.market), LinkHelper.reviewLink(review));
        Document doc = Jsoup.parse(html);
        // 账号登陆以后, 链接中才会有 sign-out 字符串
        Elements els = doc.select(".votingButtonReviews");
        String[] upAndDownLink = new String[2];
        for(Element el : els) {
            String link = el.attr("href");
            if("1".equals(StringUtils.substringBetween(link, "Helpful/", "/ref=cm"))) upAndDownLink[0] = link;
            else upAndDownLink[1] = link;
        }
        boolean isLogin = Account.isLoginFront(doc);
        // 这里检查点击不成功的原因
        if(!isLogin)
            FLog.fileLog(String.format("%s.clickReiew.%s.%s.Failed.html", this.prettyName(), review.reviewId, review.listing.market), html, FLog.T.HTTP_ERROR);
        // 将另外一个错误分开记录文件
        if(upAndDownLink[0] == null || upAndDownLink[1] == null)
            FLog.fileLog(String.format("%s.URL_NULL.%s.%s.Failed.html", this.prettyName(), review.reviewId, review.listing.market), html, FLog.T.HTTP_ERROR);

        return new F.T3<Boolean, String, String>(isLogin, upAndDownLink[0], upAndDownLink[1]);
    }

    /**
     * 判断此 Doc 页面是否成功登陆前台
     *
     * @param doc
     * @return
     */
    public static boolean isLoginFront(Document doc) {
        Element oldAmazon = doc.select("#navidWelcomeMsg").first();
        if(oldAmazon != null) {
            String navidWelcomeMsgStr = doc.select("#navidWelcomeMsg").outerHtml();
            return StringUtils.contains(navidWelcomeMsgStr, "sign-out") ||
                    StringUtils.contains(navidWelcomeMsgStr, "signout");
        } else {
            String nav_your_account_flyoutStr = doc.select("#nav_your_account_flyout").outerHtml();
            return StringUtils.contains(nav_your_account_flyoutStr, "sign-out") ||
                    StringUtils.contains(nav_your_account_flyoutStr, "signout") ||
                    StringUtils.contains(nav_your_account_flyoutStr, "Sign Out");
        }
    }

    /**
     * 判断后端是否登陆
     *
     * @param doc
     * @return
     */
    public static boolean isLoginEnd(Document doc) {
        return doc.select("#sc_quicklinks_top").first() != null;
    }

    @Override
    public String toString() {
        return StringUtils.split(this.uniqueName, "@")[0];
    }

    public String prettyName() {
        return String.format("%s.%s", this.type.nickName(), this.username.split("@")[0]);
    }

    /**
     * 所有打开的 Review 账号
     *
     * @param market 需要哪一个市场的可点击 Review Account, 如果设置为 null, 则返回全部
     * @return
     */
    public static List<Account> openedAmazonClickReviewAndLikeAccs(M market) {
        switch(market) {
            case AMAZON_UK:
                return Account.find("closeable=? AND isSaleAcc=? AND isAUK=? ORDER BY id", false, false, true).fetch();
            case AMAZON_DE:
                return Account.find("closeable=? AND isSaleAcc=? AND isADE=? ORDER BY id", false, false, true).fetch();
            case AMAZON_US:
                return Account.find("closeable=? AND isSaleAcc=? AND isAUS=? ORDER BY id", false, false, true).fetch();
            default:
                return Account.find("closeable=? AND isSaleAcc=? ORDER BY id", false, false).fetch();
        }
    }

    /**
     * 所有打开的销售账号
     *
     * @return
     */
    public static List<Account> openedSaleAcc() {
        return Account.find("closeable=? AND isSaleAcc=? ORDER BY id", false, true).fetch();
    }

    /**
     * 构造放在 Account Cookie_Store_Map 中的 KEY
     *
     * @param aid
     * @param market
     * @return
     */
    public static String cookieKey(long aid, M market) {
        return String.format("ACC_COOKIE_%s_%s", aid, market);
    }

    /**
     * 通过账户获取 FBA 的发货地址
     *
     * @param type
     * @return
     */
    public static Address address(M type) {
        switch(type) {
            case AMAZON_UK:
            case AMAZON_DE:
                return new Address("EasyAcc", "Basement Flat 203 Kilburn high road", null, null, "London", "LONDON", "UK", "NW6 7HY");
            case AMAZON_US:
                return new Address("EasyAcc", "Basement Flat 203 Kilburn high road", null, null, "London", "LONDON", "UK", "NW6 7HY");
        }
        return null;
    }


    /**
     * 初始化 Account 相关的业务;
     * 1. 将 MerchantID 持久在内存中
     * 2. 登陆 Account 账户
     */
    public static void init() {
        synchronized(Account.class) {
            List<Account> accs = Account.openedSaleAcc();
            for(Account ac : accs) {
                if(ac.isSaleAcc) merchant_id().put(ac.merchantId, ac.uniqueName);
                Logger.info(String.format("Login %s with account %s.", ac.type, ac.username));
                ac.loginAmazonSellerCenter();
            }
        }
    }

    @Override
    public boolean equals(Object o) {
        if(this == o) return true;
        if(o == null || getClass() != o.getClass()) return false;
        if(!super.equals(o)) return false;

        Account account = (Account) o;

        if(id != null ? !id.equals(account.id) : account.id != null) return false;

        return true;
    }

    @Override
    public int hashCode() {
        int result = super.hashCode();
        result = 31 * result + (id != null ? id.hashCode() : 0);
        return result;
    }
}
