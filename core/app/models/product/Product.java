package models.product;

import com.alibaba.fastjson.JSON;
import com.google.gson.annotations.Expose;
import controllers.Login;
import helper.*;
import models.ElcukRecord;
import models.User;
import models.embedded.ERecordBuilder;
import models.embedded.WhouseAttrs;
import models.market.Listing;
import models.market.M;
import models.market.OrderItem;
import models.market.Selling;
import models.material.Material;
import models.procure.CooperItem;
import models.procure.Cooperator;
import models.procure.ProcureUnit;
import models.view.dto.ProductDTO;
import org.apache.commons.io.FileUtils;
import org.apache.commons.lang.StringUtils;
import org.apache.commons.lang.Validate;
import org.apache.commons.lang.math.RandomUtils;
import org.hibernate.annotations.DynamicUpdate;
import play.Logger;
import play.cache.Cache;
import play.data.validation.Required;
import play.data.validation.Validation;
import play.db.jpa.GenericModel;
import play.libs.F;
import play.utils.FastRuntimeException;
import query.ProductQuery;

import javax.persistence.*;
import java.io.File;
import java.util.*;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

/**
 * Created by IntelliJ IDEA.
 * User: wyattpan
 * Date: 1/6/12
 * Time: 10:55 AM
 */
@Entity
@DynamicUpdate
public class Product extends GenericModel implements ElcukRecord.Log {

    private static final long serialVersionUID = 6155284260375111124L;

    public static final Pattern Nub = Pattern.compile("[0-9]*");

    /**
     * 此产品所能够符合的上架的货架, 不能够集联删除, 删除 Product 是一个很严重的事情!
     * 需要检测 Product 相关的数据
     */
    @OneToMany(mappedBy = "product", cascade = {CascadeType.DETACH, CascadeType.MERGE, CascadeType.PERSIST,
            CascadeType.REFRESH}, fetch = FetchType.LAZY)
    public List<Listing> listings = new ArrayList<>();

    @OneToMany(mappedBy = "product", cascade = {CascadeType.DETACH, CascadeType.MERGE, CascadeType.PERSIST,
            CascadeType.REFRESH}, fetch = FetchType.LAZY)
    public List<Selling> sellings = new ArrayList<>();

    @ManyToOne
    public Category category;

    /**
     * 产品拥有哪些扩展属性
     */
    @OneToMany(mappedBy = "product", cascade = CascadeType.PERSIST)
    public List<ProductAttr> productAttrs = new ArrayList<>();

    @ManyToMany(mappedBy = "products", cascade = CascadeType.PERSIST)
    public List<Material> materials = new ArrayList<>();

    /**
     * 唯一的标示
     */
    @Id
    @Expose
    public String sku;

    @Lob
    @Expose
    public String productName;

    /**
     * 长度, 单位(包材) mm
     */
    @Expose
    public Float lengths;

    /**
     * 高度, 单位(包材) mm
     */
    @Expose
    public Float heigh;

    /**
     * 宽度, 单位(包材) mm
     */
    @Expose
    public Float width;

    /**
     * 重量, 单位(包材) kg
     */
    @Expose
    public Float weight;


    /**
     * 长度, 单位(产品) mm
     */
    @Expose
    public Float productLengths;

    /**
     * 高度, 单位(产品) mm
     */
    @Expose
    public Float productHeigh;

    /**
     * 宽度, 单位(产品) mm
     */
    @Expose
    public Float productWidth;

    /**
     * 重量, 单位(产品) kg
     */
    @Expose
    public Float productWeight;

    /**
     * 申报价格 (USD)
     */
    public Float declaredValue;

    /**
     * 产品品名
     */
    public String declareName;

    /**
     * 中文品名
     */
    public String chineseName;

    /**
     * 用途
     */
    public String useWay;

    /**
     * 产品简称
     */
    @Required
    public String abbreviation;

    /**
     * 上市时间
     */
    public Date marketTime;

    /**
     * 退市时间
     */
    public Date delistingTime;

    public enum T {

        /**
         * 未上架
         */
        NOMARKET {
            @Override
            public String label() {
                return "未上架";
            }
        },

        /**
         * 上架
         */
        MARKETING {
            @Override
            public String label() {
                return "上架";
            }
        },

        /**
         * 下架
         */
        DOWN {
            @Override
            public String label() {
                return "下架";
            }
        };

        public abstract String label();

        public static T val(String str) {
            if(StringUtils.isBlank(str)) return null;
            String s = str.toLowerCase();
            if(s.equals("未上架")) {
                return NOMARKET;
            } else if(s.equals("上架")) {
                return MARKETING;
            } else if(s.equals("下架")) {
                return DOWN;
            } else {
                return null;
            }
        }
    }

    /**
     * 上架状态(手动调整)
     */
    @Enumerated(EnumType.STRING)
    public T marketState;

    public enum P {
        /**
         * 未采购
         */
        NONE {
            @Override
            public String label() {
                return "未采购";
            }
        },

        /**
         * 正常采购
         */
        NORMAL {
            @Override
            public String label() {
                return "正常采购";
            }
        },

        /**
         * 停止采购
         */
        STOP {
            @Override
            public String label() {
                return "停止采购";
            }
        };

        public abstract String label();

