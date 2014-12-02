package models.view.post;


import com.google.gson.annotations.Expose;
import org.joda.time.DateTime;
import play.data.validation.Required;

import javax.persistence.*;
import java.io.File;
import java.util.*;

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
    public boolean rbns;
}

