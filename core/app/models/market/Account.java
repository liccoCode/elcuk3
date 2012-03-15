package models.market;

import helper.Constant;
import helper.HTTP;
import org.apache.commons.io.FileUtils;
import org.apache.http.NameValuePair;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.cookie.Cookie;
import org.apache.http.message.BasicNameValuePair;
import org.apache.http.util.EntityUtils;
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
                try {
                    /**
                     * 1. Visit the website, fetch the new Cookie.
                     * 2. With the website params and user/password to login.
                     */
                    HttpGet home = new HttpGet("https://sellercentral.amazon.co.uk");
                    String body = EntityUtils.toString(HTTP.client().execute(home).getEntity());

                    if(Play.mode.isDev())
                        FileUtils.writeStringToFile(new File(Constant.HOME + "/elcuk2-logs/homepage.html"), body);

                    Document doc = Jsoup.parse(body);
                    Elements inputs = doc.select("form[name=signin] input");

                    if(inputs.size() == 0) {
                        Logger.info("WebSite [" + this.type.toString() + "] Still have the Session with User [" + this.username + "].");
                        return;
                    }

                    HttpPost login = new HttpPost("https://sellercentral.amazon.co.uk/gp/sign-in/sign-in.html/ref=xx_login_lgin_home");
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
                        FileUtils.writeStringToFile(new File(Constant.HOME + "/elcuk2-logs/afterLogin.html"), body);
                    Element navBar = Jsoup.parse(body).select("#topNavContainer").first();
                    if(navBar != null) Logger.info("Login Successful!");
                    else Logger.warn("Login Failed!");

                    HTTP.client().getCookieStore().clearExpired(new Date());
                } catch(Exception e) {
                    Logger.warn(e.getClass().getSimpleName() + "|" + e.getMessage());
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
            String body = HTTP.get(new HttpGet("https://sellercentral.amazon.co.uk"));
            Document doc = Jsoup.parse(body);
            Element countries = doc.select("#merchant-website").first();
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
            HTTP.get(new HttpGet("https://sellercentral.amazon.co.uk/gp/utilities/set-rainier-prefs.html?ie=UTF8&url=" +
                    "https%3A%2F%2Fsellercentral.amazon.co.uk%2Fgp%2Ffeedback-manager%2Fhome.html%2Fref%3" +
                    "Dag_feedback_dmar_allfeedbk%3Fie%3DUTF8%26_mpc%3D1&marketplaceID=" + value));
        } catch(IOException e) {
            e.printStackTrace();
        }
    }

    public List<Feedback> fetchFeedback(int page) {
        HttpGet feedback = null;
        switch(this.type) {
            case AMAZON_UK:
                feedback = new HttpGet("https://sellercentral.amazon.co.uk/gp/feedback-manager/view-all-feedback.html?ie=UTF8&sortType=sortByDate&pageSize=50&dateRange=&currentPage=" + page + "&descendingOrder=1");
                try {
                    String body = HTTP.get(feedback);
                    if(Play.mode.isDev())
                        FileUtils.writeStringToFile(new File(Constant.HOME + "/elcuk2-logs/feedback.p" + page + ".html"), body);
                    return Feedback.parseFeedBackFromHTML(body);
                } catch(Exception e) {
                    Logger.warn("Feedback page can not found Or the session is invalid!");
                }
                break;
        }
        return new ArrayList<Feedback>();
    }

    @Override
    public String toString() {
        return "Account{" +
                "uniqueName='" + uniqueName + '\'' +
                ", type=" + type +
                ", username='" + username + '\'' +
                ", closeable=" + closeable +
                '}';
    }

}
