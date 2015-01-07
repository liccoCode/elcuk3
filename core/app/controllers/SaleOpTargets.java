package controllers;


import controllers.api.SystemOperation;
import models.ElcukRecord;
import models.SaleOpTarget;
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

@With({GlobalExceptionHandler.class, Secure.class, SystemOperation.class})
public class SaleOpTargets extends Controller {

    @Before(only = {"show", "marketSplit", "seasonSplit"})
    public static void beforeLos() {
        String id = request.params.get("id");
        List<ElcukRecord> records = ElcukRecord.records(id + "", Messages.get("saleoptarget.update"));
        renderArgs.put("records", records);
    }

    @Check("saleoptargets.index")
    public static void index() {
        List<SaleOpTarget> saleOpTargets = SaleOpTarget.find("saleTargetType=?", SaleOpTarget.T.YEAR).fetch();
        render(saleOpTargets);
    }

    @Check("saleoptargets.create")
    public static void blank() {
        User user = User.findByUserName(Secure.Security.connected());
        List<Category> cates = User.getObjCategorys(user);
        SaleOpTarget yearSt = new SaleOpTarget();
        List<SaleOpTarget> sts = yearSt.loadCategorySaleTargets(cates);
        render(yearSt, sts);
    }

    @Check("saleoptargets.create")
    public static void create(SaleOpTarget yearSt, List<SaleOpTarget> sts) {
        User user = User.findByUserName(Secure.Security.connected());
        yearSt.createuser = user;
        if(yearSt.isExist()) Validation.addError("", String.format("已经存在 %s 年度的销售目标", yearSt.targetYear));
        //只有年度目标才会校验 name 不为空
        validation.required("目标名称", yearSt.name);
        yearSt.validate();
        yearSt.validateChild(sts);
        if(Validation.hasErrors()) render("SaleOpTargets/blank.html", yearSt, sts);
        for(SaleOpTarget st : sts) {
            st.save();
        }
        yearSt.save();
        flash.success("新建年度销售目标成功.");
        index();
    }

    public static void show(Long id) {
        SaleOpTarget yearSt = SaleOpTarget.findById(id);
        User user = User.findByUserName(Secure.Security.connected());
        List<String> categoryIds = User.getTeamCategorys(user);
        List<SaleOpTarget> sts = new ArrayList<SaleOpTarget>();
        if(categoryIds != null && categoryIds.size() > 0) {
            sts = SaleOpTarget.find("fid IN" + SqlSelect.inlineParam(categoryIds) + "AND targetYear " +
                    "= ? AND saleTargetType=?", yearSt.targetYear, SaleOpTarget.T.CATEGORY).fetch();
        }
        render(yearSt, sts);
    }

    @Check("saleoptargets.create")
    public static void update(Long id, SaleOpTarget yearSt, List<SaleOpTarget> sts) {
        SaleOpTarget manageSt = SaleOpTarget.findById(id);
        manageSt.update(yearSt, null);

        for(SaleOpTarget st : sts) {
            manageSt = SaleOpTarget.findById(st.id);
            manageSt.update(st, id);
        }
        if(Validation.hasErrors()) show(id);
        flash.success("更新成功.");
        show(id);
    }

    /**
     * 市场分解
     */
    @Check("saleoptargets.marketsplit")
    public static void marketSplit(Long id) {
        User user = User.findByUserName(Secure.Security.connected());
        SaleOpTarget categorySt = SaleOpTarget.findById(id);
        List<SaleOpTarget> sts = SaleOpTarget
                .find("fid=? AND targetYear=? AND saleTargetType=?", categorySt.fid, categorySt.targetYear,
                        SaleOpTarget.T.MARKET).fetch();
        if(sts == null || sts.size() == 0) sts = categorySt.loadMarketSaleTargets(user);
        render(categorySt, sts);
    }

    @Check("saleoptargets.marketsplit")
    public static void doMarketSplit(Long id, List<SaleOpTarget> sts) {
        for(SaleOpTarget st : sts) {
            SaleOpTarget manageSt = SaleOpTarget.findById(st.id);
            manageSt.update(st, id);
        }
        if(Validation.hasErrors()) marketSplit(id);
        flash.success("更新成功.");
        marketSplit(id);
    }


    /**
     * 季度分解
     */
    public static void seasonSplit(Long id) {
        User user = User.findByUserName(Secure.Security.connected());
        SaleOpTarget categorySt = SaleOpTarget.findById(id);
        List<SaleOpTarget> seasons = SaleOpTarget
                .find("fid=? AND targetYear=? AND saleTargetType=?", categorySt.fid, categorySt.targetYear,
                        SaleOpTarget.T.SEASON).fetch();
        if(seasons == null || seasons.size() == 0) seasons = categorySt.loadSeasonSaleTargets(user);

        List<SaleOpTarget> months = SaleOpTarget
                .find("fid=? AND targetYear=? AND saleTargetType=?", categorySt.fid, categorySt.targetYear,
                        SaleOpTarget.T.MONTH).fetch();
        if(months == null || months.size() == 0) months = categorySt.loadMonthSaleTargets(user);

        render(categorySt, seasons, months);
    }


    public static void doSeasonSplit(Long id, List<SaleOpTarget> ss, List<SaleOpTarget> ms) {
        for(SaleOpTarget st : ss) {
            SaleOpTarget manageSt = SaleOpTarget.findById(st.id);
            manageSt.update(st, id);
        }
        for(SaleOpTarget st : ms) {
            SaleOpTarget manageSt = SaleOpTarget.findById(st.id);
            manageSt.update(st, id);
        }
        if(Validation.hasErrors()) seasonSplit(id);
        flash.success("更新成功.");
        seasonSplit(id);
    }
}
