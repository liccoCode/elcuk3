package models.product;

import com.alibaba.fastjson.JSON;
import com.google.gson.annotations.Expose;
import helper.Cached;
import helper.Caches;
import helper.J;
import helper.Webs;
import models.ElcukRecord;
import models.market.Listing;
import models.market.M;
import models.market.Selling;
import models.procure.Cooperator;
import models.view.dto.ProductDTO;
import org.apache.commons.lang.StringUtils;
import org.apache.commons.lang.Validate;
import play.cache.Cache;
import play.data.validation.Min;
import play.data.validation.Required;
import play.data.validation.Validation;
import play.db.jpa.GenericModel;
import play.libs.F;
import play.utils.FastRuntimeException;
import query.ProductQuery;

import javax.persistence.*;
import java.util.*;
import java.util.regex.Pattern;

/**
 * Created by IntelliJ IDEA.
 * User: wyattpan
 * Date: 1/6/12
 * Time: 10:55 AM
 */
@Entity
@org.hibernate.annotations.Entity(dynamicUpdate = true)
public class Product extends GenericModel implements ElcukRecord.Log {
    public static final Pattern Nub = Pattern.compile("[0-9]*");
    /**
     * 此产品所能够符合的上架的货架, 不能够集联删除, 删除 Product 是一个很严重的事情!
     * 需要检测 Product 相关的数据
     */
    @OneToMany(mappedBy = "product",
            cascade = {CascadeType.DETACH, CascadeType.MERGE, CascadeType.PERSIST,
                    CascadeType.REFRESH}, fetch = FetchType.LAZY)
    public List<Listing> listings = new ArrayList<Listing>();

    @ManyToOne
    public Category category;

    @ManyToOne
    public Family family;


    /**
     * 唯一的标示
     */
    @Id
    @Expose
    public String sku;

    @Required
    @Lob
    @Expose
    public String productName;

    /**
     * 长度, 单位 mm
     */
    @Expose
    @Required
    // 因为默认是 >=
    @Min(0.001)
    public Float lengths = 0f;

    /**
     * 高度, 单位 mm
     */
    @Expose
    @Required
    @Min(0.001)
    public Float heigh = 0f;

    /**
     * 宽度, 单位 mm
     */
    @Expose
    @Required
    @Min(0.001)
    public Float width = 0f;

    /**
     * 重量, 单位 kg
     */
    @Expose
    @Required
    @Min(0.001)
    public Float weight = 0f;

    /**
     * 申报价格 (USD)
     */
    public Float declaredValue = 0f;

    /**
     * 产品品名
     */
    @Required
    public String declareName;

    /**
     * 产品简称
     */
    @Required
    public String abbreviation;

    /**
     * 上市时间
     */
    public Date marketTime;

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
        A,

        /**
         * 销量较好
         */
        B,

        /**
         * 销量低迷
         */
        C,

        /**
         * 销量较差
         */
        D,

        /**
         * 销量极差
         */
        E
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
    public List<ProductDTO> locate = new ArrayList<ProductDTO>();

    @Lob
    public String locates = "{}";

    /**
     * 产品卖点
     * Json格式类似为: [{"title":"aaa", "content": "bbb"}]
     */
    @Transient
    public List<ProductDTO> sellingPoint = new ArrayList<ProductDTO>();

    @Lob
    public String sellingPoints = "{}";

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

    /**
     * 创建一个全新的 Product
     */
    public void createProduct() {
        /**
         * 1. 检查 SKU 是否合法
         * 2. 检查废弃的 SKU
         * 3. Family 不能为空!
         * 4. 检查 SKU 前缀是否与 Family 一致
         * 5. Category 不能为空
         * 6. 产品的名称不能为空
         * 7. 长宽高一定需要填写
         * 8. 申报价不为空
         */
        if(StringUtils.isBlank(this.sku)) Webs.error("SKU 必须存在");

        if(!Product.validSKU(this.sku))
            Validation.addError("", "SKU[ " + this.sku + " ] 不合法!");
        if(Product.unUsedSKU(this.sku))
            Validation.addError("", "SKU[ " + this.sku + " ] 为废弃 SKU, 不能使用!");
        if(this.family == null)
            Validation.addError("", "Family 不存在,请先添加后再创建 Product!");
        if(this.family != null && !StringUtils.startsWith(this.sku, this.family.family))
            Validation.addError("",
                    "Family(" + this.family.family + ") 与 SKU(" + this.sku + ") 不匹配!");
        if(this.declaredValue == null)
            Validation.addError("", "申报价值必须填写");
        if(this.declareName == null)
            Validation.addError("", "产品品名必须填写");
        if(this.abbreviation == null)
            Validation.addError("", "产品简称必须填写");


        if(Validation.hasErrors()) return;

        this.category = this.family.category;
        if(this.category == null)
            Validation.addError("", "Category 不存在, 请创添加后再创建 Product!");
        if(Validation.hasErrors()) return;

        this.save();
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
        return Cooperator
                .find("SELECT c FROM Cooperator c, IN(c.cooperItems) ci WHERE ci.sku=? ORDER BY ci.id", this.sku)
                .fetch();
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
    public String to_log() {
        return String.format("[长:%s mm] [宽:%s mm] [高:%s mm] [重量:%s kg] [申报价格:$ %s] [产品名称:%s] [上架状态:%s] " +
                "[采购状态:%s] [生命周期:%s] [销售等级:%s]",
                this.lengths, this.width, this.heigh, this.weight, this.declaredValue,
                this.productName, this.marketState.label(), this.procureState.label(), this.productState.label(),
                this.salesLevel);
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
        if(parts.length != 3) return false;
        if(!Product.Nub.matcher(part0).matches()) return false;
        return true;
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
    private static final Map<String, Integer> UN_USE_SKU = new HashMap<String, Integer>();

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
        return new F.T2<List<String>, List<String>>(skus, skus);
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
            this.locates = J.json(this.locate);
            this.sellingPoints = J.json(this.sellingPoint);
        } else {
            if(StringUtils.isNotBlank(this.locates)) this.locate = JSON.parseArray(this.locates, ProductDTO.class);
            if(StringUtils.isNotBlank(this.sellingPoints)) this.sellingPoint = JSON.parseArray(this.sellingPoints,
                    ProductDTO.class);
        }
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
}
