package models.view;

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


    public Pager(int page, Long count, int size, List<T> items) {
        this.size = size;
        this.count = count;
        this.page = page;
        this.items = items;
    }

    public Pager(Long count, int page, List<T> items) {
        this(100, count, page, items);
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

    /**
     * 搜索数据的索引开始位置
     * LIMIT [0],10
     */
    public int begin;

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

    public int getTotalPage() {
        if(this.totalPage == 0 && this.count != null)
            this.totalPage = ((this.count / (float) this.size) < 1 ? 1 : new Double(Math.ceil(this.count / (float) this.size)).intValue());
        return this.totalPage;
    }
}
