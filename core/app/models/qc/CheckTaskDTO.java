package models.qc;

import java.io.Serializable;

/**
 * Created by IntelliJ IDEA.
 * User: mac
 * Date: 15-5-8
 * Time: 3:37 PM
 */
public class CheckTaskDTO implements Serializable {

    private static final long serialVersionUID = 654583576246678553L;

    /**
     * 箱数
     */
    public Integer boxNum;

    /**
     * 个数
     */
    public Integer num;

    /**
     * 单箱重量
     */
    public Double singleBoxWeight;

    /**
     * 长
     */
    public Double length;

    /**
     * 宽
     */
    public Double width;

    /**
     * 高
     */
    public Double height;

    /**
     * 计算 boxNum * num
     *
     * @return
     */
    public int qty() {
        if(this.boxNum != null && this.num != null) {
            return this.boxNum * this.num;
        }
        return 0;
    }
}
