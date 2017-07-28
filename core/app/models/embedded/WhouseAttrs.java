package models.embedded;

import javax.persistence.Embeddable;
import javax.persistence.Lob;
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
     * 产品描述
     */
    @Lob
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
     * 包装内描述
     */
    @Lob
    public String whDescription;

    /**
     * 尺寸(包材)
     */
    public String whDimensions;

    /**
     * 重量(包材)
     */
    public double whWeight;
}
