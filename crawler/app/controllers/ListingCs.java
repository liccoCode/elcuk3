package controllers;

import helper.HTTP;
import models.AmazonListingReview;
import models.ListingC;
import models.ListingOfferC;
import models.MT;
import org.apache.commons.io.FileUtils;
import org.apache.commons.lang.StringUtils;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import play.Logger;
import play.Play;
import play.data.validation.Validation;
import play.mvc.Controller;
import play.utils.FastRuntimeException;

import java.io.File;
import java.io.IOException;
import java.util.HashSet;
import java.util.Set;

public class ListingCs extends Controller {

    /**
     * 根据市场, asin 来抓取 ListingC
     *
     * @param market us, uk, de, it
     * @param asin   amazon asin
     */
    public static void crawl(String market, String asin) throws IOException {
        validation.required(market);
        validation.required(asin);
        if(Validation.hasErrors()) {
            renderJSON(validation.errorsMap());
        }
        MT m = MT.val(market);
        if(m == null) renderJSON("{flag:false, message:'invalid market[us,uk,de,it,es,fr]'}");
        HTTP.clearExpiredCookie();
        String html = HTTP.get(m.listing(asin));
        if(Play.mode.isDev())
            FileUtils.writeStringToFile(new File(String.format("%s/elcuk2-data/listings/%s/%s.html", System.getProperty("user.home"), m.name(), asin)), html);
        // TODO 根据 asin 的规则判断是 Amazon 还是 Ebay
        try {
            renderJSON(ListingC.parseAmazon(Jsoup.parse(html)));
        } catch(NullPointerException e) {
            renderJSON(new ListingC());
        }
    }

    public static void crawlOffers(String market, String asin) throws IOException {
        ////http://www.amazon.co.uk/gp/offer-listing/B007TR9VRU
        MT m = MT.val(market);
        if(m == null) throw new FastRuntimeException("Market is inValid!");
        String url = m.offers(asin);
        if(StringUtils.isBlank(url)) throw new FastRuntimeException("Offer URL null, an unsupport Market.");
        HTTP.clearExpiredCookie();
        String html = HTTP.get(url);
        if(Play.mode.isDev())
            FileUtils.writeStringToFile(new File(String.format("%s/elcuk2-data/listings/offers/%s/%s.html", System.getProperty("user.home"), m.name(), asin)), html);
        try {
            renderJSON(ListingOfferC.parseOffers(m, html));
        } catch(Exception e) {
            render(new ListingOfferC());
        }
    }

    /**
     * 抓取这个 ListingC 的 Review, 一次性最多抓取 5 页或者抓取到最后一页中所有的 Reviews;
     *
     * @param market
     * @param asin
     */
    public static void crawlReview(String market, final String asin) throws IOException {
        /**
         * 持续抓取, 直到抓取回来的次数 > 5 或者次数与最大页面一样则不再进行抓取;
         * 抓取过程中解析出得 Review 全部返回.
         */
        int maxPage = 1;
        Set<AmazonListingReview> reviews = new HashSet<AmazonListingReview>();
        final MT m = MT.val(market);
        if(m == null) throw new FastRuntimeException("Market is inValid!");
        int page = 1;
        while(true) {
            String url = m.review(asin, page);
            if(StringUtils.isBlank(url)) continue;
            Logger.info("Fetch [%s]", url);
            HTTP.clearExpiredCookie();
            String html = HTTP.get(url);

            if(Play.mode.isDev())
                FileUtils.writeStringToFile(new File(String.format("%s/elcuk2-data/reviews/%s/%s_%s.html", System.getProperty("user.home"), m.name(), asin, page)), html);

            Document doc = Jsoup.parse(html);
            reviews.addAll(AmazonListingReview.parseReviewFromHTML(doc, page));
            maxPage = AmazonListingReview.maxPage(doc);
            Logger.info("Page: %s / %s, Total Reviews: %s", page, maxPage, reviews.size());
            if(page++ == maxPage) break;
        }
        renderJSON(AmazonListingReview.filterReviewWithAsinAndMarket(asin, m, reviews));
    }
}
