package models;

import org.apache.commons.lang.StringUtils;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import play.Logger;

import java.util.ArrayList;
import java.util.List;

/**
 * ListingC 相关的买家信息
 * User: Wyatt
 * Date: 12-1-2
 * Time: 下午4:37
 */
public class ListingOfferC {

    public ListingOfferC() {
        this.fba = false;
        this.buybox = false;
    }

    public ListingOfferC(MT m) {
        this();
        this.market = m;
    }

    public MT market;

    public String name;

    public String offerId;

    public float price;

    public float shipprice;

    public boolean fba;

    public boolean buybox;

    public String cond;


    /**
     * 通过 MT.offers 返回的链接解析所有的 Offers (卖家)
     *
     * @return
     */
    public static List<ListingOfferC> parseOffers(MT market, Document doc) {
        List<ListingOfferC> offers = new ArrayList<ListingOfferC>();
        Elements offersEl = doc.select("#bucketnew tbody[class=result]");
        for(Element offer : offersEl) {
            ListingOfferC off = ListingOfferC.parseOneOffer(market, offer);
            offers.add(off);
        }
        return offers;
    }

    /**
     * 解析一个 Offer tbody
     *
     * @param market
     * @param offer
     * @return
     */
    private static ListingOfferC parseOneOffer(MT market, Element offer) {
        ListingOfferC off = new ListingOfferC();
        Element priceEl = offer.select("td:eq(0)").first();
        Element confitionEl = offer.select("td:eq(1)").first();
        Element nameEl = offer.select("td:eq(2) ul.sellerInformation").first();

        // 第一栏
        if(priceEl.select(".supersaver").first() != null) {
            off.fba = true;
            off.shipprice = 0f;
        } else {
            String shipPriceStr = priceEl.select(".price_shipping").text();
            int index = shipPriceStr.indexOf(" ");
            off.shipprice = ListingC.amazonPrice(market, shipPriceStr.substring(index + 1)); /*排除那个空格*/
        }
        off.market = market;
        off.price = ListingC.amazonPrice(market, priceEl.select(".price").text());

        // 第二栏
        String conditionText = (confitionEl.text() == null ? "" : confitionEl.text()).toLowerCase();
        if(StringUtils.contains(conditionText, "used"))
            off.cond = "used";
        else if(StringUtils.contains(conditionText, " refurbish"))
            off.cond = "refurbished";
        else if(StringUtils.contains(conditionText, "new"))
            off.cond = "new";
        else
            off.cond = "new";


        /*
     * 第三栏
     * 有图片的卖家的处理(Amazon)
    */
        Element storeImg = nameEl.select("> img").first();
        if(storeImg != null) { // Amazon 自己为卖家
            off.name = storeImg.attr("alt");
            off.offerId = off.market.toString();
            off.fba = true;
            off.buybox = false;
            off.shipprice = 0f;
        } else {
            // 有图片的卖家处理
            storeImg = nameEl.select("> a img").first();
            if(storeImg != null) {
                off.name = storeImg.attr("alt");
                off.offerId = StringUtils.splitByWholeSeparator(
                        StringUtils.splitByWholeSeparator(storeImg.parent().attr("href"), "shops/")[1], "/ref")[0];
            } else {
                // 没图片的卖家处理
                off.name = nameEl.select("div.seller b").text();
                off.offerId = StringUtils.splitByWholeSeparator(nameEl.select("div.seller a").first().attr("href"), "seller=")[1];
            }
        }
        return off;
    }


    @Override
    public String toString() {
        final StringBuilder sb = new StringBuilder();
        sb.append("ListingOfferC");
        sb.append("{name='").append(name).append('\'');
        sb.append(", offerId='").append(offerId).append('\'');
        sb.append(", price=").append(price);
        sb.append(", shipprice=").append(shipprice);
        sb.append(", fba=").append(fba);
        sb.append(", buybox=").append(buybox);
        sb.append('}');
        return sb.toString();
    }
}