        public static P val(String str) {
            if(StringUtils.isBlank(str)) return null;
            String s = str.toLowerCase();
            if(s.equals("未采购")) {
                return NONE;
            } else if(s.equals("正常采购")) {
                return NORMAL;
            } else if(s.equals("停止采购")) {
                return STOP;
            } else {
                return null;
            }
        }
    }

    /**
     * 采购状态
     */
    @Enumerated(EnumType.STRING)
    public P procureState;

    public enum L {
        /**
         * 开发期
         */
        DEVELOP {
            @Override
            public String label() {
                return "开发期";
            }
        },

        /**
         * 引进期
         */
        INTRODUCE {
            @Override
            public String label() {
                return "引进期";
            }
        },

        /**
         * 成长期
         */
        GROWTH {
            @Override
            public String label() {
                return "成长期";
            }
        },

        /**
         * 成熟期
         */
        MATURE {
            @Override
            public String label() {
                return "成熟期";
            }
        },

        /**
         * 衰退期
         */
        DOWNTURN {
            @Override
            public String label() {
                return "衰退期";
            }
        },

        /**
         * 退市
         */
        EXIT {
            @Override
            public String label() {
                return "退市";
            }
        };

        public abstract String label();

        public static L val(String str) {
            if(StringUtils.isBlank(str)) return null;
            String s = str.toLowerCase();
            if(s.equals("开发期")) {
                return DEVELOP;
            } else if(s.equals("引进期")) {
                return INTRODUCE;
            } else if(s.equals("成长期")) {
                return GROWTH;
            } else if(s.equals("成熟期")) {
                return MATURE;
            } else if(s.equals("衰退期")) {
                return DOWNTURN;
            } else if(s.equals("退市")) {
                return EXIT;
            } else {
                return null;
            }
        }
    }

    /**
     * 产品的生命周期(所处状态)
     */
    @Enumerated(EnumType.STRING)
    public L productState;

    public enum E {
        /**
         * 销量最好
         */
        A {
            public String label() {
                return "A";
            }
        },

        /**
         * 销量较好
         */
        B {
            public String label() {
                return "B";
            }
        },

        /**
         * 销量低迷
         */
        C {
            public String label() {
                return "C";
            }
        },

        /**
         * 销量较差
         */
        D {
            public String label() {
                return "D";
            }
        },

        /**
         * 销量极差
         */
        E {
            public String label() {
                return "E";
            }
        };

        public abstract String label();

        public static E val(String str) {
            if(StringUtils.isBlank(str)) return null;
            String s = str.toLowerCase();
            if(s.equals("A")) {
                return A;
            } else if(s.equals("B")) {
                return B;
            } else if(s.equals("C")) {
                return C;
            } else if(s.equals("D")) {
                return D;
            } else if(s.equals("E")) {
                return E;
            } else {
                return null;
            }
        }
    }

    /**
     * 销售等级(手动调整)
     */
    @Enumerated(EnumType.STRING)
    public E salesLevel;

    /**
     * 产品定位
     * Json格式类似为: [{"title":"aaa", "content": "bbb"}]
     */
    @Transient
    public List<ProductDTO> locate = new ArrayList<>();

    @Lob
    public String locates = "{}";

    /**
     * 产品卖点
     * Json格式类似为: [{"title":"aaa", "content": "bbb"}]
     */
    @Transient
    public List<ProductDTO> sellingPoint = new ArrayList<>();

    @Lob
    public String sellingPoints = "{}";

    /**
     * Product 在 ERP 系统内的状态
     */
    public enum S {
        /**
         * 刚创建
         */
        NEW,
        /**
         * 在系统内上架
         */
        SELLING,

        /**
         * 在系统内下架
         */
        DOWN
    }

    /**
     * Product 在系统内的状态
     */
    @Enumerated(EnumType.STRING)
    public S state = S.NEW;

    /**
     * 副标题
     */
    @Lob
    public String subtitle;

    @Required
    public String partNumber;

    @Required
    public String upc;

    public String partNumberJP;

    public String upcJP;

    public String b2bColor;

    public String b2bItemCode;

    public String b2bDescription;

    public String origin_sku;

    public String hs_code;

    public Date createDate = new Date();

    public Date updateDate = new Date();

    @OneToOne
    public User creator;

    @Enumerated(EnumType.STRING)
    @Column(length = 20)
    public User.COR origin_project;

    @Expose
    public WhouseAttrs whouseAttrs = new WhouseAttrs();

    /**
     * 供应商sku
     */
    public String supplierSku;

    /**
     * 下载fnsku时是否显示
     */
    public Boolean display;

    @Transient
    public int iscopy = 0;

    @Transient
    public String categoryId;

    public Product() {
    }

    public Product(String sku) {
        this.sku = sku.toUpperCase();
    }

    public void setSku(String sku) {
        this.sku = sku.toUpperCase();
    }

    public enum FLAG {
        ARRAY_TO_STR,
        STR_TO_ARRAY
    }

    /**
     * 删除 Product 前需要检查与 Product 有直接关系的各种对象.
     */
    @PreRemove
    public void checkDelete() {
        if(this.listings != null && this.listings.size() > 0) {
            throw new FastRuntimeException(
                    "Product [" + this.sku + "] have relate Listing, cannot be delete.");
        }
    }

