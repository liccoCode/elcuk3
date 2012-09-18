package models.view;

import play.db.jpa.GenericModel;
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
        this.page = page;
        this.count = count;
        this.size = size;
        this.items = items;
    }

    public Pager(int page, Long count, List<T> items) {
        this(page, count, 100, items);
    }

    public Pager(int page) {
        this(page, 50);
    }

    public Pager(int page, int perPage) {
        this.page = page;
        this.size = perPage;
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
