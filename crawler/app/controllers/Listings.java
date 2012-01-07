package controllers;

import models.Listing;
import play.data.validation.Validation;
import play.libs.WS;
import play.mvc.Controller;

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
            renderJSON(Validation.errors());
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
            renderJSON("{flag:false, message:'invalid market[us,uk,de,it]'}");
        }
        String html = WS.url(String.format("%s/dp/%s", url.toString(), asin)).get().getString("UTF-8");
//        IO.writeContent(html, new File(String.format("/tmp/%s.%s.html", asin, market)), "UTF-8");
//        String html = IO.readContentAsString(new File(String.format("/tmp/%s.%s.html", asin, market)), "UTF-8");
        renderJSON(new Listing(html).parseFromHTML());
    }
}
