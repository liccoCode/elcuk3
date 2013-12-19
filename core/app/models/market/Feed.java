package models.market;

import play.db.jpa.Model;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Lob;
import java.util.Date;

/**
 * 异步处理的时候的 Feed
 * User: wyatt
 * Date: 12/18/13
 * Time: 2:27 PM
 */
@Entity
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


    public static Feed newSellingFeed(String content, Selling selling) {
        Feed feed = new Feed();
        feed.content = content;
        feed.fid = selling.sellingId;
        return feed.save();
    }

}
