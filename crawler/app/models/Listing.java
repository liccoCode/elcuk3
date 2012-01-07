package models;

import helper.Extra;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import play.Logger;

import java.util.ArrayList;
import java.util.List;

/**
 * 抓取使用的 Listing 信息
 * User: Wyatt
 * Date: 12-1-2
 * Time: 下午4:36
 */
public class Listing {

    public Listing(String html) {
        this.html = html;
    }

    public String listingId;

    /**
     * 解析使用的纯 HTML 代码
     */
    public String html;

    public String asin;

    public String title;

    /**
     * title xxxx by [??]
     */
    public String byWho;

    public int reviews;

    public float rating;

    public String technicalDetails;

    public String productDescription;

    /**
     * 如果搜索不到 salerank, 那么则直接归属到 5001
     */
    public int saleRank;

    public int totalOffers;

    public List<ListingOffer> offers;

    public Listing parseFromHTML() {
        // html
        Element root = Jsoup.parse(html);

        this.asin = root.select("#ASIN").val().toUpperCase();
        this.listingId = String.format("%s_%s", this.asin, root.select("#navLogoPrimary > span").text());

        // Basic Listing Infos
        Element titleEl = root.select("#btAsinTitle").first();
        this.title = titleEl.text();
        this.byWho = titleEl.parent().nextElementSibling().text();

        Element reviewSumery = root.select(".asinReviewsSummary").first();
        if(reviewSumery == null) { // 还没有 review 呢
            this.reviews = 0;
            this.rating = 0f;
        } else {
            this.reviews = Extra.flt(reviewSumery.nextElementSibling().text()).intValue();
            this.rating = Extra.flt(reviewSumery.select(".swSprite").first().text());
        }

        Element totalOffersEl = root.select("#secondaryUsedAndNew a").first();
        if(totalOffersEl == null) this.totalOffers = 1;
        else this.totalOffers = Extra.flt(totalOffersEl.text()).intValue();

        Element middlePd = root.select("#productDetails").first();
        this.technicalDetails = middlePd.previousElementSibling().outerHtml();

        Element saleRankEl = root.select("#SalesRank").first();
        if(saleRankEl == null) this.saleRank = 5001;
        else this.saleRank = Extra.flt(saleRankEl.childNode(2).toString()).intValue();
        if(this.saleRank <= 0) this.saleRank = 1000; // 如果到达这一步表示其有排名, 但是没有大类别排名, 所以给予一个 1000 的自定义排名


        this.productDescription = root.select("#productDescription").outerHtml();

        // ListingOffers Infos
        List<ListingOffer> offers = new ArrayList<ListingOffer>();
        // buybox
        Element noFbaPrice = root.select("#pricePlusShippingQty").first();
        ListingOffer buybox = new ListingOffer();
        buybox.buybox = true;
        if(noFbaPrice != null) {
            buybox.price = Extra.flt(noFbaPrice.select(".price").text());
            buybox.shipprice = Extra.flt(noFbaPrice.select(".plusShippingText").text());
            buybox.offerId = root.select("#merchantID").val().toUpperCase();
            buybox.name = root.select("#BBAvailPlusMerchID b").first().text();
        } else {
            Element fbaTextLink = root.select("#SSOFpopoverLink").first();
            if(fbaTextLink == null) {
                buybox.name = "Currently unavailable";
                buybox.fba = true;
                buybox.offerId = root.select("#merchantID").val().toUpperCase();
            } else {
                buybox.fba = false;
                buybox.buybox = false;
                buybox.name = fbaTextLink.previousElementSibling().text();
            }

            buybox.price = Extra.flt(root.select("#actualPriceValue").text());
            buybox.shipprice = 0;
        }
        offers.add(buybox);

        // more sellers
        Elements moreSellers = root.select("#more-buying-choice-content-div > .mbcOffers tr[id]");
        for(Element seller : moreSellers) {
            try {
                ListingOffer moreSeller = new ListingOffer();
                moreSeller.offerId = seller.id().split("_")[2].toUpperCase();
                moreSeller.name = seller.select(".mbcMerch >td").first().text();
                moreSeller.price = Extra.flt(seller.select(".mbcPriceCell").text());
                String shippingText = seller.select(".plusShippingText").text().toUpperCase();
                moreSeller.shipprice = Extra.flt(shippingText);
                if(shippingText.contains("&") || shippingText.contains("FREE")) moreSeller.fba = true;
                offers.add(moreSeller);
            } catch(Exception e) {
                Logger.error("Parse More Buyers have some error! [%s]", e.getMessage());
            }
        }

        this.offers = offers;
        this.html = null;// 清空 html
        return this;
    }
}
