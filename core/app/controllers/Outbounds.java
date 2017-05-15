package controllers;

import controllers.api.SystemOperation;
import helper.Webs;
import models.OperatorConfig;
import models.User;
import models.procure.Cooperator;
import models.procure.ProcureUnit;
import models.view.Ret;
import models.view.post.OutboundPost;
import models.whouse.Outbound;
import models.whouse.StockRecord;
import models.whouse.Whouse;
import org.allcolor.yahp.converter.IHtmlToPdfTransformer;
import org.apache.commons.lang.StringUtils;
import play.data.validation.Validation;
import play.db.helper.JpqlSelect;
import play.db.helper.SqlSelect;
import play.modules.pdf.PDF;
import play.mvc.Before;
import play.mvc.Controller;
import play.mvc.With;

import java.text.SimpleDateFormat;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import static play.modules.pdf.PDF.renderPDF;

/**
 * Created by IntelliJ IDEA.
 * User: licco
 * Date: 2016/11/30
 * Time: 上午10:11
 */
@With({GlobalExceptionHandler.class, Secure.class, SystemOperation.class})
public class Outbounds extends Controller {

    @Before(only = {"index", "edit", "blank", "otherBlank"})
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
        p.flag = "Other";
        List<Outbound> others = p.query();
        p.flag = "B2B";
        List<Outbound> b2bOutbounds = p.queryForB2B();
        render(p, outbounds, others, b2bOutbounds);
    }

    public static void edit(String id) {
        Outbound outbound = Outbound.findById(id);
        String brandName = OperatorConfig.getVal("brandname");
        render(outbound, brandName);
    }

    public static void blank(List<Long> pids) {
        List<ProcureUnit> units = ProcureUnit.find("id IN " + JpqlSelect.inlineParam(pids)).fetch();
        ProcureUnit proUnit = units.get(0);
        Outbound outbound = new Outbound(proUnit);
        render(units, proUnit, outbound);
    }

    public static void otherBlank() {
        String name = String.format("%s_其它出库", LocalDate.now());
        render(name);
    }

    public static void create(Outbound outbound, List<Long> pids) {
        outbound.create(pids);
        index(new OutboundPost());
    }

    /**
     * 其它出库
     *
     * @param outbound
     * @param dtos
     */
    public static void otherCreate(Outbound outbound, List<StockRecord> dtos) {
        outbound.createOther(dtos);
        index(new OutboundPost());
    }

    public static void update(Outbound outbound) {
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
        options.pageSize = IHtmlToPdfTransformer.A4L;
        List<Outbound> outbounds = Outbound.find("id IN " + SqlSelect.inlineParam(ids)).fetch();
        if(outbounds.get(0).type.name().equals("Normal")) {
            Map<Integer, List<ProcureUnit>> ten = ProcureUnit.pageNumForTen(outbounds);
            renderPDF(options, ten);
        } else {
            Map<Integer, List<StockRecord>> ten = StockRecord.pageNumForTen(outbounds);
            renderPDF(options, ten);
        }
    }

    public static void showProcureUnitList(String id) {
        Outbound outbound = Outbound.findById(id);
        List<ProcureUnit> units = outbound.units;
        render("/Outbounds/_units.html", units);
    }

    public static void showStockRecordList(String id) {
        Outbound outbound = Outbound.findById(id);
        List<StockRecord> records = outbound.records;
        render("/Outbounds/_records.html", records);
    }

    public static void addUnits(String outId, List<Long> pids) {
        Outbound out = Outbound.findById(outId);
        if(pids == null || pids.size() == 0) {
            flash.error("请先勾选需要添加的采购计划");
            edit(outId);
        }
        out.addUnits(pids);
        flash.success("成功将 %s 采购计划添加到当前出库单.", StringUtils.join(pids, ","));
        edit(outId);
    }

    public static void validOutboundQty(String[] ids) {
        List<Outbound> list = Outbound.find("id IN " + SqlSelect.inlineParam(ids)).fetch();
        List<Long> temp = new ArrayList<>();
        for(Outbound out : list) {
            temp.addAll(out.units.stream().filter(unit -> unit.totalOutBoundQty() < unit.availableQty)
                    .map(unit -> unit.id).collect(Collectors.toList()));
        }
        if(temp.size() > 0) {
            renderJSON(new Ret(false, SqlSelect.inlineParam(temp)));
        }
        renderJSON(new Ret(true));
    }

}
