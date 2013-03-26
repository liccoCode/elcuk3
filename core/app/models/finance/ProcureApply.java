package models.finance;

import models.procure.Deliveryment;

import javax.persistence.Entity;
import javax.persistence.OneToMany;
import java.util.ArrayList;
import java.util.List;

/**
 * 具体的采购的请款单
 * User: wyatt
 * Date: 3/26/13
 * Time: 10:13 AM
 */
@Entity
public class ProcureApply extends Apply {

    @OneToMany(mappedBy = "apply")
    public List<Deliveryment> deliveryments = new ArrayList<Deliveryment>();

    /**
     * 请款单所拥有的支付信息
     */
    @OneToMany(mappedBy = "pApply")
    public List<Payment> payments = new ArrayList<Payment>();

}
