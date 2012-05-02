package models.product;

import play.db.jpa.Model;
import play.libs.Codec;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.PrePersist;
import javax.persistence.Transient;
import java.io.File;

/**
 * 系统中, 可以附加的附件; 这个 Model 存在这里, 其自己不知道自己附属与谁, 但其拥有者知道(单项关系), 但并非使用 DB 的
 * 外键来控制, 而是通过程序使用 Attach.fid 来进行控制.
 * <p/>
 * User: wyattpan
 * Date: 5/2/12
 * Time: 10:34 AM
 */
@Entity
public class Attach extends Model {

    /**
     * 一个额外的添加字段, 指明这个产品附属与谁
     */
    public enum P {
        /**
         * 产品
         */
        SKU,
        /**
         * Listing
         */
        LISTING
    }

    @PrePersist
    public void prePersist() {
        this.fid = this.fid.toUpperCase();
        this.outName = Codec.UUID();
    }

    /**
     * 给外部使用的唯一名字, 没有规律
     */
    @Column(nullable = false, unique = true)
    public String outName;

    /**
     * 这个为外键的 Id, 标识这个附件是与什么相关的;
     * 例如, fid 为 sku, 那么则可以从 SKU 加载出这个 Attach
     */
    @Column(nullable = false)
    public String fid;

    public P p;

    @Column(nullable = false)
    public String fileName;

    /**
     * 以 bytes 为单位
     */
    public Long fileSize;

    @Transient
    public File file;

    /**
     * 在服务器上的哪个位置;
     * 是给予 CONSTANT.UPLOAD_PATH 地址的
     */
    @Column(nullable = false)
    public String location;

    public void setFid(String fid) {
        this.fid = fid.toUpperCase();
    }
}
