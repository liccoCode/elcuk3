package controllers;

import models.SaleTarget;
import models.User;
import models.view.Ret;
import play.data.validation.Validation;
import play.mvc.Controller;
import play.mvc.With;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;

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

    public static void annualIndex() {
        List<SaleTarget> salesTargets = SaleTarget.find("saleTargetType=?", SaleTarget.T.YEAR).fetch();
        render(salesTargets);
    }

    public static void showAnnual(Long id) {
        SaleTarget st = SaleTarget.findById(id);
        List<SaleTarget> saleTargets = SaleTarget.find("parentId=?", id).fetch();
        saleTargets = st.beforeDetails(saleTargets);
        render(st, saleTargets);
    }

    public static void createAnnual() {
        SaleTarget st = new SaleTarget();
        render(st);
    }

    /**
     * 创建年度目标
     */
    public static void doCreateAnnual(SaleTarget st, String targetDate) throws ParseException {
        Validation.required("主题", st.theme.trim());
        Validation.required("目标时间", targetDate);
        st.targetDate = new SimpleDateFormat("yyyy").parse(targetDate);
        st.createuser = User.findByUserName(Secure.Security.connected());
        st.saleTargetType = SaleTarget.T.YEAR;
        if(Validation.hasErrors()) {
            render("SaleTargets/createAnnual.html", st);
        }
        st.save();
        flash.success("保存成功.");
        showAnnual(st.id);
    }

    public static void updateAnnual(SaleTarget st, String targetDate) throws ParseException {
        Validation.required("主题", st.theme.trim());
        Validation.required("目标时间", targetDate);
        st.targetDate = new SimpleDateFormat("yyyy").parse(targetDate);
        if(Validation.hasErrors()) {
            render("SaleTargets/createAnnual.html", st);
        }
        st.save();
        flash.success("更新成功.");
        showAnnual(st.id);
    }


    public static void showCategory(Long id) {
        SaleTarget st = SaleTarget.findById(id);
        List<SaleTarget> saleTargets = SaleTarget.find("parentId=?", id).fetch();
        saleTargets = st.beforeDetails(saleTargets);
        render(st, saleTargets);
    }

    /**
     * 创建新的子销售目标
     *
     * @param parentId     父对象 Id
     * @param fid          外键 ID（该销售目标指派给谁）
     * @param profitMargin 销售额
     * @param saleAmounts  利润率
     */
    public static void create(Long parentId, String fid, Float profitMargin, Float saleAmounts,
                              Date targetDate) {
        SaleTarget st = SaleTarget.findById(parentId);
        SaleTarget child = new SaleTarget();
        child.fid = fid;
        child.parentId = parentId;
        child.profitMargin = profitMargin;
        child.saleAmounts = saleAmounts;
        child.targetDate = targetDate;
        st.copySaleTarget(child, User.findByUserName(Secure.Security.connected()));
        child.save();
        renderJSON(new Ret(true, "保存成功."));
    }

    /**
     * 修改子销售目标
     *
     * @param pid          主键 ID
     * @param profitMargin 销售额
     * @param saleAmounts  利润率
     * @param targetDate   需要创建的子销售目标的时间
     */
    public static void update(Long pid, Float profitMargin, Float saleAmounts,
                              Date targetDate) {
        SaleTarget st = SaleTarget.findById(pid);
        st.profitMargin = profitMargin;
        st.saleAmounts = saleAmounts;
        st.targetDate = targetDate;
        st.save();
        renderJSON(new Ret(true, "保存成功."));
    }
}
