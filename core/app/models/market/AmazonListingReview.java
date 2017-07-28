package models.market;

import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.annotations.Expose;
import helper.*;
import models.view.highchart.HighChart;
import models.view.highchart.Series;
import org.apache.commons.lang.StringUtils;
import org.hibernate.annotations.DynamicUpdate;
import org.joda.time.DateTime;
import play.Logger;
import play.cache.Cache;
import play.data.validation.Required;
import play.data.validation.Unique;
import play.data.validation.Validation;
import play.db.helper.JpqlSelect;
import play.db.jpa.GenericModel;
import play.libs.F;
import play.utils.FastRuntimeException;
import services.MetricReviewService;

import javax.persistence.*;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;

/**
 * 某一个 Listing 所拥有的 Review 消息
 * User: wyattpan
 * Date: 4/17/12
 * Time: 4:33 PM
 */
@Entity
@DynamicUpdate
public class AmazonListingReview extends GenericModel {

    /**
     * 查找关联的订单, 找到了则关联上
     */
    @OneToOne
    public Orderr orderr;

    @ManyToOne(fetch = FetchType.LAZY)
    public Listing listing;

    /**
     * Amazon Listing Review 的 Id:
     * [listingId]_[username]_[userId] .toUpperCase()
     */
    @Id
    @Expose
    @Required
    public String alrId;

    /**
     * 一个冗余字段
     */
    @Expose
    @Required
    public String listingId;


    /**
     * Review 的得分
     */
    @Expose
    @Required
    public Float rating;

    @Expose
    @Required
    public String title;

    /**
     * 具体的 Review 的内容
     */
    @Lob
    @Expose
    public String review;


    /**
     * 点击了 Helpful 按钮的 YES
     */
    @Expose
    public Integer helpUp;


    /**
     * 所有点击了 HelpFul Click 的统计
     */
    @Expose
    public Integer helpClick;

    /**
     * 用户名字
     */
    @Expose
    public String username;

    /**
     * 关联的用户 Id
     */
    @Expose
    @Required
    public String userid;

    @Expose
    public Date reviewDate;

    /**
     * Review 创建的时间
     */
    @Expose
    public Date createDate;

    /**
     * 最近的更新时间
     */
    public Date updateAt;

    /**
     * Amazon 会判断这个 Review 是不是为购买了这个商品的客户发出.
     */
    @Expose
    public Boolean purchased = false;

    /**
     * 标记为是否解决了这个 Review; 当然只有当 Review <= 3 的时候才需要进行处理!
     */
    @Expose
    public Boolean resolved = false;

    /**
     * 上次一的 LastRating;
     * 更新这个字段的时机为当当前的 rating 与 lastRating 的数据不一样的时候才进行更新, 并且还会记录到 Comment 中去
     */
    @Expose
    public Float lastRating;

    /**
     * 记录发送的邮件的次数. 大于 3 次则不再提醒.
     */
    @Expose
    public Integer mailedTimes = 0;

    /**
     * 给程序自己使用的, 非人为使用的 Comment; 用来记录变化的
     */
    @Lob
    public String comment = "";

    /**
     * 是否为视频 Review
     */
    @Expose
    public Boolean isVedio = false;
    /**
     * 是不是 VineVoice
     */
    @Expose
    public Boolean isVineVoice = false;

    /**
     * 是不是真名
     */
    @Expose
    public Boolean isRealName = false;

    /**
     * 此 Review 我们是否在 Amazon 上留了 Comment
     */
    public Boolean isCommentOnAmazon = false;

    /**
     * 在 Amazon 上此 Review 的回复个数
     */
    public int comments = 0;

    /**
     * 是 Top 多少?
     */
    @Expose
    public Integer topN;


    /**
     * Amazon 给与的每个 Review 的 ID
     */
    @Expose
    @Unique
    @Column(unique = true)
    @Required
    public String reviewId;

    /**
     * 视频的预览图片链接
     */
    @Expose
    public String vedioPicUrl;

    @Column(columnDefinition = "varchar(32) DEFAULT ''")
    @Expose
    public String osTicketId;

    /**
     * 每一个 Review 创建的时候都记录一次 originJson 字符串, 记录初始状态
     */
    @Lob
    public String originJson = " ";

