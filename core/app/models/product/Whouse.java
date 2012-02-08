package models.product;

import models.market.Account;
import org.apache.commons.lang.StringUtils;
import org.apache.commons.lang.math.NumberUtils;
import play.Logger;
import play.data.validation.Required;
import play.db.jpa.Model;
import play.libs.IO;
import play.utils.FastRuntimeException;

import javax.persistence.*;
import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 不同的仓库的抽象
 * User: Wyatt
 * Date: 12-1-8
 * Time: 上午6:06
 */
@Entity
public class Whouse extends Model {
    /**
     * 记录此仓库的所有产品; 由于我们产品的数量级不大, 所以直接关联上;
     * 关系交给产品决定自己与哪一个仓库关联上;
     * 需要控制好仓库的集联删除
     */
    @OneToMany(mappedBy = "whouse", cascade = {CascadeType.PERSIST, CascadeType.REFRESH, CascadeType.MERGE})
    public List<ProductQTY> qtys;

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

    @Column(nullable = false)
    public T type;

    @Lob
    public String memo;


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

    /**
     * 解析 FBA 的库存文件
     *
     * @return
     */
    public List<ProductQTY> fbaCSVParse(File file) {
        List<String> lines = IO.readLines(file);
        lines.remove(0); // 删除第一行
        Map<String, ProductQTY> qtyMap = new HashMap<String, ProductQTY>();
        for(String line : lines) {
            String[] vals = StringUtils.splitPreserveAllTokens(line, "\t");
            String sku = vals[0].trim().toUpperCase();
            if(!qtyMap.containsKey(sku)) {
                ProductQTY qty = new ProductQTY();
                qty.whouse = this;
                qty.inbound = NumberUtils.toInt(vals[16]);
                qty.qty = NumberUtils.toInt(vals[10]);
                qty.unsellable = NumberUtils.toInt(vals[11]);
                qty.pending = NumberUtils.toInt(vals[12]);
                qty.product = new Product(sku);
                qtyMap.put(sku, qty);
            } else {
                Logger.warn(file.getAbsolutePath() + "/" + file.getName() + " have repeat MerchantSKU!");
            }
        }
        return new ArrayList<ProductQTY>(qtyMap.values());
    }

    @Override
    public boolean equals(Object o) {
        if(this == o) return true;
        if(o == null || getClass() != o.getClass()) return false;
        if(!super.equals(o)) return false;

        Whouse whouse = (Whouse) o;

        if(!name.equals(whouse.name)) return false;
        if(type != whouse.type) return false;

        return true;
    }

    @Override
    public int hashCode() {
        int result = super.hashCode();
        result = 31 * result + name.hashCode();
        result = 31 * result + type.hashCode();
        return result;
    }
}