    @PrePersist
    @PreUpdate
    public void prePersist() {
        if(StringUtils.isNotBlank(this.upc)) {
            this.upc = this.upc.trim();
        }
        if(StringUtils.isNotBlank(this.upcJP)) {
            this.upcJP = this.upcJP.trim();
        }
    }

    /**
     * 创建一个全新的 Product
     */
    public Product createProduct() {
        /*
         * 1. 检查 SKU 是否合法
         * 2. 检查废弃的 SKU
         * 4. 检查 SKU 前缀是否与 Family 一致
         * 5. Category 不能为空
         * 6. 产品的名称不能为空
         */
        if(Product.findById(this.sku) != null) {
            Validation.addError("", String.format("Product[%s]已经存在, 不允许重复创建!", sku));
            return null;
        }
        if(StringUtils.isBlank(this.sku))
            Validation.addError("", "SKU 必须存在");

        if(models.OperatorConfig.getVal("brandname").equals("EASYACC")) {
            if(!Product.validSKU(this.sku))
                Validation.addError("", "SKU[ " + this.sku + " ] 不合法!");
        }

        if(Product.unUsedSKU(this.sku))
            Validation.addError("", "SKU[ " + this.sku + " ] 为废弃 SKU, 不能使用!");
        if(this.category == null)
            Validation.addError("", "Category 不存在, 请创添加后再创建 Product!");
        if(this.category != null && !StringUtils.startsWith(this.sku, this.category.categoryId))
            Validation.addError("", "Category(" + this.category.categoryId + ") 与 SKU(" + this.sku + ") 不匹配!");
        this.checkUPCisRepeat();
        if(Validation.hasErrors()) return null;
        this.createDate = new Date();
        this.creator = Login.current();
        return this.save();
    }

    public void checkUPCisRepeat() {
        if(Product.find("upc = ? ", this.upc).fetch().size() > 0)
            Validation.addError("", "UPC已经存在，请重新填写！");
        if(Product.find("partNumber = ? ", this.partNumber).fetch().size() > 0)
            Validation.addError("", "PartNumber已经存在，请重新填写！");
        if(StringUtils.isNotEmpty(this.partNumberJP)
                && Product.find("partNumberJP = ? ", this.partNumberJP).fetch().size() > 0)
            Validation.addError("", "Part Number(JP)已经存在，请重新填写！");
        if(StringUtils.isNotEmpty(this.upcJP) && Product.find("upcJP = ? ", this.upcJP).fetch().size() > 0)
            Validation.addError("", "UPC(JP)已经存在，请重新填写！");
    }

    /**
     * 删除 Product 的时候需要做一些判断后, 才能够删除.
     */
    public void removeProduct() {
        checkDelete();
        this.delete();
    }

    /**
     * 如果指定 Market, 并且正确, 那么则从此 SKU 下属的 Listing 中过滤出对应市场的 Listing, 否则与 this.listings 相同
     *
     * @param market
     * @return
     */
    public List<Listing> listings(String market) {
        M m = M.val(market);
        if(m == null) {
            return this.listings;
        } else {
            return Listing.find("product.sku=? AND market=?", this.sku, M.val(market)).fetch();
        }
    }


    /**
     * 获取拥有这个 SKU 的所有供应商
     */
    public List<Cooperator> cooperators() {
        return Cooperator.find("SELECT c FROM Cooperator c INNER JOIN c.cooperItems ci "
                + " WHERE ci.sku=? AND ci.status=? ORDER BY ci.id", this.sku, CooperItem.S.Agree).fetch();
    }

    /**
     * 此产品拥有的图片的数量
     */
    public long pictureCount() {
        return Attach.count("fid=?", this.sku);
    }

    /**
     * 找出当前 SKU 在某个(所有)市场上的 Selling
     */
    public List<Selling> sellingCountWithMarket(M market) {
        if(market == null)
            return Selling.find("sellingId LIKE ?", this.sku + "%").fetch();
        else
            return Selling.find("market=? AND sellingId LIKE ?", market, this.sku + "%").fetch();
    }

    @Override
    public String toLog() {
        return String.format("[长:%s mm] [宽:%s mm] [高:%s mm] [重量:%s kg] [申报价格:$ %s] [产品名称:%s] [上架状态:%s] "
                        + "[采购状态:%s] [生命周期:%s] [销售等级:%s]",
                this.lengths, this.width, this.heigh, this.weight, this.declaredValue,
                this.productName, this.marketState.label(), this.procureState.label(), this.productState.label(),
                this.salesLevel);
    }


