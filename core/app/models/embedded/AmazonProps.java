package models.embedded;

import com.google.gson.annotations.Expose;
import helper.Webs;
import org.apache.commons.lang.StringUtils;
import play.data.validation.Required;

import javax.persistence.Embeddable;
import javax.persistence.Lob;
import javax.persistence.Transient;
import java.util.Date;

/**
 * 整合的 Amazon 上架使用的字段, 需要添加修改都在这个类中(Component 类)
 * User: wyattpan
 * Date: 5/16/12
 * Time: 3:54 PM
 */
@Embeddable
public class AmazonProps {
    @Lob
    @Required
    @Expose
    public String title;
    @Expose
    public String modelNumber;
    @Expose
    public String manufacturer;
    /**
     * 使用  Webs.SPLIT 进行分割, 最多 5 行
     */
    @Expose
    public String keyFetures;
    @Transient
    public String[] keyFeturess;
    /**
     * Recommended Browse Nodes;
     * 使用 [,] 进行分割, 一般为 2 个
     */
    @Expose
    public String RBN;

    @Transient
    public String[] rbns;
    /**
     * For most products, this will be identical to the model number;
     * however, some manufacturers distinguish part number from model number
     */
    @Expose
    public String manufacturerPartNumber;

    /**
     * Amazon 上表示打包这个产品的数量
     */
    @Expose
    public Integer quantity;
    /**
     * 如果这个 Condition 不为空, 那么则覆盖掉 Listing 中的 Condition
     */
    @Expose
    public String condition_;

    @Required
    @Expose
    public Float standerPrice;

    @Expose
    public Float salePrice;
    /**
     * 促销产品价格的开始日期
     */
    @Expose
    public Date startDate;
    /**
     * 促销产品价格的结束日期
     */
    @Expose
    public Date endDate;

    /**
     * Does your item have a legal disclaimer associated with it?
     */
    @Lob
    @Expose
    public String legalDisclaimerDesc;
    @Expose
    public Date launchDate;
    @Lob
    @Expose
    public String sellerWarrantyDesc;

    /**
     * 核心的产品描述
     */
    @Lob
    @Expose
    public String productDesc;

    /**
     * 使用 Webs.SPLIT 进行分割, 5 行
     */
    @Lob
    @Expose
    public String searchTerms;

    @Transient
    public String[] searchTermss;

    /**
     * 使用 Webs.SPLIT 进行分割, 5 行
     */
    @Lob
    @Expose
    public String platinumKeywords;

    @Transient
    public String[] platinumKeywordss;

    @Expose
    public String upc;

    /**
     * 此 Selling 所对应的图片名字与顺序. 使用 Webs.SPLIT 进行分割
     */
    @Expose
    public String imageName;

    /**
     * <pre>
     * 将:
     * - keyFetures
     * - searchTerms
     * - RBN
     * 三个数组形的值进行初始化
     * ps: 在从 [] <- str 的时候, 会保持 keyFetures[5], searchTerms[5], rbn[2] 的数组长度
     * </pre>
     *
     * @param flag 如果 flag &gt; 0 表示从 [] -> str; 如果 flag &lt;=0 表示从 [] <- str
     */
    public void arryParamSetUP(int flag) {
        if(flag > 0) {
            this.keyFetures = StringUtils.join(this.keyFeturess, Webs.SPLIT);
            this.searchTerms = StringUtils.join(this.searchTermss, Webs.SPLIT);
            this.RBN = StringUtils.join(this.rbns, ",");
        } else {
            this.keyFeturess = new String[5];
            this.searchTermss = new String[5];
            this.rbns = new String[2];

            String[] tmp = StringUtils.splitByWholeSeparator(this.keyFetures, Webs.SPLIT);
            if(tmp != null) System.arraycopy(tmp, 0, this.keyFeturess, 0, tmp.length);

            tmp = StringUtils.splitByWholeSeparator(this.searchTerms, Webs.SPLIT);
            if(tmp != null) System.arraycopy(tmp, 0, this.searchTermss, 0, tmp.length);

            tmp = StringUtils.split(this.RBN, ",");
            if(tmp != null) System.arraycopy(tmp, 0, this.rbns, 0, tmp.length);
        }
    }
}
