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
    public String boxNum;

    /**
     * 个数
     */
    public String num;

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

    public CheckTaskDTO(){

    }
}