    public static Product dbProduct(String sku) {
        Product dbpro = new Product();
        dbpro.sku = sku;
        String sql = "select "
                + " lengths,width,heigh,weight,declaredvalue,productname,"
                + " marketstate,procurestate,productstate,saleslevel,productlengths,"
                + " productwidth,productheigh,productweight,declaredvalue,declarename,abbreviation,"
                + " locates,sellingpoints,subtitle,markettime,delistingtime,partNumber,whColor,whDimensions,whFormat,"
                + " whModel,whProductName,whQty,whSku,whWeight "
                + " from Product where sku='" + sku + "'";
        Map<String, Object> map = DBUtils.rows(sql).get(0);
        dbpro.lengths = (Float) map.get("lengths");
        dbpro.width = (Float) map.get("width");
        dbpro.heigh = (Float) map.get("heigh");
        dbpro.weight = (Float) map.get("weight");
        dbpro.declaredValue = (Float) map.get("declaredvalue");
        dbpro.productName = (String) map.get("productname");
        String marketstate = (String) map.get("marketstate");
        if(!StringUtils.isBlank(marketstate))
            dbpro.marketState = T.valueOf(marketstate);
        String procurestate = (String) map.get("procurestate");
        if(!StringUtils.isBlank(procurestate))
            dbpro.procureState = P.valueOf(procurestate);
        String productstate = (String) map.get("productstate");
        if(!StringUtils.isBlank(productstate))
            dbpro.productState = L.valueOf(productstate);
        String saleslevel = (String) map.get("saleslevel");
        if(!StringUtils.isBlank(saleslevel))
            dbpro.salesLevel = E.valueOf(saleslevel);
        dbpro.productLengths = (Float) map.get("productlengths");
        dbpro.productWidth = (Float) map.get("productwidth");
        dbpro.productHeigh = (Float) map.get("productheigh");
        dbpro.productWeight = (Float) map.get("productweight");
        dbpro.declaredValue = (Float) map.get("declaredvalue");
        dbpro.declareName = (String) map.get("declarename");
        dbpro.abbreviation = (String) map.get("abbreviation");
        dbpro.locates = (String) map.get("locates");
        dbpro.sellingPoints = (String) map.get("sellingpoints");
        dbpro.subtitle = (String) map.get("subtitle");
        dbpro.marketTime = (Date) map.get("markettime");
        dbpro.delistingTime = (Date) map.get("delistingtime");
        dbpro.partNumber = (String) map.get("partNumber");
        dbpro.whouseAttrs.whColor = (String) map.get("whColor");
        dbpro.whouseAttrs.whDimensions = (String) map.get("whDimensions");
        dbpro.whouseAttrs.whFormat = (String) map.get("whFormat");
        dbpro.whouseAttrs.whModel = (String) map.get("whModel");
        dbpro.whouseAttrs.whProductName = (String) map.get("whProductName");
        dbpro.whouseAttrs.whQty = (Integer) map.get("whQty");
        dbpro.whouseAttrs.whSku = (String) map.get("whSku");
        dbpro.whouseAttrs.whWeight = (Double) map.get("whWeight");
        return dbpro;
    }

    /**
     * 记录修改之前的记录
     */
    public List<String> beforeDoneUpdate(Product pro) {
        List<String> logs = new ArrayList<>();
        logs.addAll(Reflects.logFieldFade(this, "lengths", "长度(包材)", pro.lengths));
        logs.addAll(Reflects.logFieldFade(this, "width", "宽度(包材)", pro.width));
        logs.addAll(Reflects.logFieldFade(this, "heigh", "高度(包材)", pro.heigh));
        logs.addAll(Reflects.logFieldFade(this, "weight", "重量(包材)", pro.weight));
        logs.addAll(Reflects.logFieldFade(this, "declaredValue", "申报价格", pro.declaredValue));
        logs.addAll(Reflects.logFieldFade(this, "productName", "产品名称", pro.productName));
        logs.addAll(Reflects.logFieldFade(this, "marketState", "上架状态", pro.marketState));
        logs.addAll(Reflects.logFieldFade(this, "procureState", "采购状态", pro.procureState));
        logs.addAll(Reflects.logFieldFade(this, "productState", "生命周期", pro.productState));
        logs.addAll(Reflects.logFieldFade(this, "salesLevel", "销售等级", pro.salesLevel));
        logs.addAll(Reflects.logFieldFade(this, "productLengths", "长度", pro.productLengths));
        logs.addAll(Reflects.logFieldFade(this, "productWidth", "宽度", pro.productWidth));
        logs.addAll(Reflects.logFieldFade(this, "productHeigh", "高度", pro.productHeigh));
        logs.addAll(Reflects.logFieldFade(this, "productWeight", "重量", pro.productWeight));
        logs.addAll(Reflects.logFieldFade(this, "declareName", "产品品名", pro.declareName));
        logs.addAll(Reflects.logFieldFade(this, "abbreviation", "产品简称", pro.abbreviation));
        logs.addAll(Reflects.logFieldFade(this, "locates", "产品定位", pro.locates));
        logs.addAll(Reflects.logFieldFade(this, "sellingPoints", "产品卖点", pro.sellingPoints));
        logs.addAll(Reflects.logFieldFade(this, "subtitle", "副标题", pro.subtitle));
        logs.addAll(Reflects.logFieldFade(this, "marketTime", "上市时间", pro.marketTime));
        logs.addAll(Reflects.logFieldFade(this, "delistingTime", "退市时间", pro.delistingTime));
        logs.addAll(Reflects.logFieldFade(this, "supplierSku", "供应商SKU", pro.supplierSku));
        logs.addAll(Reflects.logFieldFade(this, "display", "是否显示", pro.display));

        //productAttrs
        this.productAttrs = new java.util.ArrayList<>();
        logs.addAll(Reflects.logFieldFade(this, "productAttrs", "扩展属性", pro.productAttrs));

        //whouseAttrs
        if(this.whouseAttrs != null) {
            logs.addAll(Reflects.logFieldFade(this, "whouseAttrs.whSku", "SKU(仓库)", pro.whouseAttrs.whSku));
            logs.addAll(Reflects.logFieldFade(this, "whouseAttrs.whProductName", "产品名称(仓库)",
                    StringUtils.trimToNull(pro.whouseAttrs.whProductName)));
            logs.addAll(Reflects.logFieldFade(this, "whouseAttrs.whModel", "产品型号(仓库)", pro.whouseAttrs.whModel));
            logs.addAll(Reflects.logFieldFade(this, "whouseAttrs.whFormat", "产品规格(仓库)", pro.whouseAttrs.whFormat));
            logs.addAll(Reflects.logFieldFade(this, "whouseAttrs.whColor", "产品颜色(仓库)", pro.whouseAttrs.whColor));
            logs.addAll(Reflects.logFieldFade(this, "whouseAttrs.whQty", "包装内产品数量(仓库)", pro.whouseAttrs.whQty));
            logs.addAll(Reflects.logFieldFade(this, "whouseAttrs.whDescription", "包装内描述(仓库)",
                    pro.whouseAttrs.whDescription));
            logs.addAll(Reflects.logFieldFade(this, "whouseAttrs.whDimensions", "产品尺寸(仓库)",
                    StringUtils.trimToNull(pro.whouseAttrs.whDimensions)));
            logs.addAll(Reflects.logFieldFade(this, "whouseAttrs.whWeight", "产品重量(仓库)", pro.whouseAttrs.whWeight));
        }

        return logs;
    }

