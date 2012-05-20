package models.market;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.annotations.Expose;
import helper.*;
import models.product.Product;
import notifiers.Mails;
import org.apache.commons.lang.StringUtils;
import org.apache.http.NameValuePair;
import org.apache.http.message.BasicNameValuePair;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import play.Logger;
import play.Play;
import play.db.jpa.GenericModel;
import play.utils.FastRuntimeException;

import javax.persistence.*;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * Listing 对应的是不同渠道上 Listing 的信息
 * User: wyattpan
 * Date: 12/27/11
 * Time: 10:27 PM
 */
@Entity
public class Listing extends GenericModel {
    /**
     * Condition
     */
    public enum C {
        NEW
    }


    /**
     * 不能级联删除, 并且删除 Listing 的时候需要保证 Selling 都已经处理了
     */
    @OneToMany(mappedBy = "listing", cascade = {CascadeType.DETACH, CascadeType.MERGE, CascadeType.PERSIST, CascadeType.REFRESH})
    public List<Selling> sellings;

    @OneToMany(mappedBy = "listing", cascade = CascadeType.ALL)
    public List<ListingOffer> offers;

    @OneToMany(mappedBy = "listing")
    @OrderBy(value = "createDate DESC")
    public List<AmazonListingReview> listingReviews;

    @ManyToOne
    public Product product;

    /**
     * 用来表示唯一的 ListingId, [asin]_[market]
     */
    @Id
    @Expose
    public String listingId;

    @Column(nullable = false)
    @Expose
    public String asin;

    @Column(nullable = false)
    @Enumerated(EnumType.STRING)
    @Expose
    public Account.M market;

    @Column(nullable = false)
    @Lob
    @Expose
    public String title;
    /**
     * title xxxx by [??]
     */
    @Expose
    public String byWho;

    @Expose
    public Integer reviews;

    @Expose
    public Float rating;

    @Lob
    @Expose
    public String technicalDetails;

    @Lob
    @Expose
    public String productDescription;

    /**
     * 抓取的图片的 URLs, 使用 Webs.SPLIT(|-|) 进行分割
     */
    @Lob
    @Expose
    public String picUrls;

    /**
     * 此 Listing 是否需要进行警告的的标识, 并且记录警告了多少次.
     */
    @Expose
    public Integer warnningTimes;

    /**
     * 如果搜索不到 salerank, 那么则直接归属到 5001
     */
    @Expose
    public Integer saleRank;

    @Expose
    public Integer totalOffers;

    /**
     * 代表从 Category 页面抓取的时候显示的价格, 或者 buybox 的价格.
     */
    @Expose
    public Float displayPrice;

    @Expose
    public Long lastUpdateTime;

    @Expose
    public Long nextUpdateTime;


    public void setAsin(String asin) {
        this.asin = asin;
        if(this.market != null && this.asin != null)
            initListingId();
    }

    public void setMarket(Account.M market) {
        this.market = market;
        if(this.asin != null && this.market != null)
            initListingId();
    }

    private void initListingId() {
        this.listingId = String.format("%s_%s", this.asin, this.market.toString());
    }

    public void updateAttrs(Listing newListing) {
        if(!this.equals(newListing)) return; // 不是一个 Listing 则不允许更新了
//        newListing.asin 这个不更新
        if(newListing.market != null) this.market = newListing.market;
        if(StringUtils.isNotBlank(newListing.byWho)) this.byWho = newListing.byWho;
        if(StringUtils.isNotBlank(newListing.title)) this.title = newListing.title;
        if(newListing.reviews != null) this.reviews = newListing.reviews;
        if(newListing.rating != null) this.rating = newListing.rating;
        if(StringUtils.isNotBlank(newListing.technicalDetails)) this.technicalDetails = newListing.technicalDetails;
        if(StringUtils.isNotBlank(newListing.productDescription))
            this.productDescription = newListing.productDescription;
        if(newListing.saleRank != null) this.saleRank = newListing.saleRank;
        if(newListing.totalOffers != null) this.totalOffers = newListing.totalOffers;
        if(StringUtils.isNotBlank(newListing.picUrls)) this.picUrls = newListing.picUrls;
        if(newListing.offers != null && newListing.offers.size() > 0) {
            for(ListingOffer lo : this.offers) lo.delete(); //清理掉原来的 ListingOffers
            this.offers = newListing.offers;
        }
        this.lastUpdateTime = System.currentTimeMillis();
        this.save();
    }

