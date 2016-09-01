package models.qc;

import java.io.Serializable;

/**
 * Created by IntelliJ IDEA.
 * User: mac
 * Date: 15-5-8
 * Time: 8:43 PM
 */
public class CheckTaskDTO implements Serializable {

    private static final long serialVersionUID = 654583576246678553L;

    /**
     * 箱数
     */
    public int boxNum;

    /**
     * 个数
     */
    public int num;

    /**
     * 尾箱箱内的产品个数(可选)
     * <p>
     * PS: 如果尾箱的数据与主箱完全一致, 差别仅在于数量则可以使用该属性来传递数据
     */
    public Integer lastCartonNum;

    /**
     * 单箱重量
     */
    public double singleBoxWeight;

    /**
     * 长
     */
    public double length;

    /**
     * 宽
     */
    public double width;

    /**
     * 高
     */
    public double height;

    public CheckTaskDTO() {

    }
}
