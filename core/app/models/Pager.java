package models;

import play.utils.FastRuntimeException;

import java.util.List;

/**
 * Created by IntelliJ IDEA.
 * User: wyattpan
 * Date: 7/5/12
 * Time: 4:00 PM
 */
public class Pager<T> {

    public Pager() {
    }

    public Pager(int size, Long count, int page, List<T> items) {
        this.size = size;
        this.count = count;
        this.page = page;
        this.items = items;
    }

    public Pager(int page) {
        this.page = page;
        this.size = 20;
    }

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
        if(this.begin <= 0) this.begin = ((this.page < 1 ? 1 : this.page) - 1) * this.size;
        return this.begin;
    }

    public int getEnd() {
        if(this.end <= 0) {
            this.end = this.page * this.size;
            if(this.count == null || this.count < 0)
                throw new FastRuntimeException("AnalyzesPager.count must be set first and can not blow then zero!");
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
