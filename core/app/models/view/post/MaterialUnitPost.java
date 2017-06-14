package models.view.post;


import helper.Dates;
import models.OperatorConfig;
import models.material.Material;
import models.material.MaterialBom;
import models.material.MaterialUnit;
import models.procure.ProcureUnit;
import org.apache.commons.lang.StringUtils;
import org.joda.time.DateTime;
import play.db.helper.SqlSelect;
import play.libs.F;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

/**
 * Created by IntelliJ IDEA.
 * User: Even
 * Date: 17/5/31
 * Time: PM5:12
 */
public class MaterialUnitPost extends Post<MaterialUnit> {


    /**
     * 选择过滤的日期类型
     */
    public String dateType;
    public List<ProcureUnit.STAGE> stages = new ArrayList<>();
    public static final List<String> projectNames = new ArrayList<>();
    public long cooperatorId;
    public long materialId;
    public String projectName;
    public Material.T type;
    public static final List<F.T2<String, String>> DATE_TYPES;

    static {
        DATE_TYPES = new ArrayList<>();
        DATE_TYPES.add(new F.T2<>("createDate", "创建时间"));
        DATE_TYPES.add(new F.T2<>("planDeliveryDate", "预计 [交货] 时间"));
        DATE_TYPES.add(new F.T2<>("deliveryDate", "实际 [交货] 时间"));
    }


    public MaterialUnitPost() {
        //this.perSize = 100;
        this.from = DateTime.now().minusDays(25).toDate();
        this.to = new Date();
        this.stages.add(ProcureUnit.STAGE.DONE);
        this.stages.add(ProcureUnit.STAGE.DELIVERY);
        this.stages.add(ProcureUnit.STAGE.IN_STORAGE);
        this.dateType = "createDate";
        this.perSize = 70;
        projectNames.clear();
        projectNames.add(OperatorConfig.getVal("brandname"));
        projectNames.add("B2B");

    }


    public MaterialUnitPost(ProcureUnit.STAGE stage) {
        this();
        this.stages.add(stage);
    }

    @Override
    public F.T2<String, List<Object>> params() {
        StringBuilder sbd = new StringBuilder("SELECT m FROM MaterialUnit m WHERE  1=1 AND ");
        List<Object> params = new ArrayList<>();
        /** 时间参数 **/
        if(this.dateType != null) {
            if(this.dateType.equals("planDeliveryDate")) {
                sbd.append(" m.planDeliveryDate>=? AND m.planDeliveryDate<=?");
            } else if(this.dateType.equals("deliveryDate")) {
                sbd.append(" m.deliveryDate>=? AND m.deliveryDate<=?");
            } else {
                sbd.append(" m.createDate>=? AND m.createDate<=?");
            }
            params.add(Dates.morning(this.from));
            params.add(Dates.night(this.to));
        }


        /** 状态参数 **/
        if(stages.size() > 0) {
            sbd.append(" AND m.stage IN " + SqlSelect.inlineParam(stages));
        }

        /** 供应商参数 **/
        if(this.cooperatorId > 0) {
            sbd.append(" AND m.cooperator.id=?");
            params.add(this.cooperatorId);
        }

        /** 物料类型参数 **/
        if(this.materialId > 0) {
            sbd.append(" AND m.material.id=?");
            params.add(this.materialId);
        }

        /** 项目名称参数
         if(StringUtils.isNotEmpty(this.projectName)) {
         sbd.append(" AND m.projectName=? ");
         params.add(this.projectName);
         }**/

        /** 模糊查询参数 **/
        if(StringUtils.isNotBlank(this.search)) {
            String word = this.word();
            sbd.append(" AND (m.material.code LIKE ?")
                    .append(")");
            for(int i = 0; i < 1; i++) {
                params.add(word);
            }
        }
        return new F.T2<>(sbd.toString(), params);
    }

    @Override
    public List<MaterialUnit> query() {
        F.T2<String, List<Object>> params = params();
        this.count = MaterialUnit.find(params._1, params._2.toArray()).fetch().size();
        String sql = params._1 + " ";
        return MaterialUnit.find(sql, params._2.toArray()).fetch(this.page, this.perSize);
    }

    public Long getTotalCount() {
        return this.count;
    }

    @Override
    public Long count(F.T2<String, List<Object>> params) {
        return (long) MaterialUnit.find(params._1, params._2.toArray()).fetch().size();
    }

}
