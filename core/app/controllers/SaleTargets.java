package controllers;


import controllers.api.SystemOperation;
import models.ElcukRecord;
import models.SaleTarget;
import models.User;
import models.product.Category;
import play.data.validation.Validation;
import play.db.helper.SqlSelect;
import play.i18n.Messages;
import play.mvc.Before;
import play.mvc.Controller;
import play.mvc.With;

import java.util.ArrayList;
import java.util.List;

/**
 * 销售目标基本操作
 * <p/>
 * Created by IntelliJ IDEA.
 * User: mac
 * Date: 14-3-11
 * Time: PM4:50
 */

@With({GlobalExceptionHandler.class, Secure.class,SystemOperation.class})
public class SaleTargets extends Controller {

    @Before(only = {"show", "split"})
    public static void beforeLos() {
        String id = request.params.get("id");
        List<ElcukRecord> records = ElcukRecord.records(id + "", Messages.get("saletarget.update"));
        renderArgs.put("records", records);
    }

    @Check("saletargets.index")
    public static void index() {
        List<SaleTarget> salesTargets = SaleTarget.find("saleTargetType=?", SaleTarget.T.YEAR).fetch();
        render(salesTargets);
    }

    @Check("saletargets.create")
    public static void blank() {
        User user = User.findByUserName(Secure.Security.connected());
        List<Category> cates = User.getObjCategorys(user);
        SaleTarget yearSt = new SaleTarget();
        List<SaleTarget> sts = yearSt.loadCategorySaleTargets(cates);
        render(yearSt, sts);
    }

    @Check("saletargets.create")
    public static void create(SaleTarget yearSt, List<SaleTarget> sts) {
        User user = User.findByUserName(Secure.Security.connected());
        yearSt.createuser = user;
        if(yearSt.isExist()) Validation.addError("", String.format("已经存在 %s 年度的销售目标", yearSt.targetYear));
        //只有年度目标才会校验 name 不为空
        validation.required("目标名称", yearSt.name);
        yearSt.validate();
        yearSt.validateChild(sts);
        if(Validation.hasErrors()) render("SaleTargets/blank.html", yearSt, sts);
        for(SaleTarget st : sts) {
            st.save();
        }
        yearSt.save();
        flash.success("新建年度销售目标成功.");
        index();
    }

    public static void show(Long id) {
        SaleTarget yearSt = SaleTarget.findById(id);
        User user = User.findByUserName(Secure.Security.connected());
        List<String> categoryIds = User.getTeamCategorys(user);
        List<SaleTarget> sts = new ArrayList<>();
        if(categoryIds != null && categoryIds.size() > 0) {
            sts = SaleTarget.find("fid IN" + SqlSelect.inlineParam(categoryIds) + "AND targetYear " +
                    "= ? AND saleTargetType=?", yearSt.targetYear, SaleTarget.T.CATEGORY).fetch();
        }
        render(yearSt, sts);
    }

    @Check("saletargets.create")
    public static void update(Long id, SaleTarget yearSt, List<SaleTarget> sts) {
        SaleTarget manageSt = SaleTarget.findById(id);
        manageSt.update(yearSt, null);

        for(SaleTarget st : sts) {
            manageSt = SaleTarget.findById(st.id);
            manageSt.update(st, id);
        }
        if(Validation.hasErrors()) show(id);
        flash.success("更新成功.");
        show(id);
    }

    /**
     * 月度分解
     */
    @Check("saletargets.split")
    public static void split(Long id) {
        User user = User.findByUserName(Secure.Security.connected());
        SaleTarget categorySt = SaleTarget.findById(id);
        List<SaleTarget> sts = SaleTarget
                .find("fid=? AND targetYear=? AND saleTargetType=?", categorySt.fid, categorySt.targetYear,
                        SaleTarget.T.MONTH).fetch();
        if(sts == null || sts.size() == 0) sts = categorySt.loadMonthSaleTargets(user);
        render(categorySt, sts);
    }

    @Check("saletargets.split")
    public static void doSplit(Long id, List<SaleTarget> sts) {
        for(SaleTarget st : sts) {
            SaleTarget manageSt = SaleTarget.findById(st.id);
            manageSt.update(st, id);
        }
        if(Validation.hasErrors()) split(id);
        flash.success("更新成功.");
        split(id);
    }
}
