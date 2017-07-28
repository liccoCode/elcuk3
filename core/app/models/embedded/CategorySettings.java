package models.embedded;

import models.market.M;
import org.apache.commons.lang.StringUtils;

import javax.persistence.Column;
import javax.persistence.Embeddable;
import java.io.Serializable;
import java.util.HashMap;
import java.util.Map;

/**
 * 附属再 Category 身上的单独提取出来的字段(存储再 Category 表中)
 * User: wyattpan
 * Date: 6/6/12
 * Time: 10:02 AM
 */
@Embeddable
public class CategorySettings implements Serializable {
    private static final long serialVersionUID = -5252562469109859931L;
    /**
     * Amazon 允许的节点, 使用 , 分割
     */
    @Column(columnDefinition = "varchar(255) DEFAULT ''")
    public String amazonNode;

    /**
     * Amazon US 上架时候选择 CLASSIFY 的值
     */
    @Column(columnDefinition = "varchar(100) DEFAULT ''")
    public String amazonCategory;
    /**
     * Amazon US 上架时候选择 CLASSIFY 的值
     */
    @Column(columnDefinition = "varchar(100) DEFAULT ''")
    public String amazonUKCategory;
    /**
     * Amazon US 上架时候选择 CLASSIFY 的值
     */
    @Column(columnDefinition = "varchar(100) DEFAULT ''")
    public String amazonDECategory;

    @Column(columnDefinition = "varchar(100) DEFAULT ''")
    public String amazonFRCategory;

    @Column(columnDefinition = "varchar(100) DEFAULT ''")
    public String amazonITCategory;

    @Column(columnDefinition = "varchar(100) DEFAULT ''")
    public String amazonESCategory;

    @Column(columnDefinition = "varchar(100) DEFAULT ''")
    public String amazonJPCategory;

    public Map<String, String> amazonNodeMap() {
        String[] nodes = StringUtils.split(amazonNode, ",");
        Map<String, String> nodeMap = new HashMap<>();
        for(String node : nodes) {
            nodeMap.put(node, node);
        }
        return nodeMap;
    }

    /**
     * 选择 Amazon 上架的时候 Cateogry 的值
     *
     * @param market
     * @return
     */
    public String choseAmazonCategory(M market) {
        if(market == M.AMAZON_UK) {
            return amazonUKCategory;
        } else if(market == M.AMAZON_DE) {
            return amazonDECategory;
        } else if(market == M.AMAZON_US) {
            return amazonCategory;
        } else if(market == M.AMAZON_FR) {
            return amazonFRCategory;
        } else if(market == M.AMAZON_ES) {
            return amazonESCategory;
        } else if(market == M.AMAZON_IT) {
            return amazonITCategory;
        } else if(market == M.AMAZON_JP) {
            return amazonJPCategory;
        } else {
            return "";
        }
    }
}