    /**
     * <pre>
     * 此 Listing 进行上架
     * TODO: 包含一些写死的 Selling 默认值
     * - type=FBA
     * - priceStrategy
     * - state=NEW (形成一个占位符, 具体的开始销售在 Selling 对象自身完成)
     * </pre>
     *
     * @param lst
     * @param selling
     * @return
     */
    public static Selling saleAmazon(Listing lst, Selling selling) {
        /**
         * 0. 属性的逻辑性检查
         *  - TODO UPC 相同的 Selling 需要对 MSKU 进行一致性检查
         * 1. 将 Listing 相关信息同步到 Selling 上
         * 2. 检查 UPC 的值, 这个值需要在这检查一下已经使用过的 UPC 与还没有使用的 UPC.
         * 3. 开始上架
         */
        selling.listing = lst;
        selling.price = selling.aps.salePrice;
        selling.market = lst.market;

        selling.type = Selling.T.FBA;
        selling.priceStrategy = new PriceStrategy(selling);
        selling.state = Selling.S.NEW;

        selling.aps.keyFetures = StringUtils.join(selling.aps.keyFeturess, Webs.SPLIT);
        selling.aps.searchTerms = StringUtils.join(selling.aps.searchTermss, Webs.SPLIT);
        selling.aps.RBN = StringUtils.join(selling.aps.rbns, ",");

        synchronized(selling.account.cookieStore()) {
            selling.account.changeRegion(selling.market);
            /**
             * !. 前提,在确定了账户的 Region 的情况下!
             * 1. 访问 https://catalog-sc.amazon.co.uk/abis/Classify/SelectCategory 的 classify 页面, 有一个隐藏 token
             * 2. 拿着隐藏 Token 访问 https://catalog-sc.amazon.co.uk/abis/Classify/SelectCategory 进入 identify 页面
             * 3. 提交创建 Selling 的参数
             */

            // --------------   1   -------------------
            String body = HTTP.get(selling.account.cookieStore(), selling.account.type.saleSellingLink()/*从账户所在的 Market 提交*/);
            if(Play.mode.isDev())
                Devs.fileLog(String.format("%s.%s.step1.html", selling.merchantSKU, selling.account.id), body, Devs.T.SALES);

            Document doc = Jsoup.parse(body);
            Elements inputs = doc.select("form[name=selectProductTypeForm] input");
            Set<NameValuePair> classifyHiddenParams = new HashSet<NameValuePair>();
            for(Element input : inputs) {
                String name = input.attr("name");
                if("newCategory".equals(name)) { //TODO 这里的类别先写死, 需要将这个类别与系统内的类别挂钩
                    classifyHiddenParams.add(new BasicNameValuePair(name, "consumer_electronics/consumer_electronics"));
                } else classifyHiddenParams.add(new BasicNameValuePair(name, input.val()));
            }

            body = HTTP.post(selling.account.cookieStore(), selling.account.type.saleSellingLink()/*从账户所在的 Market 提交*/, classifyHiddenParams);
            if(Play.mode.isDev())
                Devs.fileLog(String.format("%s.%s.step2.html", selling.merchantSKU, selling.account.id), body, Devs.T.SALES);
            doc = Jsoup.parse(body);

            //  ------------------ 2 -----------------
            Set<NameValuePair> addSellingPrams = new HashSet<NameValuePair>();
            inputs = doc.select("form[name=productForm] input");
            if(inputs == null || inputs.size() <= 7) throw new FastRuntimeException("没有进入第二步 Identify 页面!");
            for(Element input : inputs) {
                String name = input.attr("name");
                String tagType = input.attr("type");
                if("radio".equals(tagType)) { //对于 radio 的只能选择 checked 的,不能让后面的元素把前面的值给覆盖了.
                    if(StringUtils.isBlank(input.attr("checked"))) continue;
                    addSellingPrams.add(new BasicNameValuePair(name, input.val()));
                } else if("checkbox".equals(tagType)) {
                    /**
                     * Amazon  中 checkbox 都是不需要提交的. 都有一个 hidden 的元素与其对应
                     * - offering_can_be_gift_wrapped
                     * - offering_can_be_gift_messaged
                     * - connector_gender(female/female)
                     * - connector_gender(male/male)
                     * - connector_gender(male/female)
                     * - hot_shoe_included
                     * - traffic_features_description(Traffic only)
                     * - traffic_features_description(Live HD traffic)
                     * - traffic_features_description(Live traffic)
                     * - are_batteries_included
                     * - is_discontinued_by_manufacturer
                     * 有以上这么些元素收到影响
                     */

                } else { // 非 Radio 的 input 按照如下处理
                    if("item_name".equals(name))
                        addSellingPrams.add(new BasicNameValuePair(name, selling.aps.title));
                    else if("manufacturer".equals(name))
                        addSellingPrams.add(new BasicNameValuePair(name, selling.aps.manufacturer));
                    else if("brand_name".equals(name))
                        addSellingPrams.add(new BasicNameValuePair(name, "EasyAcc")); // ?? 这个品牌的名字现在都使用我们自己的?
                    else if("part_number".equals(name))
                        addSellingPrams.add(new BasicNameValuePair(name, selling.aps.manufacturerPartNumber)); //TODO 不记得了...
                    else if("model".equals(name))
                        addSellingPrams.add(new BasicNameValuePair(name, selling.aps.modelNumber)); // TODO 不记得了...
                    else if("external_id".equals(name))
                        addSellingPrams.add(new BasicNameValuePair(name, selling.aps.upc));
                    else if("offering_sku".equals(name))
                        addSellingPrams.add(new BasicNameValuePair(name, selling.merchantSKU));
                    else if("our_price".equals(name))
                        addSellingPrams.add(new BasicNameValuePair(name, Webs.priceLocalNumberFormat(selling.market, selling.aps.standerPrice)));
                    else if("discounted_price".equals(name))
                        addSellingPrams.add(new BasicNameValuePair(name, Webs.priceLocalNumberFormat(selling.market, selling.aps.salePrice)));
                    else if("discounted_price_start_date".equals(name))
                        addSellingPrams.add(new BasicNameValuePair(name, Dates.listingUpdateFmt(selling.market, selling.aps.startDate)));
                    else if("discounted_price_end_date".equals(name))
                        addSellingPrams.add(new BasicNameValuePair(name, Dates.listingUpdateFmt(selling.market, selling.aps.endDate)));
                    else if("Offer_Inventory_Quantity".equals(name))
                        addSellingPrams.add(new BasicNameValuePair(name, selling.aps.quantity + ""));
                    else if("activeClientTimeOnTask".equals(name))
                        addSellingPrams.add(new BasicNameValuePair(name, "166279")); // 这个值是通过 JS 计算的, 而 JS 仅仅是计算一个时间, 算法无关
                    else if("matchAsin".equals(name))
                        addSellingPrams.add(new BasicNameValuePair(name, "QjAwODNRWDhBVw==")); // 在 JS 方法 preProcessMatch 执行时, 已经将 matchAsin 计算出来了,固定值
                    else if("encoded_session_hidden_map".equals(name)) {
                        addSellingPrams.add(new BasicNameValuePair(name, input.val()));
                        // 在发现了 encoded_session_hidden_map 以后需要添加这样一个属性(JS 动态添加的)
                        addSellingPrams.add(new BasicNameValuePair("sessionMapPresent", "true"));
                    } else if(StringUtils.startsWith(name, "bullet_point")) {
                        int fetureSize = selling.aps.keyFeturess.length;
                        for(int i = 0; i < fetureSize; i++)
                            addSellingPrams.add(new BasicNameValuePair("bullet_point[" + i + "]", selling.aps.keyFeturess[i]));
                        if(fetureSize < 5) { // 不足 5 个
                            for(int i = 0; i < (5 - fetureSize); i++)
                                addSellingPrams.add(new BasicNameValuePair("bullet_point[" + (i + fetureSize) + "]", ""));
                        }
                    } else if(StringUtils.startsWith(name, "generic_keywords")) {
                        int searchTermsSize = selling.aps.searchTermss.length;
                        for(int i = 0; i < searchTermsSize; i++)
                            addSellingPrams.add(new BasicNameValuePair("generic_keywords[" + i + "]", selling.aps.searchTermss[i]));
                        if(searchTermsSize < 5) {
                            for(int i = 0; i < (5 - searchTermsSize); i++)
                                addSellingPrams.add(new BasicNameValuePair("generic_keywords[" + (i + searchTermsSize) + "]", ""));
                        }
                    } else if(StringUtils.startsWith(name, "recommended_browse_nodes")) {
                        //TODO 这里没有检查 recommended_browse_nodes, 请在 Controller 中检查.
                        addSellingPrams.add(new BasicNameValuePair("recommended_browse_nodes[0]", selling.aps.rbns[0]));
                        addSellingPrams.add(new BasicNameValuePair("recommended_browse_nodes[1]", selling.aps.rbns[1]));
                    } else {
                        addSellingPrams.add(new BasicNameValuePair(name, input.val()));
                    }
                }
            }

            Elements textAreas = doc.select("form[name=productForm] textarea");
            /**
             * Product Description
             * Condition Note -- 有需要再补充
             * Seller Warranty Description -- 有需要再补充
             */
            for(Element textarea : textAreas) {
                String name = textarea.attr("name");
                if("product_description".equals(name))
                    addSellingPrams.add(new BasicNameValuePair(name, selling.aps.productDesc));
                else
                    addSellingPrams.add(new BasicNameValuePair(name, textarea.val()));
            }

            Elements selects = doc.select("form[name=productForm] select");
            for(Element select : selects) {
                String name = select.attr("name");
                // Condition
                if("offering_condition".equals(name))
                    addSellingPrams.add(new BasicNameValuePair(name, "New|New")); // 商品的 Condition 设置为 NEW
                else
                    addSellingPrams.add(new BasicNameValuePair(name, select.select("option[selected]").val()));
            }
            // -------------  3 -----------------
            /**
             * 上架时候的错误信息全部返回给前台.
             */
            for(NameValuePair n : addSellingPrams) {
                if(StringUtils.isBlank(n.getValue())) continue;
                System.out.println(n);
            }
            body = HTTP.post(selling.account.cookieStore(), selling.account.type.saleSellingPostLink()/*从账户所在的 Market 提交*/, addSellingPrams);
            if(Play.mode.isDev())
                Devs.fileLog(String.format("%s.%s.step3.html", selling.merchantSKU, selling.account.id), body, Devs.T.SALES);

            doc = Jsoup.parse(body);
            Element form = doc.select("form").first();
            if(form == null) throw new FastRuntimeException(
                    String.format("提交的参数错误.(详细错误信息咨询 IT 查看 E_LOG/listing_sale/%s.%s.step3.html)",
                            selling.merchantSKU, selling.account.id));

            for(Element hidden : doc.select("input")) {
                String name = hidden.attr("name");
                if("newItemAsin".equals(name)) selling.asin = hidden.val();
            }
            // 最后再检查是否添加成功?
            if(StringUtils.isBlank(selling.asin)) throw new FastRuntimeException("未知原因模拟手动创建 Selling 失败, 请 IT 仔细查找问题!");
        }

        //测试使用的 UPC 614444720150
        return selling.save();
    }


