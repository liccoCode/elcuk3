package helper;


import play.modules.pdf.PDF;
import play.modules.pdf.PDF.PDFDocument;
import play.modules.pdf.RenderPDFTemplate;
import play.mvc.Http;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.OutputStream;
import java.util.Map;

/**
 * Created by IntelliJ IDEA.
 * User: DyLanM
 * Date: 13-8-28
 * Time: 下午10:51
 */
public class PDFs {

    /**
     * 指定文件夹，生成PDF
     *
     * @param folder   指定PDF文件，生成的文件目录
     * @param pdfName  PDF名称
     * @param template PDF模板页面  如 ：FBAs/boxLabel.html
     * @param options  PDF  页面大小   不设置 默认为 A4  20.8d, 29.6d, 1d
     * @param args     模板中的数据
     */
    public static void templateAsPDF(File folder, String pdfName, String template, PDF.Options options, Map<String,
            Object> args) throws FileNotFoundException {
        OutputStream out = new FileOutputStream(folder.getPath() + "/" + pdfName);

        PDFDocument singleDoc = new PDFDocument();
        singleDoc.template = template;
        singleDoc.options = options;

        RenderPDFTemplate renderer = new RenderPDFTemplate(new PDF.MultiPDFDocuments().add(singleDoc),
                args);
        renderer.writePDF(out, Http.Request.current(), Http.Response.current());
    }

}