    public static List<String> replace(List<String> log, String name, String toname) {
        for(int i = 0; i < log.size(); i++) {
            log.set(i, log.get(i).replace(name, toname));
        }
        return log;
    }

    /**
     * 验证这个 SKU 是否合法
     */
    public static boolean validSKU(String sku) {
        //71SNS1-B2PE, 71-SNS1-B2PE
        if(sku == null || sku.trim().isEmpty()) return false;
        String[] parts = sku.split("-");
        String part0 = parts[0].substring(0, 2);
        if(parts.length == 2) { //做一次兼容
            parts = new String[]{part0, parts[0].substring(2), parts[1]};
        }
        return parts.length == 3 && Product.Nub.matcher(part0).matches();
    }

    /**
     * 根据 Amazon 输入的 MerchantSKU 来查找 Product, 会自动将 MerchantSKU 转换成系统内使用的 SKU;
     * 这里面拥有两个兼容性判断.
     */
    public static Product findByMerchantSKU(String msku) {
        return Product.findById(merchantSKUtoSKU(msku));
    }

    /**
     * 将 Amazon 上的 MerchantSKU 转换成 SKU, 并且对 Amazon 上几个错误的 MerchantSKU 进行处理
     *
     * @param merchantSKU
     * @return
     */
    public static String merchantSKUtoSKU(String merchantSKU) {
        Validate.notNull(merchantSKU);
        // ------ fix -----------
//                if("609132508189".equals(t_msku)) t_msku = "71-HPTOUCH-B2PG"; //对历史错误数据的修复 @_@
//                if("8Z-0JR3-1BHG".equals(t_msku.toUpperCase())) t_msku = "80-QW1A56-BE"; // Power Bank 的销售还是需要囊括进来的
        String sku = StringUtils.split(merchantSKU, ",")[0].toUpperCase();
        if("609132508189".equals(sku)) sku = "71-HPTOUCH-B2PG";
        else if("8Z-0JR3-1BHG".equals(sku)) sku = "80-QW1A56-BE";
        return sku;
    }


    /**
     * 这几个为从 Amazon 解析回来的 SKU 存在, 但不需要在系统中再出现的 SKU, 为 Amazon 与系统中的同步做过滤
     */
    private static final Map<String, Integer> UN_USE_SKU = new HashMap<>();

    static {
        UN_USE_SKU.put("15HTCG14-MB2SP", 1);
        UN_USE_SKU.put("15HTCG14-MS2SP", 1);
        UN_USE_SKU.put("15SS5ACE-LUB2SP", 1);
        UN_USE_SKU.put("15SSI9100-LUB2SP", 1);
        UN_USE_SKU.put("4N-GGOQ-2H0M", 1);
        UN_USE_SKU.put("50-TPLED-2B21BG", 1);
        UN_USE_SKU.put("67-STRASSABS-80W29SG", 1);
        UN_USE_SKU.put("70-SMP1000-BTKBBG", 1);
        UN_USE_SKU.put("71-APSL13-BG", 1);
        UN_USE_SKU.put("71-APSL15-BG", 1);
        UN_USE_SKU.put("71SMGT101-BPU", 1);
        UN_USE_SKU.put("71-SAMGT101-BPU", 1);
    }

    /**
     * 判断是否属于不再用来与 Amazon 同步的 SKU
     *
     * @param merchantSKU Amazon 上的使用的 MerchantSKU
     * @return
     */
    public static boolean unUsedSKU(String merchantSKU) {
        if(StringUtils.isBlank(merchantSKU)) return false;
        return UN_USE_SKU.containsKey(Product.merchantSKUtoSKU(merchantSKU));
    }

