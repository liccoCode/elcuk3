package models;

import models.market.Selling;
import org.apache.commons.lang.StringUtils;
import play.utils.FastRuntimeException;

import java.util.ArrayList;
import java.util.List;

/**
 * 分页使用的对象
 * User: wyattpan
 * Date: 1/23/12
 * Time: 5:34 AM
 */
public class PageInfo<T> {
    public PageInfo() {
    }

    public PageInfo(int size, Long count, int page, List<T> items) {
        this.size = size;
        this.count = count;
        this.page = page;
        this.items = items;
    }

    public PageInfo(int page) {
        this.page = page;
        this.size = 20;
    }

    public String param;

    public int size;

    public Long count;


    public int page;

    public int totalPage;


    public List<T> items;

    public int begin;
    public int end;

    public void setPage(int page) {
        if(page <= 0) page = 1;
        this.page = page;
    }

    public void setCount(long count) {
        this.count = count;
        if(this.count != null && this.items != null)
            this.totalPage = ((this.count / (float) this.size) < 1 ? 1 : new Double(Math.ceil(this.count / (float) this.size)).intValue());
    }

    public void setItems(List<T> itmes) {
        this.items = itmes;
        if(this.count != null && this.items != null)
            this.totalPage = ((this.count / (float) this.size) < 1 ? 1 : new Double(Math.ceil(this.count / (float) this.size)).intValue());
    }

    public int getBegin() {
        if(this.begin <= 0) this.begin = (this.page - 1) * this.size;
        return this.begin;
    }

    public int getEnd() {
        if(this.end <= 0) {
            this.end = this.page * this.size;
            if(this.count == null || this.count < 0)
                throw new FastRuntimeException("PageInfo.count must be set first and can not blow then zero!");
            if(this.count < (this.end + 1))
                this.end = this.count.intValue() - 1; // 这个 1 是对应与集合的大小 size(10) 与集合内元素的 索引(0~9)
        }
        return this.end;
    }

    public int getTotalPage() {
        if(this.totalPage == 0 && this.count != null)
            this.totalPage = ((this.count / (float) this.size) < 1 ? 1 : new Double(Math.ceil(this.count / (float) this.size)).intValue());
        return this.totalPage;
    }

    /**
     * PageInfo 针对 Selling 过滤与搜索的方法; 支持 SKU/MSKU/ASIN 的搜索
     *
     * @param items
     * @param p
     * @return
     */
    public static List<Selling> fixItemSize(List<Selling> items, PageInfo<Selling> p) {
        List<Selling> ar = new ArrayList<Selling>();
        List<Selling> allow = new ArrayList<Selling>();
        if(items == null || items.size() == 0) return ar;

        /**
         * 1. 根据 p.param 过滤出符合的集合;
         * 2. 然后再在符合的集合中进行分页;
         */
        if(StringUtils.isNotBlank(p.param)) {
            for(Selling se : items) {
                if(StringUtils.containsIgnoreCase(se.merchantSKU, p.param) ||
                        StringUtils.containsIgnoreCase(se.asin, p.param))
                    allow.add(se);
            }
        } else {
            allow = items;
        }

        p.count = (long) allow.size();
        int index = p.begin;
        int size = (p.size <= allow.size() ? p.size : allow.size());
        for(; ar.size() <= size; ) {
            if(index >= p.count) break;
            ar.add(allow.get(index++));
        }

        return ar;
    }
}
