package controllers;

import helper.Constant;
import helper.GTs;
import models.procure.Shipment;
import org.allcolor.yahp.converter.IHtmlToPdfTransformer;
import org.krysalis.barcode4j.HumanReadablePlacement;
import org.krysalis.barcode4j.impl.code128.Code128Bean;
import org.krysalis.barcode4j.impl.code128.Code128Constants;
import org.krysalis.barcode4j.output.bitmap.BitmapCanvasProvider;
import org.krysalis.barcode4j.tools.MimeTypes;
import org.krysalis.barcode4j.tools.UnitConv;
import play.mvc.Controller;

import java.awt.image.BufferedImage;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;

import static play.modules.pdf.PDF.Options;
import static play.modules.pdf.PDF.renderPDF;

/**
 * Created by IntelliJ IDEA.
 * User: wyattpan
 * Date: 9/12/12
 * Time: 12:29 PM
 */
public class PDFs extends Controller {

    public static void packingSlip(String id) {
        //TODO 这里需要加载 FBA Shipment Id
        Options options = new Options();
        options.pageSize = IHtmlToPdfTransformer.A4P;

        String content = GTs.render("packingSlip", GTs.newMap("id", id).build());
        renderPDF(content, options, id);
    }

    public static void packing(String id) {
        String content = GTs.render("packingSlip", GTs.newMap("id", id).build());
        render(content);
    }

    public static void code128(String id) throws IOException {
        Code128Bean bean = new Code128Bean();

        // 尽可能调整到与 Amazon 的规格一样
        bean.setModuleWidth(0.11888);
        bean.setHeight(8.344);
        // 控制值内容, CODESET_A 不允许有小写
        bean.setCodeset(Code128Constants.CODESET_ALL);
        bean.setMsgPosition(HumanReadablePlacement.HRP_NONE);

        String fileName = String.format("%s.png", id);
        File file = new File(Constant.TMP, fileName);
        file.delete(); // 删除原来的, 再写新的
        OutputStream out = new FileOutputStream(file);
        BitmapCanvasProvider canvas = new BitmapCanvasProvider(out, MimeTypes.MIME_PNG, 600, BufferedImage.TYPE_BYTE_BINARY, false, 0);
        bean.generateBarcode(canvas, id);
        canvas.finish();
        out.close();
        response.setContentTypeIfNotSet(MimeTypes.MIME_PNG);
        renderBinary(file);
    }
}
