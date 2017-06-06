package models.view.post;


import models.OperatorConfig;
import models.material.Material;
import models.material.MaterialBom;
import models.material.MaterialUnit;
import models.procure.ProcureUnit;
import org.joda.time.DateTime;
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
    
    @Override
    public F.T2<String, List<Object>> params() {
        StringBuilder sbd = new StringBuilder("SELECT m FROM MaterialUnit m ");
        List<Object> params = new ArrayList<>();

        return new F.T2<>(sbd.toString(), params);
    }

    @Override
    public List<MaterialUnit> query() {
        F.T2<String, List<Object>> params = params();
        this.count = MaterialBom.find(params._1, params._2.toArray()).fetch().size();
        String sql = params._1 + " ";
        return MaterialBom.find(sql, params._2.toArray()).fetch(this.page, this.perSize);
    }

    public Long getTotalCount() {
        return this.count;
    }

    @Override
    public Long count(F.T2<String, List<Object>> params) {
        return this.count;
    }

}