    /**
     * 判断这个 Review 是否为自己的 Listing 产生的
     */
    @Expose
    public boolean isOwner = false;

    /**
     * Listing 中 Review 自己的排序
     */
    public int reviewRank = -1;

    /**
     * 是否被删除
     */
    public boolean isRemove = false;
    /**
     * 是否为自建的 Listing
     */
    public boolean isSelf = true;

    /**
     * 记录 AmazonListingReview 的点击记录, 一般给前台参看使用
     */
    @OneToMany(mappedBy = "ownerReview")
    public List<AmazonReviewRecord> reviewRecords = new ArrayList<>();

    /**
     * 主要是为了记录 createDate 日期
     *
     * @return
     */
    public AmazonListingReview createReview() {
        if(!Validation.current().valid(this).ok)
            throw new FastRuntimeException("Not Valid, more details see Validation.errors()");
        if(this.createDate == null) this.createDate = new Date();
        // 将初始的 Review 数据全部记录下来
        this.originJson = J.G(this);
        if(this.listing == null)
            Logger.warn("AmazonListingReview %s have no relate listing!", this.reviewId);
        else if(!this.isSelf()) {
            this.isSelf = false;
            this.comment("这个 Review 对应的 Listing 非自建.");
        }
        Logger.warn("AmazonListingReview %s save!", this.reviewId);
        return this.save();
    }

    @PrePersist
    @PreUpdate
    public void prePersist() {
        if(this.purchased == null) this.purchased = false;
        this.updateAt = new Date();
    }

    public AmazonListingReview updateAttr(AmazonListingReview newReview) {
        if(StringUtils.isBlank(newReview.alrId))
            throw new FastRuntimeException("AmazonListingReview.alrId can not be blank!");
        if(!this.equals(newReview))
            throw new FastRuntimeException("Not the same AmazonListingReview, can not be update!");

        this.listingId = newReview.listingId;
        //如果两次 Rating 的值不一样需要记录
        if(newReview.rating != null && !this.rating.equals(newReview.rating)) {
            this.lastRating = this.rating;
            this.comment(String.format("Rating from %s to %s At %s", this.lastRating, newReview.rating,
                    Dates.date2DateTime()));
            this.mailedTimes = 0;// 允许重新发送一次邮件
        }
        if(newReview.rating != null) this.rating = newReview.rating;
        if(StringUtils.isNotBlank(newReview.title)) this.title = newReview.title;
        if(StringUtils.isNotBlank(newReview.review)) this.review = newReview.review;
        if(newReview.helpUp != null && newReview.helpUp > 0) this.helpUp = newReview.helpUp;
        if(newReview.helpClick != null && newReview.helpClick > 0) this.helpClick = newReview.helpClick;
        if(StringUtils.isNotBlank(newReview.userid)) this.userid = newReview.userid;
        if(StringUtils.isNotBlank(newReview.username)) this.username = newReview.username;
        //reviewDate 不修改了
        if(newReview.purchased != null) this.purchased = newReview.purchased;
        if(newReview.isVedio != null) this.isVedio = newReview.isVedio;
        if(newReview.isRealName != null) this.isRealName = newReview.isRealName;
        if(newReview.isVineVoice != null) this.isVineVoice = newReview.isVineVoice;
        if(newReview.topN != null && newReview.topN >= 0) this.topN = newReview.topN;
        if(StringUtils.isNotBlank(newReview.reviewId)) this.reviewId = newReview.reviewId;
        if(StringUtils.isNotBlank(newReview.vedioPicUrl)) this.vedioPicUrl = newReview.vedioPicUrl;
        if(newReview.reviewRank > 0) this.reviewRank = newReview.reviewRank;
        if(newReview.comments > 0) this.comments = newReview.comments;
        this.isSelf = this.isSelf();

        try {
            this.review = new String(this.review.getBytes(), "UTF-8");
            this.title = new String(this.title.getBytes(), "UTF-8");
        } catch(Exception e) {
            Logger.warn("review UTF-8: %s", e.getMessage());
        }

        DBUtils.execute("update AmazonListingReview set rating=" + this.rating
                + ",lastRating=" + this.lastRating
                + " where alrid='" + this.alrId + "'");
        // resolved 不做处理
        return AmazonListingReview.findById(this.alrId);
    }

