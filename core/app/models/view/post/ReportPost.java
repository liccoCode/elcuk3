package models.view.post;

import models.ReportRecord;
import play.db.helper.SqlSelect;
import play.libs.F;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

/**
 * 报表相关的搜索 Model
 * User: mac
 * Date: 4/5/14
 * Time: 6:59 PM
 */
public class ReportPost extends Post<ReportRecord> {
    private static final long serialVersionUID = -4955491389269838111L;

    public ReportPost() {
        this.year = LocalDate.now().getYear();
        this.perSize = 10;
        this.page = 1;
    }

    public int year;
    public int month;

    public ReportRecord.RT reporttype;

    /**
     * 在未指定具体某一种报表类型的情况下用来区分销售报表与财务报表大的报表类型
     */
    public List<ReportRecord.RT> reportTypes = new ArrayList<>();

    /**
     * 所有销售报表的报表类型
     *
     * @return
     */
    public static List<ReportRecord.RT> saleReportTypes() {
        return Arrays.asList(ReportRecord.RT.SKUMONTHALL, ReportRecord.RT.SKUMONTHCATEGORY, ReportRecord.RT.SKUINVTOTAL,
                ReportRecord.RT.SKUINVSELLING, ReportRecord.RT.SALEYEARTOTAL, ReportRecord.RT.SALEYEARCATEGORY,
                ReportRecord.RT.INVENTORYRATIANALITY, ReportRecord.RT.SELLINGCYCLE, ReportRecord.RT.INVRNTORYCOST);
    }

    public static List<ReportRecord.RT> shipmentMonthlyTypes() {
        return Collections.singletonList(ReportRecord.RT.SHIPMENTMONTHLY);
    }

    /**
     * 所有财务报表的报表类型
     *
     * @return
     */
    public static List<ReportRecord.RT> applyReportTypes() {
        return Arrays.asList(ReportRecord.RT.REVENUEANDCOST, ReportRecord.RT.SALESFEELIST, ReportRecord.RT.PAYBILLDETAIL,
                ReportRecord.RT.ANALYZEREPORT);
    }

    public static List<ReportRecord.RT> procureReportTypes() {
        return Arrays.asList(ReportRecord.RT.PROCURECOSTANALYSIS);
    }

    public List<ReportRecord> query() {
        F.T2<String, List<Object>> params = params();
        this.count = ReportRecord.find(params._1, params._2.toArray()).fetch().size();
        return ReportRecord.find(params._1 + " ORDER BY createAt DESC", params._2.toArray())
                .fetch(this.page, this.perSize);
    }

    @Override
    public Long count(F.T2<String, List<Object>> params) {
        return this.count;
    }

    public Long getTotalCount() {
        return this.count;
    }

    @Override
    public F.T2<String, List<Object>> params() {
        StringBuilder sbd = new StringBuilder();
        List<Object> params = new ArrayList<>();
        sbd.append(" 1=1 ");
        if(this.year != 0) {
            sbd.append("AND year=?");
            params.add(this.year);
        }
        if(this.month != 0) {
            sbd.append("AND month=?");
            params.add(this.month);
        }
        if(this.reporttype != null) {
            sbd.append("AND reporttype=?");
            params.add(this.reporttype);
        } else {
            sbd.append("AND reporttype IN ").append(SqlSelect.inlineParam(this.reportTypes));
        }
        return new F.T2<>(sbd.toString(), params);
    }

}