    /**
     * 加载此 Listing 所具有的所有 Selling 的状态的个数
     * NEW, 0
     * SELLING, 1
     * NO_INVENTORY, 2
     * HOlD, 3
     * DOWN 4
     *
     * @return
     */
    public Integer[] relateSellingStateQtys(String nickMarketName) {
        Integer[] stateQtys = {0, 0, 0, 0, 0};
        for(Selling s : this.sellings) {
            if(!nickMarketName.equalsIgnoreCase(s.market.nickName())) continue;
            switch(s.state) {
                case NEW:
                    stateQtys[0] += 1;
                    break;
                case SELLING:
                    stateQtys[1] += 1;
                    break;
                case NO_INVENTORY:
                    stateQtys[2] += 1;
                    break;
                case HOlD:
                    stateQtys[3] += 1;
                    break;
                case DOWN:
                    stateQtys[4] += 1;
                    break;
                default:
                    Logger.warn("Other State? Can not happed!");
            }
        }
        return stateQtys;
    }

    /**
     * 通过 Account 此 Listing 所属的所有 Selling 中过滤出指定 Account 的; 否则与 this.sellings 一样
     *
     * @param a
     * @return
     */
    public List<Selling> sellings(Account a) {
        if(a != null && a.id != null && a.id > 0) {
            return Selling.find("listing=? AND account=?", this, a).fetch();
        } else {
            return this.sellings;
        }
    }