    private boolean isSelf() {
        if(this.listing == null) return false;
        return Listing.isSelfBuildListing(this.listing.title);
    }

    public List<Orderr> relateOrder() {
        return Orderr.find("userid=?", this.userid).fetch();
    }

    /**
     * 对此 AmazonListingReview 进行检查, 判断是否需要进行警告通知
     * 在 Check 方法执行完成以后将数据同步回数据库;
     * 如果此 Review 需要开 Ticket 进行处理, 也在此判断了
     */
    public void checkMailAndTicket() {
        if(StringUtils.isBlank(this.alrId)) return;
        AmazonListingReview dbreview = AmazonListingReview.find("alrid=?", this.alrId).first();
        if(dbreview == null) {
            Logger.info("Review alrId is not exist!! %s", this.alrId);
            return;
        }

        if(StringUtils.isNotBlank(dbreview.osTicketId)) {
            Logger.info("dbReview OsTicket is exist! %s", dbreview.osTicketId);
            return;
        }

        if(!this.isPersistent()) return;// 如果没有保存进入数据库的, 那么则不进行判断
        if(Selling.count("listing.listingId=?", this.listingId) == 0)
            return;// 判断这个 Listing 是我们自己有上架的
        if(this.createDate.getTime() - DateTime.now().plusDays(-70).getMillis() < 0)
            return;// 超过 70 天的不处理

        if(this.rating != null && this.rating <= 3) {
            if(StringUtils.isNotBlank(this.osTicketId)) {
                Logger.info("Review OsTicket is exist!! %s", this.osTicketId);
                return;
            }

            long reviewcount = AmazonListingReview.count("alrid=?", this.alrId);
            if(reviewcount != 1L) {
                Logger.info("Review alrId count is not exist!! %s", this.alrId);
                return;
            }
            this.openTicket(null);
            DBUtils.execute("update AmazonListingReview set osTicketId=" + this.osTicketId
                    + " where alrid='" + this.alrId + "'");
//            this.save();
//            Mails.listingReviewWarn(this);
//            this.save();
        }
    }


    @Override
    public String toString() {
        final StringBuilder sb = new StringBuilder();
        sb.append("AmazonListingReview");
        sb.append("{alrId='").append(alrId).append('\'');
        sb.append(", listingId='").append(listing.listingId).append('\'');//使用了 model
        sb.append(", rating=").append(rating);
        sb.append(", title='").append(title).append('\'');
        sb.append(", review='").append(review).append('\'');
        sb.append(", helpUp=").append(helpUp);
        sb.append(", helpClick=").append(helpClick);
        sb.append(", username='").append(username).append('\'');
        sb.append(", userid='").append(userid).append('\'');
        sb.append(", reviewDate=").append(reviewDate);
        sb.append(", createDate=").append(createDate);
        sb.append(", purchased=").append(purchased);
        sb.append(", resolved=").append(resolved);
        sb.append(", lastRating=").append(lastRating);
        sb.append(", mailedTimes=").append(mailedTimes);
        sb.append(", comment='").append(comment).append('\'');
        sb.append('}');
        return sb.toString();
    }

    @Override
    public boolean equals(Object o) {
        if(this == o) return true;

        if(o == null || getClass() != o.getClass()) {
            return false;
        }
        // if(!super.equals(o)) {
        //    Logger.warn("REVIEWID111:[%s]. [%s]",getClass().toString(),o.getClass().toString());
        //   return false;
        //}

        AmazonListingReview that = (AmazonListingReview) o;

        if(reviewId != null ? !reviewId.equals(that.reviewId) : that.reviewId != null) return false;

        return true;
    }

    @Override
    public int hashCode() {
        int result = super.hashCode();
        result = 31 * result + (reviewId != null ? reviewId.hashCode() : 0);
        return result;
    }

