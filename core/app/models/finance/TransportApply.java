package models.finance;

import helper.Currency;
import helper.Dates;
import models.ElcukRecord;
import models.User;
import models.embedded.ERecordBuilder;
import models.market.M;
import models.procure.Cooperator;
import models.procure.Shipment;
import models.view.dto.ApplyPaymentDTO;
import org.apache.commons.lang.StringUtils;
import org.joda.time.DateTime;
import play.data.validation.Validation;
import play.db.helper.JpqlSelect;
import play.libs.F;

import javax.persistence.Entity;
import javax.persistence.ManyToOne;
import javax.persistence.OneToMany;
import javax.persistence.OneToOne;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.Stream;

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
    public List<Shipment> shipments = new ArrayList<>();

    @OneToMany(mappedBy = "tApply")
    public List<Payment> payments = new ArrayList<>();


    @Override
    public String generateSerialNumber(Cooperator cooper) {
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
        return String.format("SQK-%s-%03d-%s", this.cooperator.name, count + 1, now.toString("yy"));
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
        return ElcukRecord.records(this.id + "", Arrays.asList("transportapply.save", "shipment.departFromApply"), 50);
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
        apply.serialNumber = apply.generateSerialNumber(shipCoperPair._2.iterator().next());
        apply.createdAt = new Date();
        apply.updateAt = new Date();
        apply.applier = User.current();
        apply.save();
        apply.appendShipment(shipmentIds);
        return apply;
    }

    private static F.T2<List<Shipment>, Set<Cooperator>> shipmentApplyCheck(List<String> shipmentId) {
        List<Shipment> shipments = Shipment.find(JpqlSelect.whereIn("id", shipmentId)).fetch();
        if(shipments.size() != shipmentId.size())
            Validation.addError("", "提交的运输单参数与系统中的不符.");

        Set<Cooperator> coopers = shipments.stream().filter(ship -> ship.cooper != null).map(ship -> ship.cooper)
                .collect(Collectors.toSet());

        if(coopers.size() > 1)
            Validation.addError("", "请仅对同一个运输商.");
        if(coopers.size() < 1)
            Validation.addError("", "请款单至少需要一个拥有供应商的运输单.");
        if(shipments.stream().anyMatch(shipment -> shipment.apply != null))
            Validation.addError("", "请全部选择未创建请款单的运输单！");
        return new F.T2<>(shipments, coopers);
    }

    /**
     * 总请款金额
     *
     * @return
     */
    public F.T2<Float, Float> totalFees() {
        float usd = 0;
        float cny = 0;
        for(Payment payment : this.payments) {
            usd += payment.totalFees()._1;
            cny += payment.totalFees()._2;
        }
        return new F.T2<>(usd, cny);
    }

    /**
     * 总实际支付金额
     */
    public F.T2<Float, Float> totalActualPaid() {
        float usd = 0;
        float cny = 0;
        for(Payment payment : this.payments) {
            if(payment.actualCurrency != null) {
                usd += payment.actualCurrency.toUSD(payment.actualPaid.floatValue());
                cny += payment.actualCurrency.toCNY(payment.actualPaid.floatValue());
            }
        }
        return new F.T2<>(usd, cny);
    }


    /**
     * 金额的明细
     *
     * @return
     */
    public List<ApplyPaymentDTO> currencyFees() {
        List<ApplyPaymentDTO> apply = new java.util.ArrayList<>();
        for(Currency currency : helper.Currency.values()) {
            ApplyPaymentDTO dto = new ApplyPaymentDTO();
            dto.currency = currency;
            for(Shipment ship : shipments) {
                BigDecimal paidamount = new BigDecimal(0);
                BigDecimal applyamount = new BigDecimal(0);
                BigDecimal totalamount = new BigDecimal(0);
                for(PaymentUnit payment : ship.fees) {
                    if(payment.currency == currency) {
                        totalamount = totalamount.add(payment
                                .decimalamount());

                        //已批准和已支付的
                        if(payment.state == PaymentUnit.S.APPROVAL || payment.state == PaymentUnit.S.PAID) {
                            paidamount = paidamount.add(payment.decimalamount());
                        } else {
                            applyamount = applyamount.add(payment.decimalamount());

                        }

                    }
                }
                paidamount = paidamount.setScale(2, RoundingMode.HALF_UP);
                applyamount = applyamount.setScale(2, RoundingMode.HALF_UP);
                totalamount = totalamount.setScale(2, RoundingMode.HALF_UP);
                dto.total_fee = dto.total_fee
                        .add(totalamount);
                dto.approval_fee = dto.approval_fee.
                        add(paidamount);
                dto.noapproval_fee = dto.noapproval_fee
                        .add(applyamount);

            }
            dto.total_fee = dto.total_fee.setScale(2, RoundingMode.HALF_UP);
            dto.approval_fee = dto.approval_fee.setScale(2, RoundingMode.HALF_UP);
            dto.noapproval_fee = dto.total_fee.subtract(dto.approval_fee);
            if(dto.total_fee.compareTo(new BigDecimal(0)) != 0) apply.add(dto);
        }
        return apply;
    }

    /**
     * 最后 10 个运输单
     *
     * @return
     */
    public List<Shipment> lastShipments() {
        return this.shipments.stream().limit(10).collect(Collectors.toList());
    }

    /**
     * 输出给 typeahead 所使用的 source
     * <p>
     * 需要支持: 运输单 ID 、TrackNo、FBA、备注
     *
     * @return
     */
    public List<String> pickSource(String search) {
        List<String> sources = new ArrayList<>();
        for(Shipment shipment : this.shipments) {
            sources.addAll(shipment.fbas().stream().map(fba -> fba.shipmentId).collect(Collectors.toList()));
            sources.addAll(Stream.concat(Stream.of(shipment.id, shipment.trackNo),
                    shipment.fees.stream().map(fee -> fee.memo)).collect(Collectors.toList())
            );
        }
        sources.addAll(Arrays.asList(M.amazonVals()).stream()
                .map(M::marketAndWhouseMapping)
                .collect(Collectors.toList()));

        if(StringUtils.isNotBlank(search)) {
            return sources.stream()
                    .filter(source -> StringUtils.isNotBlank(source)
                            && source.toUpperCase().contains(search.toUpperCase()))
                    .limit(10)
                    .collect(Collectors.toList());
        }
        return sources.stream().filter(StringUtils::isNotBlank).limit(10).collect(Collectors.toList());
    }
}
