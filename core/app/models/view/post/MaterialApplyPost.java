package models.view.post;

import helper.Dates;
import models.finance.Apply;
import models.finance.ProcureApply;
import models.material.MaterialApply;
import org.apache.commons.lang.StringUtils;
import org.joda.time.DateTime;
import play.db.jpa.GenericModel.JPAQuery;
import play.libs.F;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Created by IntelliJ IDEA.
 * User: wyatt
 * Date: 4/2/13
 * Time: 3:25 PM
 */
public class MaterialApplyPost extends Post<Apply> {

    private static final long serialVersionUID = -1524975991851751905L;
    private static final Pattern NUM = Pattern.compile("^[0-9]*$");

    public MaterialApplyPost() {
        DateTime now = DateTime.now(Dates.timeZone(null));
        this.from = now.minusMonths(2).toDate();
        this.to = now.toDate();
        this.dateType = ProcreApplyPost.DateType.CREATE;
        this.perSize = 25;
    }

    public MaterialApplyPost(int perSize) {
        this.perSize = perSize;
    }
    public Date from;
    public Date to;
    public ProcreApplyPost.DateType dateType;
    public Long supplierId;


    public enum DateType {

        CREATE {
            @Override
            public String label() {
                return "创建时间";
            }
        },
        UPDATE {
            @Override
            public String label() {
                return "更新时间";
            }
        };

        public abstract String label();
    }


    @Override
    public F.T2<String, List<Object>> params() {
        StringBuilder sql = new StringBuilder("SELECT DISTINCT p FROM MaterialApply p LEFT JOIN p.materialPlans d ");
        sql.append(" LEFT JOIN d.units u WHERE 1=1 ");
        List<Object> params = new ArrayList<>();

        if(this.dateType != null) {
            if(this.dateType == ProcreApplyPost.DateType.CREATE) {
                sql.append(" AND p.createdAt>=?  AND p.createdAt <=?");
            } else {
                sql.append(" AND p.updateAt>=? AND p.updateAt<=?");
            }
            params.add(Dates.morning(this.from));
            params.add(Dates.night(this.to));
        }
        if(this.supplierId != null) {
            sql.append(" AND p.cooperator.id=? ");
            params.add(this.supplierId);
        }

        /** 模糊查询参数 **/
        if(StringUtils.isNotBlank(this.search)) {
            String word = this.word();
            sql.append(" AND (")
                    .append(" p.serialNumber LIKE ?")
                    .append(" OR u.material.code LIKE ?")
                    .append(")");
            for(int i = 0; i < 2; i++) {
                params.add(word);
            }
        }

        sql.append(" AND p.status <> ? ");
        params.add(ProcureApply.S.CLOSE);
        sql.append(" ORDER BY p.createdAt DESC ");
        return new F.T2<>(sql.toString(), params);
    }

    public List<Apply> query() {
        F.T2<String, List<Object>> params = params();
        JPAQuery query = MaterialApply.find(params._1, params._2.toArray());
        this.count = query.fetch().size();
        return query.fetch(this.page, this.perSize);
    }

    private Long isNumForSearch() {
        if(StringUtils.isNotBlank(this.search)) {
            Matcher matcher = NUM.matcher(this.search);
            if(matcher.find()) return Long.parseLong(matcher.group(0));
        }
        return null;
    }

    @Override
    public Long count(F.T2<String, List<Object>> params) {
        return MaterialApply.count(params._1, params._2.toArray());
    }

    @Override
    public Long getTotalCount() {
        return MaterialApply.count();
    }
}
