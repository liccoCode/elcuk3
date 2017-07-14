package models.view.post;

import org.apache.commons.lang3.StringUtils;
import org.elasticsearch.search.builder.SearchSourceBuilder;
import org.joda.time.DateTime;

import java.io.Serializable;
import java.util.Date;
import java.util.List;

/**
 * 针对 ElasticSearch 的搜索的 Post;
 * API 还需要优化
 * User: wyatt
 * Date: 10/24/13
 * Time: 5:16 PM
 */
public abstract class ESPost<T> implements Serializable, Cloneable {

    public Date end = DateTime.now().toDate();
    public Date begin = DateTime.now().minusMonths(2).toDate();
    public String search;

    public int page = 1;
    public int perSize = 50;
    public long count = 1;

    /**
     * 用来计算搜索的条件
     *
     * @return
     */
    public abstract SearchSourceBuilder params();


    /**
     * 计算总行数(带搜索条件的)
     *
     * @return
     */
    public Long count(SearchSourceBuilder searchBuilder) {
        throw new UnsupportedOperationException("请自行实现");
    }

    /**
     * 具体的查询方法
     *
     * @return
     */
    public List<T> query() {
        throw new UnsupportedOperationException("请自行实现");
    }

    public int getFrom() {
        return (this.page - 1) * this.perSize;
    }

    public int totalPage() {
        return (int) (Math.ceil(this.count / ((float) this.perSize)));
    }

    public int getTotalPage() {
        return totalPage();
    }

    public String search() {
        if(StringUtils.isBlank(this.search)) {
            return "*";
        } else {
            return this.search.toLowerCase();
        }
    }
}
