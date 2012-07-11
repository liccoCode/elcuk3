package models.view;

import models.market.Selling;
import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.collections.Predicate;
import org.apache.commons.lang.StringUtils;

import java.util.ArrayList;
import java.util.List;

/**
 * 分页使用的对象
 * User: wyattpan
 * Date: 1/23/12
 * Time: 5:34 AM
 */
public class AnalyzesPager<T> extends Pager<T> {
    public AnalyzesPager() {
    }

    public AnalyzesPager(int size, Long count, int page, List<T> items) {
        this.size = size;
        this.count = count;
        this.page = page;
        this.items = items;
    }

    public AnalyzesPager(int page) {
        this.page = page;
        this.size = 20;
    }

    /**
     * Analyzes 页面的搜索框框
     */
    public String param;

    /**
     * account Id
     */
    public Long aid;

    /**
     * 根据 AnalyzesPager 对 Items 进行过滤
     *
     * @return
     */
    public static List<Selling> filterSellings(List<Selling> items, final AnalyzesPager<Selling> p) {
        if(items == null || items.size() == 0) return new ArrayList<Selling>();
        List<Selling> innerList = new ArrayList<Selling>(items);

        /**
         * 1. 首先过滤,
         * 2. 然后进行分页
         */

        /**
         * Params 关键字
         */
        if(StringUtils.isNotBlank(p.param)) {
            CollectionUtils.filter(innerList, new Predicate() {
                @Override
                public boolean evaluate(Object object) {
                    Selling se = (Selling) object;
                    return StringUtils.containsIgnoreCase(se.merchantSKU, p.param) || StringUtils.containsIgnoreCase(se.asin, p.param);
                }
            });
        }

        if(p.aid != null && p.aid > 0) {
            CollectionUtils.filter(innerList, new Predicate() {
                @Override
                public boolean evaluate(Object object) {
                    Selling se = (Selling) object;
                    return se.account.id.equals(p.aid);
                }
            });
        }

        List<Selling> ar = new ArrayList<Selling>();
        p.count = (long) innerList.size();
        int index = p.begin;
        int size = (p.size <= innerList.size() ? p.size : innerList.size());
        for(; ar.size() < size; ) {
            if(index >= p.count) break;
            ar.add(innerList.get(index++));
        }

        return ar;
    }
}
