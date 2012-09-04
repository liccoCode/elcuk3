package models.view.post;

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
public abstract class Post {
    public Date from = DateTime.now().minusMonths(2).toDate();
    public Date to = new Date();
    public String search;

    public abstract F.T2<String, List<Object>> params();

    /**
     * [search] -> [%search%] ,用在 SQL 语句的 LIKE 查询中
     * @return
     */
    public String word() {
        return String.format("%%%s%%", this.search);
    }
}
