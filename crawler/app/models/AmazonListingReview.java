package models;

import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.collections.Predicate;
import org.apache.commons.lang.StringUtils;
import org.apache.commons.lang.math.NumberUtils;
import org.joda.time.DateTime;
import org.joda.time.format.DateTimeFormat;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import play.Logger;

import java.util.*;

/**
 * 某一个 ListingC 所拥有的 Review 消息
 * User: wyattpan
 * Date: 4/17/12
 * Time: 4:33 PM
 */
public class AmazonListingReview {

    /**
     * Amazon ListingC Review 的 Id:
     * ([listingId]_[userid])toUppercase()
     */
    public String alrId;

    /**
     * 一个冗余字段
     */
    public String listingId;


    /**
     * Review 的得分
     */
    public Float rating;

    public String title;

    /**
     * 具体的 Review 的内容
     */
    public String review;


    /**
     * 点击了 Helpful 按钮的 YES
     */
    public Integer helpUp;


    /**
     * 所有点击了 HelpFul Click 的统计
     */
    public Integer helpClick;

    /**
     * 用户名字
     */
    public String username;

    /**
     * 关联的用户 Id
     */
    public String userid;

    public String reviewDate;

    /**
     * Amazon 会判断这个 Review 是不是为购买了这个商品的客户发出.
     */
    public Boolean purchased;

    /**
     * 标记为是否解决了这个 Review; 当然只有当 Review <= 3 的时候才需要进行处理!
     */
    public Boolean resolved = false;

    /**
     * 上次一的 LastRating;
     * 更新这个字段的时机为当当前的 rating 与 lastRating 的数据不一样的时候才进行更新, 并且还会记录到 Comment 中去
     */
    public Float lastRating;

    /**
     * Amazon 给与的每个 Review 的 ID
     */
    public String reviewId;

    /**
     * 是否为 Vedio 的 Review
     */
    public boolean isVedio;

    /**
     * 是不是 VineVoice
     */
    public boolean isVineVoice;

    /**
     * 是不是真名
     */
    public boolean isRealName;

    /**
     * 是 Top 多少?
     */
    public int topN;

    /**
     * 视频的预览图片链接
     */
    public String vedioPicUrl = "";

    public int reviewRank = 0;

    /**
     * 给程序自己使用的, 非人为使用的 Comment; 用来记录变化的
     */
    public String comment = "";

    public static List<AmazonListingReview> parseReviewFromHTML(Document doc) {
        List<AmazonListingReview> reviewList = new ArrayList<AmazonListingReview>();
        Element rtr = doc.select("#productReviews tr").first();
        if(rtr == null) return reviewList;

        Elements reviews = rtr.select("td > div[style]");
        if(reviews == null) return reviewList;
        Logger.debug("Fetched Review Size is %s", reviews.size());

        String asin = doc.select(".asinReviewsSummary").attr("name");
        String market = doc.select(".navFooterLogoLine img").attr("alt");

        int rank = 1;
        for(Element r : reviews) {
            try {
                AmazonListingReview review = new AmazonListingReview();
                review.reviewRank = rank++;
                Element fromListing = r.select("> div.tiny b a").first();
                if(fromListing != null) {
                    String fixAsin = StringUtils.substringBetween(fromListing.attr("href"), "dp/", "/ref");
                    review.listingId = String.format("%s_%s", fixAsin, market);
                } else {
                    review.listingId = String.format("%s_%s", asin, market);
                }
                Logger.debug("Actually listingId is %s", review.listingId);

                String ratingStr = r.select("> div span.swSprite").first().text();
                review.rating = NumberUtils.toFloat(StringUtils.split(ratingStr)[0]);
                review.lastRating = review.rating; // 对 LastRating 的初始化

                review.isRealName = r.select(".s_BadgeRealName").first() != null;
                review.isVineVoice = r.select(".s_BadgeVineVoice").first() != null;
                if(r.select(".s_BadgeTop50").first() != null) review.topN = 50;
                if(r.select(".s_BadgeTop100").first() != null) review.topN = 100;
                if(r.select(".s_BadgeTop500").first() != null) review.topN = 500;
                if(r.select(".s_BadgeTop1000").first() != null) review.topN = 1000;

                review.title = r.select("> div span b").first().text().trim();

                String helpfulStr = r.select("> div").first().text();
                int[] two = {0, 0};
                if(isHelpfulStr(helpfulStr)) {
                    String[] words = StringUtils.split(helpfulStr);
                    int n = 0; // 存储数字的索引
                    for(String s : words) {
                        s = StringUtils.remove(s, "sur "); // 在 FR, [sur 1] 为第二个数字, 但是 sur 与 1 之间那个符号不是空格, 所以做删除处理
                        int number = NumberUtils.toInt(s, -1);
                        if(number != -1) two[n++] = number;
                    }
                }
                review.helpUp = two[0];
                review.helpClick = two[1];

                Element user = r.select("> div div div a").first();
                review.username = user.text();
                review.userid = StringUtils.splitPreserveAllTokens(user.attr("href"), "/")[6];

                String[] dates = StringUtils.split(StringUtils.remove(r.select("> div span nobr").first().text(), "."), " ");
                review.reviewDate = DateTime.parse(String.format("%s %s %s", dates[0], dateMap(dates[1]), dates[2]), DateTimeFormat.forPattern("dd MMM yyyy")).toString("yyyy-MM-dd");

                Element purchasedEl = r.select("> div span.crVerifiedStripe").first();
                review.purchased = purchasedEl != null;

                review.review = r.ownText();

                review.alrId = String.format("%s_%s", review.listingId, review.userid).toUpperCase();

                review.reviewId = StringUtils.split(r.select(".crVotingButtons").first().previousElementSibling().attr("name"), ".")[0];
                review.isVedio = r.select(".flashPlayer").first() != null;
                if(review.isVedio) {
                    review.vedioPicUrl = r.select(".flashPlayer").first().select("img").first().attr("src");
                }

                reviewList.add(review);
            } catch(Exception e) {
                Logger.warn(String.format("%s|%s", e.getClass().getSimpleName(), e.getMessage()));
            }
        }
        return reviewList;
    }

