package models.material;

import com.google.gson.annotations.Expose;
import models.User;
import models.procure.CooperItem;
import models.procure.Cooperator;
import models.product.Product;
import models.whouse.Outbound;
import org.hibernate.annotations.DynamicUpdate;
import play.data.validation.Required;
import play.db.jpa.Model;

import javax.persistence.*;
import java.text.Collator;
import java.text.RuleBasedCollator;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;

/**
 * Created by IntelliJ IDEA.
 * User: licco
 * Date: 2017/5/16
 * Time: 下午3:30
 */
@Entity
@DynamicUpdate
public class Material extends Model {

    private static final long serialVersionUID = 4894533191306168541L;
    private static final RuleBasedCollator collator = (RuleBasedCollator) Collator.getInstance(Locale.CHINA);
    /**
     * 物料编码
     */
    public String code;

    /**
     * 物料名称
     */
    public String name;

    @Required
    @Expose
    @Enumerated(EnumType.STRING)
    public T type;

    public enum T {

        Parts {
            @Override
            public String label() {
                return "配件";
            }
        },

        Package {
            @Override
            public String label() {
                return "包材";
            }
        },

        RawMaterial {
            @Override
            public String label() {
                return "原材料";
            }
        };

        public abstract String label();
    }

    /**
     * 规格
     */
    public String specification;

    /**
     * 材质
     */
    public String texture;

    /**
     * 工艺
     */
    public String technology;

    /**
     * 工厂物料代码
     */
    public String version;

    @ManyToMany(mappedBy = "materials", cascade = CascadeType.PERSIST)
    public List<MaterialBom> boms = new ArrayList<>();

    @ManyToMany(cascade = CascadeType.PERSIST)
    public List<Product> products = new ArrayList<>();
    @OneToMany(mappedBy = "material", fetch = FetchType.LAZY)
    public List<CooperItem> cooperItems = new ArrayList<>();

    /**
     * 创建人
     */
    @OneToOne
    public User creator;

    public Date createDate;

    /**
     * 最近更新时间
     */
    public Date updateDate;

    /**
     * 是否删除
     */
    public boolean isDel = false;

    public String memo;

    /**
     * 项目名称(所属公司)
     */
    @Enumerated(EnumType.STRING)
    @Column(length = 20)
    public User.COR projectName;

    /**
     * 实际出库数(用于物料出库单创建,不会在数据库产生映射)
     */
    @Transient
    public int outQty;

    @Transient
    public String number;

    @Transient
    public String cooperName;

    /**
     *  采购余量
     */
    @Transient
    public int qty;

    /**
     *采购未确认数量
     */
    @Transient
    public int pendingQty;

    /**
     * 仓库可用库存
     */
    @Transient
    public int availableQty;

    /**
     * 返回所有物料信息
     */
    public static List<Material> suppliers() {
        List<Material> materials = Material.findAll();
        materials.sort((c1, c2) -> collator.compare(c1.name, c2.name));
        return materials;
    }


    /**
     * 根据物料ID查询可用库存
     *
     * @return
     */
    public int availableQty() {
        //1 查询出货计划总数
        List<MaterialPlanUnit> materialPlanUnitList = MaterialPlanUnit
                .find(" material.id=? AND materialPlan.receipt = ? AND materialPlan.state = ?",
                        id, MaterialPlan.R.WAREHOUSE, MaterialPlan.P.DONE).fetch();
        //1 查询已确认的出库总数
        List<MaterialOutboundUnit> materialOutboundUnitList = MaterialOutboundUnit
                .find(" material.id=? AND materialOutbound.status = ?", id, Outbound.S.Outbound).fetch();
        return materialPlanUnitList.stream().mapToInt(unit -> unit.receiptQty > 0 ? unit.receiptQty : unit.qty).sum()
                - materialOutboundUnitList.stream().mapToInt(unit -> unit.outQty).sum();
    }

    /**
     * 根据物料ID查询采购余量(采购单已确认)
     *
     * @return
     */
    public int surplusConfirmQty() {
        //1 查询已确认的采购计划总数
        List<MaterialUnit> materialUnitList = MaterialUnit
                .find(" material.id=? AND materialPurchase.state = ?", id, MaterialPurchase.S.CONFIRM).fetch();
        //2 查询已出库的出货计划总数
        List<MaterialPlanUnit> materialPlanUnitList = MaterialPlanUnit
                .find(" material.id=? AND materialPlan.state = ?", id, MaterialPlan.P.DONE).fetch();
        return materialUnitList.stream().mapToInt(unit -> unit.planQty).sum()
                - materialPlanUnitList.stream().mapToInt(unit -> unit.qty).sum();
    }

    /**
     * 根据物料ID查询采购余量(采购单未确认)
     *
     * @return
     */
    public int surplusPendingQty() {
        List<MaterialUnit> materialUnitList = MaterialUnit
                .find(" material.id=? AND materialPurchase.state = ?", id, MaterialPurchase.S.PENDING).fetch();
        return materialUnitList.stream().mapToInt(unit -> unit.planQty).sum();
    }

    /**
     * 根据物料查询对应 的所有供应商
     */
    public String cooperators() {
        List<Cooperator> cooperatorList = Cooperator
                .find("SELECT distinct c FROM Cooperator c, IN(c.cooperItems) ci WHERE ci.material.id=? ORDER BY ci"
                        + ".id", id).fetch();
        StringBuilder buff = new StringBuilder();
        for(Cooperator co : cooperatorList) {
            buff.append("," + co.name);
        }
        if(buff.length() > 0) {
            return buff.substring(1).toString();
        }
        return null;
    }

    /**
     * 查询物料的最近采购单价
     *
     * @return
     */
    public String nowPurchasePlanPrice() {
        MaterialUnit materialUnit = MaterialUnit.find(" material.id=? AND materialPurchase.state = ? ORDER BY "
                + "createDate DESC ", id, MaterialPurchase.S.CONFIRM).first();
        return materialUnit == null ? "" : materialUnit.planCurrency.symbol() + " " + materialUnit.planPrice;

    }
}
