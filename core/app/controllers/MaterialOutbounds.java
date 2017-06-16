package controllers;

import controllers.api.SystemOperation;
import models.material.MaterialOutbound;
import models.material.MaterialUnit;
import play.db.helper.JpqlSelect;
import play.mvc.Controller;
import play.mvc.With;

import java.util.List;

/**
 * 物料出库单Controller
 * Created by IntelliJ IDEA.
 * User: Even
 * Date: 17/6/8
 * Time: PM3:04
 */
@With({GlobalExceptionHandler.class, Secure.class, SystemOperation.class})
public class MaterialOutbounds extends Controller {


    /**
     * 跳转 到 创建物料出库单页面
     * @param pids
     */
    public static void blank(List<Long> pids) {
        List<MaterialUnit> units = MaterialUnit.find("id IN " + JpqlSelect.inlineParam(pids)).fetch();
        MaterialUnit materialUnit = units.get(0);
        MaterialOutbound outbound = new MaterialOutbound(materialUnit);
        render(units, materialUnit, outbound);
    }
    
}
