package models.material;

import com.google.gson.annotations.Expose;
import controllers.Login;
import helper.Reflects;
import models.User;
import models.embedded.ERecordBuilder;
import models.procure.Cooperator;
import models.whouse.Outbound;
import org.apache.commons.lang.StringUtils;
import org.hibernate.annotations.DynamicUpdate;
import org.joda.time.DateTime;
import play.data.validation.Required;
import play.data.validation.Validation;
import play.db.jpa.GenericModel;

import javax.persistence.*;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

/**
 * 物料出库单model
 * 直接跟MaterialUnit建立一对多的关系
 * Created by IntelliJ IDEA.
 * User: Even
 * Date: 17/6/8
 * Time: AM10:18
 */
@Entity
@DynamicUpdate
public class MaterialOutbound extends GenericModel {

    private static final long serialVersionUID = 163177419089864527L;


    @Id
    @Column(length = 30)
    @Expose
    public String id;

    /**
     * 名称
     */
    @Required
    public String name;

    /**
     * 项目名称
     */
    @Required
    public String projectName;

    /**
     * 出库类型
     */
    @Enumerated(EnumType.STRING)
    @Required
    @Expose
    public C type;


    public enum C {
        Scll {
            @Override
            public String label() {
                return "生产领料";
            }
        },
        Wfgc {
            @Override
            public String label() {
                return "外发工厂";
            }
        },
        Shll {
            @Override
            public String label() {
                return "损毁领料";
            }
        },
        Other {
            @Override
            public String label() {
                return "其他";
            }
        };

        public abstract String label();
    }


    /**
     * (收货方)供应商
     * 一个采购单只能拥有一个供应商
     */
    @ManyToOne
    public Cooperator cooperator;

    /**
     * 目的地
     */
    public String whouse;

    /**
     * 发货人
     */
    public String consignor;

    /**
     * 出库时间
     */
    @Required
    public Date outboundDate;

    /**
     * 物料出库计划
     */
    @OneToMany(mappedBy = "materialOutbound", cascade = {CascadeType.PERSIST})
    public List<MaterialOutboundUnit> units = new ArrayList<>();

    @Enumerated(EnumType.STRING)
    @Required
    @Expose
    public Outbound.S status;

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
     * 备注
     */
    public String memo;


    public MaterialOutbound() {
        init();
    }

    public void init() {
        this.id = id();
        this.status = Outbound.S.Create;
        this.createDate = new Date();
        this.handler = Login.current();
        this.projectName = Login.current().projectName.label();
    }


    public static String id() {
        DateTime dt = DateTime.now();
        DateTime nextMonth = dt.plusMonths(1);
        String count = MaterialOutbound.count("createDate>=? AND createDate<?",
                DateTime.parse(String.format("%s-%s-01", dt.getYear(), dt.getMonthOfYear())).toDate(),
                DateTime.parse(String.format("%s-%s-01", nextMonth.getYear(), nextMonth.getMonthOfYear())).toDate())
                + "";
        return String.format("WLC|%s|%s", dt.toString("yyyyMM"), count.length() == 1 ? "0" + count : count);
    }


    public void saveAndLog(MaterialOutbound outbound) {
        List<String> logs = new ArrayList<>();
        logs.addAll(Reflects.logFieldFade(this, "name", outbound.name));  //出库单名称
        logs.addAll(Reflects.logFieldFade(this, "type", outbound.type));  //出库类型
        if(outbound.cooperator != null)
            //logs.addAll(Reflects.logFieldFade(this, "cooperator.id", outbound.cooperator.id)); //收货方
            logs.addAll(Reflects.logFieldFade(this, "whouse", outbound.whouse));// 目的地
        logs.addAll(Reflects.logFieldFade(this, "consignor", outbound.consignor));// 发货人
        logs.addAll(Reflects.logFieldFade(this, "outboundDate", outbound.outboundDate));//出库时间
        logs.addAll(Reflects.logFieldFade(this, "memo", outbound.memo));    //备注
        if(logs.size() > 0) {
            new ERecordBuilder("materialOutbound.update").msgArgs(this.id, StringUtils.join(logs, "<br>")).fid(this
                    .id)
                    .save();
        }
        if(outbound.cooperator != null){
        Cooperator cp = Cooperator.findById(outbound.cooperator.id);
        this.cooperator = cp;
        }
        this.save();
    }

    public static void confirmOutBound(List<String> ids) {
        for(String id : ids) {
            MaterialOutbound out = MaterialOutbound.findById(id);
            if(out.units.stream().anyMatch(p -> p.outQty <= 0)){
                Validation.addError("", "物料出库单【" + out.id + "】的下的出库计划 出库数量不能小于等于0，请先检查！");
                return;
            }
            if(out.units.stream().anyMatch(p -> p.outQty > p.material.availableQty())){
                Validation.addError("", "物料出库单【" + out.id + "】的下的出库计划 出库数量大于物料可用库存量，请先检查！");
                return;
            }
            if(Validation.hasErrors()) {
                return;
            }
            out.status = Outbound.S.Outbound;
            out.outboundDate = new Date();
            out.save();
        }
    }


    /**
     * 出库单快速添加物料编码
     *
     * @param id
     * @param code
     * @return
     */
    public static MaterialOutbound addunits(String id, String code) {
        MaterialOutbound materialOutbound = MaterialOutbound.findById(id);
        //验证物料编码是否存在于出库单元里面
        long count = materialOutbound.units.stream().filter(unit -> unit.material.code.equals(code)).count();
        if(count > 0) {
            Validation.addError("", "物料编码 %s 已经存在于物料出库单元！", code);
            return materialOutbound;
        }
        Material material = Material.find("byCode", code).first();
        if(material == null) {
            Validation.addError("", "物料编码 %s 不存在！", code);
            return materialOutbound;
        }
        // 将 Material 添加进入 出库单
        MaterialOutboundUnit planUnit = new MaterialOutboundUnit();
        planUnit.materialOutbound = materialOutbound;
        planUnit.material = material;
        planUnit.handler = Login.current();
        materialOutbound.units.add(planUnit);
        materialOutbound.save();
        new ERecordBuilder("materialOutbound.addunits")
                .msgArgs(code, materialOutbound.id).fid(materialOutbound.id).save();
        return materialOutbound;
    }
}
