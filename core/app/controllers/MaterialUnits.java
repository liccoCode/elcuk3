package controllers;

import controllers.api.SystemOperation;
import models.ElcukRecord;
import models.OperatorConfig;
import models.material.MaterialUnit;
import models.procure.Cooperator;
import models.product.Category;
import models.view.post.MaterialPost;
import models.view.post.MaterialUnitPost;
import models.view.post.ProcurePost;
import models.whouse.Whouse;
import org.joda.time.DateTime;
import play.mvc.Before;
import play.mvc.Controller;
import play.mvc.With;

import java.util.Arrays;
import java.util.List;

/**
 * Created by IntelliJ IDEA.
 * User: licco
 * Date: 2017/5/31
 * Time: 下午4:53
 */
@With({GlobalExceptionHandler.class, Secure.class, SystemOperation.class})
public class MaterialUnits extends Controller {

    @Before(only = {"index"})
    public static void beforeIndex() {
        List<Cooperator> cooperators = Cooperator.suppliers();
        String brandName = OperatorConfig.getVal("brandname");
        renderArgs.put("brandName", brandName);
        renderArgs.put("whouses", Whouse.find("type=?", Whouse.T.FBA).fetch());
        renderArgs.put("cooperators", cooperators);
        renderArgs.put("categoryIds", Category.categoryIds());

        //为视图提供日期
        DateTime dateTime = new DateTime();
        renderArgs.put("tomorrow1", dateTime.plusDays(1).toString("yyyy-MM-dd"));
        renderArgs.put("tomorrow2", dateTime.plusDays(2).toString("yyyy-MM-dd"));
        renderArgs.put("tomorrow3", dateTime.plusDays(3).toString("yyyy-MM-dd"));
    }

    /**
     * 列表查询
     *
     * @param p 分页参数
     */
    //@Check("materialUnits.index")
    public static void index(MaterialUnitPost p) {
//        if(p == null) p = new MaterialUnitPost();
//        List<MaterialUnit> units = p.query();
//        render(p, units);
        if(p == null) {
            p = new MaterialUnitPost();
            p.dateType = "attrs.planShipDate";
        }
        p.pagination = false;
        render(p);
    }

}
