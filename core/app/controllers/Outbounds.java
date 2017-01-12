package controllers;

import controllers.api.SystemOperation;
import helper.Webs;
import models.OperatorConfig;
import models.User;
import models.procure.Cooperator;
import models.procure.ProcureUnit;
import models.view.post.OutboundPost;
import models.whouse.Inbound;
import models.whouse.InboundUnit;
import models.whouse.Outbound;
import models.whouse.Whouse;
import org.allcolor.yahp.converter.IHtmlToPdfTransformer;
import play.data.validation.Validation;
import play.db.helper.JpqlSelect;
import play.db.helper.SqlSelect;
import play.modules.pdf.PDF;
import play.mvc.Before;
import play.mvc.Controller;
import play.mvc.With;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Map;

import static play.modules.pdf.PDF.renderPDF;

/**
 * Created by licco on 2016/11/30.
 */
@With({GlobalExceptionHandler.class, Secure.class, SystemOperation.class})
public class Outbounds extends Controller {

    @Before(only = {"index", "edit", "blank"})
    public static void beforeIndex() {
        List<Cooperator> cooperators = Cooperator.suppliers();
        renderArgs.put("cooperators", cooperators);
        renderArgs.put("whouses", Whouse.selfWhouses(false));
        renderArgs.put("tragets", Whouse.find("type!=?", Whouse.T.FORWARD).fetch());
        renderArgs.put("shippers", Cooperator.shippers());
        renderArgs.put("suppliers", Cooperator.suppliers());
        renderArgs.put("users", User.findAll());
    }

    public static void index(OutboundPost p) {
        if(p == null) p = new OutboundPost();
        List<Outbound> outbounds = p.query();
        render(p, outbounds);
    }

    public static void edit(String id) {
        Outbound outbound = Outbound.findById(id);
        render(outbound);
    }

    public static void blank(List<Long> pids) {
        List<ProcureUnit> units = ProcureUnit.find("id IN " + JpqlSelect.inlineParam(pids)).fetch();
        ProcureUnit proUnit = units.get(0);
        Outbound outbound = new Outbound(proUnit);
        render(units, proUnit, outbound);
    }

    public static void create(Outbound outbound, List<Long> pids) {
        outbound.create(pids);
        index(new OutboundPost());
    }

    public static void update(Outbound outbound) {
        outbound.projectName = outbound.isb2b ? "B2B" : OperatorConfig.getVal("brandname");
        outbound.save();
        flash.success("更新成功!");
        index(new OutboundPost());
    }

    public static void confirmOutBound(List<String> ids) {
        Outbound.confirmOutBound(ids);
        if(Validation.hasErrors()) {
            Webs.errorToFlash(flash);
            index(new OutboundPost());
        }
        flash.success(SqlSelect.inlineParam(ids) + "出库成功!");
        index(new OutboundPost());
    }

    public static void printOutboundForm(List<String> ids) {
        final PDF.Options options = new PDF.Options();
        SimpleDateFormat formatter = new SimpleDateFormat("yyyyMMdd");
        options.filename = "PTC" + formatter.format(new Date()) + ".pdf";
        options.pageSize = IHtmlToPdfTransformer.A4P;
        List<Outbound> outbounds = Outbound.find("id IN " + SqlSelect.inlineParam(ids)).fetch();
        Map<Integer, List<ProcureUnit>> ten = ProcureUnit.pageNumForTen(outbounds);
        renderPDF(options, ten);
    }

    public static void showProcureUnitList(String id) {
        Outbound outbound = Outbound.findById(id);
        List<ProcureUnit> units = outbound.units;
        render("/Outbounds/_units.html", units);
    }

}
