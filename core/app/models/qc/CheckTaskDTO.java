package models.qc;

import play.data.validation.Validation;

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
    public Integer boxNum;

    /**
     * 个数
     */
    public Integer num;

    /**
     * 尾箱箱内的产品个数(可选)
     * <p>
     * PS: 如果尾箱的数据与主箱完全一致, 差别仅在于数量则可以使用该属性来传递数据
     */
    public Integer lastCartonNum;

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

    public CheckTaskDTO() {
    }

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

    public Double weight() {
        if(this.boxNum != null && this.singleBoxWeight != null) {
            return this.boxNum * this.singleBoxWeight;
        }
        return 0d;
    }

    public Double volume() {
        if(this.boxNum != null && this.length != null && this.width != null && this.height != null) {
            return this.length * this.width * this.height * this.boxNum / 1000000;
        }
        return 0d;
    }

    public boolean validedQtys(int shipedQty) {
        if(this.boxNum == 0) Validation.addError("", "箱数不能为 0");
        if(this.num == 0) Validation.addError("", "单箱个数不能为空");
        if(this.boxNum != 0 && this.num != 0) {
            int qtySum = this.boxNum * this.num;
            if(this.lastCartonNum != null) qtySum += lastCartonNum;
            if(qtySum != shipedQty) Validation.addError("", "数量不匹配!(主箱*个数 + 尾箱个数)");
        }
        return !Validation.hasErrors();
    }
}
