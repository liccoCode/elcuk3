package controllers;

import models.Listing;
import play.data.validation.Validation;
import play.libs.IO;
import play.libs.WS;
import play.mvc.Controller;

import java.io.File;

public class Listings extends Controller {

    /**
     * 根据市场, asin 来抓取 Listing
     *
     * @param market us, uk, de, it
     * @param asin   amazon asin
     */
    public static void crawl(String market, String asin) {
        validation.required(market);
        validation.required(asin);
        if(Validation.hasErrors()) {
            renderJSON(validation.errorsMap());
        }
        market = market.toLowerCase();
        StringBuilder url = new StringBuilder("http://www.amazon.");
        if("us".equals(market)) {
            url.append("com");
        } else if("uk".equals(market)) {
            url.append("co.uk");
        } else if("de".equals(market)) {
            url.append("de");
        } else if("es".equals(market)) {
            url.append("es");
        } else if("fr".equals(market)) {
            url.append("fr");
        } else if("it".equals(market)) {
            url.append("it");
        } else {
            renderJSON("{flag:false, message:'invalid market[us,uk,de,it,es,fr]'}");
        }
        String html = WS.url(String.format("%s/dp/%s", url.toString(), asin)).get().getString();
        IO.writeContent(html, new File(String.format("/tmp/%s.%s.html", asin, market)), "UTF-8");
//        String html = IO.readContentAsString(new File(String.format("/tmp/%s.%s.html", asin, market)), "UTF-8");
        // TODO 根据 asin 的规则判断是 Amazon 还是 Ebay
        try {
            renderJSON(new Listing(html).parseFromHTML(Listing.T.AMAZON));
        } catch(NullPointerException e) {
            renderJSON(new Listing(e.getMessage()));
        }
    }

    /**
     * 抓取这个 Listing 的 Review, 一次性最多抓取 5 页或者抓取到最后一页中所有的 Reviews;
     * @param market
     * @param asin
     */
    public static void crawlReview(String market, String asin) {
        /**
         * 持续抓取, 直到抓取回来的次数 > 5 或者次数与最大页面一样则不再进行抓取;
         * 抓取过程中解析出得 Review 全部返回.
         */
    }
}
