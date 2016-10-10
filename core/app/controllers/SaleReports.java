package controllers;

import controllers.api.SystemOperation;
import helper.Webs;
import models.view.dto.SaleReportDTO;
import models.view.post.SaleReportPost;
import play.mvc.Controller;
import play.mvc.With;
import play.utils.FastRuntimeException;

import java.util.ArrayList;
import java.util.List;

/**
 * 销售报表控制器
 * User: mac
 * Date: 14-6-9
 * Time: AM10:12
 * @deprecated
 */
@With({GlobalExceptionHandler.class, Secure.class,SystemOperation.class})
public class SaleReports extends Controller {

    /**
     * 产品的销售统计报表
     */
    @Check("salereports.salecount")
    public static void saleCount(SaleReportPost p) {
        List<SaleReportDTO> dtos = new ArrayList<SaleReportDTO>();
        try {
            if(p == null) p = new SaleReportPost();
            dtos = p.query();
        } catch(FastRuntimeException e) {
            flash.error(Webs.E(e));
        }
        render(p, dtos);
    }
}
