package models.view.post;


/**
 * 上架需要更新的信息
 * User: cary
 * Date: 26/11/14
 * Time: 10:48 AM
 */
public class SellingAmzPost {

    public SellingAmzPost() {
        this.title = false;
        this.saleprice = false;
        this.keyfeturess = false;
        this.searchtermss = false;
        this.productdesc = false;
        this.standerprice = false;
        this.rbns = false;
    }

    public boolean title;
    public boolean saleprice;
    public boolean keyfeturess;
    public boolean searchtermss;
    public boolean productdesc;
    public boolean standerprice;
    public boolean modelNumber;
    public boolean rbns;


    public boolean productvolume;
    public String volumeunit;// 长度单位

    public Float productLengths;
    public Float productWidth;
    public Float productHeigh;

    public Float proWeight;
    public Float packWeight;

    public boolean productWeight;
    public String productWeightUnit;
    public boolean weight;
    public String weightUnit;// 重量单位
}

