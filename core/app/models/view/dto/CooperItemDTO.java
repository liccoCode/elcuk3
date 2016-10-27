package models.view.dto;

import play.data.validation.Min;

/**
 * Created by licco on 2016/10/25.
 */
public class CooperItemDTO {

    /**
     * 一箱的数量
     */
    @Min(0)
    public Integer boxSize;

    /**
     * 单箱重量
     */
    public double singleBoxWeight;

    /**
     * 单箱长
     */
    public double length;

    /**
     * 单箱宽
     */
    public double width;

    /**
     * 单箱高
     */
    public double height;

    /**
     * 方案名
     */
    public String schemeName;

    /**
     * 主箱箱数
     *
     * @param shipedQty
     * @return
     */
    public int boxNum(int shipedQty) {
        if(this.boxSize == null) return 0;
        float boxNum = shipedQty / (float) this.boxSize;
        if(boxNum < 1) {
            return 1;
        } else {
            return (int) Math.floor(boxNum);
        }
    }

    /**
     * 尾箱内的产品数量
     *
     * @return
     */
    public int lastCartonNum(int shipedQty) {
        if(this.boxSize == null) return 0;
        int boxNum = this.boxNum(shipedQty);
        int lastCartonNum = shipedQty - boxNum * this.boxSize;
        if(lastCartonNum <= 0) {
            return 0;
        } else {
            return lastCartonNum;
        }
    }

}