    /**
     * 支持 DE 与 FR 语言中的月份字符串转换成 英语 月份的字符串
     *
     * @param m
     * @return
     */
    private static String dateMap(String m) {
        // de -> uk
        if("Januar".equalsIgnoreCase(m)) return "January";
        else if("Februar".equalsIgnoreCase(m)) return "February";
        else if("März".equalsIgnoreCase(m)) return "March";
        else if("April".equalsIgnoreCase(m)) return "April";
        else if("Mai".equalsIgnoreCase(m)) return "May";
        else if("Juni".equalsIgnoreCase(m)) return "June";
        else if("Juli".equalsIgnoreCase(m)) return "July";
        else if("August".equalsIgnoreCase(m)) return "August";
        else if("September".equalsIgnoreCase(m)) return "September";
        else if("Oktober".equalsIgnoreCase(m)) return "October";
        else if("November".equalsIgnoreCase(m)) return "November";
        else if("Dezember".equalsIgnoreCase(m)) return "December";
            // fr -> uk
        else if("janvier".equalsIgnoreCase(m)) return "January";
        else if("février".equalsIgnoreCase(m)) return "February";
        else if("mars".equalsIgnoreCase(m)) return "March";
        else if("avril".equalsIgnoreCase(m)) return "April";
        else if("mai".equalsIgnoreCase(m)) return "May";
        else if("juin".equalsIgnoreCase(m)) return "June";
        else if("juillet".equalsIgnoreCase(m)) return "July";
        else if("août".equalsIgnoreCase(m)) return "August";
        else if("septembre".equalsIgnoreCase(m)) return "September";
        else if("octobre".equalsIgnoreCase(m)) return "October";
        else if("novembre".equalsIgnoreCase(m)) return "November";
        else if("décembre".equalsIgnoreCase(m)) return "December";
        else return m;
    }


    /**
     * 判断字符串是否为 HelpFul 的字符串, 会需要用来解析 helpUp 与 helpDown
     *
     * @param str
     * @return
     */
    private static boolean isHelpfulStr(String str) {
        return StringUtils.containsIgnoreCase(str, "review helpful") || // uk
                StringUtils.containsIgnoreCase(str, "Rezension hilfreich") || // de
                StringUtils.containsIgnoreCase(str, "commentaire utile"); // fr
    }

    /**
     * 解析出 Review 页面的最大页数
     *
     * @param doc Jsoup 解析的 Document
     * @return
     */
    public static int maxPage(Document doc) {
        Element page = doc.select("span.paging").first();
        if(page == null) return 1;
        Elements links = page.select("a");

        int maxLinkIndex = links.size() - 2;

        Element maxLink = links.get(maxLinkIndex < 0 ? 0 : maxLinkIndex);
        String[] params = StringUtils.split(maxLink.attr("href"), "&");
        for(String param : params) {
            if(!StringUtils.containsIgnoreCase(param, "pageNumber")) continue;
            return NumberUtils.toInt(StringUtils.split(param, "=")[1], 1);
        }
        return 1;
    }

    /**
     * 根据 Market 与 Asin 对抓取到的 ASIN 进行 variation 的过滤
     *
     * @return
     */
    public static <T extends Collection<AmazonListingReview>> Set<AmazonListingReview> filterReviewWithAsinAndMarket(final String asin, final MT m, T reviews) {
        Set<AmazonListingReview> filterVariationReview = new HashSet<AmazonListingReview>(reviews);
        CollectionUtils.filter(filterVariationReview, new Predicate() {
            @Override
            public boolean evaluate(Object o) {
                AmazonListingReview review = (AmazonListingReview) o;
                return StringUtils.equals(review.listingId, String.format("%s_%s", asin, m.toString()));
            }
        });
        return filterVariationReview;
    }
}
