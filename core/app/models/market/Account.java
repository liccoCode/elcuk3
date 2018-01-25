package models.market;

import com.amazonservices.mws.FulfillmentInboundShipment.model.Address;
import com.google.gson.annotations.Expose;
import ext.LinkHelper;
import helper.Constant;
import helper.FLog;
import helper.HTTP;
import helper.Webs;
import models.OperatorConfig;
import org.apache.commons.io.FileUtils;
import org.apache.commons.lang.StringUtils;
import org.apache.http.HttpHeaders;
import org.apache.http.NameValuePair;
import org.apache.http.client.CookieStore;
import org.apache.http.client.config.RequestConfig;
import org.apache.http.client.protocol.HttpClientContext;
import org.apache.http.cookie.Cookie;
import org.apache.http.impl.client.BasicCookieStore;
import org.apache.http.message.BasicHeader;
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
import java.net.URI;
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
    /**
     * 需要过滤掉的 MerchantId
     */
    public final static Map<String, String> OFFER_IDS = new HashMap<>();
    private static final long serialVersionUID = -5304090358536948808L;

    static {
        OFFER_IDS.put("A2OAJ7377F756P", "Amazon Warehouse Deals"); //UK
        OFFER_IDS.put("A8KICS1PHF7ZO", "Amazon Warehouse Deals"); //DE
        OFFER_IDS.put("A2L77EE7U53NWQ", "Amazon Warehouse Deals"); //US
    }

    /**
     * 必须把每个 Account 对应的 CookieStore 给缓存起来, 否则重新加载的 Account 对象没有登陆过的 CookieStore 了
     */
    private static Map<String, BasicCookieStore> COOKIE_STORE_MAP;

    public static Map<String, BasicCookieStore> cookieMap() {
        if(COOKIE_STORE_MAP == null) COOKIE_STORE_MAP = new HashMap<>();
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
     * Amazon MerchantID/SellerId
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

    @Expose
    public boolean isAFR = false;

    @Expose
    public boolean isAIT = false;

    @Expose
    public boolean isAJP = false;

    @Expose
    public boolean isAES = false;

    @Expose
    public boolean isAMX = false;

    @Expose
    public boolean isACA = false;

    @Expose
    public boolean isAIN = false;

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
    public BasicCookieStore cookieStore(M market) {
        if(market == null) market = this.type;
        String key = cookieKey(this.uniqueName, market);
        if(!cookieMap().containsKey(key)) {
            cookieMap().put(key, new BasicCookieStore());
        }
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

    public boolean logged() {
        String html = HTTP.get(this.cookieStore(), this.type.sellerCentralHomePage());
        Document doc = Jsoup.parse(html);
        return doc.select("form[name=signIn]").isEmpty();
    }

    /**
     * 销售账号需要登陆的后台系统
     */
    public void loginAmazonSellerCenter() {
        if(this.logged()) return;
        switch(this.type) {
            case AMAZON_UK:
            case AMAZON_DE:
            case AMAZON_US:
            case AMAZON_IT:
            case AMAZON_FR:
            case AMAZON_CA:
            case AMAZON_JP:
                String body = "";
                try {
                    /*
                      1. Visit the website, fetch the new Cookie.
                      2. With the website params and user/password to login.
                     */
                    this.cookieStore().clear();
                    String uri = this.loginAmazonSellerCenterStep1();
                    loginAmazonSellerCenterStep2(uri);
                    F.T3<List<NameValuePair>, List<BasicHeader>, String> params = loginAmazonSellerCenterStep3(uri);
                    body = HTTP.post(this.cookieStore(), params._3, params._2, params._1,
                            RequestConfig.custom().setCookieSpec("amazon").build());

                    if(haveCorrectCookie()) {
                        Logger.info("%s Seller Central Login Successful!", this.prettyName());
                        this.cookieStore().clearExpired(new Date());
                    } else {
                        Logger.warn("%s Seller Central Login Failed!", this.prettyName());
                    }
                } catch(Exception e) {
                    try {
                        FileUtils.writeStringToFile(
                                new File(Constant.L_LOGIN + "/" + this.type.name() + ".id_" + this.id + ".error.html"),
                                body
                        );
                    } catch(IOException e1) {
                        //ignore
                    }
                    Logger.warn(Webs.e(e));
                }
                break;
            default:
                Logger.warn(
                        "Right now, can only login Amazon(UK,DE,FR) Seller Central. " + this.type + " is not support!");
        }
    }

    private boolean haveCorrectCookie() {
        return StringUtils.isNotBlank(this.cookie("at-acbde")) || //DE
                StringUtils.isNotBlank(this.cookie("at-acbit")) || //IT
                StringUtils.isNotBlank(this.cookie("at-main")) || //US
                StringUtils.isNotBlank(this.cookie("at-acbuk")) || //UK
                StringUtils.isNotBlank(this.cookie("at-acbfr")) || //fr
                StringUtils.isNotBlank(this.cookie("at-acbca")) || //ca
                StringUtils.isNotBlank(this.cookie("at-acbjp")); //JP
    }

    /**
     * 访问 HomePage, 返回最后一个 Redirect 的 URI
     *
     * @return
     */
    public String loginAmazonSellerCenterStep1() {
        HttpClientContext context = HTTP.request(this.cookieStore(), this.type.sellerCentralHomePage());
        List<URI> uris = context.getRedirectLocations();
        if(uris != null && uris.size() > 0) {
            return uris.get(uris.size() - 1).toString();
        } else {
            return context.getTargetHost().getAddress().getHostAddress();
        }
    }

    public void loginAmazonSellerCenterStep2(String uri) {
        HTTP.get(this.cookieStore(), uri);
    }

    /**
     * @param uri
     * @return
     * @throws IOException
     */
    public F.T3<List<NameValuePair>, List<BasicHeader>, String> loginAmazonSellerCenterStep3(String uri) throws
            IOException {
        List<BasicHeader> headers = loginHeaders(uri);
        List<NameValuePair> params = new ArrayList<>();

        String body = HTTP.get(this.cookieStore(), uri);
        Document doc = Jsoup.parse(body);
        Elements inputs = doc.select("form[name=signIn] input");
        if(inputs.size() == 0) {
            Logger.info("WebSite [%s] Still have the Session with User [%s].", this.type.toString(), this.username);
            return new F.T3<>(params, headers, "");
        }

        for(Element el : inputs) {
            String att = el.attr("name");
            if("email".equals(att)) {
                params.add(new BasicNameValuePair(att, this.username));
            } else if("password".equals(att)) {
                params.add(new BasicNameValuePair(att, this.password));
            } else {
                params.add(new BasicNameValuePair(att, el.val()));
            }
        }
        params.add(new BasicNameValuePair("sign-in-button", ""));
        return new F.T3<>(params, headers, doc.select("form[name=signIn]").attr("action"));
    }

    /**
     * Login 需要用到的 Headers
     *
     * @return
     */
    public List<BasicHeader> loginHeaders(String uri) {
        List<BasicHeader> headers = new ArrayList<>();
        headers.add(new BasicHeader(HttpHeaders.ACCEPT,
                "text/html,application/xhtml+xml,application/xml;q=0.9,image/webp,*/*;q=0.8"));
        headers.add(new BasicHeader(HttpHeaders.CACHE_CONTROL, "max-age=0"));
        headers.add(new BasicHeader("Origin", this.type.sellerCentralHomePage()));
        headers.add(new BasicHeader("Upgrade-Insecure-Requests", "1"));
        headers.add(new BasicHeader(HttpHeaders.USER_AGENT,
                "Mozilla/5.0 (Macintosh; Intel Mac OS X 10_11_2) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/49.0.2623.75 Safari/537.36"));
        headers.add(new BasicHeader(HttpHeaders.REFERER, uri));
        headers.add(new BasicHeader(HttpHeaders.ACCEPT_LANGUAGE, "zh-CN,zh;q=0.8,en-US;q=0.6,en;q=0.4"));
        return headers;
    }

    /**
     * 非销售账号在 Amazon 的前台登陆,<br>
     * 支持同账户登陆多个市场, 例如: 一个账户登陆 Uk/DE
     */
    public boolean loginAmazonSite(M market) {
        switch(this.type) {
            case AMAZON_CA:
            case AMAZON_UK:
            case AMAZON_DE:
            case AMAZON_FR:
            case AMAZON_US:
            case AMAZON_IT:
            case AMAZON_JP:
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
                    Logger.info("WebSite [" + market.toString()
                            + "] Still have the Session with User [" + this.username + "].");
                    FLog.fileLog(String.format("%s.Login.html", this.prettyName()), body,
                            FLog.T.HTTP_ERROR);
                    return false;
                }

                Set<NameValuePair> params = new HashSet<>();
                for(Element el : inputs) {
                    String att = el.attr("name");
                    if("email".equals(att)) params.add(new BasicNameValuePair(att, this.username));
                    else if("password".equals(att))
                        params.add(new BasicNameValuePair(att, this.password));
                    else if("create".equals(att)) params.add(new BasicNameValuePair(att, "0"));//登陆
                    else params.add(new BasicNameValuePair(att, el.val()));
                }
                body = HTTP.post(this.cookieStore(market),
                        doc.select("#ap_signin_form").first().attr("action"), params);
                boolean isLogin = Account.isLoginFront(Jsoup.parse(body));
                if(Play.mode.isDev())
                    FLog.fileLog(String.format("%s.afterLogin.html", this.prettyName()), body,
                            FLog.T.HTTP_ERROR);
                boolean loginSucc;
                if(isLogin) {
                    Logger.info("%s Amazon Site Login Successful!", this.prettyName());
                    loginSucc = true;
                } else {
                    Logger.warn("%s Amazon Site Login Failed!", this.prettyName());
                    FLog.fileLog(String.format("%s.afterLogin.html", this.prettyName()), body,
                            FLog.T.HTTP_ERROR);
                    loginSucc = false;
                }
                this.cookieStore(market).clearExpired(new Date());
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
        if(Arrays.asList(M.EBAY_UK).contains(this.type)) return;
        String url = "Account.changeRegion.";
        try {
            url = this.type.changeRegion(m.amid().name());
            HTTP.get(this.cookieStore(), url);
        } catch(Exception e) {
            throw new FastRuntimeException(
                    String.format("Invoke %s with error.[%s]", url, Webs.e(e)));
        }
    }


    /**
     * 返回这个市场所对应的 MarketPlaceId, 仅仅支持 UK/DE/FR, 其他市场默认返回 UK 的
     *
     * @return
     */
    public M.MID marketplaceId() {
        switch(this.type) {
            case AMAZON_CA:
                return M.MID.A2EUQ1WTGCTBG2;
            case AMAZON_UK:
                return M.MID.A1F83G8C2ARO7P;
            case AMAZON_DE:
                return M.MID.A1PA6795UKMFR9;
            case AMAZON_FR:
                return M.MID.A13V1IB3VIYZZH;
            case AMAZON_US:
                return M.MID.ATVPDKIKX0DER;
            case AMAZON_IT:
                return M.MID.APJ6JRA9NG5V4;
            case AMAZON_JP:
                return M.MID.A1VC38T7YXB528;
            default:
                return M.MID.A1F83G8C2ARO7P;
        }
    }

    public boolean addToWishList(Listing listing) {
        /**
         * 1.检查账户是否登陆
         * 2.抓取wishlist的页面数据,判断账户是否已经创建wishlist 如果没有创建一个新的
         * 3.抓取listing页面的表单数据添加Listing到 wishlist
         */
        String sessionId = this.cookie("session-id", listing.market);
        if(sessionId == null) {
            //登陆失败
            if(!this.loginAmazonSite(listing.market)) {
                return false;
            }
            sessionId = this.cookie("session-id", listing.market);
        }

        String wishlistBody = HTTP.get(this.cookieStore(listing.market),
                listing.market.amazonWishList());
        //判断是否存在WishList
        if(!wishlistBody.contains("listActions")) {
            HTTP.post(this.cookieStore(listing.market), listing.market.amazonNewWishList(),
                    Arrays.asList(
                            new BasicNameValuePair("manual-create", "Y"),
                            new BasicNameValuePair("sid", sessionId),
                            new BasicNameValuePair("submit.movecopy", "1"),
                            new BasicNameValuePair("isPrivate", "N"),
                            new BasicNameValuePair("dest-list", "new-wishlist"),
                            new BasicNameValuePair("isSearchable", "Y"),
                            new BasicNameValuePair("sourceVendorId", "website.wishlist.intro"),
                            new BasicNameValuePair("type", "wishlist"),
                            new BasicNameValuePair("movecopy", "createnew")
                    ));

        }

        String listing_body = HTTP.get(this.cookieStore(listing.market),
                listing.market.amazonAsinLink(listing.asin));
        Document doc = Jsoup.parse(listing_body);
        Elements inputs = doc.select("#handleBuy input");
        Set<NameValuePair> params = new HashSet<>();
        for(Element el : inputs) {
            if(StringUtils.isNotBlank(el.val())) {
                params.add(new BasicNameValuePair(el.attr("name"), el.val()));
            }
        }
        //每次的请求参数会根据listing变化而变化.所以全部加上去
        params.add(new BasicNameValuePair("asin-redirect", listing.asin));
        params.add(new BasicNameValuePair("quantity", "1"));
        //下面两个参数用来避免某些含有参数offerListingId的请求 被添加到 Basket中去
        params.add(new BasicNameValuePair("submit.add-to-registry.wishlist.x", "-1710"));
        params.add(new BasicNameValuePair("submit.add-to-registry.wishlist.y", "-357"));
        String result = HTTP
                .post(this.cookieStore(listing.market),
                        doc.select("#handleBuy").first().attr("action"), params);

        //如果添加成功,或者是账户已经添加该Listing但是系统中无记录.
        if(result.contains("hucSuccessMsg") | result.contains("appMessageBoxInfo")) {
            new AmazonWishListRecord(listing, this).save();
            return true;
        }

        return false;

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
                this.loginAmazonSite(review.listing.market);
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
        return new F.T2<>(record, content);
    }

    /**
     * 访问 Review 的页面, 检查是否登陆, 并且解析出点击 Up 与 Down 的两个链接.
     *
     * @param review
     * @return
     */
    public F.T3<Boolean, String, String> checkLoginAndFetchClickLinks(AmazonListingReview review) {
        String html = HTTP
                .get(this.cookieStore(review.listing.market), LinkHelper.reviewLink(review));
        Document doc = Jsoup.parse(html);
        // 账号登陆以后, 链接中才会有 sign-out 字符串
        Elements els = doc.select(".votingButtonReviews");
        String[] upAndDownLink = new String[2];
        for(Element el : els) {
            String link = el.attr("href");
            if("1".equals(StringUtils.substringBetween(link, "Helpful/", "/ref=cm")))
                upAndDownLink[0] = link;
            else upAndDownLink[1] = link;
        }
        boolean isLogin = Account.isLoginFront(doc);
        // 这里检查点击不成功的原因
        if(!isLogin)
            FLog.fileLog(String.format("%s.clickReiew.%s.%s.Failed.html", this.prettyName(),
                    review.reviewId, review.listing.market), html, FLog.T.HTTP_ERROR);
        // 将另外一个错误分开记录文件
        if(upAndDownLink[0] == null || upAndDownLink[1] == null)
            FLog.fileLog(String.format("%s.URL_NULL.%s.%s.Failed.html", this.prettyName(),
                    review.reviewId, review.listing.market), html, FLog.T.HTTP_ERROR);

        return new F.T3<>(isLogin, upAndDownLink[0], upAndDownLink[1]);
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
            return StringUtils.contains(navidWelcomeMsgStr, "sign-out")
                    || StringUtils.contains(navidWelcomeMsgStr, "signout");
        } else {
            String nav_your_account_flyoutStr = doc.select("#nav_your_account_flyout").outerHtml();
            return StringUtils.contains(nav_your_account_flyoutStr, "sign-out")
                    || StringUtils.contains(nav_your_account_flyoutStr, "signout")
                    || StringUtils.contains(nav_your_account_flyoutStr, "Sign Out");
        }
    }

    /**
     * 判断后端是否登陆
     *
     * @param doc
     * @return
     */
    public static boolean isLoginEnd(Document doc) {
        return doc.select("#sc-quicklinks-top").first() != null;
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
     * 1. 规则为欧洲市场的账号可以通用
     * 2. 美国市场单独区分
     *
     * @param market 需要哪一个市场的可点击 Review Account, 如果设置为 null, 则返回全部
     * @return
     */
    public static List<Account> openedAmazonClickReviewAndLikeAccs(M market) {
        switch(market) {
            case AMAZON_CA:
            case AMAZON_DE:
            case AMAZON_FR:
            case AMAZON_ES:
            case AMAZON_IT:
            case AMAZON_JP:
            case AMAZON_UK:
            case AMAZON_US:
                return Account.find("closeable=? AND isSaleAcc=? ORDER BY id", false, false)
                        .fetch();
            default:
                return Account.find("closeable=? AND isSaleAcc=? ORDER BY id", false, false)
                        .fetch();
        }
    }

    /**
     * 所有打开的销售账号
     */
    public static List<Account> openedSaleAcc() {
        return Account.find("closeable=? AND isSaleAcc=? ORDER BY id", false, true).fetch();
    }

    /**
     * 根据市场找出销售账号
     */
    public static Account saleAccount(M market) {
        return Account.find("isSaleAcc=? AND type=?", true, market).first();
    }

    /**
     * 构造放在 Account Cookie_Store_Map 中的 KEY
     */
    public static String cookieKey(String uniq, M market) {
        return String.format("ACC_COOKIE_%s_%s", uniq, market);
    }

    /**
     * 通过账户获取 FBA 的发货地址;
     */
    public static Address address(M type) {
        // 统一为一个地址, 但接口参数预留
        return new Address(OperatorConfig.getVal("addressname"),
                OperatorConfig.getVal("addressline1"), OperatorConfig.getVal("addressline2"), null,
                OperatorConfig.getVal("addresscity"),
                OperatorConfig.getVal("addressstate"),
                OperatorConfig.getVal("addresscountrycode"),
                OperatorConfig.getVal("addresspostalcode"));
    }


    /**
     * 初始化 Account 相关的业务;
     * 1. 将 MerchantID 持久在内存中
     * 2. 登陆 Account 账户
     */
    public static void initLogin() {
        synchronized(Account.class) {
            List<Account> accs = Account.openedSaleAcc();
            for(Account ac : accs) {
                Logger.info(String.format("Login %s with account %s.", ac.type, ac.username));
                ac.loginAmazonSellerCenter();
            }
        }
    }

    /**
     * 初始化加载系统内账户的 OffersId
     */
    public static void initOfferIds() {
        List<Account> accs = Account.openedSaleAcc();
        for(Account ac : accs) {
            if(ac.isSaleAcc)
                OFFER_IDS.put(ac.merchantId, ac.uniqueName);
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
