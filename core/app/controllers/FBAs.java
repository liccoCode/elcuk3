package controllers;

import controllers.api.SystemOperation;
import helper.Webs;
import models.market.Account;
import models.procure.FBAShipment;
import models.procure.ProcureUnit;
import models.procure.Shipment;
import models.qc.CheckTaskDTO;
import org.allcolor.yahp.converter.IHtmlToPdfTransformer;
import play.data.validation.Validation;
import play.modules.pdf.PDF;
import play.mvc.Controller;
import play.mvc.With;

import java.util.List;

import static play.modules.pdf.PDF.renderPDF;

/**
 * Created by IntelliJ IDEA.
 * User: wyattpan
 * Date: 11/29/12
 * Time: 11:38 AM
 */
@With({GlobalExceptionHandler.class, Secure.class, SystemOperation.class})
public class FBAs extends Controller {

    @Check("fbas.deploytoamazon")
    public static void deploysToAmazon(String deliveryId,
                                       List<Long> pids,
                                       List<CheckTaskDTO> dtos) {
        if(pids == null || pids.size() == 0) {
            Validation.addError("", "必须选择需要创建 FBA 的采购计划");
        } else if(pids.size() != dtos.size()) {
            Validation.addError("", "FBA 箱内信息的个数与采购计划的数量不一致");
        }
        if(Validation.hasErrors()) {
            Webs.errorToFlash(flash);
            Deliveryments.show(deliveryId);
        }

        ProcureUnit.postFbaShipments(pids, dtos);
        if(Validation.hasErrors()) {
            Webs.errorToFlash(flash);
        } else {
            flash.success("选择的采购计划全部成功创建 FBA");
        }
        Deliveryments.show(deliveryId);
    }

    @Check("fbas.update")
    public static void update(Long procureUnitId) {
        ProcureUnit unit = ProcureUnit.findById(procureUnitId);
        unit.fba.updateFBAShipment(null);
        if(Validation.hasErrors()) {
            Webs.errorToFlash(flash);
        } else {
            flash.success("FBA %s 更新成功.", unit.fba.shipmentId);
        }
        Deliveryments.show(unit.deliveryment.id);
    }

    /**
     * 更换FBA
     *
     * @param procureUnitId
     */
    public static void changeFBA(Long procureUnitId, CheckTaskDTO dto) {
        ProcureUnit unit = ProcureUnit.findById(procureUnitId);
        unit.removeFBAShipment();
        unit.postFbaShipment(dto);
        if(Validation.hasErrors()) {
            Webs.errorToFlash(flash);
        } else {
            flash.success("FBA %s 更换成功.", unit.fba.shipmentId);
        }
        Deliveryments.show(unit.deliveryment.id);
    }

    /**
     * 箱內麦
     *
     * @param id
     */
    public static void packingSlip(Long id, boolean html) {
        final FBAShipment fba = FBAShipment.findById(id);
        renderArgs.put("shipmentId", fba.shipmentId);
        renderArgs.put("fba", fba);
        renderArgs.put("shipFrom", Account.address(fba.account.type));

        if(html) {
            render();
        } else {
            final PDF.Options options = new PDF.Options();
            options.pageSize = IHtmlToPdfTransformer.A4P;
            renderPDF(options);
        }
    }

    /**
     * 箱外麦
     *
     * @param id
     */
    public static void boxLabel(Long id, boolean html, Long boxNumber) {
        FBAShipment fba = FBAShipment.findById(id);

        renderArgs.put("fba", fba);
        renderArgs.put("shipFrom", Account.address(fba.account.type));

        ProcureUnit procureUnit = fba.units.get(0);


        String shipmentid = fba.shipmentId;
        if(procureUnit.shipType == Shipment.T.EXPRESS) {
            shipmentid = shipmentid.trim() + "U";
        }
        renderArgs.put("shipmentId", shipmentid);
        renderArgs.put("procureUnit", procureUnit);
        renderArgs.put("boxNumber", boxNumber);
        if(html) {
            render();
        } else {
            PDF.Options options = new PDF.Options();
            //只设置 width height    margin 为零
            options.pageSize = new org.allcolor.yahp.converter.IHtmlToPdfTransformer.PageSize(20.8d, 29.6d);
            renderPDF(options);
        }
    }

    /**
     * 重新提交 Feed 给 Amazon 处理(只适用于 Rockend 和 API Limit 等造成的提交失败)
     *
     * @param id
     * @param feedId
     */
    public static void reSubmit(Long id, Long feedId) {
        FBAShipment fba = FBAShipment.findById(id);
        if(fba.reSubmit(feedId)) {
            renderText("成功提交 Feed 给 Amazon 处理, 请等待 2~3 分钟后再来查看处理结果.");
        } else {
            renderText(String.format("Amazon 已经处理成功 Feed[%s], 请勿重复提交.", feedId));
        }
    }

    /**
     * 更新 FBA 的箱内包装信息
     *
     * @param deliveryId
     * @param pids
     * @param dtos
     */
    public static void updateCartonContents(String deliveryId,
                                            List<Long> pids,
                                            List<CheckTaskDTO> dtos) {

        if(pids == null || pids.size() == 0) {
            Validation.addError("", "必须选择需要创建 FBA 的采购计划");
        } else if(pids.size() != dtos.size()) {
            Validation.addError("", "FBA 箱内信息的个数与采购计划的数量不一致");
        }
        if(Validation.hasErrors()) {
            Webs.errorToFlash(flash);
            Deliveryments.show(deliveryId);
        }

        ProcureUnit.postFbaCartonContents(pids, dtos);
        if(Validation.hasErrors()) {
            Webs.errorToFlash(flash);
        } else {
            flash.success("选择的采购计划全部成功提交更新 FBA, 请等待 3~5 分钟后查看.");
        }
        Deliveryments.show(deliveryId);
    }
}