    /**
     * 从所有 ListingOffer 中查找自己
     *
     * @return
     */
    public ListingOffer easyacceu() {
        if(this.offers == null) return null;
        for(ListingOffer offer : this.offers) {
            if("AJUR3R8UN71M4".equalsIgnoreCase(offer.offerId) ||
                    "A22H6OV6Q7XBYK".equalsIgnoreCase(offer.offerId)) {
                return offer;
            }
        }
        return null;
    }

    /**
     * 从 ListingOffer 中价格最低的价格
     *
     * @return 如果没有 ListingOffer 则返回 -1
     */
    public Float lowestPrice() {
        float lowest = Float.MAX_VALUE;
        if(this.offers == null || this.offers.size() == 0) return -1f;
        for(ListingOffer offer : this.offers) {
            if(offer.price < lowest) lowest = offer.price;
        }
        return lowest;
    }

    /**
     * 根据从 Crawler 抓取回来的 ListingJSON数据转换成系统内使用的 Listing + LisitngOffer 对象,
     * 并更新返回已经存在的 Listing 的持久对象或者返回未保存的 Listing 瞬时对象
     *
     * @param listingJson
     * @return
     */
    public static Listing parseAndUpdateListingFromCrawl(JsonElement listingJson) {
        /**
         * 根据 market 与 asin 先从数据库中加载, 如果存在则更新返回一个持久状态的 Listing 对象,
         * 否则返回一个瞬时状态的 Listing 对象
         */
        JsonObject lst = listingJson.getAsJsonObject();
        String listingId = lst.get("listingId").getAsString();
        if(listingId == null || listingId.trim().isEmpty()) return null; // 排除 404 没有的 Listing
        Listing oldListing = Listing.find("listingId=?", listingId).first();
        Listing tobeChangeed;
        if(oldListing != null) {
            // 更新 OldListing 并保存更新
            tobeChangeed = oldListing;
        } else {
            // 返回一个全新 Listing
            tobeChangeed = new Listing();
        }

        tobeChangeed.market = Account.M.val(listingId.split("_")[1]);
        tobeChangeed.asin = lst.get("asin").getAsString();
        tobeChangeed.byWho = lst.get("byWho").getAsString();
        tobeChangeed.title = lst.get("title").getAsString();
        tobeChangeed.reviews = lst.get("reviews").getAsInt();
        tobeChangeed.rating = lst.get("rating").getAsFloat();
        tobeChangeed.technicalDetails = lst.get("technicalDetails").getAsString();
        tobeChangeed.productDescription = lst.get("productDescription").getAsString();
        tobeChangeed.saleRank = lst.get("saleRank").getAsInt();
        tobeChangeed.totalOffers = lst.get("totalOffers").getAsInt();
        tobeChangeed.picUrls = lst.get("picUrls").getAsString();

        JsonArray offers = lst.get("offers").getAsJsonArray();
        List<ListingOffer> newOffers = new ArrayList<ListingOffer>();
        if(oldListing != null) { // 如果不为空, 那么保持最新的 LisitngOffer 信息, 删除老的重新记录
            for(ListingOffer of : tobeChangeed.offers) of.delete();
        }
        for(JsonElement offerEl : offers) {
            JsonObject offer = offerEl.getAsJsonObject();
            ListingOffer off = new ListingOffer();
            off.name = offer.get("name").getAsString();
            off.offerId = offer.get("offerId").getAsString();
            // 价格根据不同的市场进行转换成 GBP 价格
            switch(tobeChangeed.market) {
                case AMAZON_UK:
                    off.price = offer.get("price").getAsFloat();
                    off.shipprice = offer.get("shipprice").getAsFloat();
                    break;
                case AMAZON_US:
                    off.price = Currency.USD.toGBP(offer.get("price").getAsFloat());
                    off.shipprice = Currency.USD.toGBP(offer.get("shipprice").getAsFloat());
                    break;
                case AMAZON_DE:
                case AMAZON_FR:
                default:
                    off.price = Currency.EUR.toGBP(offer.get("price").getAsFloat());
                    off.shipprice = Currency.EUR.toGBP(offer.get("shipprice").getAsFloat());
            }
            off.fba = offer.get("fba").getAsBoolean();
            off.buybox = offer.get("buybox").getAsBoolean();
            off.listing = tobeChangeed;
            newOffers.add(off);

            // set display price
            if(tobeChangeed.displayPrice == null && off.buybox)
                tobeChangeed.displayPrice = off.price;
        }
        tobeChangeed.offers = newOffers;
        tobeChangeed.lastUpdateTime = System.currentTimeMillis();
        if(oldListing != null) return tobeChangeed.save();
        return tobeChangeed;
    }

