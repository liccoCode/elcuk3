package models.product;

import com.google.gson.annotations.Expose;
import helper.Constant;
import helper.Webs;
import org.apache.commons.io.FileUtils;
import org.apache.commons.lang.StringUtils;
import org.apache.commons.lang.math.RandomUtils;
import org.hibernate.annotations.Cache;
import org.hibernate.annotations.CacheConcurrencyStrategy;
import play.Logger;
import play.db.jpa.Model;
import play.libs.Codec;
import play.utils.FastRuntimeException;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.PrePersist;
import javax.persistence.Transient;
import java.io.File;
import java.net.URLDecoder;

/**
 * 系统中, 可以附加的附件; 这个 Model 存在这里, 其自己不知道自己附属与谁, 但其拥有者知道(单项关系), 但并非使用 DB 的
 * 外键来控制, 而是通过程序使用 Attach.fid 来进行控制.
 * <p/>
 * User: wyattpan
 * Date: 5/2/12
 * Time: 10:34 AM
 */
@Entity
@Cache(usage = CacheConcurrencyStrategy.READ_WRITE)
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
        LISTING,

        /**
         * Deliveryment
         */
        DELIVERYMENT,
        /**
         * SHIPMENT
         */
        SHIPMENT
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
    @Expose
    public String outName;

    /**
     * 这个为外键的 Id, 标识这个附件是与什么相关的;
     * 例如, fid 为 sku, 那么则可以从 SKU 加载出这个 Attach
     */
    @Column(nullable = false)
    @Expose
    public String fid;

    /**
     * TODO 这个没使用到
     */
    @Expose
    public P p;

    @Column(nullable = false, unique = true)
    @Expose
    public String fileName;

    /**
     * 文件的原始文件名
     */
    @Column(nullable = false, columnDefinition = "varchar(255) DEFAULT ''")
    @Expose
    public String originName;

    /**
     * 以 bytes 为单位
     */
    @Expose
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

    public File getFile() {
        if(this.file == null)
            this.file = new File(this.location);
        return this.file;
    }

    public void rm() {
        // 1. 删除数据库中存储的文件
        // 2. 删除本地存储的文件
        String localtion = this.location;
        this.delete();
        FileUtils.deleteQuietly(new File(localtion));
    }

    public static Attach findAttach(Attach a) {
        if(a == null) throw new FastRuntimeException("Attach is null!");
        Attach rtAttach = null;
        if(a.isPersistent())
            rtAttach = a;
        else if(StringUtils.isNotBlank(a.fileName))
            rtAttach = Attach.findByFileName(a.fileName);
        else if(StringUtils.isNotBlank(a.outName))
            rtAttach = Attach.findByOutName(a.outName);
        else
            Logger.warn("Attach[%s] is Not found.", Webs.G(a));

        return rtAttach;
    }

    /**
     * 从 Controller 上传上了的附件, 进行初始化 Attach 的一些参数
     *
     * @return
     */
    public Attach setUpAttachName() {
        long subfix = RandomUtils.nextInt();
        this.fileSize = this.file.length();
        this.originName = URLDecoder.decode(this.file.getName());
        this.fileName = String.format("%s_%s%s", this.fid, subfix, this.file.getPath().substring(this.file.getPath().lastIndexOf("."))).trim();
        this.location = String.format("%s/%s/%s", Constant.UPLOAD_PATH, p, this.fileName);
        return this;
    }

    public static Attach findByFileName(String fileName) {
        return Attach.find("fileName=?", fileName).first();
    }

    public static Attach findByOutName(String outName) {
        return Attach.find("outName=?", outName).first();
    }
}