    /**
     * 选择出一个能够点击此 Review 的 Account
     *
     * @return
     */
    public F.T2<Account, Integer> pickUpOneAccountToClick() {
        /**
         * 一步一步的过滤掉 Account, 然后返回一个 Account
         * 1. 找出所有的 Comment Account, 过滤掉 SaleAcc
         * 2. 从 AmazonReviewRecord 中检查此 Review 的点击日志, 首先找出点击过此 Review 的 Account
         */
        List<Account> opendAccs = Account.openedAmazonClickReviewAndLikeAccs(this.listing.market);
        if(opendAccs.size() == 0) throw new FastRuntimeException("没有打开的 Review 账号了.");
        List<Account> nonClickAccs = AmazonReviewRecord.checkNonClickAccounts(opendAccs, this);
        if(nonClickAccs.size() == 0)
            throw new FastRuntimeException("系统内所有的账号都已经点击过这个 Review 了, 请添加新账号再进行点击.");
        Logger.info("To Click Review %s, hava %s valid accounts.", this.reviewId,
                nonClickAccs.size());
        StringBuilder sb = new StringBuilder();
        for(Account a : nonClickAccs) {
            sb.append(a.id).append("|").append(a.prettyName()).append(",");
        }
        Logger.info("Account List: %s", sb.toString());
        return new F.T2<>(nonClickAccs.get(0), nonClickAccs.size());
    }


    /**
     * 根据这个 Review 的 userId, 与 ReviewDate 去尝试 Order 并且关联上
     *
     * @return
     */
    public Orderr tryToRelateOrderByUserId() {
        if(this.orderr != null) return this.orderr;
        if(StringUtils.isBlank(this.userid)) return null;
        return Orderr.find("createDate<=? AND userid=? ORDER BY createDate DESC", this.reviewDate,
                this.userid).first();
    }

    /**
     * 检查此 Review 是否在 Top1000 内
     *
     * @return
     */
    public Boolean isWithinTop1000() {
        return this.topN != null && this.topN > 0 && this.topN <= 1000;
    }

    public void comment(String comment) {
        this.comment = String.format("%s%n%s", comment.trim(), this.comment);
    }

    /**
     * AmazonListingReview 开 OsTicket, 在 OsTicket 创建一个 Ticket 的同时,也在系统中创建
     */
    public String openTicket(String title) {
        if(StringUtils.isNotBlank(this.osTicketId)) {
            Logger.info("Review OsTicket is exist! %s", this.osTicketId);
            return null;
        }

        String name = String.format("%s(%s)", this.username, this.userid);
        String subject = title;
        String content = GTs.render("OsTicketReviewWarn", GTs.newMap("review", this).build());

        if(StringUtils.isBlank(subject)) {
            if(this.listing.market == M.AMAZON_DE) {
                subject = "Sie haben eine neutrale/negative Rezension bei Amazon hinterlassen. Dürfen wir Ihnen helfen??";
            } else { // 除了 DE 使用德语其他的默认使用'英语'
                subject = "We would like to address your review!!";
            }
        }

        if(this.orderr == null) this.orderr = this.tryToRelateOrderByUserId();
        if(this.orderr == null) {
            Logger.warn("Review (%s) relate order have no email.", this.alrId);
            subject += " - No Order found...";
            content += "\r\n检查: 1. 是我们的跟的 Listing 产生的? 2. 订单的 userId 还没抓取回来? 3. 是非购买用户留的?";
            this.osTicketId = Jitbit.addTicket("support@easya.cc", "support", subject, content, Jitbit.Category.REVIEW);
        } else {
            this.osTicketId = Jitbit.addTicket(this.orderr.email, name, subject, content, Jitbit.Category.REVIEW);
        }
        LogUtils.JOBLOG.info(String.format("sendticket :%s, %s", this.alrId, this.osTicketId));
        return this.osTicketId;
    }