    /**
     * 返回所有的 SKU
     *
     * @param forceClearCache 是否清除缓存
     * @return
     */
    @SuppressWarnings("unchecked")
    @Cached("lifetime")
    public static List<String> skus(boolean forceClearCache) {
        List<String> skus = null;
        if(forceClearCache) {
            skus = new ProductQuery().skus();
            Cache.delete(Caches.SKUS);
            Cache.add(Caches.SKUS, skus, "10h");
        } else {
            skus = Cache.get(Caches.SKUS, List.class);
            if(skus != null) return skus;
            skus = new ProductQuery().skus();
            Cache.add(Caches.SKUS, skus, "10h");
        }

        return skus;
    }

    /**
     * 将获取的sku集合 转换成JSON，便于页面展示
     *
     * @return
     */
    public static F.T2<List<String>, List<String>> fetchSkusJson() {
        List<String> skus = Product.skus(true);
        return new F.T2<>(skus, skus);
    }

    public static boolean exist(String sku) {
        return Product.count("sku=?", sku) > 0;
    }

    /**
     * 将产品定位属性转换成 String 存入DB
     * 或者将 String 转换成 List
     *
     * @param flag
     */
    public void arryParamSetUP(FLAG flag) {
        if(flag.equals(FLAG.ARRAY_TO_STR)) {
            /*
             * 在转换成Json字符串之前需要对空字符串做一点处理
             */
            this.locates = J.json(this.fixNullStr(this.locate));
            this.sellingPoints = J.json(this.fixNullStr(this.sellingPoint));
        } else {
            if(StringUtils.isNotBlank(this.locates)) {
                this.locate = JSON.parseArray(this.locates, ProductDTO.class);
                if(this.locate.size() == 0)
                    this.locate.add(new ProductDTO());
            }
            if(StringUtils.isNotBlank(this.sellingPoints)) {
                this.sellingPoint = JSON.parseArray(this.sellingPoints, ProductDTO.class);
                if(this.sellingPoint.size() == 0)
                    this.sellingPoint.add(new ProductDTO());
            }
        }
    }

    /**
     * 对空字符进行处理
     *
     * @return
     */
    private List<ProductDTO> fixNullStr(List<ProductDTO> target) {
        Iterator<ProductDTO> iterator = target.iterator();
        while(iterator.hasNext()) {
            ProductDTO p = iterator.next();
            if(null == p) {
                iterator.remove();
            }
        }
        return target;
    }

    /**
     * 准备数据
     */

    public void beforeData() {
        for(int i = 0; i <= 4; i++) {
            this.locate.add(new ProductDTO());
            this.sellingPoint.add(new ProductDTO());
        }
    }

    /**
     * 修改 Product 在系统内的状态
     */
    public static void changeProductType(String merchantSKU) {
        //当某一个 SKU 下所有的 Selling 都下架了则这个 SKU 状态改为"DOWN" ,反之则这个 SKU 状态则为“SELLING"
        Product product = Product.findByMerchantSKU(merchantSKU);
        long count = Selling.count("state IN ('SELLING', 'NEW') AND sellingId LIKE ?", product.sku + "%");
        if(count > 0) {
            product.state = S.SELLING;
        } else {
            product.state = S.DOWN;
        }
        product.save();
    }

    /**
     * Product 删除时的限制条件
     */
    public void safeDelete(String reason) {
        if(StringUtils.isBlank(reason))
            Validation.addError("", "必须填写原因才可以删除");

        if(this.listings.size() > 0)
            Validation.addError("", String.format("Product[%s]下拥有 %s 个 相关Listing，无法删除", this.sku, this.listings.size()));
        long orderItemCount = OrderItem.count("product_sku = ?", this.sku);
        if(orderItemCount > 0)
            Validation.addError("", String.format("Product[%s]下找到 %s 个相关订单项，无法删除", this.sku, orderItemCount));
        long procureUnitCount = ProcureUnit.count("sku=?", this.sku);
        if(procureUnitCount > 0)
            Validation.addError("", String.format("Product[%s]下找到 %s 个相关采购计划，无法删除", this.sku, procureUnitCount));
        long prodAttrCount = this.productAttrs.size();
        if(prodAttrCount > 0)
            Validation.addError("", String.format("Product[%s]下找到 %s 个附加属性，无法删除，请先删除附加属性", this.sku, prodAttrCount));
        if(Validation.hasErrors()) return;
        this.delete();
        new ERecordBuilder("product.destroy")
                .msgArgs(reason, this.sku)
                .fid("product.destroy")
                .save();
    }


