package controllers;

import controllers.api.SystemOperation;
import models.procure.Cooperator;
import models.view.post.RefundPost;
import models.whouse.InboundUnit;
import models.whouse.Refund;
import models.whouse.RefundUnit;
import models.whouse.Whouse;
import org.allcolor.yahp.converter.IHtmlToPdfTransformer;
import play.db.helper.SqlSelect;
import play.modules.pdf.PDF;
import play.mvc.Before;
import play.mvc.Controller;
import play.mvc.With;

import java.util.List;
import java.util.Map;

import static play.modules.pdf.PDF.renderPDF;

/**
 * Created by licco on 2016/11/28.
 */
@With({GlobalExceptionHandler.class, Secure.class, SystemOperation.class})
public class Refunds extends Controller {

    @Before(only = {"index", "edit"})
    public static void beforeIndex() {
        List<Cooperator> cooperators = Cooperator.suppliers();
        renderArgs.put("cooperators", cooperators);
        renderArgs.put("whouses", Whouse.selfWhouses(false));
    }

    public static void index(RefundPost p) {
        if(p == null) p = new RefundPost();
        List<Refund> refunds = p.query();
        render(p, refunds);
    }

    public static void edit(String id) {
        Refund refund = Refund.findById(id);
        render(refund);
    }

    public static void update(Refund refund) {
        refund.save();
        flash.success("退货单【" + refund.id + "】更新成功!");
        index(new RefundPost());

    }

    public static void confirmRefund(List<String> ids) {
        Refund.confirmRefund(ids);
        flash.success(SqlSelect.inlineParam(ids) + "出库成功!");
        index(new RefundPost());
    }

    public static void printRefundForm(List<String> ids) {
        final PDF.Options options = new PDF.Options();
        options.pageSize = IHtmlToPdfTransformer.A4L;
        List<Refund> refunds = Refund.find("id IN " + SqlSelect.inlineParam(ids)).fetch();
        Map<Integer, List<RefundUnit>> ten = RefundUnit.pageNumForTen(refunds);
        renderPDF(options, ten);
    }

}
