package controllers;

import helper.DBUtils;
import helper.Webs;
import models.ElcukRecord;
import models.SaleTarget;
import models.User;
import models.product.Category;
import models.view.Ret;
import play.data.validation.Validation;
import play.db.helper.SqlSelect;
import play.i18n.Messages;
import play.mvc.Controller;
import play.mvc.With;

import java.util.Date;
import java.util.List;
import java.util.Map;

/**
 * 销售目标基本操作
 * <p/>
 * Created by IntelliJ IDEA.
 * User: mac
 * Date: 14-3-11
 * Time: PM4:50
 */

@With({GlobalExceptionHandler.class, Secure.class})
public class SaleTargets extends Controller {

    @Check("saletargets.index")
    public static void index() {
        List<SaleTarget> salesTargets = SaleTarget.find("saleTargetType=?", SaleTarget.T.YEAR).fetch();
        render(salesTargets);
    }

    @Check("saletargets.create")
    public static void blank() {
        SaleTarget yearSt = new SaleTarget();
        List<Category> cates = Category.findAll();
        List<SaleTarget> sts = yearSt.loadCategorySaleTargets(cates);
        render(yearSt, sts);
    }

    @Check("saletargets.create")
    public static void create(SaleTarget yearSt, List<SaleTarget> sts) {
        yearSt.createuser = User.findByUserName(Secure.Security.connected());
        yearSt.saleTargetType = SaleTarget.T.YEAR;
        if(yearSt.isExist()) Validation.addError("", String.format("已经存在 %s 年度的销售目标", yearSt.targetYear));
        yearSt.validate();
        sts = yearSt.copySaleTarget(sts);
        if(Validation.hasErrors()) render("SaleTargets/blank.html", yearSt, sts);
        yearSt.save();
        for(SaleTarget st : sts) {
            st.save();
        }
        flash.success("新建年度销售目标成功.");
        index();
    }

    public static void show(Long id) {
        SaleTarget yearSt = SaleTarget.findById(id);
        User user = User.findByUserName(Secure.Security.connected());
        List<String> categoryIds = User.getTeamCategorys(user);

        List<SaleTarget> sts = SaleTarget.find("fid IN" + SqlSelect.inlineParam(categoryIds) + "AND targetYear " +
                "= ? AND saleTargetType=?", yearSt.targetYear, SaleTarget.T.CATEGORY).fetch();
        //操作日志
        List<ElcukRecord> records = ElcukRecord.records(id + "", Messages.get("saletarget.update"));
        render(yearSt, sts, records);
    }

    public static void update(SaleTarget yearSt, List<SaleTarget> sts) {
        yearSt.validate();
        sts = yearSt.copySaleTarget(sts);
        if(Validation.hasErrors()) show(yearSt.id);
        yearSt.save();
        for(SaleTarget st : sts) {
            st.updateOld();
            new ElcukRecord(Messages.get("saletarget.update"), Messages.get("action.base", st.to_log()), yearSt.id + "")
                    .save();
        }
        new ElcukRecord(Messages.get("saletarget.update"), Messages.get("action.base", yearSt.to_log()), yearSt.id + "")
                .save();
        flash.success("更新成功.");
        show(yearSt.id);
    }

    /**
     * 月度分解
     *
     * @param id
     */
    public static void split(Long id) {
        SaleTarget categorySt = SaleTarget.findById(id);
        List<SaleTarget> sts = categorySt.loadMonthSaleTargets();
        //操作日志
        List<ElcukRecord> records = ElcukRecord.records(id + "", Messages.get("saletarget.split"));
        render(categorySt, sts, records);
    }

    public static void doSplit(SaleTarget categorySt, List<SaleTarget> sts) {
        categorySt.validate();
        sts = categorySt.copySaleTarget(sts);
        if(Validation.hasErrors()) split(categorySt.id);
        categorySt.save();
        for(SaleTarget st : sts) {
            if(st.id == null)st.save();
            else st.updateOld();
            new ElcukRecord(Messages.get("saletarget.split"), Messages.get("action.base", st.to_log()),
                    categorySt.id + "").save();
        }
        new ElcukRecord(Messages.get("saletarget.update"), Messages.get("action.base", categorySt.to_log()),
                categorySt.id + "").save();
        flash.success("更新成功.");
        split(categorySt.id);
    }
}
