package models.embedded;

import com.google.gson.annotations.Expose;
import play.data.validation.Required;

import javax.persistence.Embeddable;
import javax.persistence.Lob;
import java.util.Date;

/**
 * 整合的 Amazon 上架使用的字段, 需要添加修改都在这个类中(Component 类)
 * User: wyattpan
 * Date: 5/16/12
 * Time: 3:54 PM
 */
@Embeddable
public class AmazonProps {
    public String modelNumber;
    public String manufacturer;
    /**
     * 使用  Webs.SPLIT 进行分割, 最多 5 行
     */
    public String keyFetures;
    /**
     * Recommended Browse Nodes;
     * 使用 [,] 进行分割, 一般为 2 个
     */
    public String RBN;
    /**
     * For most products, this will be identical to the model number;
     * however, some manufacturers distinguish part number from model number
     */
    public String manufacturerPartNumber;
    /**
     * 如果这个 Condition 不为空, 那么则覆盖掉 Listing 中的 Condition
     */
    public String condition_;
    @Required
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
    public String legalDisclaimerDesc;
    public Date launchDate;
    @Lob
    public String sellerWarrantyDesc;

    /**
     * 核心的产品描述
     */
    @Lob
    public String productDesc;

    /**
     * 使用 Webs.SPLIT 进行分割, 5 行
     */
    @Lob
    public String searchTerms;

    /**
     * 使用 Webs.SPLIT 进行分割, 5 行
     */
    @Lob
    public String platinumKeywords;

    @Expose
    public String upc;
}
