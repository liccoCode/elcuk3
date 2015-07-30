package models.market;

import models.User;
import org.apache.commons.lang.StringUtils;
import play.db.jpa.Model;

import javax.persistence.*;
import java.util.Date;
import java.util.concurrent.TimeUnit;

/**
 * 异步处理的时候的 Feed
 * User: wyatt
 * Date: 12/18/13
 * Time: 2:27 PM
 */
@Entity
@org.hibernate.annotations.Entity(dynamicUpdate = true)
public class Feed extends Model {
    private static final long serialVersionUID = 370209511312724644L;

    public Feed() {
    }

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

    public Date createdAt;

    public Date updatedAt;

    /**
     * Feed 的创建者
     */
    public String byWho;

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
     * @param id
     * @return
     */
    public static int feedcount(Long id) {
        return Feed.find("SUBSTRING_INDEX(fid,'|','-1') LIKE ? and createdAt>=date_sub(now(), interval 10 minute) ",
                id).fetch().size();
    }

    public static Feed newSellingFeed(String content, Selling selling) {
        Feed feed = new Feed();
        feed.content = content;
        feed.fid = selling.sellingId;
        feed.byWho = User.username();
        return feed.save();
    }

    public static Feed updateSellingFeed(String content, Selling selling) {
        return newSellingFeed(content, selling);
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
}
