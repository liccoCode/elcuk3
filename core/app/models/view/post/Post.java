package models.view.post;

import org.apache.commons.lang.StringUtils;
import org.joda.time.DateTime;
import play.libs.F;

import java.util.Date;
import java.util.List;

/**
 * Created by IntelliJ IDEA.
 * User: wyattpan
 * Date: 9/4/12
 * Time: 5:10 PM
 */
public abstract class Post<T> {
    public Date from = DateTime.now().minusMonths(2).toDate();
    public Date to = new Date();
    public String search;

    public int page = 1;
    public int perSize = 50;
    public long count = 1;

    /**
     * 用来计算搜索的条件
     *
     * @return
     */
    public abstract F.T2<String, List<Object>> params();

    /**
     * 计算总行数
     *
     * @return
     */
    public Long count() {
        return count(params());
    }

    /**
     * 计算总行数
     *
     * @return
     */
    public Long count(F.T2<String, List<Object>> params) {
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

    /**
     * [search] -> [%search%] ,用在 SQL 语句的 LIKE 查询中
     *
     * @return
     */
    public String word() {
        return String.format("%%%s%%", StringUtils.replace(this.search.trim(), "'", "''"));
    }

    public int totalPage() {
        return new Double(Math.ceil(this.count / ((float) this.perSize))).intValue();
    }

    public int getTotalPage() {
        return totalPage();
    }
}
