package helper;


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
public class PDF {

    /**
     * 指定文件路径，生成PDF
     *
     * @param folder   指定PDF文件，生成的文件目录
     * @param PDFName  PDF名称
     * @param template PDF模板页面  如 ：FBAs/boxLabel.html
     * @param args     模板中的数据
     */
    public static void templateAsPDF(File folder, String PDFName, String template, Map<String, Object> args) {
        OutputStream out = null;
        try {
            out = new FileOutputStream(folder.getPath()+"/"+PDFName);
        } catch(FileNotFoundException e) {
            e.printStackTrace();
        }

        PDFDocument singleDoc = new PDFDocument();
        singleDoc.template = template;

        RenderPDFTemplate renderer = new RenderPDFTemplate(new play.modules.pdf.PDF.MultiPDFDocuments().add(singleDoc),
                args);
        renderer.writePDF(out, Http.Request.current(), Http.Response.current());
    }

}
