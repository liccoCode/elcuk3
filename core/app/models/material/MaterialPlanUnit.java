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
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

/**
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
     * 物料计划
     */
    @ManyToOne
    public MaterialUnit materialUnit;

    /**
     * 实际交货数量
     * 目前版本对应 收货数量
     */
    public int qty;

    /**
     * 可用库存数量
     */
    public int availableQty;

    /**
     * 签收数量
     */
    public int receiptQty;

    /**
     * 预计交货时间
     */
    @Expose
    @Temporal(TemporalType.DATE)
    @Required
    public Date planDeliveryDate;

    /**
     * 实际交货时间
     */
    @Expose
    @Temporal(TemporalType.DATE)
    public Date deliveryDate;


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
     * @param attr
     * @param value
     */
    public void updateAttr(String attr, String value) {
            List<String> logs = new ArrayList<>();
            switch(attr) {
                case "qty":
                    this.qty = NumberUtils.toInt(value);
                    logs.addAll(Reflects.logFieldFade(this, attr, NumberUtils.toInt(value)));
                    break;
                case "deliveryDate":
                    try {
                        this.deliveryDate = new SimpleDateFormat("yyyy-MM-dd").parse(value);
                    } catch(ParseException e) {
                        e.printStackTrace();
                    }
                    logs.addAll(Reflects.logFieldFade(this, "deliveryDate", value));
                    break;
                default:
                    throw new FastRuntimeException("不支持的属性类型!");
            }
            new ERecordBuilder("materialPlan.updateAttr").msgArgs(this.id, StringUtils.join(logs, "<br/>"))
                    .fid(this.id).save();
            this.save();
        }
}