    /**
     * 同步另一个SKU的信息，但不保存
     *
     * @param choseid
     * @param skuid
     * @param base
     * @param extend
     * @param attach
     * @return
     */
    public static Product copyProduct(String choseid, String skuid, String base, String extend, String attach) {
        Product pro = Product.findByMerchantSKU(choseid);
        pro.arryParamSetUP(Product.FLAG.STR_TO_ARRAY);
        Product copysku = Product.findByMerchantSKU(skuid);
        copysku.arryParamSetUP(Product.FLAG.STR_TO_ARRAY);
        //基本属性
        if(StringUtils.isNotBlank(base) && base.equals("1")) {
            pro.lengths = copysku.lengths;
            pro.productName = copysku.productName;
            pro.heigh = copysku.heigh;
            pro.width = copysku.width;
            pro.weight = copysku.weight;
            pro.productLengths = copysku.productLengths;
            pro.productHeigh = copysku.productHeigh;
            pro.productWidth = copysku.productWidth;
            pro.productWeight = copysku.productWeight;
            pro.procureState = copysku.procureState;
            pro.declaredValue = copysku.declaredValue;
            pro.declareName = copysku.declareName;
            pro.salesLevel = copysku.salesLevel;
            pro.abbreviation = copysku.abbreviation;
            pro.marketTime = copysku.marketTime;
            pro.locates = copysku.locates;
            pro.subtitle = copysku.subtitle;
            pro.locates = copysku.locates;
            pro.sellingPoints = copysku.sellingPoints;

            pro.iscopy = 2;
        }
        //扩展信息
        if(StringUtils.isNotBlank(extend) && extend.equals("1")) {
            List<ProductAttr> proattrs = copysku.productAttrs;
            List<ProductAttr> attrs = new ArrayList<>();
            for(ProductAttr p : proattrs) {
                ProductAttr np = new ProductAttr();
                np.product = pro;
                np.attribute = p.attribute;
                np.value = p.value;
                attrs.add(np);
            }
            pro.productAttrs = attrs;
        }
        //附件
        if(StringUtils.isNotBlank(attach) && attach.equals("1")) {
            List<Attach> attaches = Attach.attaches(copysku.sku, null);
            List<Attach> skuattaches = Attach.attaches(pro.sku, null);

            for(Attach att : attaches) {
                Attach skuatt = new Attach();
                skuatt.fid = pro.sku;
                skuatt.outName = att.outName;
                skuatt.p = att.p;
                skuatt.originName = "C_" + att.originName;
                skuatt.remove = att.remove;
                skuatt.attachType = att.attachType;

                //判断是否存在此附件
                boolean isexists = false;
                for(Attach existsatt : skuattaches) {
                    if(existsatt.originName.equals(skuatt.originName)) {
                        isexists = true;
                    }
                }
                if(!isexists) {
                    try {
                        skuatt.file = new File(att.location);
                        long subfix = RandomUtils.nextInt();
                        skuatt.fileSize = skuatt.file.length();
                        skuatt.fileName = String.format("%s_%s%s", skuatt.fid, subfix,
                                skuatt.file.getPath().substring(skuatt.file.getPath().lastIndexOf("."))).trim();
                        skuatt.location = skuatt.location();
                        skuatt.createDate = new Date();

                        FileUtils.copyFile(skuatt.file, new File(skuatt.location));
                        skuatt.save();
                    } catch(Exception e) {
                        Logger.error(Webs.s(e));
                    }
                }
            }
        }
        pro.arryParamSetUP(Product.FLAG.STR_TO_ARRAY);
        return pro;

    }


    /**
     * 复制另一个SKU的信息，将会保存
     *
     * @param skuid
     * @param extend
     * @param attach
     * @return
     */
    public static Product backupProduct(String skuid, String extend, String attach, String sku) {
        Product pro = Product.findByMerchantSKU(skuid);
        pro.arryParamSetUP(Product.FLAG.STR_TO_ARRAY);
        Product backupsku = new Product();
        Product validpro = Product.findByMerchantSKU(sku);
        if(validpro != null) {
            Validation.addError("", String.format("已经存在SKU%s!", sku));
            return backupsku;
        }
        if(pro.category != null && !StringUtils.startsWith(sku, pro.category.categoryId)) {
            Validation.addError("", "Category(" + pro.category.categoryId + ") 与 SKU(" + sku + ") 不匹配!");
            return backupsku;
        }
        backupsku.sku = sku;
        backupsku.category = pro.category;
        backupsku.lengths = pro.lengths;
        backupsku.productName = pro.productName;
        backupsku.heigh = pro.heigh;
        backupsku.width = pro.width;
        backupsku.weight = pro.weight;
        backupsku.productLengths = pro.productLengths;
        backupsku.productHeigh = pro.productHeigh;
        backupsku.productWidth = pro.productWidth;
        backupsku.productWeight = pro.productWeight;
        backupsku.procureState = pro.procureState;
        backupsku.declaredValue = pro.declaredValue;
        backupsku.declareName = pro.declareName;
        backupsku.salesLevel = pro.salesLevel;
        backupsku.abbreviation = pro.abbreviation;
        backupsku.marketTime = pro.marketTime;
        backupsku.locates = pro.locates;
        backupsku.subtitle = pro.subtitle;
        backupsku.locates = pro.locates;
        backupsku.sellingPoints = pro.sellingPoints;
        backupsku.arryParamSetUP(Product.FLAG.STR_TO_ARRAY);

        backupsku.arryParamSetUP(Product.FLAG.ARRAY_TO_STR);

        if(backupsku.declaredValue == null)
            backupsku.declaredValue = 0f;
        if(backupsku.declareName == null)
            backupsku.declareName = "";
        if(backupsku.abbreviation == null)
            backupsku.abbreviation = "";
        backupsku.createProduct();


        //扩展信息
        if(StringUtils.isNotBlank(extend) && extend.equals("1")) {
            List<ProductAttr> proattrs = pro.productAttrs;
            List<ProductAttr> attrs = new ArrayList<>();
            for(ProductAttr p : proattrs) {
                ProductAttr np = new ProductAttr();
                np.product = backupsku;
                np.attribute = p.attribute;
                np.value = p.value;
                attrs.add(np);
            }
            backupsku.productAttrs = attrs;
            if(!Validation.hasErrors())
                backupsku.save();
        }


        //附件
        if(StringUtils.isNotBlank(attach) && attach.equals("1")) {
            List<Attach> attaches = Attach.attaches(pro.sku, null);
            List<Attach> skuattaches = Attach.attaches(backupsku.sku, null);

            for(Attach att : attaches) {
                Attach skuatt = new Attach();
                skuatt.fid = backupsku.sku;
                skuatt.outName = att.outName;
                skuatt.p = att.p;
                skuatt.originName = "B_" + att.originName;
                skuatt.remove = att.remove;
                skuatt.attachType = att.attachType;

                //判断是否存在此附件
                boolean isexists = false;
                for(Attach existsatt : skuattaches) {
                    if(existsatt.originName.equals(skuatt.originName)) {
                        isexists = true;
                    }
                }
                if(!isexists) {
                    try {
                        skuatt.file = new File(att.location);
                        long subfix = RandomUtils.nextInt();
                        skuatt.fileSize = skuatt.file.length();
                        skuatt.fileName = String.format("%s_%s%s", skuatt.fid, subfix,
                                skuatt.file.getPath().substring(skuatt.file.getPath().lastIndexOf("."))).trim();
                        skuatt.location = skuatt.location();
                        skuatt.createDate = new Date();

                        FileUtils.copyFile(skuatt.file, new File(skuatt.location));
                        if(!Validation.hasErrors())
                            skuatt.save();
                    } catch(Exception e) {
                        Logger.error(Webs.s(e));
                    }
                }
            }
        }
        backupsku.arryParamSetUP(Product.FLAG.STR_TO_ARRAY);
        return backupsku;
    }

