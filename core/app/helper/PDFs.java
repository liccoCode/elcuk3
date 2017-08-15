package helper;


import org.allcolor.yahp.cl.converter.CHtmlToPdfFlyingSaucerTransformer;
import org.allcolor.yahp.converter.IHtmlToPdfTransformer;
import org.apache.commons.lang3.StringUtils;
import play.Logger;
import play.modules.pdf.PDF;
import play.modules.pdf.PDF.PDFDocument;
import play.modules.pdf.RenderPDFTemplate;
import play.mvc.Http;
import play.templates.TemplateLoader;

import java.io.*;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

/**
 * Created by IntelliJ IDEA.
 * User: DyLanM
 * Date: 13-8-28
 * Time: 下午10:51
 */
public class PDFs {

    public PDF.MultiPDFDocuments docs;

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
            Object> args) {
        try {
            pdfName = StringUtils.replace(pdfName, "/", "|");
            OutputStream out = new FileOutputStream(folder.getPath() + "/" + pdfName);

            PDFDocument singleDoc = new PDFDocument();
            singleDoc.template = template;
            singleDoc.options = options;

            RenderPDFTemplate renderer = new RenderPDFTemplate(new PDF.MultiPDFDocuments().add(singleDoc), args);
            renderer.writePDF(out, Http.Request.current(), Http.Response.current());
        } catch(Exception e) {
            Logger.error(Webs.s(e));
        }
    }

    /***
     * 指定文件夹，生成PDF
     * 在无request的情况下
     * 目前主要是给每月自动生成发票的job用
     * @param folder
     * @param pdfName
     * @param appTemplate
     * @param args
     */
    public static void templateAsPDFWithNoRequest(File folder, String pdfName, String appTemplate, PDF.Options options,
                                                  Map<String, Object> args) {
        try {
            String content = TemplateLoader.load(appTemplate).render(args);
            IHtmlToPdfTransformer.PageSize pageSize = options.pageSize;
            pdfName = StringUtils.replace(pdfName, "/", "|");
            OutputStream out = new FileOutputStream(folder.getPath() + "/" + pdfName);
            new CHtmlToPdfFlyingSaucerTransformer().transform(
                    new ByteArrayInputStream(content.getBytes("UTF-8")), "", pageSize,
                    new ArrayList(), new HashMap(), out);
        } catch(UnsupportedEncodingException | IHtmlToPdfTransformer.CConvertException | FileNotFoundException e) {
            Logger.error(Webs.s(e));
        }
    }


}
