package controllers;

import models.view.dto.SaleReportDTO;
import models.view.post.SaleReportPost;
import play.mvc.Controller;

import java.util.List;

/**
 * 销售报表控制器
 * User: mac
 * Date: 14-6-9
 * Time: AM10:12
 */
public class SaleReports extends Controller {

    /**
     * 产品的销售统计报表
     */
    public static void saleCount() {
        SaleReportPost p = new SaleReportPost();
        List<SaleReportDTO> dtos = p.query();
        render(p, dtos);
    }
}
