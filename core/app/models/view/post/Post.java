package models.view.post;

import org.apache.commons.lang.StringUtils;
import org.joda.time.DateTime;
import play.libs.F;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

/**
 * <pre>
 *
 * 提供的参数:
 * * from:Date
 * * to:Date
 * * search:String
 * * page:int
 * * perSize:int
 * * count:long
 * </pre>
 * User: wyattpan
 * Date: 9/4/12
 * Time: 5:10 PM
 */
public abstract class Post<T> implements Serializable, Cloneable {
    public Date from = DateTime.now().minusMonths(2).toDate();
    public Date to = new Date();
    public String search;
    public int page = 1;
    public int perSize = 50;
    public long count = 1;
    /**
     * 是否需要翻页
     */
    public boolean pagination = true;

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
     * 计算总行数(带搜索条件的)
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
        return (int) (Math.ceil(this.count / ((float) this.perSize)));
    }

    public int getTotalPage() {
        return totalPage();
    }

    /**
     * 当前这个 Model 的所有数据
     *
     * @return
     */
    public Long getTotalCount() {
        throw new UnsupportedOperationException("需要分页, 请自行实现获取 TotalCount 的方法");
    }

    @Override
    public Object clone() throws CloneNotSupportedException {
        return super.clone();
    }

    /**
     * 使用程序自己对 List 集合进行分页操作
     *
     * @param dtos
     * @return
     */
    public List<T> programPager(List<T> dtos) {
        this.count = dtos.size();
        List<T> afterPager = new ArrayList<T>();
        int index = (this.page - 1) * this.perSize;
        int end = index + this.perSize;
        for(; index < end; index++) {
            if(index >= this.count) break;
            afterPager.add(dtos.get(index));
        }
        return afterPager;
    }
}
