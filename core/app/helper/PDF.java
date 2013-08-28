package helper;

import play.modules.pdf.RenderPDFTemplate;
import play.mvc.Http;
import play.mvc.Scope;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.OutputStream;

/**
 * Created by IntelliJ IDEA.
 * User: DyLanM
 * Date: 13-8-28
 * Time: 下午10:51
 */
public class PDF {

    /**
     * 指定文件路径，生成PDF
     * @param folderPath 指定PDF文件，生成的文件目录
     * @param PDFName    PDF名称
     * @param template   PDF模板页面  如 ：FBAs/boxLabel.html
     * @param templateBinding 模板中的数据
     */
    public static void fbaAsPDF(String folderPath, String PDFName, String template, Scope.RenderArgs templateBinding) {

        File filePDF = new File(folderPath + "/", PDFName);
        OutputStream out = null;
        try {
            out = new FileOutputStream(filePDF);
        } catch(FileNotFoundException e) {
            e.printStackTrace();
        }

        play.modules.pdf.PDF.PDFDocument singleDoc = new play.modules.pdf.PDF.PDFDocument();
        singleDoc.template = template;

        RenderPDFTemplate renderer = new RenderPDFTemplate(new play.modules.pdf.PDF.MultiPDFDocuments().add(singleDoc),
                templateBinding.data);
        renderer.writePDF(out, Http.Request.current(), Http.Response.current());
    }
}
