package models.view.post;

import helper.Dates;
import models.qc.CheckTask;
import models.CategoryAssignManagement;
import models.qc.SkuCheck;
import org.apache.commons.lang.StringUtils;
import org.apache.commons.lang.math.NumberUtils;
import org.joda.time.DateTime;
import play.db.helper.SqlSelect;
import play.libs.F;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Created by IntelliJ IDEA.
 * User: cary
 * Date: 5/4/14
 * Time: 3:25 PM
 */
public class CheckTaskPost extends Post<CheckTask> {


    private static final Pattern ID = Pattern.compile("^id:(\\d*)$");
    private static final Pattern SKU = Pattern.compile("^sku:(\\S*)$");


    public CheckTaskPost() {
        this.perSize = 25;
        this.from = new Date();
        this.to = DateTime.now().plusDays(30).toDate();
        this.dateType = "u.attrs.planShipDate";
    }

    public CheckTaskPost(int perSize) {
        this.perSize = perSize;
    }

    public CheckTaskPost(String checkor) {
        this.checkor = checkor;
        this.from = DateTime.now().minusDays(30).toDate();
        this.to = new Date();
        this.dateType = "u.attrs.planShipDate";
    }

    public static final List<F.T2<String, String>> DATE_TYPES;

    static {
        DATE_TYPES = new ArrayList<F.T2<String, String>>();
        DATE_TYPES.add(new F.T2<String, String>("u.attrs.planShipDate", "预计 [运输] 时间"));
        DATE_TYPES.add(new F.T2<String, String>("u.attrs.planDeliveryDate", "预计 [交货] 时间"));
        DATE_TYPES.add(new F.T2<String, String>("u.attrs.deliveryDate", "实际 [交货] 时间"));
        DATE_TYPES.add(new F.T2<String, String>("c.endTime", "质检完成时间"));
    }

    /**
     * 在 ProcureUnits中，planView 和noPlaced 方法 需要调用 index，必须重写，否则总是构造方法中的时间
     */
    public Date from;
    public Date to;

    /**
     * 采购计划仓库
     */
    public long whouseId;

    /**
     * 货代仓库
     */
    public long shipwhouseId;

    public long cooperatorId;

    /**
     * 选择过滤的日期类型
     */
    public String dateType;

    public String search;

    /**
     * 检测人
     */
    public String checkor;

    /**
     * 处理方式
     */
    public CheckTask.DealType dealway;

    /**
     * 是否合格结果
     */
    public CheckTask.ResultType result;
    /**
     * 是否发货
     */
    public CheckTask.ShipType isship;
    /**
     * 状态
     */
    public CheckTask.StatType checkstat;

    @Override
    public F.T2<String, List<Object>> params() {
        List<Object> params = new ArrayList<Object>();

        StringBuilder sbd = new StringBuilder(
                "SELECT DISTINCT c FROM CheckTask c LEFT JOIN c.units u WHERE 1=1 AND ");

        Long procrueId = isSearchForId();
        if(procrueId != null) {
            sbd.append("c.id=?");
            params.add(procrueId);
            return new F.T2<String, List<Object>>(sbd.toString(), params);
        }

        String sku = isSearchForSKU();
        if(StringUtils.isNotBlank(sku)) {
            sbd.append("u.product.sku=?");
            params.add(sku);
            return new F.T2<String, List<Object>>(sbd.toString(), params);
        }

        if(StringUtils.isBlank(this.dateType)) this.dateType = "u.attrs.planDeliveryDate";
        sbd.append(this.dateType).append(">=?").append(" AND ").append(this.dateType)
                .append("<=?");
        params.add(Dates.morning(this.from));
        params.add(Dates.night(this.to));

        if(this.shipwhouseId > 0) {
            sbd.append(" AND c.shipwhouse.id=?");
            params.add(this.shipwhouseId);
        }

        if(this.whouseId > 0) {
            sbd.append(" AND u.whouse.id=?");
            params.add(this.whouseId);
        }

        if(this.cooperatorId > 0) {
            sbd.append(" AND u.cooperator.id=? ");
            params.add(this.cooperatorId);
        }

        if(this.checkstat != null) {
            sbd.append(" AND c.checkstat=? ");
            params.add(this.checkstat);
        }

        if(this.dealway != null) {
            sbd.append(" AND c.dealway=? ");
            params.add(this.dealway);
        }

        if(this.result != null) {
            sbd.append(" AND c.result=? ");
            params.add(this.result);
        }

        if(this.isship != null) {
            sbd.append(" AND c.isship=? ");
            params.add(this.isship);
        }

        if(StringUtils.isNotBlank(this.search)) {
            String word = this.word();
            sbd.append(" AND (")
                    .append("c.id = ?")
                    .append("OR u.id = ?")
                    .append("OR u.product.sku LIKE ?")
                    .append(") ");
            for(int i = 0; i < 2; i++) params.add(NumberUtils.toLong(this.search));
            params.add(word);
        }

        if(StringUtils.isNotBlank(this.checkor)){
            if(CategoryAssignManagement.showCategoryByUserName(this.checkor).size() > 0) {
                sbd.append(" AND c.units.product.category.categoryId IN " + SqlSelect.inlineParam(
                        CategoryAssignManagement.showCategoryByUserName(this.checkor)));
            }else {
                sbd.append(" AND c.units.product.category.categoryId = 0 ");
            }
        }
        return new F.T2<String, List<Object>>(sbd.toString(), params);
    }

