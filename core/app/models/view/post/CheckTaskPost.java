package models.view.post;

import helper.Dates;
import models.CategoryAssignManagement;
import models.User;
import models.procure.Shipment;
import models.qc.CheckTask;
import org.apache.commons.lang.StringUtils;
import org.apache.commons.lang.math.NumberUtils;
import org.joda.time.DateTime;
import play.db.helper.SqlSelect;
import play.libs.F;

import java.util.ArrayList;
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
    public static Pattern NUMBER_PATTEN = Pattern.compile("^\\d+$");
    public static final List<F.T2<String, String>> DATE_TYPES;

    static {
        DATE_TYPES = new ArrayList<>();
        DATE_TYPES.add(new F.T2<>("creatat", "创建时间"));
        DATE_TYPES.add(new F.T2<>("endTime", "确认时间"));
        DATE_TYPES.add(new F.T2<>("units.attrs.planShipDate", "预计运输时间"));
    }

    public long cooperatorId;

    /**
     * 选择过滤的日期类型
     */
    public String dateType;

    /**
     * 检测人
     */
    public String checkor;

    /**
     * 是否合格结果
     */
    public CheckTask.ResultType result;

    /**
     * 状态
     */
    public CheckTask.StatType checkstat;

    /**
     * 是否超时
     */
    public String isTimeout;

    /**
     * 运输方式
     */
    public Shipment.T shipType;

    public CheckTaskPost() {
        DateTime now = DateTime.now();
        this.from = now.minusDays(2).toDate();
        this.to = now.toDate();
        this.checkor = User.username();
        this.dateType = "creatat";
        this.checkstat = CheckTask.StatType.UNCHECK;
        this.perSize = 30;
    }

    @Override
    public F.T2<String, List<Object>> params() {
        StringBuilder sbd = new StringBuilder("1=1");
        List<Object> params = new ArrayList<>();

        Long id = this.isSearchForId();
        if(id != null) {
            sbd.append(" AND id=?");
            params.add(id);
            return new F.T2<>(sbd.toString(), params);
        }

        sbd.append(String.format(" AND %s>=? AND %s<=?", this.dateType, this.dateType));
        params.add(Dates.morning(this.from));
        params.add(Dates.night(this.to));

        if(this.cooperatorId > 0) {
            sbd.append(" AND cooperator.id=? ");
            params.add(this.cooperatorId);
        }

        if(this.checkstat != null) {
            sbd.append(" AND checkstat=? ");
            params.add(this.checkstat);
        }

        if(this.result != null) {
            sbd.append(" AND result=? ");
            params.add(this.result);
        }

        if(StringUtils.isNotBlank(this.isTimeout)) {
            if("true".equals(this.isTimeout)) {
                sbd.append(" AND isTimeout=true");
            } else {
                sbd.append(" AND isTimeout=false OR isTimeout IS NULL");
            }
        }

        if(StringUtils.isNotBlank(this.checkor)) {
            List<String> categories = CategoryAssignManagement.showCategoryByUserName(this.checkor);
            if(categories != null && !categories.isEmpty()) {
                sbd.append(" AND units.product.category.categoryId IN ")
                        .append(SqlSelect.inlineParam(categories));
            }
        }

        if(StringUtils.isNotBlank(this.search)) {
            sbd.append(" AND (");
            Matcher matcher = NUMBER_PATTEN.matcher(this.search);
            if(matcher.find()) {
                sbd.append(" id=?").append(" OR units.id=? OR ");
                long searchId = NumberUtils.toLong(matcher.group(0));
                for(int i = 0; i < 2; i++) params.add(searchId);
            }

            String word = this.word();
            sbd.append(" receiveRecord.id = ?")
                    .append(" OR units.product.sku LIKE ?")
                    .append(" OR units.product.abbreviation LIKE ?")
                    .append(" OR units.selling.fnSku LIKE ?")
                    .append(") ");
            for(int i = 0; i < 4; i++) params.add(word);
        }

        sbd.append(String.format(" ORDER BY %s DESC", this.dateType));
        return new F.T2<>(sbd.toString(), params);
    }

    public List<CheckTask> query() {
        F.T2<String, List<Object>> params = params();
        this.count = this.count(params);
        return CheckTask.find(params._1, params._2.toArray()).fetch();
    }

    @Override
    public Long count(F.T2<String, List<Object>> params) {
        return CheckTask.count(params._1, params._2.toArray());
    }

    @Override
    public Long getTotalCount() {
        return this.count();
    }
}
