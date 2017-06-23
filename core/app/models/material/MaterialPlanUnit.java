package models.material;

import com.google.gson.annotations.Expose;
import helper.Reflects;
import models.User;
import models.embedded.ERecordBuilder;
import models.procure.ProcureUnit;
import org.apache.commons.lang.StringUtils;
import org.apache.commons.lang.math.NumberUtils;
import org.hibernate.annotations.DynamicUpdate;
import play.data.validation.Required;
import play.db.jpa.Model;
import play.utils.FastRuntimeException;

import javax.persistence.*;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

/**
 * 物料出貨計劃
 * Created by IntelliJ IDEA.
 * User: Even
 * Date: 17/6/12
 * Time: PM6:18
 */
@Entity
@DynamicUpdate
public class MaterialPlanUnit extends Model {

    private static final long serialVersionUID = 4894533191306168541L;
    /**
     * 出货单
     */
    @ManyToOne
    public MaterialPlan materialPlan;

    /**
     * 物料信息
     */
    @OneToOne(fetch = FetchType.LAZY)
    public Material material;

    /**
     * 实际交货数量
     * 目前版本对应 收货数量
     */
    public int qty;

    /**
     * 签收数量
     */
    public int receiptQty;


    /**
     * 物料计划状态
     */
    @Expose
    @Enumerated(EnumType.STRING)
    @Column(length = 20)
    public ProcureUnit.STAGE stage;

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

    /**
     * 将 MaterialPlanUnit 添加到/移出 出库单,状态改变
     *
     * @param materialPlan
     */
    public void toggleAssignToMaterialPlan(MaterialPlan materialPlan, boolean assign) {
        if(assign) {
            this.materialPlan = materialPlan;
            this.stage = ProcureUnit.STAGE.DELIVERY;
        } else {
            this.materialPlan = null;
            this.stage = ProcureUnit.STAGE.PLAN;
        }
    }

    /**
     * 修改对象属性
     * @param value
     */
    public void updateAttr(String value) {
        List<String> logs = new ArrayList<>();
        this.qty = NumberUtils.toInt(value);
        logs.addAll(Reflects.logFieldFade(this, "qty", NumberUtils.toInt(value)));
        new ERecordBuilder("materialPlan.updateAttr").msgArgs(this.id, StringUtils.join(logs, "<br/>"))
                .fid(this.id).save();
        this.save();
    }
}
