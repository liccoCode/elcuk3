package controllers;

import helper.Webs;
import models.procure.Deliveryment;
import models.procure.ProcureUnit;
import models.view.dto.AnalyzeDTO;
import models.view.dto.DeliveryExcel;
import models.view.post.AnalyzePost;
import models.view.post.DeliveryPost;
import play.data.validation.Validation;
import play.db.helper.JpqlSelect;
import play.modules.excel.RenderExcel;
import play.mvc.Controller;
import play.mvc.With;

import java.text.SimpleDateFormat;
import java.util.List;

/**
 * Created by IntelliJ IDEA.
 * User: wyattpan
 * Date: 10/31/12
 * Time: 11:47 AM
 */
@With({GlobalExceptionHandler.class, Secure.class})
public class Excels extends Controller {


    @Check("excels.deliveryment")
    public static void deliveryment(String id, DeliveryExcel excel) {
        excel.dmt = Deliveryment.findById(id);
        request.format = "xls";
        renderArgs.put(RenderExcel.RA_FILENAME, id + ".xls");
        renderArgs.put(RenderExcel.RA_ASYNC, false);
        render(excel);
    }

    /**
     * 下载采购单综合Excel表格
     */
    public static void deliveryments(DeliveryPost p) {
        List<Deliveryment> deliverymentList = p.query();
        if(deliverymentList != null && deliverymentList.size() != 0) {
            SimpleDateFormat formatter = new SimpleDateFormat("yyyyMMdd");
            request.format = "xls";
            renderArgs.put(RenderExcel.RA_FILENAME,
                    String.format("%s-%s采购单.xls", formatter.format(p.from), formatter.format(p.to)));
            renderArgs.put(RenderExcel.RA_ASYNC, false);
            renderArgs.put("dateFormat", formatter);
            render(deliverymentList);
        } else {
            renderText("没有数据无法生成Excel文件！");
        }
    }

    /**
     * 下载选定的采购计划的出货单
     *
     * @param id   采购单 id
     * @param pids 选定的采购计划的 id 集合
     * @throws Exception
     */
    public static synchronized void procureunitsOrder(String id, List<Long> pids) throws Exception {
        if(pids == null || pids.size() == 0)
            Validation.addError("", "必须选择需要下载的采购计划");
        if(Validation.hasErrors()) {
            Webs.errorToFlash(flash);
            Deliveryments.show(id);
        }
        List<ProcureUnit> units = ProcureUnit.find("id IN " + JpqlSelect.inlineParam(pids)).fetch();
        StringBuffer pidstr = new StringBuffer();
        for(Long pid : pids) {
            pidstr.append(pid.toString()).append("，");
        }
        SimpleDateFormat formatter = new SimpleDateFormat("yyyyMMdd");
        request.format = "xls";
        renderArgs.put(RenderExcel.RA_FILENAME,
                String.format("%s出货计划.xls", pidstr.toString()));
        renderArgs.put(RenderExcel.RA_ASYNC, false);
        renderArgs.put("dateFormat", formatter);
        renderArgs.put("dmt", Deliveryment.findById(id));
        render(units);
    }


    /**
     * 下载采购单综合Excel表格
     */
    public static void analyzes(AnalyzePost p) {
        List<AnalyzeDTO> dtos = p.query();
        if(dtos != null && dtos.size() != 0) {
            SimpleDateFormat formatter = new SimpleDateFormat("yyyyMMdd");
            request.format = "xls";
            renderArgs.put(RenderExcel.RA_FILENAME,
                    String.format("%s-%s销售分析.xls", formatter.format(p.from), formatter.format(p.to)));
            renderArgs.put(RenderExcel.RA_ASYNC, false);
            renderArgs.put("dateFormat", formatter);
            render(dtos);
        } else {
            renderText("没有数据无法生成Excel文件！");
        }
    }
}
