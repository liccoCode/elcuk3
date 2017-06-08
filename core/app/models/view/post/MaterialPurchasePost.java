package models.view.post;

import helper.Dates;
import models.material.MaterialPurchase;
import models.procure.Deliveryment;
import org.apache.commons.lang.StringUtils;
import org.joda.time.DateTime;
import play.libs.F;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

/**
 * Created by IntelliJ IDEA.
 * User: licco
 * Date: 2017/5/31
 * Time: 下午5:39
 */
public class MaterialPurchasePost extends Post<MaterialPurchase> {

    private static final long serialVersionUID = 503452890747084694L;

    public MaterialPurchasePost() {
        DateTime now = DateTime.now(Dates.timeZone(null));
        this.from = now.minusDays(7).toDate();
        this.to = now.toDate();
        this.dateType = DateType.CREATE;
        this.perSize = 25;
    }


    public Date from;
    public Date to;
    public Deliveryment.S state;
    public Long cooperId;

    public DateType dateType;
    public Long materialId;


    public enum DateType {
        /**
         * 创建时间
         */
        CREATE {
            @Override
            public String label() {
                return "创建时间";
            }
        },
        /**
         * 预计交货时间
         */
        PLAYDELIVERY {
            @Override
            public String label() {
                return "预计交货时间";
            }
        },
        /**
         * 实际交货时间
         */
        DELIVERY {
            @Override
            public String label() {
                return "实际交货时间";
            }
        };

        public abstract String label();
    }

    public List<MaterialPurchase> query() {
        F.T2<String, List<Object>> params = params();
        this.count = MaterialPurchase.find(params._1, params._2.toArray()).fetch().size();
        return MaterialPurchase.find(params._1, params._2.toArray()).fetch(this.page, this.perSize);
    }

    @Override
    public Long getTotalCount() {
        return this.count;
    }

    @Override
    public Long count(F.T2<String, List<Object>> params) {
        return this.count;
    }


    @Override
    public F.T2<String, List<Object>> params() {
        StringBuilder sbd = new StringBuilder("SELECT DISTINCT  m FROM MaterialPurchase m LEFT JOIN m.units u WHERE " +
                "1=1 AND");
        List<Object> params = new ArrayList<>();

        /** 时间参数 **/
        if(this.dateType != null) {
            if(this.dateType == DateType.PLAYDELIVERY) {
                sbd.append(" u.planDeliveryDate>=? AND u.planDeliveryDate<=?");
            } else if(this.dateType == DateType.DELIVERY) {
                sbd.append(" u.deliveryDate>=? AND u.deliveryDate<=?");
            } else {
                sbd.append(" m.createDate>=? AND m.createDate<=?");
            }
            params.add(Dates.morning(this.from));
            params.add(Dates.night(this.to));
        }

        /** 状态参数 **/
        if(this.state != null) {
            sbd.append(" AND m.state=?");
            params.add(this.state);
        }

        /** 供应商参数 **/
        if(this.cooperId != null && this.cooperId > 0) {
            sbd.append(" AND m.cooperator.id=?");
            params.add(this.cooperId);
        }

        /** 物料类型参数 **/
        if(this.materialId != null && this.materialId > 0) {
            sbd.append(" AND u.material.id=?");
            params.add(this.materialId);
        }

        /** 模糊查询参数 **/
        if(StringUtils.isNotBlank(this.search)) {
            String word = this.word();
            sbd.append(" AND (")
                    .append(" m.id LIKE ?")
                    .append(" OR u.material.code LIKE ?")
                    .append(")");
            for(int i = 0; i < 2; i++) {
                params.add(word);
            }
        }


        return new F.T2<>(sbd.toString(), params);
    }
}
