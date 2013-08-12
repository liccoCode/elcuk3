package models.finance;

import helper.Dates;
import models.ElcukRecord;
import models.User;
import models.embedded.ERecordBuilder;
import models.procure.Cooperator;
import models.procure.Shipment;
import org.apache.commons.lang.StringUtils;
import org.joda.time.DateTime;
import play.data.validation.Validation;
import play.db.helper.JpqlSelect;
import play.libs.F;

import javax.persistence.Entity;
import javax.persistence.ManyToOne;
import javax.persistence.OneToMany;
import javax.persistence.OneToOne;
import java.util.*;

/**
 * Created by IntelliJ IDEA.
 * User: wyatt
 * Date: 7/16/13
 * Time: 2:10 PM
 */
@Entity
public class TransportApply extends Apply {
    @OneToOne
    public Cooperator cooperator;

    /**
     * 请款人
     */
    @ManyToOne
    public User applier;

    @OneToMany(mappedBy = "apply")
    public List<Shipment> shipments = new ArrayList<Shipment>();

    @OneToMany(mappedBy = "tApply")
    public List<Payment> payments = new ArrayList<Payment>();


    @Override
    public void generateSerialNumber(Cooperator cooper) {
        /**
         * format: [SQK-合作伙伴-[当年第几次]-[年份]
         */
        this.cooperator = cooper;
        DateTime now = DateTime.now();
        String year = now.toString("yyyy");
        long count = TransportApply.count("cooperator=? AND createdAt>=? AND createdAt<=?",
                this.cooperator,
                Dates.cn(String.format("%s-01-01", year)).toDate(),
                Dates.cn(String.format("%s-01-01", year)).plusYears(1).minusSeconds(1).toDate());
        this.serialNumber = String.format("SQK-%s-%03d-%s", this.cooperator.name, count + 1, now.toString("yy"));
    }

    /**
     * 想运输请款单中 append 运输单
     */
    public void appendShipment(List<String> shipmentId) {
        F.T2<List<Shipment>, Set<Cooperator>> shipCoperPair = shipmentApplyCheck(shipmentId);
        if(shipCoperPair._2.iterator().hasNext() && shipCoperPair._2.iterator().next() != this.cooperator)
            Validation.addError("", "供应商不同, 无法添加成功.");
        if(Validation.hasErrors()) return;
        for(Shipment ship : shipCoperPair._1) {
            ship.apply = this;
            ship.save();
        }
        new ERecordBuilder("transportapply.save")
                .msgArgs(StringUtils.join(shipmentId, ","), this.serialNumber)
                .fid(this.id)
                .save();
    }

    public List<ElcukRecord> records() {
        return ElcukRecord.records(this.id + "", Arrays.asList("transportapply.save", "shipment.departFromApply"));
    }


    /**
     * 创建一个新的运输请款单
     *
     * @param shipmentIds
     */
    public static TransportApply buildTransportApply(List<String> shipmentIds) {
        F.T2<List<Shipment>, Set<Cooperator>> shipCoperPair = shipmentApplyCheck(shipmentIds);

        if(Validation.hasErrors()) return null;
        TransportApply apply = new TransportApply();
        apply.generateSerialNumber(shipCoperPair._2.iterator().next());
        apply.createdAt = apply.updateAt = new Date();
        apply.applier = User.current();
        apply.save();
        apply.appendShipment(shipmentIds);
        return apply;
    }

    private static F.T2<List<Shipment>, Set<Cooperator>> shipmentApplyCheck(List<String> shipmentId) {
        List<Shipment> shipments = Shipment.find(JpqlSelect.whereIn("id", shipmentId)).fetch();
        if(shipments.size() != shipmentId.size())
            Validation.addError("", "提交的运输单参数与系统中的不符.");

        Set<Cooperator> coopers = new HashSet<Cooperator>();
        for(Shipment ship : shipments) {
            if(ship.cooper != null) coopers.add(ship.cooper);
        }

        if(coopers.size() > 1)
            Validation.addError("", "请仅对同一个运输商.");
        if(coopers.size() < 1)
            Validation.addError("", "请款单至少需要一个拥有供应商的运输单.");
        return new F.T2<List<Shipment>, Set<Cooperator>>(shipments, coopers);
    }
}
