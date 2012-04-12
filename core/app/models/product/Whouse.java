package models.product;

import models.market.Account;
import models.market.SellingQTY;
import org.apache.commons.lang.StringUtils;
import org.apache.commons.lang.math.NumberUtils;
import play.data.validation.Required;
import play.db.jpa.Model;
import play.libs.IO;

import javax.persistence.*;
import java.io.File;
import java.util.ArrayList;
import java.util.List;

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


    public void setName(String name) {
        this.name = name;
        if(this.name != null) this.name = this.name.toUpperCase();
    }

    /**
     * 从 FBA 的 CSV report 文件中解析出系统中存在或者不存在的 SellingQTY, 但不做保存到数据库的处理.
     *
     * @param file
     * @return
     */
    public List<SellingQTY> fbaCSVParseSQTY(File file) {
        List<String> lines = IO.readLines(file);
        lines.remove(0);

        List<SellingQTY> qtys = new ArrayList<SellingQTY>();
        for(String line : lines) {
            String[] vals = StringUtils.splitPreserveAllTokens(line, "\t");

            // 如果属于 UnUsedSKU 那么则跳过这个解析
            if(Product.unUsedSKU(vals[0])) continue;

            String sqtyId = String.format("%s_%s", vals[0].toUpperCase(), this.id);
            SellingQTY qty = SellingQTY.findById(sqtyId);
            if(qty == null) qty = new SellingQTY(sqtyId);

            qty.inbound = NumberUtils.toInt(vals[16]);
            qty.qty = NumberUtils.toInt(vals[10]);
            qty.unsellable = NumberUtils.toInt(vals[11]);
            qty.pending = NumberUtils.toInt(vals[12]);

            qtys.add(qty);
        }

        return qtys;
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
