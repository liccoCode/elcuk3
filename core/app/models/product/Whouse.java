package models.product;

import com.google.gson.annotations.Expose;
import models.market.Account;
import play.data.validation.Required;
import play.db.jpa.Model;

import javax.persistence.*;

/**
 * 不同的仓库的抽象
 * User: Wyatt
 * Date: 12-1-8
 * Time: 上午6:06
 */
@Entity
public class Whouse extends Model {

    /**
     * 如果这个 Whouse 为 FBA 的, 那么则一定需要绑定一个 Account, 只有这样才能从 Account 获取必要的信息到 Amazon FBA 下载
     * FBA Inventory 库存信息
     */
    @OneToOne(fetch = FetchType.LAZY)
    public Account account;

    public enum T {
        /**
         * FBA 仓库, 独立出来
         */
        FBA,
        // 如果还有新的第三方仓库, 则再从代码中添加新类别
        /**
         * 自有仓库
         */
        SELF
    }


    @Required
    @Column(nullable = false, unique = true)
    @Expose
    public String name;

    @Column(nullable = false)
    @Expose
    public String address;

    @Column(nullable = false)
    @Expose
    public String city;

    @Column(nullable = false)
    @Expose
    public String province;

    @Column(nullable = false)
    @Expose
    public String postalCode;

    @Column(nullable = false)
    @Expose
    public String country;

    @Column(nullable = false)
    @Expose
    public T type;

    @Lob
    @Expose
    public String memo;


    @PrePersist
    public void prePersist() {
        if(this.address == null) {
            this.address = String.format("%s %s %s %s",
                    this.country, this.province, this.city, this.postalCode);
        }
    }


    public void setName(String name) {
        this.name = name;
        if(this.name != null) this.name = this.name.toUpperCase();
    }


    @Override
    public boolean equals(Object o) {
        if(this == o) return true;
        if(o == null || getClass() != o.getClass()) return false;
        if(!super.equals(o)) return false;

        Whouse whouse = (Whouse) o;

        if(id != null ? !id.equals(whouse.id) : whouse.id != null) return false;

        return true;
    }

    @Override
    public int hashCode() {
        int result = super.hashCode();
        result = 31 * result + (id != null ? id.hashCode() : 0);
        return result;
    }

    public String name() {
        return String.format("%s: %s", this.type, this.name);
    }
}
