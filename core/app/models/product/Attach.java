package models.product;

import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.google.gson.annotations.Expose;
import exception.PaymentException;
import helper.*;
import models.User;
import models.embedded.ERecordBuilder;
import org.apache.commons.io.FileUtils;
import org.apache.commons.lang.StringUtils;
import org.apache.commons.lang.math.RandomUtils;
import org.apache.http.impl.client.BasicCookieStore;
import org.apache.http.impl.cookie.BasicClientCookie;
import org.hibernate.annotations.DynamicUpdate;
import play.Logger;
import play.db.jpa.Model;
import play.libs.Codec;
import play.libs.F;
import play.utils.FastRuntimeException;
//BEGIN GENERATED CODE
import sun.misc.BASE64Decoder;
//END GENERATED CODE
import javax.persistence.*;
import java.io.*;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLDecoder;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.List;


/**
 * 系统中, 可以附加的附件; 这个 Model 存在这里, 其自己不知道自己附属与谁, 但其拥有者知道(单项关系), 但并非使用 DB 的
 * 外键来控制, 而是通过程序使用 Attach.fid 来进行控制.
 * <p/>
 * User: wyattpan
 * Date: 5/2/12
 * Time: 10:34 AM
 */
@Entity
@DynamicUpdate
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
        CHECKTASK,

        /**
         * 仓库上传的和产品相关的图片
         */
        PRODUCTWHOUSE,
        /**
         * 出库单上传附件
         */
        OUTBOUND,
        /**
         * 物料采购单上传附件
         */
        MATERIALPURCHASES,
        /**
         * 物料出货单上传附件
         */
        MATERIALPLAN,
        /**
         * 汇签审核
         */
        BATCHAPPLY;


        /**
         * 默认的附件删除方法.物理删除
         *
         * @param attach
         */
        public void delete(Attach attach) {
            attach.delete();
            //QiniuUtils.delete(attach.fid+"-"+attach.originName);
            //String localtion = attach.location;
            //FileUtils.deleteQuietly(new File(localtion));
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
    /**
     * 七牛云 服务器 url地址
     */
    public String qiniuLocation;

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

    /**
     * 七牛云是否同步
     */
    public int sync;

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
            Logger.warn("Attach[%s] is Not found.", J.g(a));

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

    /**
     * 获取文件目录的网址
     *
     * @param sku
     * @return
     */
    public static String attachsLink(String sku) {
        return System.getenv(Constant.KOD_HOST) + "/index.php?explorer/pathList&path=SKU/" + sku;
    }

    /**
     * 读取图片的网址
     *
     * @param sku
     * @param name
     * @return
     */
    public static String attachImage(String sku, String name) {
        return System.getenv(Constant.KOD_HOST) + "/data/User/elcuk2/home/SKU/" + sku
                + "/" + name;
    }

    public static String attachImageSend(String sku, String name) {
        return System.getenv(Constant.KOD_HOST) + "/data/User/elcuk2/home/SKU/" + sku + "/" + name;
    }

    /**
     * 获取SKU下的图片列表
     *
     * @param fid
     * @return
     */
    public static List<java.util.Map<String, String>> attachImages(String fid) {
        List<java.util.Map<String, String>> imgs = new ArrayList<>();
        JSONObject jsonObject = HTTP.getJson(kodCookieStore(), attachsLink(fid));

        if(jsonObject != null) {
            JSONArray fileList = null;
            try {
                fileList = jsonObject.getJSONObject("data").getJSONArray("filelist");
            } catch(NullPointerException e) {
                Logger.error(e.getMessage());
            }
            if(fileList == null || fileList.isEmpty()) return imgs;

            for(Object object : fileList) {
                JSONObject entry = (JSONObject) object;
                String name = entry.getString("name");

                imgs.add(GTs.MapBuilder.map("name", name)
                        .put("href", Attach.attachImage(fid, name))
                        .build()
                );
            }
        }
        return imgs;
    }

    /**
     * KOD 系统所需要的 Cookie
     *
     * @return
     */
    public static BasicCookieStore kodCookieStore() {
        BasicCookieStore cookieStore = new BasicCookieStore();
        cookieStore.addCookies((BasicClientCookie[]) Arrays.asList(
                HTTP.buildCrossDomainCookie("kod_name", "elcuk2"),
                HTTP.buildCrossDomainCookie("kod_token", Webs.md5(User.userMd5("elcuk2"))),
                HTTP.buildCrossDomainCookie("kod_user_language", "zh_CN"),
                HTTP.buildCrossDomainCookie("kod_user_online_version", "check-at-1418867695")
        ).toArray());
        return cookieStore;
    }


    /**
     * 图片网址转为文件
     *
     * @param destUrl
     * @param imagename
     * @throws IOException
     */

    public static F.T2<String, BufferedInputStream> urlToFile(String destUrl, String imagename) {
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

            return new F.T2<>(imagename, bis);
        } catch(Exception e) {

        }
        return null;
    }

    //获得指定文件的byte数组
    public byte[] getBytes() {
        byte[] buffer = null;
        try {
            if(this.file == null) {
                this.file = new File(this.location);
            }
            FileInputStream fis = new FileInputStream(file);
            ByteArrayOutputStream bos = new ByteArrayOutputStream(1000);
            byte[] b = new byte[1000];
            int n;
            while((n = fis.read(b)) != -1) {
                bos.write(b, 0, n);
            }
            fis.close();
            bos.close();
            buffer = bos.toByteArray();
        } catch(IOException e) {
            e.printStackTrace();
        }
        return buffer;
    }

}












