package models.material;

import org.hibernate.annotations.DynamicUpdate;
import play.db.jpa.GenericModel;
import play.db.jpa.Model;

import javax.persistence.*;

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


}
