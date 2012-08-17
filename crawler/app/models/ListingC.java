package models;

import helper.Extra;
import helper.Webs;
import org.apache.commons.lang.StringUtils;
import org.apache.commons.lang.math.NumberUtils;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import play.Logger;

import java.util.ArrayList;
import java.util.List;

/**
 * 抓取使用的 ListingC 信息
 * User: Wyatt
 * Date: 12-1-2
 * Time: 下午4:36
 */
public class ListingC {

    /**
     * 这个 listing 是哪个市场的
     */
    public enum T {
        AMAZON,
        EBAY
    }

    public enum S {
        NORMAL,
        CLOSE
    }

    public MT market;


    public String listingId;

    /**
     * 返回用的 html
     */
    public String msg;

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

    public String picUrls;

    /**
     * like 的数量
     */
    public int likes;

    public List<ListingOfferC> offers;

    public S state;

    public ListingC() {
    }

    public ListingC(String msg) {
        this.msg = msg;
    }

    public static ListingC parseAmazon(Document doc) {
        ListingC lst = new ListingC();
        if(StringUtils.contains(doc.select("title").text(), "404")) {
            lst.state = S.CLOSE;
            return lst;
        }
        lst.state = S.NORMAL;

        lst.asin = doc.select("#ASIN").val().toUpperCase();
        String site = doc.select(".navFooterLogoLine img").attr("alt");
        lst.market = MT.val(site);
        lst.listingId = String.format("%s_%s", lst.asin, site);
//        Elements images = doc.select("#PIAltImagesDiv img"); // 这个图片导航 Amazon 是动态生成的...
        Element img = doc.select("#prodImage").first();
        if(img != null) {
            //http://ecx.images-amazon.com/images/I/41p1LIEYgYL._SL75_AA30_.jpg
            //http://ecx.images-amazon.com/images/I/41p1LIEYgYL._SL500_AA300_.jpg
            lst.picUrls = StringUtils.splitByWholeSeparator(img.attr("src"), "._")[0] + "._SL500_AA300_.jpg";
        } else {
            lst.picUrls = "";
        }

        // Basic ListingC Infos
        Element titleEl = doc.select("#btAsinTitle").first();
        lst.title = titleEl.text();
        lst.byWho = titleEl.parent().nextElementSibling().text();

        // 通过 titleEl 的 id 定位元素后, 再进行 reviewSummary 的定位.
        Element reviewSumery = titleEl.parent().parent().nextElementSibling().select(".asinReviewsSummary").first();
        if(reviewSumery == null) { // 还没有 review 呢
            lst.reviews = 0;
            lst.rating = 0f;
        } else {
            lst.reviews = Extra.flt(reviewSumery.nextElementSibling().text()).intValue();
            lst.rating = Extra.flt(reviewSumery.select(".swSprite").first().text());
        }

        lst.likes = NumberUtils.toInt(doc.select(String.format("#amznLike_%s .amazonLikeCount", lst.asin)).text());

        Element totalOffersEl = doc.select("#secondaryUsedAndNew a").first();
        if(totalOffersEl == null) lst.totalOffers = 1;
        else lst.totalOffers = Extra.flt(totalOffersEl.text()).intValue();

        Element middlePd = doc.select("#technical_details").first();
        if(middlePd != null) lst.technicalDetails = middlePd.parent().outerHtml();

        Element saleRankEl = doc.select("#SalesRank").first();
        if(saleRankEl == null) lst.saleRank = 5001;
        else {
            String comma = StringUtils.replace(saleRankEl.childNode(2).toString(), ",", "");
            lst.saleRank = Extra.flt(StringUtils.replace(comma, ".", "")/*由于排名没有小数点后面, 所以直接去除这个*/).intValue();
        }
        if(lst.saleRank <= 0) lst.saleRank = 1000; // 如果到达这一步表示其有排名, 但是没有大类别排名, 所以给予一个 1000 的自定义排名


        lst.productDescription = doc.select("#productDescription").outerHtml();

        // ListingOffers Infos
        List<ListingOfferC> offers = new ArrayList<ListingOfferC>();

        // buybox, 并不是一定会有 buybox(异常)
        Element noFbaPrice = doc.select("#pricePlusShippingQty").first();
        try {
            ListingOfferC buybox = new ListingOfferC();
            buybox.buybox = true;
            if(noFbaPrice != null) {
                buybox.price = ListingC.amazonPrice(lst.market, noFbaPrice.select(".price").text());
                buybox.shipprice = ListingC.amazonPrice(lst.market, noFbaPrice.select(".plusShippingText").text());
                buybox.offerId = doc.select("#merchantID").val().toUpperCase();
                buybox.name = doc.select("#BBAvailPlusMerchID b").first().text();
                buybox.fba = false;
            } else {
                Element fbaTextLink = doc.select("#SSOFpopoverLink").first();
                if(fbaTextLink == null) {
                    /**
                     *  没有 fbalink 还几种情况:
                     *  1. 产品正常销售, Amazon 绿色
                     *  2. 产品无法销售, Currently unavailable 红色
                     *  3. 产品预期到货, 橙色
                     */
                    Element tmpEl = doc.select(".availGreen").first();
                    boolean goon = true; // 是否继续处理
                    if(tmpEl == null) {
                        tmpEl = doc.select(".availRed").first();
                    } else { // availGreen
                        goon = false;
                        buybox.name = tmpEl.parent().select("b").first().text();
                        buybox.buybox = buybox.fba = true; // Selled by Amazon.
                    }
                    if(tmpEl == null) {
                        //TODO 搜索橙色的
                    } else if(goon) { // availRed
                        goon = false;
                        buybox.name = tmpEl.text();
                        buybox.buybox = buybox.fba = false;
                    }
                    if(tmpEl == null) {
                        //绿色, 红色, 橙色都找不到了... 直接记录 offers 什么都没找到
                        buybox.name = "Find Nothing.";
                        buybox.buybox = buybox.fba = false;
                    } else if(goon) {
                        goon = false;
                        buybox.name = "橙色的提示, 预期到货";
                        buybox.buybox = buybox.fba = false;
                    }
                } else { // 正常抓取到了 FBA
                    buybox.fba = true;
                    buybox.name = fbaTextLink.previousElementSibling().text();
                }
                buybox.offerId = doc.select("#merchantID").val().toUpperCase();
                buybox.price = ListingC.amazonPrice(lst.market, doc.select(".priceLarge").text());
                buybox.shipprice = 0;
            }
            offers.add(buybox);
        } catch(Exception e) {
            Logger.warn(e.getClass().getSimpleName() + "|" + e.getMessage() + "|" + "No Buybox.");
        }

        // more sellers
        Elements moreSellers = doc.select("#more-buying-choice-content-div > .mbcOffers tr[id]");
        for(Element seller : moreSellers) {
            try {
                ListingOfferC moreSeller = new ListingOfferC();
                moreSeller.offerId = seller.id().split("_")[2].toUpperCase();
                moreSeller.name = seller.select(".mbcMerch >td").first().text();
                moreSeller.price = ListingC.amazonPrice(lst.market, seller.select(".mbcPriceCell").text());
                String shippingText = seller.select(".plusShippingText").text().toUpperCase();
                if(shippingText.contains("&") || shippingText.contains("FREE")) {
                    moreSeller.fba = true;
                    moreSeller.shipprice = 0f;
                } else {
                    moreSeller.shipprice = ListingC.amazonPrice(lst.market, shippingText);
                }
                offers.add(moreSeller);
            } catch(Exception e) {
                Logger.error("Parse More Buyers have some error! [%s]", e.getMessage());
            }
        }
        lst.offers = offers;
        return lst;
    }

    public static Float amazonPrice(MT mt, String priceStr) {
        try {
            switch(mt) {
                case AUK:
                case AUS:
                    return Webs.amazonPriceNumber(mt, priceStr.substring(1));
                case ADE:
                case AIT:
                case AES:
                case AFR:
                    return Webs.amazonPriceNumber(mt, priceStr.split(" ")[1]);
                default:
                    return 0f;

            }
        } catch(Exception e) {
            Logger.warn("ListingC.amazonPrice error.(" + mt.toString() + ") [" + priceStr + "|" + e.getMessage() + "]");
            return 0f;
        }
    }

    private ListingC parseEbay() {
        // Not Yet...
        return this;
    }
}