    /**
     * 解析单个 Review JsonElement
     *
     * @param jsonReviewElement
     * @return
     */
    public static AmazonListingReview parseAmazonReviewJson(JsonElement jsonReviewElement) {
        JsonObject rwObj = jsonReviewElement.getAsJsonObject();
        AmazonListingReview review = new AmazonListingReview();
        //review.alrId = rwObj.get("alrId").getAsString();
        review.listingId = rwObj.get("listingId").getAsString();
        review.rating = rwObj.get("rating").getAsFloat();
        review.lastRating = rwObj.get("lastRating").getAsFloat();
        review.title = rwObj.get("title").getAsString();
        review.review = rwObj.get("review").getAsString();
        review.helpUp = rwObj.get("helpUp").getAsInt();
        review.helpClick = rwObj.get("helpClick").getAsInt();
        review.username = rwObj.get("username").getAsString();
        review.userid = rwObj.get("userid").getAsString();

        //解析英文日期
        String reviewdate = rwObj.get("reviewDate").getAsString();
        review.reviewDate = parseDate(reviewdate);

        review.purchased = rwObj.get("purchased").getAsBoolean();
        review.resolved = rwObj.get("resolved").getAsBoolean();
        review.isVedio = rwObj.get("isVedio").getAsBoolean();
        review.reviewId = rwObj.get("reviewId").getAsString();
        review.vedioPicUrl = rwObj.get("vedioPicUrl").getAsString();
        review.isRealName = rwObj.get("isRealName").getAsBoolean();
        review.isVineVoice = rwObj.get("isVineVoice").getAsBoolean();
        review.topN = rwObj.get("topN").getAsInt();
        //review.reviewRank = rwObj.get("reviewRank").getAsInt();
        review.reviewRank = 1;
        review.comments = rwObj.get("comments").getAsInt();

        review.alrId = review.listingId.toUpperCase() + "_" + review.reviewId.toUpperCase();
        try {
            review.review = new String(review.review.getBytes(), "UTF-8");
            review.title = new String(review.title.getBytes(), "UTF-8");
        } catch(Exception e) {
            Logger.warn("review UTF-8: %s", e.getMessage());
        }

        return review;
    }

    public static Date parseDate(String reviewdate) {
        Date reviewDate = new Date();
        if(reviewdate != null && reviewdate.length() > 0 && reviewdate.contains(",")) {
            java.text.SimpleDateFormat sdf = new java.text.SimpleDateFormat("MMM dd, yyyy", java.util.Locale.ENGLISH);
            try {
                reviewDate = sdf.parse(reviewdate);
            } catch(Exception e) {
                Logger.error(Webs.S(e));
            }
        } else {
            if(reviewdate != null && reviewdate.length() > 0) {
                reviewDate = DateTime.parse(reviewdate).toDate();
            }
        }
        return reviewDate;
    }

    public static AmazonListingReview findByReviewId(String reviewId) {
        return AmazonListingReview.find("reviewId=?", reviewId).first();
    }

    public static List<AmazonListingReview> listingReviews(String listingId, String orderBy) {
        return AmazonListingReview.listingReviews(listingId, orderBy,
                ("rating".equals(orderBy) ? " ASC" : " DESC"));
    }

    public static List<AmazonListingReview> listingReviews(String listingId, String orderBy, String desc) {
        return AmazonListingReview.find(String.format("listing.listingId=? ORDER BY %s %s", orderBy, desc), listingId)
                .fetch();
    }

    public static List<AmazonListingReview> listingReviewsBySKU(String sku, String orderBy) {
        return AmazonListingReview.find(String.format("listing.product.sku=? ORDER BY %s ", orderBy), sku)
                .fetch();
    }

    /**
     * 计算出指定 Listing 的 Review 个数
     *
     * @param lid
     * @return
     */
    public static long countListingReview(String lid) {
        return AmazonListingReview.count("listing.listingId=?", lid);
    }

    /**
     * Amazon Review 在系统中剩下的可以点击的次数
     *
     * @param reviewIds
     * @return
     */
    public static List<F.T2<String, Integer>> reviewLeftClickTimes(List<String> reviewIds) {
        List<AmazonListingReview> reviews = AmazonListingReview
                .find("reviewId IN " + JpqlSelect.inlineParam(reviewIds)).fetch();
        List<F.T2<String, Integer>> reviewLeftClicks = new ArrayList<>();
        for(AmazonListingReview review : reviews) {
            int leftClick = 0;
            try {
                leftClick = review.pickUpOneAccountToClick()._2;
            } catch(FastRuntimeException e) {
                leftClick = 0;
            }
            reviewLeftClicks.add(new F.T2<>(review.reviewId, leftClick));
        }
        return reviewLeftClicks;
    }

