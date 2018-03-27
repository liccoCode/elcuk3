package models.product;

import controllers.Login;
import org.apache.commons.lang3.StringUtils;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;

/**
 * Created by IntelliJ IDEA.
 * User: wyattpan
 * Date: 1/6/12
 * Time: 10:55 AM
 */

public class ProductJxl {


    public String categoryId;
    public String sku;
    public String lengths;
    public String width;
    public String heigh;
    public String weight;
    public String productLengths;
    public String productWidth;
    public String productHeigh;
    public String productWeight;
    public String declaredValue;
    public String declareName;
    public String useWay;
    public String chineseName;
    public String salesLevel;
    public String marketState;
    public String procureState;
    public String productState;
    public String upc;
    public String partNumber;
    public String upcJP;
    public String partNumberJP;
    public String abbreviation;
    public String marketTime;
    public String delistingTime;
    public String productName;
    public String subtitle;
    public String state;

    public SimpleDateFormat formatter = new SimpleDateFormat("yyyy/MM/dd");

    public Product setProduct() throws Exception {
        Product pro = Product.findById(sku);
        if(pro == null) pro = new Product();
        pro.sku = this.sku;

        pro.category =  Category.findById(this.categoryId);
        if(StringUtils.isNotBlank(this.lengths)) pro.lengths = Float.valueOf(this.lengths);
        if(StringUtils.isNotBlank(this.width)) pro.width = Float.valueOf(this.width);
        if(StringUtils.isNotBlank(this.heigh)) pro.heigh = Float.valueOf(this.heigh);
        if(StringUtils.isNotBlank(this.weight)) pro.weight = Float.valueOf(this.weight);
        if(StringUtils.isNotBlank(this.productLengths)) pro.productLengths = Float.valueOf(this.productLengths);
        if(StringUtils.isNotBlank(this.productWidth)) pro.productWidth = Float.valueOf(this.productWidth);
        if(StringUtils.isNotBlank(this.productHeigh)) pro.productHeigh = Float.valueOf(this.productHeigh);
        if(StringUtils.isNotBlank(this.productWeight)) pro.productWeight = Float.valueOf(this.productWeight);

        pro.declareName = this.declareName;
        pro.useWay = this.useWay;
        pro.chineseName = this.chineseName;
        if(StringUtils.isNotBlank(this.salesLevel)) pro.salesLevel = Product.E.val(this.salesLevel);
        if(StringUtils.isNotBlank(this.marketState)) pro.marketState = Product.T.val(this.marketState);
        if(StringUtils.isNotBlank(this.procureState)) pro.procureState = Product.P.val(this.procureState);
        if(StringUtils.isNotBlank(this.productState)) pro.productState = Product.L.val(this.productState);

        pro.upc = this.upc;
        pro.partNumber = this.partNumber;
        pro.upcJP = this.upcJP;
        pro.partNumberJP = this.partNumberJP;
        pro.abbreviation = this.abbreviation;

        if(StringUtils.isNotBlank(this.marketTime)) pro.marketTime = formatter.parse(this.marketTime);
        if(StringUtils.isNotBlank(this.delistingTime)) pro.delistingTime = formatter.parse(this.delistingTime);
        pro.productName = this.productName;
        pro.subtitle = this.subtitle;
        pro.locates = "[]";
        pro.sellingPoints = "[]";
        if(StringUtils.isNotBlank(this.state)) pro.state = Product.S.valueOf(this.state);
        pro.createDate = new Date();
        pro.creator = Login.current();
        pro.save();
        return pro;

    }


}

