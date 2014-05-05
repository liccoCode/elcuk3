package models.view.post;

import helper.Dates;
import models.procure.ProcureUnit;
import models.procure.Shipment;
import org.apache.commons.lang.StringUtils;
import org.apache.commons.lang.math.NumberUtils;
import org.joda.time.DateTime;
import play.db.helper.SqlSelect;
import play.libs.F;

import java.util.ArrayList;
import java.util.Arrays;
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
public class ProcurePost extends Post<ProcureUnit> {
    private static final Pattern ID = Pattern.compile("^id:(\\d*)$");
    private static final Pattern FBA = Pattern.compile("^fba:(\\w*)$");
    public static final List<F.T2<String, String>> DATE_TYPES;

    static {
        DATE_TYPES = new ArrayList<F.T2<String, String>>();
        DATE_TYPES.add(new F.T2<String, String>("createDate", "创建时间"));
        DATE_TYPES.add(new F.T2<String, String>("attrs.planDeliveryDate", "预计 [交货] 时间"));
        DATE_TYPES.add(new F.T2<String, String>("attrs.deliveryDate", "实际 [交货] 时间"));
        DATE_TYPES.add(new F.T2<String, String>("attrs.planArrivDate", "预计 [到库] 时间"));
        DATE_TYPES.add(new F.T2<String, String>("attrs.planShipDate", "预计 [发货] 时间"));
    }

    /**
     * 在 ProcureUnits中，planView 和noPlaced 方法 需要调用 index，必须重写，否则总是构造方法中的时间
     */
    public Date from;
    public Date to;

    public long whouseId;

    public long cooperatorId;

    public ProcureUnit.STAGE stage;

    public PLACEDSTATE isPlaced;

    public Shipment.T shipType;

    public String unitIds;
    /**
     * 选择过滤的日期类型
     */
    public String dateType;

    /**
     * 在 ProcureUnits中，downloadFBAZIP 方法 需要调用 传入 POST 查询条件， PLay 无法解析父类的属性，必须重写
     */
    public String search;

    public enum PLACEDSTATE {
        ARRIVE {
            @Override
            public String label() {
                return "抵达货代处";
            }
        },
        NOARRIVE {
            @Override
            public String label() {
                return "未抵达货代处";
            }

        };

        public abstract String label();
    }


    public ProcurePost() {
        this.from = DateTime.now().minusDays(25).toDate();
        this.to = new Date();
        this.stage = ProcureUnit.STAGE.DONE;
        this.dateType = "createDate";
    }

    public ProcurePost(ProcureUnit.STAGE stage) {
        this();
        this.stage = stage;
    }

    public List<ProcureUnit> query() {
        F.T2<String, List<Object>> params = params();
        return ProcureUnit.find(params._1 + " ORDER BY createDate DESC", params._2.toArray()).fetch();
    }

    @Override
    public Long count(F.T2<String, List<Object>> params) {
        return ProcureUnit.count("SELECT COUNT(*) FROM ProcureUnit WHERE " + params._1,
                params._2.toArray()
        );
    }

    public F.T2<String, List<Object>> params() {
        StringBuilder sbd = new StringBuilder();
        List<Object> params = new ArrayList<Object>();

        Long procrueId = isSearchForId();
        if(procrueId != null) {
            sbd.append("id=?");
            params.add(procrueId);
            return new F.T2<String, List<Object>>(sbd.toString(), params);
        }

        String fba = isSearchFBA();
        if(fba != null) {
            sbd.append("fba.shipmentId=?");
            params.add(fba);
            return new F.T2<String, List<Object>>(sbd.toString(), params);
        }

        if(StringUtils.isBlank(this.dateType)) this.dateType = "attrs.planDeliveryDate";
        sbd.append(this.dateType).append(">=?").append(" AND ").append(this.dateType)
                .append("<=?");
        params.add(Dates.morning(this.from));
        params.add(Dates.night(this.to));

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

        if(this.shipType != null) {
            sbd.append(" AND shipType=? ");
            params.add(this.shipType);
        }

        if(this.isPlaced != null) {
            sbd.append(" AND isPlaced=? ");
            params.add(this.isPlaced == PLACEDSTATE.ARRIVE);
        }

        if(StringUtils.isNotBlank(this.search)) {
            String word = this.word();
            sbd.append(" AND (")
                    .append("product.sku LIKE ? OR ")
                    .append("selling.sellingId LIKE ?")
//                        .append("fba.shipmentId LIKE ?")
                    .append(") ");
            for(int i = 0; i < 2; i++) params.add(word);
        }
        if(StringUtils.isNotBlank(this.unitIds)) {
            List<String> unitIdList = Arrays.asList(StringUtils.split(this.unitIds, "_"));
            sbd.append(" AND id IN " + SqlSelect.inlineParam(unitIdList));
        }

        return new F.T2<String, List<Object>>(sbd.toString(), params);
    }

    /**
     * 根据正则表达式搜索是否有类似 id:123 这样的搜索如果有则直接进行 id 搜索
     *
     * @return
     */
    private Long isSearchForId() {
        if(StringUtils.isNotBlank(this.search)) {
            Matcher matcher = ID.matcher(this.search);
            if(matcher.find()) return NumberUtils.toLong(matcher.group(1));
        }
        return null;
    }

    /**
     * 根据正则表达式搜索是否有类似 id:123 这样的搜索如果有则直接进行 id 搜索
     *
     * @return
     */
    private String isSearchFBA() {
        if(StringUtils.isNotBlank(this.search)) {
            Matcher matcher = FBA.matcher(this.search);
            if(matcher.find()) return matcher.group(1);
        }
        return null;
    }
}
