package models.view.post;

import helper.Dates;
import models.procure.ProcureUnit;
import org.apache.commons.lang.StringUtils;
import org.apache.commons.lang.math.NumberUtils;
import org.joda.time.DateTime;
import play.libs.F;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Created by IntelliJ IDEA.
 * User: wyattpan
 * Date: 9/3/12
 * Time: 4:32 PM
 */
public class ProcurePost extends Post {
    private static final Pattern ID = Pattern.compile("^id:(\\d*)$");
    public static final List<F.T2<String, String>> DATE_TYPES;

    static {
        DATE_TYPES = new ArrayList<F.T2<String, String>>();
        DATE_TYPES.add(new F.T2<String, String>("createDate", "创建时间"));
        DATE_TYPES.add(new F.T2<String, String>("planArrivDate", "预计 [到库] 时间"));
        DATE_TYPES.add(new F.T2<String, String>("planShipDate", "预计 [发货] 时间"));
        DATE_TYPES.add(new F.T2<String, String>("planDeliveryDate", "预计 [交货] 时间"));
        DATE_TYPES.add(new F.T2<String, String>("arriveDate", "实际 [到库] 时间"));
        DATE_TYPES.add(new F.T2<String, String>("shipDate", "实际 [发货] 时间"));
        DATE_TYPES.add(new F.T2<String, String>("deliveryDate", "实际 [交货] 时间"));
    }


    public long whouseId;

    public long cooperatorId;

    public ProcureUnit.STAGE stage;

    public boolean isPlaced = false;

    /**
     * 选择过滤的日期类型
     */
    public String dateType;

    public ProcurePost() {
        this.from = DateTime.now().minusMonths(2).toDate();
        this.to = new Date();
    }

    public List<ProcureUnit> query() {
        F.T2<String, List<Object>> params = params();
        return ProcureUnit.find(params._1 + " ORDER BY createDate DESC", params._2.toArray()).fetch();
    }

    public F.T2<String, List<Object>> params() {
        StringBuilder sbd = new StringBuilder();
        List<Object> params = new ArrayList<Object>();

        long procrueId = isSearchForId();
        if(procrueId > 0) {
            sbd.append("id=?");
            params.add(procrueId);
        } else {
            if(StringUtils.isBlank(this.dateType)) this.dateType = "planDeliveryDate";
            sbd.append(this.dateType).append(">=?").append(" AND ").append(this.dateType).append("<=?");
            params.add(this.from);
            params.add(this.to);

            if(this.whouseId > 0) {
                sbd.append(" AND whouse.id=?");
                params.add(this.whouseId);
            }

            if(this.cooperatorId > 0) {
                sbd.append(" AND cooperator.id=? ");
                params.add(this.cooperatorId);
            }

            if(this.stage != null) {
                sbd.append(" AND stage=? ");
                params.add(this.stage);
            }

            if(this.isPlaced) {
                sbd.append(" AND isPlaced=? ");
                params.add(this.isPlaced);
            }

            if(StringUtils.isNotBlank(this.search)) {
                String word = String.format("%%%s%%", this.search.trim());
                sbd.append(" AND (")
                        .append("comment LIKE ? OR ")
                        .append("product.sku LIKE ? OR ")
                        .append("selling.sellingId LIKE ?")
                        .append(") ");
                for(int i = 0; i < 3; i++) params.add(word);
            }
        }

        return new F.T2<String, List<Object>>(sbd.toString(), params);
    }

    /**
     * 根据正则表达式搜索是否有类似 id:123 这样的搜索如果有则直接进行 id 搜索
     *
     * @return
     */
    private long isSearchForId() {
        if(StringUtils.isNotBlank(this.search)) {
            Matcher matcher = ID.matcher(this.search);
            if(matcher.find()) return NumberUtils.toLong(matcher.group(1));
        }
        return 0;
    }
}
