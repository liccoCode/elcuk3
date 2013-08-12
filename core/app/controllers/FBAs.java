package controllers;

import helper.Webs;
import models.embedded.ERecordBuilder;
import models.market.Account;
import models.procure.FBAShipment;
import models.procure.ProcureUnit;
import org.allcolor.yahp.converter.IHtmlToPdfTransformer;
import play.data.validation.Validation;
import play.modules.pdf.PDF;
import play.mvc.Controller;
import play.mvc.With;

import static play.modules.pdf.PDF.renderPDF;

/**
 * Created by IntelliJ IDEA.
 * User: wyattpan
 * Date: 11/29/12
 * Time: 11:38 AM
 */
@With({GlobalExceptionHandler.class, Secure.class})
public class FBAs extends Controller {
    /**
     * 通过 ProcureUnit 创建其对应的 FBA
     *
     * @param procureUnitId 采购计划 ID
     */
    @Check("fbas.deploytoamazon")
    public static void deployToAmazon(Long procureUnitId) {
        ProcureUnit unit = ProcureUnit.findById(procureUnitId);

        unit.postFbaShipment();
        if(Validation.hasErrors()) {
            Webs.errorToFlash(flash);

        } else {
            new ERecordBuilder("shipment.createFBA")
                    .msgArgs(unit.id, unit.sku, unit.fba.shipmentId)
                    .fid(unit.id)
                    .save();
            flash.success("成功在 Amazon 创建 FBA: %s", unit.fba.shipmentId);
        }

        Deliveryments.show(unit.deliveryment.id);
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
    public static void boxLabel(Long id, boolean html) {
        FBAShipment fba = FBAShipment.findById(id);
        renderArgs.put("shipmentId", fba.shipmentId);
        renderArgs.put("fba", fba);
        renderArgs.put("shipFrom", Account.address(fba.account.type));
        if(html) {
            render();
        } else {
            PDF.Options options = new PDF.Options();
            options.pageSize = IHtmlToPdfTransformer.A4P;
            renderPDF(options);
        }
    }
}
