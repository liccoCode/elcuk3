package models.embedded;

import javax.persistence.Embeddable;
import java.io.Serializable;

/**
 * 仓库定义的内部数据
 * 备注: wh = Warehousing
 * Created by IntelliJ IDEA.
 * User: kenyon
 * Date: 11/27/16
 * Time: 12:38 PM
 */
@Embeddable
public class WhouseAttrs implements Serializable {
    /**
     * SKU
     */
    public String whSku;

    /**
     * 产品名称
     */
    public String whProductName;

    /**
     * 产品型号
     */
    public String whModel;

    /**
     * 产品规格
     */
    public String whFormat;

    /**
     * 产品颜色
     */
    public String whColor;

    /**
     * 包装内产品数量
     */
    public int whQty;

    /**
     * 产品尺寸
     */
    public String whDimensions;

    /**
     * 产品重量
     */
    public double whWeight;
}
