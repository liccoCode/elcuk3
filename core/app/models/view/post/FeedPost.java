package models.view.post;

import models.market.Feed;
import play.libs.F;

import java.util.List;

/**
 * Created by IntelliJ IDEA.
 * User: mac
 * Date: 13-12-26
 * Time: 下午5:53
 */
public class FeedPost extends Post<Feed> {
    public FeedPost() {
        this.perSize = 20;
    }

    @Override
    public F.T2<String, List<Object>> params() {
        throw new UnsupportedOperationException("FeedPost 不需要调用 params()");
    }

    @Override
    public List<Feed> query() {
        F.T2<String, List<Object>> params = params();
        this.count = this.count();
        return Feed.find(params._1, params._2.toArray()).fetch(this.page, this.perSize);
    }

    @Override
    public Long count() {
        return Feed.count();
    }

    @Override
    public Long getTotalCount() {
        return this.count();
    }
}
