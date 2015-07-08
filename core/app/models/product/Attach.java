package models.product;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.google.gson.annotations.Expose;
import exception.PaymentException;
import helper.Constant;
import helper.HTTP;
import helper.J;
import models.User;
import models.embedded.ERecordBuilder;
import org.apache.commons.io.FileUtils;
import org.apache.commons.lang.StringUtils;
import org.apache.commons.lang.math.RandomUtils;
import org.apache.http.impl.client.BasicCookieStore;
import org.apache.http.impl.cookie.BasicClientCookie;
import play.Logger;
import play.db.jpa.Model;
import play.libs.Codec;
import play.libs.F;
import play.utils.FastRuntimeException;
import sun.misc.BASE64Decoder;

import javax.persistence.*;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.net.URLDecoder;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Map;

import java.io.BufferedInputStream;
import java.net.URL;
import java.net.HttpURLConnection;

/**
 * 系统中, 可以附加的附件; 这个 Model 存在这里, 其自己不知道自己附属与谁, 但其拥有者知道(单项关系), 但并非使用 DB 的
 * 外键来控制, 而是通过程序使用 Attach.fid 来进行控制.
 * <p/>
 * User: wyattpan
 * Date: 5/2/12
 * Time: 10:34 AM
 */
@Entity
@org.hibernate.annotations.Entity(dynamicUpdate = true)
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
        SHIPMENT,
        /**
         * 支付信息的凭证
         */
        PAYMENTS {
            /**
             * Payment 删除方法为软删除.
             * @param attach
             */
            @Override
            public void delete(Attach attach) {
                // 1. 标记文件软删除
                // 2. 将文件挪动到软删除的目录下.
                attach.remove = true;
                try {
                    FileUtils.moveFile(attach.getFile(), new File(attach.softDeleteLocation()));
                    attach.location = attach.softDeleteLocation();
                } catch(IOException e) {
                    throw new PaymentException(PaymentException.MKDIR_ERROR);
                }
                attach.save();
                new ERecordBuilder("payment.uploadDestroy")
                        .msgArgs(attach.fileName)
                        .fid(attach.fid)
                        .save();
            }
        },

        /**
         * 质检的凭证
         */
        CHECKTASK;

        /**
         * 默认的附件删除方法.物理删除
         *
         * @param attach
         */
        public void delete(Attach attach) {
            attach.delete();
            String localtion = attach.location;
            FileUtils.deleteQuietly(new File(localtion));
        }
    }

    public Attach() {
    }

    public Attach(P p, String fid) {
        this.p = p;
        this.fid = fid;
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

    /**
     * 软删除标记
     */
    public boolean remove = false;

    @Transient
    public File file;

    /**
     * 在服务器上的哪个位置;
     * 是给予 CONSTANT.UPLOAD_PATH 地址的
     */
    @Column(nullable = false)
    public String location;

    public enum T {
        /**
         * 图片
         */
        IMAGE,

        /**
         * 包装
         */
        PACKAGE,

        /**
         * 说明书
         */
        INSTRUCTION,

        /**
         * 丝印文件
         */
        SILKSCREEN
    }

    /**
     * 附件类型(这个文件是用来干什么的)
     */
    @Enumerated(EnumType.STRING)
    @Expose
    public T attachType;

    /**
     * 创建时间
     */
    @Expose
    public Date createDate = new Date();

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
        this.p.delete(this);
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
            Logger.warn("Attach[%s] is Not found.", J.G(a));

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
        this.fileName = String.format("%s_%s%s", this.fid, subfix,
                this.file.getPath().substring(this.file.getPath().lastIndexOf("."))).trim();
        this.location = this.location();
        this.createDate = new Date();
        return this;
    }

    public String location() {
        return String.format("%s/%s/%s", Constant.UPLOAD_PATH, p, this.fileName);
    }

    public String softDeleteLocation() {
        return String.format("%s/%s_DELETE/%s", Constant.UPLOAD_PATH, p, this.fileName);
    }

    // --------------- scopes -------------

    public static Attach findByFileName(String fileName) {
        return Attach.find("fileName=?", fileName).first();
    }

    public static Attach findByOutName(String outName) {
        return Attach.find("outName=?", outName).first();
    }

    /**
     * 根据外键查出所有的附件并进行分组和排序
     *
     * @param fid
     * @param p
     * @return
     */
    public static List<Attach> attaches(String fid, String p) {
        if(StringUtils.isNotBlank(p))
            return Attach
                    .find("fid=? AND p=? AND remove=false ORDER BY originName,createDate ASC", fid, Attach.P.valueOf(p))
                    .fetch();
        else
            return Attach.find("fid=? AND remove=false ORDER BY originName,createDate ASC", fid).fetch();
    }

    public static File generateImageByBase64(String base64Str, String originName) throws IOException {
        BASE64Decoder decoder = new BASE64Decoder();
        byte[] bytes = decoder.decodeBuffer(base64Str);
        String tmpLocation = String.format("%s/%s", Constant.TMP, originName);
        File image = new File(tmpLocation);
        FileOutputStream fos = new FileOutputStream(tmpLocation);
        fos.write(bytes);
        fos.close();
        return image;
    }

    public static BasicClientCookie cookie(String name, String value) {
        BasicClientCookie cookie = new BasicClientCookie(name, value);
        cookie.setDomain("easyacc.com");
        cookie.setVersion(0);
        cookie.setPath("/");
        return cookie;
    }

    /**
     * 获取文件目录的网址
     *
     * @param sku
     * @return
     */
    public static String attachPathList(String sku) {
        return "http://kod.easyacc.com/index.php?explorer/pathList&path=SKU/" + sku;
    }

    /**
     * 读取图片的网址
     *
     * @param sku
     * @param name
     * @return
     */
    public static String attachImage(String sku, String name) {
        return "http://kod.easyacc.com/data/User/elcuk2/home/SKU/" + sku + "/" + name;
    }

    /**
     * 获取SKU下的图片列表
     *
     * @param fid
     * @return
     */
    public static List<java.util.Map<String, String>> attachImages(String fid) {
        JsonArray rows = null;
        String url = Attach.attachPathList(fid);
        BasicCookieStore store = new BasicCookieStore();
        store.addCookie(Attach.cookie("kod_name", "elcuk2"));
        store.addCookie(Attach.cookie("kod_token", User.Md5(User.userMd5("elcuk2"))));
        store.addCookie(Attach.cookie("kod_user_language", "zh_CN"));
        store.addCookie(Attach.cookie("kod_user_online_version", "check-at-1418867696"));
        String rtJson = HTTP.get(store, url);
        JsonObject data = null;
        try {
            data = new JsonParser().parse(rtJson).getAsJsonObject().get("data")
                    .getAsJsonObject();
        } catch(Exception e) {
        }
        rows = data.get("filelist").getAsJsonArray();
        List<java.util.Map<String, String>> imgs = new ArrayList<Map<String, String>>();
        for(JsonElement row : rows) {
            try {
                String name = row.getAsJsonObject().get("name").getAsString();
                java.util.Map<String, String> map = new java.util.HashMap<String, String>();
                map.put("name", name);
                map.put("href", Attach.attachImage(fid, name));
                imgs.add(map);
            } catch(Exception e) {
            }
        }
        return imgs;
    }


    /**
     * 图片网址转为文件
     *
     * @param destUrl
     * @param fileName
     * @throws IOException
     */

    public static F.T2<String, BufferedInputStream> urlToFile(String destUrl,String imagename){
        try {
            URL url = null;
            BufferedInputStream bis = null;
            HttpURLConnection httpUrl = null;
            // 建立链接
            url = new URL(destUrl);
            httpUrl = (HttpURLConnection) url.openConnection();
            // 连接指定的资源
            httpUrl.connect();
            // 获取网络输入流
            bis = new BufferedInputStream(httpUrl.getInputStream());

            return new F.T2<String,BufferedInputStream>(imagename, bis);
        } catch(Exception e) {

        }
        return null;
    }

}












