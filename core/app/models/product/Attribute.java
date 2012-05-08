package models.product;

import com.google.gson.annotations.Expose;
import org.apache.commons.lang.StringUtils;
import play.db.jpa.GenericModel;
import play.utils.FastRuntimeException;

import javax.persistence.*;

/**
 * 具体需要关联到的 Product 身上去的 Attribute 属性
 * User: wyattpan
 * Date: 4/26/12
 * Time: 10:29 AM
 */
@Entity
public class Attribute extends GenericModel {

    @OneToOne
    public AttrName attName;

    @ManyToOne(fetch = FetchType.LAZY)
    public Product product;

    /**
     * 属性 ID, 为  [sku]_[attname]
     */
    @Id
    @Expose
    public String id;

    @Lob
    @Expose
    public String value;

    @Expose
    public Boolean close = false;

    public Attribute updateAttr(Attribute att) {
        if(!StringUtils.equalsIgnoreCase(this.id, att.id)) throw new FastRuntimeException("不一样的 Attribute 不能更新!");
        if(StringUtils.isNotBlank(att.value)) this.value = att.value;
        if(att.close != null) this.close = att.close;
        return this.save();
    }
}
