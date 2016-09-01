package models.market;

import helper.Constant;
import helper.DBUtils;
import helper.HTTP;
import models.User;
import org.apache.commons.lang.StringUtils;
import org.apache.commons.lang3.math.NumberUtils;
import org.apache.http.NameValuePair;
import org.apache.http.message.BasicNameValuePair;
import org.hibernate.annotations.DynamicUpdate;
import play.Play;
import play.data.validation.Validation;
import play.db.helper.SqlSelect;
import play.db.jpa.Model;

import javax.persistence.*;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;

/**
 * 异步处理的时候的 Feed
 * User: wyatt
 * Date: 12/18/13
 * Time: 2:27 PM
 */
@Entity
@DynamicUpdate
public class Feed extends Model {
    private static final long serialVersionUID = 370209511312724644L;

    public String feedId;

    /**
     * 外键,唯一的外键, 必须存在
     */
    @Column(nullable = false)
    public String fid;

    /**
     * 提交的 Feed 的内容
     */
    @Lob
    public String content = " ";

    @Lob
    public String result = " ";

    /**
     * 分析结果，由ruby端解析回填而来
     */
    public String analyzeResult;

    public Date createdAt;

    public Date updatedAt;

    /**
     * Feed 的创建者
     */
    public String byWho;

    /**
     * 描述信息
     */
    public String memo;

    public enum T {
        SALE_AMAZON {
            @Override
            public String label() {
                return "上架 Listing 到 Amazon";
            }
        },
        ASSIGN_AMAZON_LISTING_PRICE {
            @Override
            public String label() {
                return "更新 Listing 的 Price 属性";
            }
        },
        FULFILLMENT_BY_AMAZON {
            @Override
            public String label() {
                return "设置 Listing Fulfillment By Amazon";
            }
        },
        FBA_INBOUND_CARTON_CONTENTS {
            @Override
            public String label() {
                return "提交 FBA 包装信息到 Amazon";
            }
        };

        public abstract String label();
    }

    @Enumerated(EnumType.STRING)
    public T type;

    public Feed() {
    }

    /**
     * @param content
     * @param memo
     * @param selling
     * @deprecated
     */
    public Feed(String content, String memo, Selling selling) {
        this.content = content;
        this.fid = selling.sellingId;
        this.memo = memo;
        this.byWho = User.username();
    }

    public Feed(String content, T type, Selling selling) {
        this(content, type, selling.sellingId);
    }

    public Feed(String content, T type, String fid) {
        this.content = content;
        this.type = type;
        this.fid = fid;
        this.byWho = User.username();
    }

    /**
     * 因 API 的限制, 所以每一个 Selling 不可以无限制的上传 Feed.
     * 每个 Feed 间隔 3 分钟以上
     * 更新：由于 Amazon 使用账户区分，所以系统也更新采用区分账户来做提交限制。
     *
     * @return
     */
    public static boolean isFeedAvalible(Long accountId) {
        int count = Feed.feedcount(accountId);
        if(count < 15) return true;
        Feed feed = Feed.newest(accountId);
        if(feed == null) return true;
        if(feed.createdAt == null) {
            feed.createdAt = new Date();
            feed.save();
        }
        return Math.abs(System.currentTimeMillis() - feed.createdAt.getTime()) >= TimeUnit.MINUTES.toMillis(1);
    }

    /**
     * 拿到该账户提交的最新的一个 Feed
     *
     * @return
     */
    public static Feed newest(Long id) {
        return Feed.find("SUBSTRING_INDEX(fid,'|','-1') LIKE ? ORDER BY createdAt DESC", id).first();
    }

    /**
     * 取10分钟内有多少个feed
     *
     * @param id
     * @return
     */
    public static int feedcount(Long id) {
        SqlSelect countFeed = new SqlSelect().select("count(id) AS count").from("Feed")
                .where("SUBSTRING_INDEX(fid,'|','-1') LIKE ? AND createdAt>=DATE_SUB(now(), INTERVAL 10 MINUTE)")
                .param(id);
        Map<String, Object> row = DBUtils.row(countFeed.toString(), countFeed.getParams().toArray());
        if(row == null) {
            return 0;
        } else {
            return NumberUtils.toInt(row.get("count").toString());
        }
    }

    public static Feed newSellingFeed(String content, Selling selling) {
        return new Feed(content, T.SALE_AMAZON, selling).save();
    }

    public static Feed newAssignPriceFeed(String content, Selling selling) {
        return new Feed(content, T.ASSIGN_AMAZON_LISTING_PRICE, selling).save();
    }

    public static Feed setFulfillmentByAmazonFeed(String content, Selling selling) {
        return new Feed(content, T.FULFILLMENT_BY_AMAZON, selling).save();
    }

    public static Feed updateSellingFeed(String content, Selling selling) {
        return new Feed(content, "更新 Listing 属性", selling).save();
    }

    @PrePersist
    public void beforeSave() {
        this.updatedAt = new Date();
        this.createdAt = new Date();
    }

    @PreUpdate
    public void beforeUpdate() {
        this.updatedAt = new Date();
    }

    public String checkResult() {
        String[] lines = StringUtils.split(this.result, "\r\n");
        for(String line : lines) {
            String[] args = StringUtils.splitPreserveAllTokens(line, "\t");
            if(args.length == 5 && "Error".equals(args[3])) {
                return "Amazon 报告该 Feed 包含错误,请检查";
            }
        }
        if(this.result.contains("Encoding::UndefinedConversionError")) {
            return "Feed 使用了非法字符,请报告开发";
        } else {
            return "Feed 处理成功";
        }
    }

    public static Feed getFeedWithSellingAndType(Selling selling, T type) {
        return Feed.find(
                String.format("fid=? AND (type=? OR memo=?) %s ORDER BY createdAt DESC",
                        Play.mode.isProd() ? "AND feedId IS NULL" : ""),
                selling.sellingId,
                type,
                type.label()).first();
    }

    public boolean sameHours(Feed other) {
        return this.createdAt.getTime() - other.createdAt.getTime() <= TimeUnit.HOURS.toMillis(1);
    }

    public void submit(List<NameValuePair> params) {
        params.add(new BasicNameValuePair("feed_id", this.id.toString()));// 提交哪一个 Feed
        String response = HTTP.post(System.getenv(Constant.ROCKEND_HOST) + "/amazon_submit_feed",
                params);
        if(StringUtils.isBlank(response)) Validation.addError("", "向 Rockend 提交请求: submit_feed 时出现了错误, 请稍后再重试!");
    }

    public boolean isFailed() {
        return this.analyzeResult != null && "失败".equalsIgnoreCase(this.analyzeResult);
    }
}