    /**
     * 计算指定的产品线的 Listing 下所有的 Review 在一个时间段内的评分情况, 并且返回的 Map 组装成 HightChart 使用的格式;
     * <p>
     * (首先取得所选时间段范围内所有有效的周一与周日 List<Date> points)
     * (然后取得系统内第一个 Review 产生的时间作为 begin)
     * (分别用第一个 Review 产生的时间作为 begin, 循环出 points 内每一个元素作为 end, 这样去查询出当前时间范围内的 Review 评分情况)
     * (组装成 HighChart 返回给前端绘制图形)
     * </p>
     *
     * @param from     开始时间
     * @param to       结束时间
     * @param category 品线
     * @return
     */
    @Cached("4h")
    public static HighChart reviewRatingLine(Date from, Date to, String category) {
        String cacked_key = Caches.Q.cacheKey("reviewRatingLine", category, from, to);
        HighChart lineChart = Cache.get(cacked_key, HighChart.class);
        if(lineChart != null) return lineChart;

        synchronized(cacked_key.intern()) {
            lineChart = Cache.get(cacked_key, HighChart.class);
            if(lineChart != null) return lineChart;

            lineChart = new HighChart(Series.LINE);
            lineChart.title = "Review 星级趋势图";
            SimpleDateFormat formatter = new SimpleDateFormat("yyyy-MM-dd");
            List<Date> sundays = Dates.getAllSunday(from, to);

            MetricReviewService service = new MetricReviewService(from, to, category);
            JSONObject result = service.countReviewRating();
            if(result == null) return new HighChart(Series.LINE);

            HashMap<Date, F.T2<Long, Long>> sumResults = new HashMap<>();
            for(M m : Promises.MARKETS) {
                Series.Line line = new Series.Line(m.name(), false);

                JSONObject marketResult = result.getJSONObject(m.name());
                for(Date sunday : sundays) {
                    JSONObject sundayResult = marketResult.getJSONObject(formatter.format(sunday));
                    long scoreSum = 0L;
                    long sumCount = 0L;
                    if(sundayResult != null) {
                        JSONArray buckets = sundayResult.getJSONObject("group_by_rating").getJSONArray("buckets");
                        sumCount = sundayResult.getInteger("doc_count");
                        for(Object o : buckets) {
                            JSONObject entry = (JSONObject) o;
                            if(entry.getIntValue("key") == 1) scoreSum += entry.getLongValue("doc_count") * 1;
                            if(entry.getIntValue("key") == 2) scoreSum += entry.getLongValue("doc_count") * 2;
                            if(entry.getIntValue("key") == 3) scoreSum += entry.getLongValue("doc_count") * 3;
                            if(entry.getIntValue("key") == 4) scoreSum += entry.getLongValue("doc_count") * 4;
                            if(entry.getIntValue("key") == 5) scoreSum += entry.getLongValue("doc_count") * 5;
                        }
                        float rating = sumCount == 0 ? 0 : (float) scoreSum / sumCount;
                        line.add(sunday, Webs.scale2PointUp(rating)); // 四舍五入且保留两位小数
                    } else {
                        line.add(sunday, 0f);// 未找到当前日期的数据, 直接下一个,赋值为 0
                    }
                    //将此次计算出来的 Review 个数与 得分总数储存起来供给计算 SUM 线的时候使用
                    if(sumResults.containsKey(sunday)) {
                        F.T2<Long, Long> sunDayTotal = sumResults.get(sunday);
                        sumResults.put(sunday, new F.T2<>(
                                (sunDayTotal._1 + scoreSum),
                                (sunDayTotal._2 + sumCount))
                        );
                    } else {
                        sumResults.put(sunday, new F.T2<>(scoreSum, sumCount));
                    }
                }
                lineChart.series(line);
            }

            Series.Line sumLine = new Series.Line("SUM", true);
            //计算 SUM
            for(Date sunday : sundays) {
                F.T2<Long, Long> sunDayTotal = sumResults.get(sunday);
                float rating = sunDayTotal._2 == 0 ? 0 : (float) sunDayTotal._1 / sunDayTotal._2;
                sumLine.add(sunday, Webs.scale2PointUp(rating));
            }
            lineChart.series(sumLine);
            Cache.add(cacked_key, lineChart, "4h");
        }
        return Cache.get(cacked_key, HighChart.class);
    }

