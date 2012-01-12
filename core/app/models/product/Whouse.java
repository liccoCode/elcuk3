package models.product;

import play.Logger;
import play.data.validation.Required;
import play.db.jpa.Model;
import play.utils.FastRuntimeException;

import javax.persistence.*;
import java.util.List;

/**
 * 不同的仓库的抽象
 * User: Wyatt
 * Date: 12-1-8
 * Time: 上午6:06
 */
@Entity
public class Whouse extends Model {
    @Required
    @Column(nullable = false, unique = true)
    public String name;

    @Column(nullable = false)
    public String address;

    @Column(nullable = false)
    public String city;

    @Column(nullable = false)
    public String province;

    @Column(nullable = false)
    public String postalCode;

    @Column(nullable = false)
    public String country;

    @Lob
    public String memo;

    /**
     * 记录此仓库的所有产品; 由于我们产品的数量级不大, 所以直接关联上;
     * 关系交给产品决定自己与哪一个仓库关联上;
     * 需要控制好仓库的集联删除
     */
    @OneToMany(mappedBy = "whouse", cascade = {CascadeType.PERSIST, CascadeType.REFRESH, CascadeType.MERGE})
    public List<ProductQTY> qtys;

    @PrePersist
    public void prePersist() {
        if(this.address == null) {
            this.address = String.format("%s %s %s %s",
                    this.country, this.province, this.city, this.postalCode);
        }
    }

    /**
     * 删除 Warehouse 的时候需要进行检查
     */
    @PreRemove
    public void checkDelete() {
        if(this.qtys != null && this.qtys.size() > 0) {
            for(ProductQTY qty : this.qtys) {
                if(qty.qty + qty.pending + qty.unsellable > 0) {
                    Logger.warn("Unexpect operation.");
                    throw new FastRuntimeException("Ops, warehouse " + this.name + " have product quantity, so cannot be remove.");
                }
            }
        }
    }

    public void setName(String name) {
        this.name = name;
        if(this.name != null) this.name = this.name.toUpperCase();
    }
}
