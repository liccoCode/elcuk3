package models.view.post;

import models.finance.BatchReviewApply;
import org.apache.commons.lang.StringUtils;
import play.libs.F;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by IntelliJ IDEA.
 * User: licco
 * Date: 2017/7/29
 * Time: 下午6:21
 */
public class BatchApplyPost extends Post<BatchReviewApply> {

    private static final long serialVersionUID = -1785846471942379798L;

    public BatchReviewApply.S status;
    public BatchReviewApply.W way;

    public Long cooperatorId;


    public List<BatchReviewApply> query() {
        F.T2<String, List<Object>> params = params();
        this.count = BatchReviewApply.find(params._1, params._2.toArray()).fetch().size();
        return BatchReviewApply.find(params._1, params._2.toArray()).fetch(this.page, this.perSize);
    }

    @Override
    public F.T2<String, List<Object>> params() {
        StringBuilder sbd = new StringBuilder("SELECT DISTINCT a FROM BatchReviewApply a LEFT JOIN a.paymentList p");
        sbd.append(" WHERE 1=1");
        List<Object> params = new ArrayList<>();
        if(status != null) {
            sbd.append(" AND a.status = ? ");
            params.add(status);
        }
        if(way != null) {
            sbd.append(" AND a.way = ? ");
            params.add(way);
        }
        if(cooperatorId != null) {
            sbd.append(" AND a.cooperator.id = ? ");
            params.add(cooperatorId);
        }
        if(StringUtils.isNotBlank(this.search)) {
            sbd.append(" AND (a.id LIKE ? OR a.name LIKE ? OR p.paymentNumber LIKE ? )");
            params.add(this.word());
            params.add(this.word());
            params.add(this.word());
        }
        sbd.append(" ORDER BY a.createDate DESC ");
        return new F.T2<>(sbd.toString(), params);
    }

    @Override
    public Long getTotalCount() {
        return this.count;
    }

    @Override
    public Long count(F.T2<String, List<Object>> params) {
        return this.count;
    }
}