    /**
     * 根据 Listing 的状态, 进行 Listing 的检查;
     * 1. 如果这个 Listing 是我们自己的, 检查是否被跟.
     */
    public void check() {
        /**
         *  1. 检查此 Listing 上是否有自己多个店铺上架
         *  2. 检查此 Listing 是否有被其他卖家上架!(如果是跟着卖的就不需要检查这个了)
         */
        if(this.offers == null || this.offers.size() == 0) {
            Logger.warn("Listing [" + this.listingId + "] have no offers!");
            return;
        }

        int selfSale = 0;
        boolean mailed = false;

        for(ListingOffer off : this.offers) {
            // -------- 1
            if(Account.merchant_id().containsKey(off.offerId)) selfSale++;

            // ------- 1
            if(Listing.isSelfBuildListing(this.title)) {
                if(StringUtils.isBlank(off.offerId)) { //没有 OfferId 的为不可销售的很多原因, 很重要的例如缺货
                    Logger.debug("Listing [" + this.listingId + "] current can`t sale. Message[" + off.name + "]");
                } else if(!Account.merchant_id().containsKey(off.offerId)) {
                    // Mail 警告
                    if(this.warnningTimes == null) this.warnningTimes = 0;
                    this.warnningTimes++; // 查询也记录一次
                    if(this.warnningTimes > 4) {
                        Logger.debug("Listing [" + this.listingId + "] has warnned more than 3 times.");
                    } else {
                        if(mailed) continue;
                        Mails.listingOffersWarning(off);
                        mailed = true;
                    }
                } else {
                    this.warnningTimes = 0; // 其余的归零
                }
            }
        }
        if(selfSale >= 2) Mails.moreOfferOneListing(offers, this);
    }

