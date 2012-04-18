package controllers;

import models.ARW;
import models.AmazonListingReview;
import models.Listing;
import models.MT;
import org.apache.commons.io.FileUtils;
import org.apache.commons.lang.StringUtils;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import play.Logger;
import play.Play;
import play.data.validation.Validation;
import play.libs.IO;
import play.libs.WS;
import play.mvc.Controller;
import play.utils.FastRuntimeException;

import java.io.File;
import java.io.IOException;
import java.util.HashSet;
import java.util.Set;

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
        MT m = MT.val(market);
        if(m == null) renderJSON("{flag:false, message:'invalid market[us,uk,de,it,es,fr]'}");
        String html = WS.url(ARW.listing(m, asin)).get().getString();
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
     *
     * @param market
     * @param asin
     */
    public static void crawlReview(String market, String asin) throws IOException {
        /**
         * 持续抓取, 直到抓取回来的次数 > 5 或者次数与最大页面一样则不再进行抓取;
         * 抓取过程中解析出得 Review 全部返回.
         */
        int maxPage = 11;
        Set<AmazonListingReview> reviews = new HashSet<AmazonListingReview>();
        for(int p = 1; p <= 10; p++) { // 最多 10 页
            if(p > maxPage) continue;
            MT m = MT.val(market);
            if(m == null) throw new FastRuntimeException("Market is inValid!");
            String url = ARW.review(m, asin, p);
            if(StringUtils.isBlank(url)) continue;
            Logger.info("Fetch [%s]", url);
            String html = WS.url(url).get().getString();

            if(Play.mode == Play.Mode.DEV)
                FileUtils.writeStringToFile(new File(String.format("%s/elcuk2-data/reviews/%s/%s_%s.html", System.getProperty("user.home"), m.name(), asin, p)), html);

            Document doc = Jsoup.parse(html);
            if(maxPage == 11) maxPage = AmazonListingReview.maxPage(doc);
            reviews.addAll(AmazonListingReview.parseReviewFromHTML(doc));
            Logger.info("Total Page: %s, Total Reviews: %s", maxPage, reviews.size());
        }

        renderJSON(reviews);
    }
}