    /**
     * 计算指定的产品线的 Listing 下所有的 Review 在一个时间段内的中差评情况, 并且返回的 Map 组装成 HightChart 使用的格式;
     *
     * @param from     开始时间
     * @param to       结束时间
     * @param category 品线
     * @return
     */
    @Cached("4h")
    public static HighChart poorRatingLine(Date from, Date to, String category) {
        String cacked_key = Caches.Q.cacheKey("poorRatingLine", from, to, category);
        HighChart lineChart = Cache.get(cacked_key, HighChart.class);
        if(lineChart != null) return lineChart;
        synchronized(cacked_key.intern()) {
            lineChart = Cache.get(cacked_key, HighChart.class);
            if(lineChart != null) return lineChart;
            lineChart = new HighChart(Series.LINE);
            lineChart.title = "产品线 Review 中差评率趋势图(单位: %)";

            List<Date> sundayList = Dates.getAllSunday(from, to);
            MetricReviewService service = new MetricReviewService(from, to, category);
            JSONObject aggregations = service.countPoorRatingByDateRange();
            List<String> jsonKeys = new ArrayList<>();
            for(M m : Promises.MARKETS) jsonKeys.add(m.name());
            jsonKeys.add("SUM");

            for(String jsonKey : jsonKeys) {
                Series.Line line = new Series.Line(jsonKey);
                List<Date> sundayListCopy = new ArrayList<>(sundayList);

                if(aggregations != null) {
                    JSONObject countByMarket = aggregations.getJSONObject(jsonKey);
                    if(countByMarket != null) {
                        JSONObject countByReviewDate = countByMarket.getJSONObject("count_by_review_date");
                        for(Object o : countByReviewDate.getJSONArray("buckets")) {
                            JSONObject entry = (JSONObject) o;
                            long reviewCount = entry.getLongValue("doc_count");
                            Date point = Dates.night(
                                    new DateTime(Dates.date2JDate(entry.getDate("key"))).plusDays(6).toDate());
                            //由于 ES 返回的都是周一,因此需要加上 6 天得到周末
                            int poorReviewCount = entry.getJSONObject("poor_rating_review").getIntValue("doc_count");
                            float poorRate = ((float) poorReviewCount / (float) reviewCount) * 100;
                            if(sundayList.contains(point)) {
                                line.add(point, Webs.scale2PointUp(poorRate)); //
                                // 四舍五入且保留两位小数
                                sundayListCopy.remove(point);
                            }
                        }
                    }
                }
                for(Date sunday : sundayListCopy) line.add(sunday, 0f); //补充没有数据的那些点
                line.visible = jsonKey.equalsIgnoreCase("sum");
                lineChart.series(line.sort());
            }
            Cache.add(cacked_key, lineChart, "4h");
        }
        return Cache.get(cacked_key, HighChart.class);
    }

    /**
     * 系统内第一个 Review 产生的时间
     *
     * @return
     */
    public static Date firstReviewDate() {
        AmazonListingReview firstReview = AmazonListingReview.find("ORDER BY reviewDate ASC").first();
        if(firstReview == null) return new java.util.Date();
        return firstReview.reviewDate;
    }

    /**
     * 初始化一些 Review 参数，再根据 Review 来判断更新还是保存
     */
    public void saveOrUpdate() {
        AmazonListingReview fromDB = AmazonListingReview.findById(this.alrId);
        if(fromDB == null) {
            if(StringUtils.isNotBlank(this.listingId)) {
                Listing entity = Listing.findById(this.listingId);
                if(entity == null) {
                    F.T2<String, M> asinAndMarket = Listing.unLid(this.listingId);
                    entity = Listing.crawl(asinAndMarket._1, asinAndMarket._2);
                    entity.save();
                }
                this.listing = entity;
                this.isOwner = this.listing.product != null;
            }
            this.createDate = this.reviewDate;
            Orderr ord = this.tryToRelateOrderByUserId();
            if(ord != null) this.orderr = ord;

            this.createReview();// 创建新的
            this.checkMailAndTicket();
        } else {
            fromDB.updateAttr(this); // 更新
            fromDB.checkMailAndTicket();
        }
    }
}