    /**
     * 判断这个 Listing 是否为自己自建的 Listing
     * 不是判断产品的标题是否为 EasyAcc 而是判断这个产品是否为 EasyAcc 自己销售(不是跟的)
     *
     * @return
     */
    public static boolean isSelfBuildListing(String title) {
        title = title.toLowerCase();
        if(StringUtils.startsWith(title, "easyacc")) return true;
        else if(StringUtils.startsWith(title, "nosson")) return true;
        else if(StringUtils.startsWith(title, "fencer")) return true;
        else if(StringUtils.startsWith(title, "saner")) return true;
        else return false;
    }

    @Override
    public boolean equals(Object o) {
        if(this == o) return true;
        if(o == null || getClass() != o.getClass()) return false;
        if(!super.equals(o)) return false;

        Listing listing = (Listing) o;

        if(!listingId.equals(listing.listingId)) return false;

        return true;
    }

    @Override
    public int hashCode() {
        int result = super.hashCode();
        result = 31 * result + listingId.hashCode();
        return result;
    }

    @Override
    public String toString() {
        return "Listing{" +
                "listingId='" + listingId + '\'' +
                ", title='" + title + '\'' +
                ", reviews=" + reviews +
                ", rating=" + rating +
                ", byWho='" + byWho + '\'' +
                ", saleRank=" + saleRank +
                ", displayPrice=" + displayPrice +
                ", lastUpdateTime=" + lastUpdateTime +
                ", nextUpdateTime=" + nextUpdateTime +
                '}';
    }
}