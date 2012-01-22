package models;

import play.utils.FastRuntimeException;

import java.util.List;

/**
 * 分页使用的对象
 * User: wyattpan
 * Date: 1/23/12
 * Time: 5:34 AM
 */
public class PageInfo<T> {
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
        if(page < 0) page = 1;
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
}
