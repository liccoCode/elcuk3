package models;

import helper.Extra;
import org.apache.commons.lang.StringUtils;
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

    /**
     * 这个 listing 是哪个市场的
     */
    public enum T {
        AMAZON,
        EBAY
    }

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

    public String picUrls;

    public List<ListingOffer> offers;

    /**
     * 根据指定的抓取的 Listing 类型进行解析
     *
     * @param type
     * @return
     */
    public Listing parseFromHTML(T type) {
        switch(type) {
            case AMAZON:
                parseAmazon();
                break;
            case EBAY:
                parseEbay();

        }
        this.html = null;// 清空 html
        return this;
    }

    private Listing parseAmazon() {
        // html
        Element root = Jsoup.parse(html);

        this.asin = root.select("#ASIN").val().toUpperCase();
        this.listingId = String.format("%s_%s", this.asin, root.select("#navLogoPrimary > span").text());
//        Elements images = root.select("#PIAltImagesDiv img"); // 这个图片导航 Amazon 是动态生成的...
        Element img = root.select("#prodImage").first();
        if(img != null) {
            //http://ecx.images-amazon.com/images/I/41p1LIEYgYL._SL75_AA30_.jpg
            //http://ecx.images-amazon.com/images/I/41p1LIEYgYL._SL500_AA300_.jpg
            this.picUrls = StringUtils.splitByWholeSeparator(img.attr("src"), "._")[0] + "._SL500_AA300_.jpg";
        } else {
            this.picUrls = "";
        }

        // Basic Listing Infos
        Element titleEl = root.select("#btAsinTitle").first();
        this.title = titleEl.text();
        this.byWho = titleEl.parent().nextElementSibling().text();

        // 通过 titleEl 的 id 定位元素后, 再进行 reviewSummary 的定位.
        Element reviewSumery = titleEl.parent().parent().nextElementSibling().select(".asinReviewsSummary").first();
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

        Element middlePd = root.select("#technical_details").first();
        this.technicalDetails = middlePd.parent().outerHtml();

        Element saleRankEl = root.select("#SalesRank").first();
        if(saleRankEl == null) this.saleRank = 5001;
        else
            this.saleRank = Extra.flt(saleRankEl.childNode(2).toString().replaceAll(",", "").replaceAll(".", "")/*由于排名没有小数点后面, 所以直接去除这个*/).intValue();
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
            buybox.fba = false;
        } else {
            Element fbaTextLink = root.select("#SSOFpopoverLink").first();
            if(fbaTextLink == null) {
                /**
                 *  没有 fbalink 还几种情况:
                 *  1. 产品正常销售, Amazon 绿色
                 *  2. 产品无法销售, Currently unavailable 红色
                 *  3. 产品预期到货, 橙色
                 */
                Element tmpEl = root.select(".availGreen").first();
                boolean goon = true; // 是否继续处理
                if(tmpEl == null) {
                    tmpEl = root.select(".availRed").first();
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
            buybox.offerId = root.select("#merchantID").val().toUpperCase();
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
        return this;
    }

    private Listing parseEbay() {
        // Not Yet...
        return this;
    }
}
