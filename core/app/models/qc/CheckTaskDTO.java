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
    public int boxNum;

    /**
     * 个数
     */
    public int num;

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

    /**
     * 计算 boxNum * num
     *
     * @return
     */
    public int qty() {
        return this.boxNum * this.num;
    }
}
