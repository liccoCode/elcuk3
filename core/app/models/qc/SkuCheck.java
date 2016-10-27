package models.qc;

import com.google.gson.annotations.Expose;
import models.product.Category;
import models.product.Product;
import play.data.validation.Validation;
import play.db.jpa.Model;

import javax.persistence.Entity;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

/**
 * Created by IntelliJ IDEA.
 * User: cary
 * Date: 5/6/13
 * Time: 10:07 AM
 */
@Entity
public class SkuCheck extends Model {
    /**
     * checklist名称
     */
    @Expose
    public String checkName;

    @Expose
    public Long pid;

    /**
     * 主表，细表标识
     */
    @Expose
    public LineType lineType;
    /**
     * 类型
     */
    @Expose
    public CheckType checkType;
    /**
     * 产品线/SKU名称
     */
    @Expose
    public String SkuName;

    /**
     * 检测要求
     */
    @Expose
    public String checkRequire;

    /**
     * 检测方法
     */
    @Expose
    public String checkMethod;


    /**
     * 最后更新时间
     */
    @Expose
    public Date updateAt;

    /**
     * 创建时间
     */
    @Expose
    public Date createdAt;

    /**
     * 创建人
     */
    @Expose
    public String creator;

    /**
     * 更新人
     */
    @Expose
    public String updator;


    public enum CheckType {
        CATEGORY {
            @Override
            public String label() {
                return "CATEGORY";
            }
        },
        SKU {
            @Override
            public String label() {
                return "SKU";
            }
        };

        public abstract String label();
    }

    public enum LineType {

        HEAD {
            @Override
            public String label() {
                return "HEAD";
            }
        },
        LINE {
            @Override
            public String label() {
                return "LINE";
            }
        };

        public abstract String label();
    }


    public List<SkuCheck> linelist() {
        StringBuilder sql = new StringBuilder(" 1=1 ");
        List<Object> params = new ArrayList<>();
        sql.append(" AND pid=? ");
        params.add(this.id);

        sql.append(" AND lineType=? ");
        params.add(LineType.LINE);

        List<SkuCheck> list = SkuCheck.find(sql.toString(),
                params.toArray()).fetch();
        return list;
    }


    /**
     * ProcureUnit 的检查
     */
    public void validate() {
        Validation.current().valid(this);
        Validation.required("SKU_CHECK名称", this.checkName);
        Validation.required("产品线/SKU", this.SkuName);
        Validation.required("类型", this.checkType);
        if(this.checkType == CheckType.CATEGORY) {
            Category cat = Category.findById(this.SkuName);
            if(cat == null) {
                Validation.addError("", String.format("Category %s 不存在!", this.SkuName));
            }
        }

        if(this.checkType == CheckType.SKU) {
            Product prt = Product.findById(this.SkuName);
            if(prt == null) {
                Validation.addError("", String.format("Product %s 不存在!", this.SkuName));
            }
        }

        if(this.id==null) {
            SkuCheck sc = SkuCheck.find("SkuName=?", this.SkuName).first();
            if(sc != null) {
                Validation.addError("", String.format("SKU_CHECK %s 已经存在!", this.SkuName));
            }
        }

    }
}
