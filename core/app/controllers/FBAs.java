package controllers;

import helper.Constant;
import helper.Webs;
import models.Notification;
import models.embedded.ERecordBuilder;
import models.market.Account;
import models.procure.FBACenter;
import models.procure.FBAShipment;
import models.procure.ProcureUnit;
import org.allcolor.yahp.converter.IHtmlToPdfTransformer;
import org.krysalis.barcode4j.HumanReadablePlacement;
import org.krysalis.barcode4j.impl.code128.Code128Bean;
import org.krysalis.barcode4j.impl.code128.Code128Constants;
import org.krysalis.barcode4j.output.bitmap.BitmapCanvasProvider;
import org.krysalis.barcode4j.tools.MimeTypes;
import play.data.validation.Validation;
import play.i18n.Messages;
import play.modules.pdf.PDF;
import play.mvc.Before;
import play.mvc.Controller;
import play.mvc.With;

import java.awt.image.BufferedImage;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;

import static play.modules.pdf.PDF.renderPDF;

/**
 * Created by IntelliJ IDEA.
 * User: wyattpan
 * Date: 11/29/12
 * Time: 11:38 AM
 */
@With({GlobalExceptionHandler.class, Secure.class})
public class FBAs extends Controller {
    @Before
    public static void centerIds() {
        renderArgs.put("centerIds", FBACenter.centerIds());
    }

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
            Notification.notifies("FBA 创建成功",
                    Messages.get("shipment.createFBA.msg", unit.id, unit.sku, unit.fba.shipmentId),
                    Notification.PROCURE);
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
    public static void packingSlip(Long id) {
        final FBAShipment fba = FBAShipment.findById(id);
        final PDF.Options options = new PDF.Options();
        options.pageSize = IHtmlToPdfTransformer.A4P;

        renderArgs.put("shipmentId", fba.shipmentId);
        renderArgs.put("fba", fba);
        renderArgs.put("shipFrom", Account.address(fba.account.type));
        renderPDF(options);
    }

    /**
     * 箱外麦
     *
     * @param id
     */
    public static void boxLabel(Long id) {
        FBAShipment fba = FBAShipment.findById(id);
        PDF.Options options = new PDF.Options();
        options.pageSize = IHtmlToPdfTransformer.A4P;
        renderArgs.put("shipmentId", fba.shipmentId);
        renderArgs.put("shipFrom", Account.address(fba.account.type));
        renderArgs.put("fba", fba);
        renderPDF(options);
    }

    /**
     * 产生 FBA 的 2维 码
     *
     * @param shipmentId FBA 的 shipmentId
     * @throws IOException
     */
    public static void code128(String shipmentId) throws IOException {
        Code128Bean bean = new Code128Bean();

        // 尽可能调整到与 Amazon 的规格一样
        bean.setModuleWidth(0.11888);
        bean.setHeight(8.344);
        // 控制值内容, CODESET_A 不允许有小写
        bean.setCodeset(Code128Constants.CODESET_ALL);
        bean.setMsgPosition(HumanReadablePlacement.HRP_NONE);

        String fileName = String.format("%s.png", shipmentId);
        File file = new File(Constant.TMP, fileName);
        file.delete(); // 删除原来的, 再写新的
        OutputStream out = new FileOutputStream(file);
        BitmapCanvasProvider canvas = new BitmapCanvasProvider(out, MimeTypes.MIME_PNG, 600,
                BufferedImage.TYPE_BYTE_BINARY, false, 0);
        bean.generateBarcode(canvas, shipmentId);
        canvas.finish();
        out.close();
        response.setContentTypeIfNotSet(MimeTypes.MIME_PNG);
        renderBinary(file);
    }
}
