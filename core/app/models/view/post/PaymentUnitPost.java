package models.view.post;

import models.finance.PaymentUnit;
import play.libs.F;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by IntelliJ IDEA.
 * User: duan
 * Date: 1/13/17
 * Time: 3:22 PM
 */
public class PaymentUnitPost extends Post<PaymentUnit> {
    public Long paymentId;

    public PaymentUnitPost() {
        this.perSize = 50;
    }

    public PaymentUnitPost(long paymentId, int page) {
        this.perSize = 50;
        this.page = page;
        this.paymentId = paymentId;
    }

    @Override
    public F.T2<String, List<Object>> params() {
        StringBuilder sql = new StringBuilder();
        List<Object> params = new ArrayList<>();

        if(paymentId != null) {
            sql.append("payment_id=?");
            params.add(paymentId);
        }
        return new F.T2(sql.toString(), params);
    }

    public List<PaymentUnit> query() {
        F.T2<String, List<Object>> params = params();
        return PaymentUnit.find(params._1, params._2.toArray()).fetch(this.page(), this.perSize);
    }

    public Long getTotalCount() {
        return this.count();
    }

    @Override
    public Long count(F.T2<String, List<Object>> params) {
        return PaymentUnit.count(params._1, params._2.toArray());
    }
}
