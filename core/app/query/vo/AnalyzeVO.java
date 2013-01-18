package query.vo;

import models.market.M;

import java.util.Date;

/**
 * --- VO 仅仅在业务层之间传递数据, 不要将 VO 传递到 Web 页面上 ---
 * <p/>
 * 值对象, 在使用 SQL 语句的时候, 一些无法满足从不同 Models 中加载出来的数据,
 * 封装为一个 VO 对象传递到前面去.
 * (数量少的时候 Tuple 可以解决, 当字段多了, Tuple 好难维护)
 * User: wyatt
 * Date: 1/17/13
 * Time: 11:47 AM
 */
public class AnalyzeVO {
    public String sku;
    public String sid;
    public String asin;

    public Integer qty;
    public Date date;

    public String aid;
    public Float usdCost;

    public M market;
}

