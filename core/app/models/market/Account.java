package models.market;

import exception.NotLoginFastException;
import exception.NotSupportChangeRegionFastException;
import helper.AWS;
import helper.Constant;
import helper.HTTP;
import helper.Webs;
import org.apache.commons.io.FileUtils;
import org.apache.commons.lang.StringUtils;
import org.apache.http.NameValuePair;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.cookie.Cookie;
import org.apache.http.message.BasicNameValuePair;
import org.apache.http.util.EntityUtils;
import org.joda.time.DateTime;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import play.Logger;
import play.Play;
import play.data.validation.Required;
import play.db.jpa.Model;

import javax.persistence.*;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Set;

/**
 * 不同的账户, Market Place 可以相同, 但是 Account 不一定相同.
 * User: wyattpan
 * Date: 1/6/12
 * Time: 4:39 PM
 */
@Entity
public class Account extends Model {
    /**
     * 不同的 Market place
     */
    public enum M {
        AMAZON_UK,
        AMAZON_DE,
        AMAZON_IT,
        AMAZON_FR,
        AMAZON_ES,
        AMAZON_US,
        EBAY_UK;

        public String toString() {
            switch(this) {
                case AMAZON_UK:
                    return "amazon.co.uk";
                case AMAZON_DE:
                    return "amazon.de";
                case AMAZON_IT:
                    return "amazon.it";
                case AMAZON_FR:
                    return "amazon.fr";
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

        public static M val(String str) {
            String strLowercase = str.toLowerCase();
            if(strLowercase.equals("amazon_uk") || strLowercase.equals("amazon.co.uk") || strLowercase.equals("www.amazon.co.uk")) {
                return AMAZON_UK;
            } else if(strLowercase.equals("amazon_fr") || strLowercase.equals("amazon.fr") || strLowercase.equals("www.amazon.fr")) {
                return AMAZON_FR;
            } else if(strLowercase.equals("amazon_es") || strLowercase.equals("amazon.es") || strLowercase.equals("www.amazon.es")) {
                return AMAZON_ES;
            } else if(strLowercase.equals("amazon_de") || strLowercase.equals("amazon.de") || strLowercase.equals("www.amazon.de")) {
                return AMAZON_DE;
            } else if(strLowercase.equals("amazon_it") || strLowercase.equals("amazon.it") || strLowercase.equals("www.amazon.it")) {
                return AMAZON_IT;
            } else if(strLowercase.equals("amazon_us") || strLowercase.equals("amazon.com") || strLowercase.equals("www.amazon.com")) {
                return AMAZON_US;
            } else if(strLowercase.equals("ebay_uk") || strLowercase.equals("ebay.co.uk") || strLowercase.equals("www.ebay.co.uk")) {
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

        public String flatFinance() {
            //https://sellercentral.amazon.co.uk/gp/payments-account/export-transactions.html?ie=UTF8&pageSize=DownloadSize&daysAgo=Seven&subview=daysAgo&mostRecentLast=0&view=filter&eventType=
            switch(this) {
                case AMAZON_UK:
                case AMAZON_DE:
                case AMAZON_ES:
                case AMAZON_FR:
                case AMAZON_IT:
                case AMAZON_US:
                    return "https://sellercentral." + this.toString() + "/gp/payments-account/export-transactions.html?ie=UTF8&pageSize=DownloadSize&daysAgo=Seven&subview=daysAgo&mostRecentLast=0&view=filter&eventType=";
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
    public String uniqueName;

    /**
     * 哪一个市场
     */
    @Required
    @Column(nullable = false)
    @Enumerated(EnumType.STRING)
    public M type;

    /**
     * 市场登陆用户名
     */
    @Required
    @Column(nullable = false)
    public String username;

    /**
     * 市场登陆的密码
     */
    @Required
    @Column(nullable = false)
    public String password;

    /**
     * 不同市场所拥有的钥匙;
     * Secret Key; [Amazon]
     * Toekn; [Ebay]
     */
    public String token;

    public String accessKey;

    /**
     * Amazon MerchantID
     */
    public String merchantId;

    /**
     * 是否可用
     */
    public boolean closeable = false;

    @Transient
    public Set<Cookie> cookies;

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
                    HttpGet home = new HttpGet(this.type.homePage());
                    body = EntityUtils.toString(HTTP.client().execute(home).getEntity());

                    if(Play.mode.isDev())
                        FileUtils.writeStringToFile(new File(Constant.HOME + "/elcuk2-logs/" + this.type.name() + ".id_" + this.id + ".homepage.html"), body);

                    Document doc = Jsoup.parse(body);
                    Elements inputs = doc.select("form[name=signin] input");

                    if(inputs.size() == 0) {
                        Logger.info("WebSite [" + this.type.toString() + "] Still have the Session with User [" + this.username + "].");
                        return;
                    }

                    HttpPost login = new HttpPost(this.type.loginPage());
                    List<NameValuePair> params = new ArrayList<NameValuePair>();
                    for(Element el : inputs) {
                        String att = el.attr("name");
                        if("email".equals(att)) params.add(new BasicNameValuePair(att, this.username));
                        else if("password".equals(att)) params.add(new BasicNameValuePair(att, this.password));
                        else params.add(new BasicNameValuePair(att, el.val()));
                    }
                    login.setEntity(new UrlEncodedFormEntity(params));
                    body = EntityUtils.toString(HTTP.client().execute(login).getEntity());
                    if(Play.mode.isDev())
                        FileUtils.writeStringToFile(new File(Constant.HOME + "/elcuk2-logs/" + this.type.name() + ".id_" + this.id + ".afterLogin.html"), body);
                    Element navBar = Jsoup.parse(body).select("#topNavContainer").first();
                    if(navBar != null) Logger.info("Login Successful!");
                    else Logger.warn("Login Failed!");

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
        try {
            String body = HTTP.get(new HttpGet(this.type.homePage()));
            Document doc = Jsoup.parse(body);
            Element countries = doc.select("#merchant-website").first();
            if(countries == null) throw new NotLoginFastException();
            String value = null;
            for(Element ct : countries.select("option")) {
                switch(m) {
                    case AMAZON_UK:
                        if("www.amazon.co.uk".equalsIgnoreCase(ct.text().trim())) value = ct.attr("value");
                        break;
                    case AMAZON_DE:
                        if("www.amazon.de".equalsIgnoreCase(ct.text().trim())) value = ct.attr("value");
                        break;
                    case AMAZON_FR:
                        if("www.amazon.fr".equalsIgnoreCase(ct.text().trim())) value = ct.attr("value");
                        break;
                    case AMAZON_ES:
                        if("www.amazon.es".equalsIgnoreCase(ct.text().trim())) value = ct.attr("value");
                        break;
                    case AMAZON_IT:
                        if("www.amazon.it".equalsIgnoreCase(ct.text().trim())) value = ct.attr("value");
                        break;
                    default:
                        Logger.info("Not Support The Market out of Europe.");
                        return;
                    //ignore
                }
            }
            if(value == null) {
                Logger.warn("Value parse Error!");
                return;
            }
            HTTP.get(new HttpGet(this.type.changeRegion(value)));
        } catch(IOException e) {
            e.printStackTrace();
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
                feedback = new HttpGet(this.type.feedbackPage(page));
                try {
                    String body = HTTP.get(feedback);
                    if(Play.mode.isDev())
                        FileUtils.writeStringToFile(new File(Constant.HOME + "/elcuk2-logs/" + this.type.name() + ".id_" + this.id + "feedback_p" + page + ".html"), body);
                    return Feedback.parseFeedBackFromHTML(body);
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
        try {
            this.loginWebSite();
            this.changeRegion(market);
            Logger.info("Downloading [%s] File...", this.username);
            String body = HTTP.get(new HttpGet(this.type.flatFinance()));
            DateTime dt = DateTime.now();
            File f = new File(String.format("%s/%s/%s/%s_%s.txt", Constant.E_FINANCE, market, dt.toString("yyyy.MM"), this.username, dt.toString("yyyy.MM.dd_HH'h'")));
            Logger.info("File Save to :[" + f.getAbsolutePath() + "]");
            FileUtils.writeStringToFile(f, body);
            return f;
        } catch(IOException e) {
            Logger.warn(Webs.E(e));
        }
        return null;
    }

    /**
     * 搜索开发的所有账户
     *
     * @return
     */
    public static List<Account> openedAcc() {
        return Account.find("closeable=?", false).fetch();
    }

    @Override
    public String toString() {
        return StringUtils.split(this.username, "@")[0];
    }

}