    /**
     * 查询待检任务、已检任务、重检任务
     *
     * @return
     */
    public F.T2<String, List<Object>> checkOrUncheckParams(F.T2<String, List<Object>> params, String flag) {
        String sbd = params._1;
        if(StringUtils.equalsIgnoreCase("check", flag)) {
            sbd += " AND c.checkstat =  '" + CheckTask.StatType.UNCHECK + "'";
        } else if(StringUtils.equalsIgnoreCase("checked", flag)) {
            sbd += " AND c.finishStat ='" + CheckTask.ConfirmType.CONFIRM + "'";
        } else {
            CheckTask.StatType[] checkstats = {
                    //CheckTask.StatType.CHECKNODEAL, CheckTask.StatType.CHECKDEAL,
                    CheckTask.StatType.REPEATCHECK};
            sbd += " AND c.checkstat IN " + SqlSelect.inlineParam(checkstats) + "";
        }
        return new F.T2<String, List<Object>>(sbd, params._2);
    }

    public List<CheckTask> query() {
        F.T2<String, List<Object>> params = params();
        this.count = this.count(params);
        return CheckTask.find(params._1 + "ORDER BY c.creatat DESC", params._2.toArray()).fetch();
    }

    public List<CheckTask> check() {
        F.T2<String, List<Object>> params = checkOrUncheckParams(params(), "check");
        return CheckTask.find(params._1 + "ORDER BY c.creatat DESC", params._2.toArray()).fetch();
    }

    public List<CheckTask> checked() {
        F.T2<String, List<Object>> params = checkOrUncheckParams(params(), "checked");
        return CheckTask.find(params._1 + "ORDER BY c.creatat DESC", params._2.toArray()).fetch();
    }

    public List<CheckTask> checkRepeat() {
        F.T2<String, List<Object>> params = checkOrUncheckParams(params(), "checkRepeat");
        return CheckTask.find(params._1 + "ORDER BY c.creatat DESC", params._2.toArray()).fetch();
    }

    @Override
    public Long count(F.T2<String, List<Object>> params) {
        return new Long(CheckTask.find(params._1 + "ORDER BY c.creatat DESC", params._2.toArray()).fetch().size());
    }

    @Override
    public Long getTotalCount() {
        return SkuCheck.count();
    }


    /**
     * 根据正则表达式搜索是否有类似 id:123 这样的搜索如果有则直接进行 id 搜索
     *
     * @return
     */
    private Long isSearchForId() {
        if(StringUtils.isNotBlank(this.search)) {
            Matcher matcher = ID.matcher(this.search);
            if(matcher.find()) {
                return NumberUtils.toLong(matcher.group(1));
            }
        }
        return null;
    }

    /**
     * 根据正则表达式搜索是否有类似 id:123 这样的搜索如果有则直接进行 id 搜索
     *
     * @return
     */
    private String isSearchForSKU() {
        if(StringUtils.isNotBlank(this.search)) {
            Matcher matcher = SKU.matcher(this.search);
            if(matcher.find()) {
                return matcher.group(1);
            }
        }
        return null;
    }
}
