package models.qc;

import java.io.Serializable;

/**
 * Created by mac on 15-5-8.
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

    public CheckTaskDTO(){

    }
}
