package controllers;

import controllers.api.SystemOperation;
import helper.Webs;
import models.market.Account;
import models.market.M;
import models.procure.FBAShipment;
import models.procure.ProcureUnit;
import models.procure.Shipment;
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
@With({GlobalExceptionHandler.class, Secure.class,SystemOperation.class})
public class FBAs extends Controller {

    @Check("fbas.deploytoamazon")
    public static void deploysToAmazon(String deliveryId, List<Long> pids) {
        if(pids == null || pids.size() == 0)
            Validation.addError("", "必须选择需要创建的采购计划");

        if(Validation.hasErrors()) {
            Webs.errorToFlash(flash);
            Deliveryments.show(deliveryId);
        }

        ProcureUnit.postFbaShipments(pids);
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
     * @param procureUnitId
     */
    public static void changeFBA(Long procureUnitId) {
        ProcureUnit unit = ProcureUnit.findById(procureUnitId);
        unit.fba.removeFBAShipment();
        unit.postFbaShipment();
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
}
