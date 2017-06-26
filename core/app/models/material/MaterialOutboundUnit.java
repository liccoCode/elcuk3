package models.material;

import com.google.gson.annotations.Expose;
import models.User;
import org.hibernate.annotations.DynamicUpdate;
import play.data.validation.Required;
import play.db.jpa.GenericModel;
import play.db.jpa.Model;

import javax.persistence.*;
import java.util.Date;

/**
 * Created by IntelliJ IDEA.
 * User: Even
 * Date: 17/6/19
 * Time: AM11:52
 */

@Entity
@DynamicUpdate
public class MaterialOutboundUnit extends Model {


    private static final long serialVersionUID = 163177419089860527L;

    /**
     * 物料出库单
     */
    @ManyToOne(fetch = FetchType.LAZY, cascade = {CascadeType.PERSIST})
    public MaterialOutbound materialOutbound;

    /**
     * 物料信息
     */
    @OneToOne(fetch = FetchType.LAZY)
    public Material material;
    
    /**
     * 实际出库数
     */
    public int outQty;

    /**
     * 操作人员
     */
    @OneToOne
    public User handler;

    /**
     * 创建时间
     */
    @Expose
    @Required
    public Date createDate = new Date();
}
