package models.view.post;

import models.procure.ProcureUnit;
import models.whouse.Whouse;
import org.apache.commons.lang.StringUtils;
import org.apache.commons.lang.math.NumberUtils;
import play.libs.F;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Created by licco on 2016/12/8.
 */
public class StockPost extends Post<ProcureUnit> {

    private static final Pattern ID = Pattern.compile("^id:(\\d*)$");
    public Whouse whouse;
    public String projectName;

    @Override
    public F.T2<String, List<Object>> params() {
        StringBuilder sbd = new StringBuilder("1=1");
        List<Object> params = new ArrayList<>();

        Long unit_id = isSearchForId();
        if(unit_id != null) {
            sbd.append(" AND id=?");
            params.add(unit_id);
            return new F.T2<>(sbd.toString(), params);
        }

        if(this.whouse != null && this.whouse.id != null) {
            sbd.append(" AND currWhouse.id=?");
            params.add(this.whouse.id);
        }
        if(StringUtils.isNotBlank(this.projectName)) {
            sbd.append(" AND projectName=?");
            params.add(this.projectName);
        }
        if(StringUtils.isNotBlank(this.search)) {
            sbd.append(" AND (product.sku LIKE ? OR fba.shipmentId LIKE ? )");
            for(int i = 0; i < 2; i++) params.add(this.word());
        }
        return new F.T2<>(sbd.toString(), params);
    }

    /**
     * 库存查询
     *
     * @return
     */
    public List<ProcureUnit> query() {
        F.T2<String, List<Object>> params = this.params();
        this.count = this.count(params);
        String sql = params._1 + " ORDER BY currWhouse.id ASC, createDate DESC";
        return ProcureUnit.find(sql, params._2.toArray()).fetch(this.page, this.perSize);
    }


    @Override
    public Long count(F.T2<String, List<Object>> params) {
        return ProcureUnit.count(params._1, params._2.toArray());
    }

    @Override
    public Long getTotalCount() {
        return this.count();
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
}