    /**
     * 最近的采购单
     *
     * @return
     */
    public ProcureUnit recentlyUnit() {
        ProcureUnit procureUnit = ProcureUnit.find("sku=? and stage!=? ORDER BY createDate desc", this.sku,
                ProcureUnit.STAGE.PLAN).first();
        if(procureUnit == null) return new ProcureUnit();
        return procureUnit;
    }

    public void changePartNumber(String oldNumber) {
        if(StringUtils.isNotBlank(this.partNumber) && !this.partNumber.equals(oldNumber)) {
            for(Selling selling : this.sellings) {
                selling.aps.manufacturerPartNumber = this.partNumber;
                selling.save();
            }
        }
    }

    /**
     * 输出给 typeahead 所使用的 source
     *
     * @return
     */
    public static List<String> pickSourceItems(String search) {
        String sql = "SELECT p.sku, p.family_family, s.fnSku, pa.value FROM Product p"
                + " LEFT JOIN ProductAttr pa ON p.sku=pa.product_sku"
                + " LEFT JOIN Listing l ON p.sku=l.product_sku"
                + " LEFT JOIN Selling s ON l.listingId=s.listing_listingId"
                + " WHERE p.sku LIKE ?"
                + " OR s.fnSku LIKE ?"
                + " OR pa.value LIKE ?"
                + " LIMIT 5";
        String word = String.format("%%%s%%", StringUtils.replace(search.trim(), "'", "''"));
        List<Map<String, Object>> rows = DBUtils.rows(sql, Arrays.asList(word, word, word).toArray());
        return rows.stream()
                .filter(row -> row != null && !row.isEmpty())
                .flatMap(row -> row.values().stream())
                .filter(Objects::nonNull)
                .distinct()
                .limit(10)
                .map(val -> StringUtils.abbreviate(val.toString(), 20))
                .collect(Collectors.toList());
    }

    public String dimensions() {
        return String.format("%s*%s*%s", this.lengths, this.width, this.heigh);
    }

    public float weightWithGram() {
        return this.weight != null ? this.weight * 1000 : 0;
    }

    public static Product findSkuForB2b(String sku) {
        List<Product> products = Product.find("origin_sku=?", sku).fetch();
        if(products.size() == 0)
            return null;
        else
            return products.get(0);
    }

    public String showImg() {
        Attach attach = Attach.find(" fid=? and originName=?", this.sku, "0.jpg").first();
        if(attach == null)
            return null;
        else
            return attach.qiniuLocation;
    }

    public double getRecentlyWeight() {
        List<ProcureUnit> units = ProcureUnit.find("product.sku =? AND mainBoxInfo IS NOT NULL "
                + " ORDER BY createDate DESC", this.sku).fetch();
        if(units.size() == 0) {
            return this.weight == null ? 0 : this.weight;
        } else {
            units = units.stream().filter(unit -> unit.mainBox.singleBoxWeight > 0 && unit.mainBox.num > 0)
                    .collect(Collectors.toList());
            ProcureUnit unit = units.get(0);
            if(unit.mainBox.singleBoxWeight / unit.mainBox.num == 0) {
                return this.weight == null ? 0 : this.weight;
            }
            return unit.mainBox.singleBoxWeight / unit.mainBox.num;
        }
    }
}

