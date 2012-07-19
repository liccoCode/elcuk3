package models.market;

import com.google.gson.annotations.Expose;
import exception.NotSupportChangeRegionFastException;
import helper.*;
import models.ElcukRecord;
import org.apache.commons.io.FileUtils;
import org.apache.commons.lang.StringUtils;
import org.apache.http.NameValuePair;
import org.apache.http.client.CookieStore;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.BasicCookieStore;
import org.apache.http.message.BasicNameValuePair;
import org.hibernate.annotations.Cache;
import org.hibernate.annotations.CacheConcurrencyStrategy;
import org.joda.time.DateTime;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import play.Logger;
import play.Play;
import play.data.validation.Equals;
import play.data.validation.Required;
import play.db.jpa.Model;
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
     * 当使用 MERCHANT_ID 为 final 的时候, 在 OnStartUP 中的 loadModules 初始化报错...
     *
     * @return
     */
    public static Map<String, String> merchant_id() {
        if(MERCHANT_ID == null) MERCHANT_ID = new HashMap<String, String>();
        MERCHANT_ID.put("A2OAJ7377F756P", "Amazon Warehouse Deals"); //UK
        MERCHANT_ID.put("A8KICS1PHF7ZO", "Amazon Warehouse Deals"); //DE
        //TODO 其实市场的以后看到再添加进来
        return MERCHANT_ID;
    }

    /**
     * 必须把每个 Account 对应的 CookieStore 给缓存起来, 否则重新加载的 Account 对象没有登陆过的 CookieStore 了
     */
    private static final Map<String, CookieStore> COOKIE_STORE_MAP = new HashMap<String, CookieStore>();

    /**
     * 不同的 Market place
     */
    public enum M {
        AMAZON_UK,
        AMAZON_DE,
        AMAZON_FR,
        AMAZON_IT,
        AMAZON_ES,
        AMAZON_US,
        EBAY_UK;

        public boolean isAmazon() {
            return (StringUtils.startsWith(this.name(), "AMAZON"));
        }

        public boolean isEbay() {
            return (StringUtils.startsWith(this.name(), "EBAY"));
        }

        /**
         * Amazon MarketPlaceId, 在 Amazon 上用来区分 uk/de/us 等等市场的 ID
         * !! TODO US 市场的暂时还没有
         *
         * @return
         */
        public String amid() {
            switch(this) {
                case AMAZON_UK:
                    return "A1F83G8C2ARO7P";
                case AMAZON_DE:
                    return "A1PA6795UKMFR9";
                case AMAZON_IT:
                    return "APJ6JRA9NG5V4";
                case AMAZON_FR:
                    return "A13V1IB3VIYZZH";
                case AMAZON_ES:
                    return "A1RKKUPIHCS9HS";
                case AMAZON_US:
                case EBAY_UK:
                default:
                    return "";
            }
        }

        /**
         * 返回 html 页面中单位的格式
         *
         * @return
         */
        public String htmlCurrency() {
            switch(this) {
                case AMAZON_US:
                    return "$";
                case AMAZON_DE:
                case AMAZON_ES:
                case AMAZON_FR:
                case AMAZON_IT:
                    return "&euro;";
                case AMAZON_UK:
                case EBAY_UK:
                default:
                    return "&pound;";
            }
        }

        public String toString() {
            switch(this) {
                case AMAZON_UK:
                    return "amazon.co.uk";
                case AMAZON_DE:
                    return "amazon.de";
                case AMAZON_FR:
                    return "amazon.fr";
                case AMAZON_IT:
                    return "amazon.it";
                case AMAZON_ES:
                    return "amazon.es";
                case AMAZON_US:
                    return "amazon.com";
                case EBAY_UK:
                    return "ebay.co.uk";
                default:
                    return "amazon.co.uk";
            }
        }

        public String nickName() {
            switch(this) {
                case AMAZON_UK:
                    return "A_UK";
                case AMAZON_DE:
                    return "A_DE";
                case AMAZON_FR:
                    return "A_FR";
                case AMAZON_IT:
                    return "A_IT";
                case AMAZON_ES:
                    return "A_ES";
                case AMAZON_US:
                    return "A_US";
                case EBAY_UK:
                    return "E_UK";
                default:
                    return "A_UK";
            }
        }

        public static M val(String str) {
            String s = str.toLowerCase();
            if(s.equals("auk") || s.equals("amazon_uk") || s.equals("amazon.co.uk") || s.equals("www.amazon.co.uk")) {
                return AMAZON_UK;
            } else if(s.equals("afr") || s.equals("amazon_fr") || s.equals("amazon.fr") || s.equals("www.amazon.fr")) {
                return AMAZON_FR;
            } else if(s.equals("aes") || s.equals("amazon_es") || s.equals("amazon.es") || s.equals("www.amazon.es")) {
                return AMAZON_ES;
            } else if(s.equals("ade") || s.equals("amazon_de") || s.equals("amazon.de") || s.equals("www.amazon.de")) {
                return AMAZON_DE;
            } else if(s.equals("ait") || s.equals("amazon_it") || s.equals("amazon.it") || s.equals("www.amazon.it")) {
                return AMAZON_IT;
            } else if(s.equals("aus") || s.equals("amazon_us") || s.equals("amazon.com") || s.equals("www.amazon.com")) {
                return AMAZON_US;
            } else if(s.equals("euk") || s.equals("ebay_uk") || s.equals("ebay.co.uk") || s.equals("www.ebay.co.uk")) {
                return EBAY_UK;
            } else {
                return null;
            }
        }

        /**
         * 账户对应的网站的后台首页
         *
         * @return
         */
        public String homePage() {
            switch(this) {
                case AMAZON_UK:
                case AMAZON_DE:
                case AMAZON_ES:
                case AMAZON_FR:
                case AMAZON_IT:
                case AMAZON_US:
                    return "https://sellercentral." + this.toString();
                case EBAY_UK:
                    return "unknow..";
                default:
                    return "Not Support.";
            }
        }

        /**
         * 账户对应的网站的后台登陆 URL
         *
         * @return
         */
        public String loginPage() {
            switch(this) {
                case AMAZON_UK:
                case AMAZON_DE:
                case AMAZON_ES:
                case AMAZON_FR:
                case AMAZON_IT:
                case AMAZON_US:
                    return "https://sellercentral." + this.toString() + "/gp/sign-in/sign-in.html/ref=xx_login_lgin_home";
                case EBAY_UK:
                    return "unknow..";
                default:
                    return "Not Support.";
            }
        }

        /**
         * 账户对应的网站的 feedback 抓取的地址
         *
         * @param page
         * @return
         */
        public String feedbackPage(int page) {
            //https://sellercentral.amazon.co.uk/gp/feedback-manager/view-all-feedback.html?ie=UTF8&sortType=sortByDate&pageSize=50&dateRange=&descendingOrder=1&currentPage=1
            switch(this) {
                case AMAZON_UK:
                case AMAZON_DE:
                case AMAZON_ES:
                case AMAZON_FR:
                case AMAZON_IT:
                case AMAZON_US:
                    return "https://sellercentral." + this.toString() + "/gp/feedback-manager/view-all-feedback.html?ie=UTF8&sortType=sortByDate&pageSize=50&dateRange=&descendingOrder=1&currentPage=" + page;
                case EBAY_UK:
                    return "unknow..";
                default:
                    return "Not Support.";
            }
        }

        /**
         * 仅仅只有 Amazon Europe 才有的区域转换
         *
         * @param marketplaceID
         * @return
         */
        public String changeRegion(String marketplaceID) {
            //https://sellercentral.amazon.de/gp/utilities/set-rainier-prefs.html?ie=UTF8&marketplaceID=A1PA6795UKMFR9
            switch(this) {
                case AMAZON_UK:
                case AMAZON_DE:
                case AMAZON_ES:
                case AMAZON_FR:
                case AMAZON_IT:
                case AMAZON_US:
                    return "https://sellercentral." + this.toString() + "/gp/utilities/set-rainier-prefs.html?ie=UTF8&marketplaceID=" + marketplaceID;
                case EBAY_UK:
                default:
                    throw new NotSupportChangeRegionFastException();
            }
        }

        /**
         * 下载 7 天内的 Transaction 数据
         *
         * @return
         */
        public String flatFinance() {
            //https://sellercentral.amazon.co.uk/gp/payments-account/export-transactions.html?ie=UTF8&pageSize=DownloadSize&daysAgo=Seven&subview=daysAgo&mostRecentLast=0&view=filter&eventType=
            //https://sellercentral.amazon.co.uk/gp/payments-account//export-transactions.html?ie=UTF8&pageSize=DownloadSize&daysAgo=Seven&subview=daysAgo&mostRecentLast=0&view=filter&eventType=
            switch(this) {
                case AMAZON_UK:
                case AMAZON_DE:
                case AMAZON_ES:
                case AMAZON_FR:
                case AMAZON_IT:
                case AMAZON_US:
                    return "https://sellercentral." + this.toString() + "/gp/payments-account//export-transactions.html?ie=UTF8&pageSize=DownloadSize&daysAgo=Seven&subview=daysAgo&mostRecentLast=0&view=filter&eventType=";
                case EBAY_UK:
                default:
                    throw new NotSupportChangeRegionFastException();
            }
        }

        /**
         * 从 Past Settlements 页面下载 Flat File V2 文件进行 Finance 数据的补充
         *
         * @param reportId
         * @return
         */
        public String flatV2Finance(String reportId) {
            //https://sellercentral.amazon.co.uk/gp/reports/documents/_GET_ALT_FLAT_FILE_PAYMENT_SETTLEMENT_DATA__11567294004.txt?ie=UTF8&contentType=text%2Fxls
            switch(this) {
                case AMAZON_UK:
                case AMAZON_DE:
                case AMAZON_ES:
                case AMAZON_FR:
                case AMAZON_IT:
                case AMAZON_US:
                    return "https://sellercentral." + this.toString() + "/gp/reports/documents/_GET_ALT_FLAT_FILE_PAYMENT_SETTLEMENT_DATA__" + reportId + ".txt?ie=UTF8&contentType=text%2Fxls";
                case EBAY_UK:
                default:
                    throw new NotSupportChangeRegionFastException();
            }
        }

        /**
         * Amazon 后台的订单页面
         *
         * @param oid
         * @return
         */
        public String orderDetail(String oid) {
            //https://sellercentral.amazon.co.uk/gp/orders-v2/details?orderID=203-5364157-2572327
            switch(this) {
                case AMAZON_UK:
                case AMAZON_DE:
                case AMAZON_ES:
                case AMAZON_FR:
                case AMAZON_IT:
                case AMAZON_US:
                    return "https://sellercentral." + this.toString() + "/gp/orders-v2/details?orderID=" + oid;
                case EBAY_UK:
                default:
                    throw new NotSupportChangeRegionFastException();
            }
        }

        /**
         * 模拟人工上架使用的链接.
         * - Amazon: 由于 Amazon 上架方式, 其需要访问两次这个页面, 一次 CLASSIFY 一次 IDENTIFY
         *
         * @return
         */
        public String saleSellingLink() {
            switch(this) {
                case AMAZON_UK:
                case AMAZON_DE:
                case AMAZON_ES:
                case AMAZON_FR:
                case AMAZON_IT:
                case AMAZON_US:
                    return String.format("https://catalog-sc.%s/abis/Classify/SelectCategory", this.toString());
                case EBAY_UK:
                default:
                    throw new NotSupportChangeRegionFastException();
            }
        }

        /**
         * 模拟人工上架使用的链接.
         * - Amazon: 创建 Listing 的提交地址
         *
         * @return
         */
        public String saleSellingPostLink() {
            //https://catalog-sc.amazon.co.uk/abis/product/ProcessCreateProduct
            switch(this) {
                case AMAZON_UK:
                case AMAZON_DE:
                case AMAZON_ES:
                case AMAZON_FR:
                case AMAZON_IT:
                case AMAZON_US:
                    return String.format("https://catalog-sc.%s/abis/product/ProcessCreateProduct", this.toString());
                case EBAY_UK:
                default:
                    throw new NotSupportChangeRegionFastException();
            }
        }


        /**
         * 模拟人工上架使用的链接.
         * - Amazon: 创建全新的 Listing 的时候, 最后需要回掉方法寻找 New UPC 对应的 ASIN
         *
         * @return
         */
        public String productCreateStatusLink() {
            // https://sellercentral.amazon.de/myi/search/ajax/ProductCreateStatus
            switch(this) {
                case AMAZON_UK:
                case AMAZON_DE:
                case AMAZON_ES:
                case AMAZON_FR:
                case AMAZON_IT:
                case AMAZON_US:
                    return String.format("https://sellercentral.%s/myi/search/ajax/ProductCreateStatus", this.toString());
                case EBAY_UK:
                default:
                    throw new NotSupportChangeRegionFastException();
            }
        }

        public String matchAsinAjaxLink() {
            switch(this) {
                case AMAZON_UK:
                case AMAZON_DE:
                case AMAZON_ES:
                case AMAZON_FR:
                case AMAZON_IT:
                case AMAZON_US:
                    return String.format("https://catalog-sc.%s/abis/product/ajax/Match.ajax", this.toString());
                case EBAY_UK:
                default:
                    throw new NotSupportChangeRegionFastException();
            }
        }

        public String saleSellingStateLink() {
            //https://sellercentral.amazon.de/gp/ezdpc-gui/inventory-status/status.html
            switch(this) {
                case AMAZON_UK:
                case AMAZON_DE:
                case AMAZON_ES:
                case AMAZON_FR:
                case AMAZON_IT:
                case AMAZON_US:
                    return String.format("https://sellercentral.%s/gp/ezdpc-gui/inventory-status/status.html", this.toString());
                case EBAY_UK:
                default:
                    throw new NotSupportChangeRegionFastException();
            }
        }

        public String uploadImageLink() {
            //https://catalog-sc.amazon.co.uk/abis/image/AddImage.ajax
            switch(this) {
                case AMAZON_UK:
                case AMAZON_DE:
                case AMAZON_ES:
                case AMAZON_FR:
                case AMAZON_IT:
                case AMAZON_US:
                    return String.format("https://catalog-sc.%s/abis/image/AddImage.ajax", this.toString());
                case EBAY_UK:
                default:
                    throw new NotSupportChangeRegionFastException();
            }
        }

        public String removeImageLink() {
            //https://catalog-sc.amazon.co.uk/abis/image/RemoveImage.ajax
            switch(this) {
                case AMAZON_UK:
                case AMAZON_DE:
                case AMAZON_ES:
                case AMAZON_FR:
                case AMAZON_IT:
                case AMAZON_US:
                    return String.format("https://catalog-sc.%s/abis/image/RemoveImage.ajax", this.toString());
                case EBAY_UK:
                default:
                    throw new NotSupportChangeRegionFastException();
            }
        }

        /**
         * 这个是根据 Account 所在地来确定, 不同的市场需要先进行 region 切换
         *
         * @param from
         * @param to
         * @param currentPage
         * @return
         */
        public String salesAndTrafficByAsinLink(Date from, Date to, int currentPage) {
            /**
             * https://sellercentral.amazon.co.uk/gp/site-metrics/load-report-JSON.html/ref=au_xx_cont_sitereport?
             * fromDate=12/05/2012&
             * toDate=12/05/2012&
             * reportID=102:DetailSalesTrafficBySKU&
             * currentPage=0
             */
            switch(this) {
                case AMAZON_UK:
                case AMAZON_DE:
                case AMAZON_ES:
                case AMAZON_FR:
                case AMAZON_IT:
                case AMAZON_US:
                    return String.format("https://sellercentral.%s/gp/site-metrics/load-report-JSON.html/ref=au_xx_cont_sitereport?" +
                            "fromDate=%s&toDate=%s&reportID=102:DetailSalesTrafficBySKU&currentPage=%s", this.toString(),
                            Dates.listingUpdateFmt(AMAZON_UK, from), Dates.listingUpdateFmt(AMAZON_UK, to), currentPage);
                case EBAY_UK:
                default:
                    throw new NotSupportChangeRegionFastException();
            }
        }

        /**
         * 下载 FBA 最新的 Label 的链接
         *
         * @return
         */
        public String fnSkuDownloadLink() {
            /**
             * https://sellercentral.amazon.de/gp/ssof/product-label.pdf/ref=ag_xx_cont_fbaprntlab?ie=UTF8&ascending=1&sortAttribute=MerchantSKU
             * 是 0(零) 不是 O(字母)
             * qty.0:27
             * fnSku.0:X0003U9PEH
             * mSku.0:10HTCG14-1900S
             * labelType:ItemLabel_A4_27
             */
            switch(this) {
                case AMAZON_UK:
                case AMAZON_DE:
                case AMAZON_ES:
                case AMAZON_FR:
                case AMAZON_IT:
                case AMAZON_US:
                    return String.format("https://sellercentral.%s/gp/ssof/product-label.pdf/ref=ag_xx_cont_fbaprntlab?ie=UTF8&ascending=1&sortAttribute=MerchantSKU",
                            this.toString());
                case EBAY_UK:
                default:
                    throw new NotSupportChangeRegionFastException();
            }
        }

        /**
         * 模拟人工方式修改 Listing 信息的地址
         *
         * @return
         */
        public static String listingEditPage(Selling sell) {
            //https://catalog-sc.amazon.co.uk/abis/product/DisplayEditProduct?sku=71APNIP-BSLPU&asin=B007LE3Y88
            switch(sell.market) {
                case AMAZON_UK:
                case AMAZON_DE:
                case AMAZON_ES:
                case AMAZON_FR:
                case AMAZON_IT:
                case AMAZON_US:
                    String msku = sell.merchantSKU;
                    if("68-MAGGLASS-3X75BG,B001OQOK5U".equalsIgnoreCase(sell.merchantSKU)) {
                        msku = "68-MAGGLASS-3x75BG,B001OQOK5U";
                    } else if("80-qw1a56-be,2".equalsIgnoreCase(sell.merchantSKU)) {
                        msku = "80-qw1a56-be,2";
                    } else if("80-qw1a56-be".equalsIgnoreCase(sell.merchantSKU)) {
                        msku = "80-qw1a56-be";
                    }
                    return String.format("https://catalog-sc.%s/abis/product/DisplayEditProduct?sku=%s&asin=%s",
                            sell.account.type.toString()/*更新的链接需要账号所在地的 URL*/, msku, sell.asin);

                case EBAY_UK:
                default:
                    throw new NotSupportChangeRegionFastException();
            }
        }


        public static String listingPostPage(Account.M market, String jsessionId) {
            //https://catalog-sc.amazon.co.uk/abis/product/ProcessEditProduct
            switch(market) {
                case AMAZON_UK:
                case AMAZON_DE:
                case AMAZON_ES:
                case AMAZON_FR:
                case AMAZON_IT:
                case AMAZON_US:
                    return "https://catalog-sc." + market.toString() + "/abis/product/ProcessEditProduct" +
                            (StringUtils.isNotBlank(jsessionId) ? ";" + jsessionId : "");
                case EBAY_UK:
                default:
                    throw new NotSupportChangeRegionFastException();
            }
        }

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

    @Transient
    public Account mirror;


    @PostLoad
    public void postLoad() {
        this.mirror = J.from(J.json(this), Account.class);
    }

    @PostUpdate
    public void records() {
        ElcukRecord.postUpdate(this, "Account.update");
    }

    /**
     * 将 CookieStore 按照 Account 区分开来以后, 那么在系统中对应的 sellercentral.amazon.co.uk 可以有多个 Account 登陆, 他们的 Cookie 各不影响
     *
     * @return
     */
    public CookieStore cookieStore() {
        if(!COOKIE_STORE_MAP.containsKey(this.uniqueName))
            COOKIE_STORE_MAP.put(this.uniqueName, new BasicCookieStore());
        return COOKIE_STORE_MAP.get(this.uniqueName);
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
     * 登陆对应的网站
     */
    public void loginWebSite() {
        switch(this.type) {
            case AMAZON_UK:
            case AMAZON_DE:
                String body = "";
                try {
                    /**
                     * 1. Visit the website, fetch the new Cookie.
                     * 2. With the website params and user/password to login.
                     */
                    body = HTTP.get(this.cookieStore(), this.type.homePage());

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
                    body = HTTP.post(this.cookieStore(), this.type.loginPage(), params);
                    if(Play.mode.isDev())
                        FileUtils.writeStringToFile(new File(Constant.HOME + "/elcuk2-logs/" + this.type.name() + ".id_" + this.id + ".afterLogin.html"), body);
                    Element navBar = Jsoup.parse(body).select("#topNavContainer").first();
                    if(navBar != null) Logger.info("%s Login Successful!", this.prettyName());
                    else Logger.warn("%s Login Failed!", this.prettyName());

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
                Logger.warn("Right now, can only login Amazon.co.uk. " + this.type + " is not support!");
        }
    }

    /**
     * 只有 Amazon EU 才会有的转换区域.
     *
     * @param m
     */
    public void changeRegion(M m) {
        String url = "Account.changeRegion.";
        try {
            url = this.type.changeRegion(m.amid());
            HTTP.get(this.cookieStore(), url);
        } catch(Exception e) {
            throw new FastRuntimeException(String.format("Invoke %s with error.[%s]", url, Webs.E(e)));
        }
    }

    /**
     * 抓取 Account 对应网站的 FeedBack
     *
     * @param page
     * @return
     */
    public List<Feedback> fetchFeedback(int page) {
        HttpGet feedback = null;
        switch(this.type) {
            case AMAZON_UK:
            case AMAZON_DE:
                try {
                    String body = HTTP.get(this.cookieStore(), this.type.feedbackPage(page));
                    if(Play.mode.isDev())
                        FileUtils.writeStringToFile(new File(Constant.HOME + "/elcuk2-logs/" + this.type.name() + ".id_" + this.id + "feedback_p" + page + ".html"), body);
                    List<Feedback> feedbacks = Feedback.parseFeedBackFromHTML(body);
                    for(Feedback f : feedbacks) {
                        try {
                            f.account = this;
                            f.orderr = Orderr.findById(f.orderId);
                        } catch(Exception e) {
                            Logger.warn(Webs.E(e));
                        }
                    }
                    return feedbacks;
                } catch(Exception e) {
                    Logger.warn("[" + this.type + "] Feedback page can not found Or the session is invalid!");
                }
                break;
            default:
                Logger.warn("Not support fetch [" + this.type + "] Feedback.");
        }
        return new ArrayList<Feedback>();
    }

    /**
     * 返回这个市场所对应的 MarketPlaceId, 仅仅支持 UK/DE/FR, 其他市场默认返回 UK 的
     *
     * @return
     */
    public AWS.MID marketplaceId() {
        switch(this.type) {
            case AMAZON_UK:
                return AWS.MID.A1F83G8C2ARO7P;
            case AMAZON_DE:
                return AWS.MID.A1PA6795UKMFR9;
            case AMAZON_FR:
                return AWS.MID.A13V1IB3VIYZZH;
            default:
                return AWS.MID.A1F83G8C2ARO7P;
        }
    }


    /**
     * 下载 7 天的 Flat Finance
     *
     * @return
     */
    public File briefFlatFinance(M market) {
        String body = Account.downFileFromAmazon(this.type.flatFinance(), market, this);
        DateTime dt = DateTime.now();
        File f = new File(String.format("%s/%s/%s/%s_%s_%s.txt",
                Constant.D_FINANCE, market, dt.toString("yyyy.MM"), this.username, this.id, dt.toString("dd_HH'h'")));
        Logger.info("File Save to :[" + f.getAbsolutePath() + "]");
        try {
            FileUtils.writeStringToFile(f, body);
        } catch(IOException e) {
            //ignore
        }
        return f;
    }

    /**
     * 下载 Past Settlements 页面的 Flat V2 文件
     *
     * @param market
     * @param reportId
     * @return
     */
    public File briefFlatV2Finance(M market, String reportId) {
        String body = Account.downFileFromAmazon(this.type.flatV2Finance(reportId), market, this);
        File f = new File(String.format("%s/%s/%s.txt", Constant.D_FINANCE, market, reportId));
        Logger.info("FlatV2 File Save to :[%s]", f.getAbsolutePath());
        try {
            FileUtils.writeStringToFile(f, body);
        } catch(IOException e) {
            //ignore
        }
        return f;
    }

    @Override
    public String toString() {
        return StringUtils.split(this.uniqueName, "@")[0];
    }

    public String prettyName() {
        return String.format("%s.%s", this.type.nickName(), this.username.split("@")[0]);
    }

    /**
     * 在 Amazon 上通过 URL 下载文件
     *
     * @param url
     * @param market
     * @return
     */
    public static String downFileFromAmazon(String url, M market, Account acc) {
        String body = "";
        synchronized(acc.cookieStore()) {
            acc.changeRegion(market);
            body = HTTP.get(acc.cookieStore(), url);
        }
        if(StringUtils.isBlank(body)) {
            Logger.warn("URL [%s] Download file error.", url);
            throw new FastRuntimeException(String.format("Download [%s] error.", url));
        }
        return body;
    }

    /**
     * 所有打开的销售账号
     *
     * @return
     */
    public static List<Account> openedSaleAcc() {
        return Account.find("closeable=? AND isSaleAcc=?", false, true).fetch();
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
                ac.loginWebSite();
            }
        }
    }
}
