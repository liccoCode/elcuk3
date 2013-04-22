package controllers;

import models.ElcukRecord;
import models.User;
import models.finance.ProcureApply;
import models.procure.Cooperator;
import models.procure.Deliveryment;
import models.product.Category;
import models.view.Ret;
import models.view.post.DeliveryPost;
import org.apache.commons.lang.StringUtils;
import play.data.validation.Error;
import play.data.validation.Validation;
import play.i18n.Messages;
import play.mvc.Before;
import play.mvc.Controller;
import play.mvc.With;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

/**
 * 采购单控制器
 * User: wyattpan
 * Date: 6/19/12
 * Time: 2:29 PM
 */
@With({GlobalExceptionHandler.class, Secure.class})
public class Deliveryments extends Controller {

    @Before(only = {"show", "update", "addunits", "delunits", "cancel", "confirm"})
    public static void showPageSetUp() {
        String deliverymentId = request.params.get("id");
        if(StringUtils.isBlank(deliverymentId)) deliverymentId = request.params.get("dmt.id");
        Deliveryment dmt = Deliveryment.findById(deliverymentId);
        if(dmt != null)
            renderArgs.put("plan_units", dmt.availableInPlanStageProcureUnits());
        renderArgs.put("records", ElcukRecord.records(deliverymentId));
        renderArgs.put("shippers", Cooperator.shippers());
        renderArgs.put("buyers", User.procurers());
    }

    @Before(only = {"index", "deliverymentToApply"})
    public static void beforeIndex(DeliveryPost p) {
        List<Cooperator> suppliers = Cooperator.suppliers();
        List<ProcureApply> avaliableApplies = ProcureApply.unPaidApplies(p.cooperId);
        renderArgs.put("suppliers", suppliers);
        renderArgs.put("avaliableApplies", avaliableApplies);
    }

    @Check("deliveryments.index")
    public static void index(DeliveryPost p, List<String> deliverymentIds) {
        List<Deliveryment> deliveryments = null;
        if(deliverymentIds == null) deliverymentIds = new ArrayList<String>();
        if(p == null) p = new DeliveryPost();
        deliveryments = p.query();
        render(deliveryments, p, deliverymentIds);
    }

    //DL|201301|08
    public static void show(String id) {
        Deliveryment dmt = Deliveryment.findById(id);
        render(dmt);
    }

    public static void update(Deliveryment dmt) {
        validation.valid(dmt);
        if(Validation.hasErrors())
            render("Deliveryments/show.html", dmt);
        dmt.save();
        flash.success("更新成功.");
        show(dmt.id);
    }

    /**
     * 向 Deliveryment 添加 ProcureUnit
     *
     * @param pids
     * @param dmt
     */
    public static void addunits(List<Long> pids, Deliveryment dmt) {
        Validation.required("deliveryments.addunits", pids);
        if(Validation.hasErrors())
            render("Deliveryments/show.html", dmt);
        dmt.assignUnitToDeliveryment(pids);

        // 再一次检查, 是否有错误
        if(Validation.hasErrors()) {
            renderArgs.put("plan_units", dmt.availableInPlanStageProcureUnits());
            render("Deliveryments/show.html", dmt);
        }
        show(dmt.id);
    }

    /**
     * 将 ProcureUnit 从 Deliveryment 中解除
     *
     * @param pids
     * @param dmt
     */
    public static void delunits(List<Long> pids, Deliveryment dmt) {
        Validation.required("deliveryments.delunits", pids);
        if(Validation.hasErrors())
            render("Deliveryments/show.html", dmt);
        dmt.unAssignUnitInDeliveryment(pids);

        if(Validation.hasErrors())
            render("Deliveryments/show.html", dmt);

        show(dmt.id);
    }

    /**
     * 确认采购单, 这样才能进入运输单进行挑选
     */
    public static void confirm(String id) {
        Deliveryment dmt = Deliveryment.findById(id);
        validation.equals(dmt.state, Deliveryment.S.PENDING);
        validation.required(dmt.deliveryTime);
        validation.required(dmt.orderTime);
        if(Validation.hasErrors()) render("Deliveryments/show.html", dmt);
        dmt.confirm();
        new ElcukRecord(Messages.get("deliveryment.confirm"), String.format("确认[采购单] %s", id), id)
                .save();
        show(id);
    }

    /**
     * 取消采购单
     */
    @Check("deliveryments.cancel")
    public static void cancel(String id, String msg) {
        Validation.required("deliveryments.cancel", msg);
        Deliveryment dmt = Deliveryment.findById(id);
        dmt.cancel(msg);
        if(Validation.hasErrors())
            render("Deliveryments/show.html", dmt, msg);

        show(dmt.id);
    }

    /**
     * 获取某一个采购单所有产品的产品要求
     */
    public static void productTerms(String id) {
        Deliveryment dmt = Deliveryment.findById(id);
        Set<Category> categoryses = dmt.unitsCategorys();
        StringBuilder sbd = new StringBuilder("");
        for(Category cate : categoryses) {
            if(StringUtils.isNotBlank(cate.productTerms))
                sbd.append(cate.productTerms);
        }
        renderJSON(new Ret(true, sbd.toString()));
    }

    /**
     * 进入采购单请款生成页面
     */
    @Check("deliveryments.deliverymenttoapply")
    public static void deliverymentToApply(List<String> deliverymentIds, DeliveryPost p,
                                           Long procureApplyId) {
        if(deliverymentIds == null) deliverymentIds = new ArrayList<String>();
        if(deliverymentIds.size() <= 0) {
            flash.error("请选择需纳入请款的采购单(相同供应商).");
            index(p, deliverymentIds);
        }

        ProcureApply apply = ProcureApply.findById(procureApplyId);
        if(apply == null)
            apply = ProcureApply.buildProcureApply(deliverymentIds);
        else
            apply.appendDelivery(deliverymentIds);

        if(apply == null || Validation.hasErrors()) {
            for(Error error : Validation.errors()) {
                flash.error(error.message());
            }
            index(p, deliverymentIds);
        } else {
            flash.success("请款单 %s 申请成功.", apply.serialNumber);
            Applys.procure(apply.id);
        }
    }
}
