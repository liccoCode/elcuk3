package models.market;

import controllers.Login;
import helper.Currency;
import helper.Reflects;
import models.ElcukRecord;
import models.User;
import models.embedded.ERecordBuilder;
import models.procure.BtbCustom;
import models.product.Product;
import org.apache.commons.lang3.StringUtils;
import play.data.validation.Validation;
import play.db.jpa.Model;

import javax.persistence.*;
import java.math.BigDecimal;
import java.text.SimpleDateFormat;
import java.util.*;

/**
 * B2B订单
 * Created by licco on 16/1/21.
 */
@Entity
public class BtbOrder extends Model {

    public String orderNo;

    @ManyToOne
    public BtbCustom btbCustom;

    public Date saleDate;

    public String memo;

    /**
     * 客户运费单位
     */
    @Enumerated(EnumType.STRING)
    public Currency customShipUnit;

    /**
     * 客户运费
     */
    @Column(precision = 12, scale = 2)
    public BigDecimal customShipCost;

    /**
     * 银行手续费方式
     */
    @Enumerated(EnumType.STRING)
    public C bankCharges;

    /**
     * 其他手续费方式
     */
    public String bankChargesOther;

    /**
     * 银行手续费
     */
    @Column(precision = 12, scale = 2)
    public BigDecimal bankChargesCost;

    /**
     * 银行手续费单位
     */
    @Enumerated(EnumType.STRING)
    public Currency bankChargesUnit;

    /**
     * 运输计量单位
     */
    @Enumerated(EnumType.STRING)
    public S shipWeightUnit;

    public Float shipWeight;

    @Enumerated(EnumType.STRING)
    public Currency shipCostUnit;

    @Column(precision = 12, scale = 2)
    public BigDecimal shipCost;

    /**
     * 运输方式
     */
    @Enumerated(EnumType.STRING)
    public SH shipWay;

    /**
     * 运输备注
     */
    public String shipRemark;

    /**
     * 创建日期
     */
    public Date createDate;

    @ManyToOne
    public User creator;

    public enum S {
        weight {
            @Override
            public String label() {
                return "重量";
            }
        },
        volume {
            @Override
            public String label() {
                return "体积";
            }
        };

        public abstract String label();
    }

    /**
     * 银行收费方式
     */
    public enum C {
        TT {
            @Override
            public String label() {
                return "T/T";
            }
        },
        PayPal {
            @Override
            public String label() {
                return "PayPal";
            }
        },
        Other {
            @Override
            public String label() {
                return "其它";
            }
        };

        public abstract String label();
    }

    /**
     * 运输方式
     */
    public enum SH {
        UPS {
            @Override
            public String label() {
                return "UPS";
            }
        },
        DHL {
            @Override
            public String label() {
                return "DHL";
            }
        },
        FEDEX {
            @Override
            public String label() {
                return "FEDEX";
            }
        },
        TNT {
            @Override
            public String label() {
                return "TNT";
            }
        },
        AIR {
            @Override
            public String label() {
                return "空运";
            }
        },
        SEA {
            @Override
            public String label() {
                return "海运";
            }
        };

        public abstract String label();
    }

    @OneToMany(mappedBy = "btbOrder", fetch = FetchType.LAZY)
    public List<BtbOrderItem> btbOrderItemList = new ArrayList<BtbOrderItem>();

    public void saveEntity(BtbOrder btbOrder) {
        List<String> logs = new ArrayList<String>();
        if(this.id == null) {
            btbOrder.createDate = new Date();
            btbOrder.creator = Login.current();
            this.createLogs(" 新增订单【" + btbOrder.orderNo + "】");
            btbOrder.save();
        } else {
            logs.addAll(this.doneUpdate(btbOrder));
            this.btbCustom.id = btbOrder.btbCustom.id;
            this.saleDate = btbOrder.saleDate;
            this.memo = btbOrder.memo;
            this.customShipCost = btbOrder.customShipCost;
            this.customShipUnit = btbOrder.customShipUnit;
            this.bankCharges = btbOrder.bankCharges;
            this.bankChargesOther = btbOrder.bankChargesOther;
            this.bankChargesCost = btbOrder.bankChargesCost;
            this.bankChargesUnit = btbOrder.bankChargesUnit;
            this.shipWeight = btbOrder.shipWeight;
            this.shipWeightUnit = btbOrder.shipWeightUnit;
            this.shipCost = btbOrder.shipCost;
            this.shipCostUnit = btbOrder.shipCostUnit;
            this.shipWay = btbOrder.shipWay;
            this.shipRemark = btbOrder.shipRemark;
            this.save();
        }
        if(btbOrder.btbOrderItemList != null && btbOrder.btbOrderItemList.size() > 0) {
            for(BtbOrderItem item : btbOrder.btbOrderItemList) {
                if(item != null && item.id == null) {
                    item.btbOrder = btbOrder.id == null ? this : btbOrder;
                    logs.add("新增SKU" + item.product.sku);
                    item.save();
                } else if(item != null && item.id != null) {
                    BtbOrderItem it = BtbOrderItem.findById(item.id);
                    if(StringUtils.isEmpty(item.product.sku)) {
                        logs.add("删除SKU" + it.product.sku);
                        it.delete();
                    } else {
                        logs.addAll(this.doneItemUpdate(it, item));
                        it.product = Product.findById(item.product.sku);
                        it.save();
                    }
                }
            }
        }
        if(logs.size() > 0) {
            new ERecordBuilder("btbOrder.update").msgArgs(this.orderNo, StringUtils.join(logs, "<br>"))
                    .fid(this.orderNo).save();
        }
    }

    public String showTotalCost() {
        String show = "";
        Map<Currency, BigDecimal> total = this.totalOrderCost();
        Iterator it = total.entrySet().iterator();
        while(it.hasNext()) {
            Map.Entry entry = (Map.Entry) it.next();
            if(entry != null) {
                Currency now = (Currency) entry.getKey();
                show += " " + now.symbol() + " " + total.get(now);
            }
        }
        return show;
    }

    public Map<Currency, BigDecimal> totalOrderCost() {
        Map<Currency, BigDecimal> map = new HashMap<Currency, BigDecimal>();
        /**客户运费**/
        if(this.customShipCost != null) {
            if(map.containsKey(this.customShipUnit)) {
                map.put(this.customShipUnit, map.get(this.customShipUnit).add(this.customShipCost));
            } else {
                map.put(this.customShipUnit, this.customShipCost);
            }
        }
        /**手续费用**/
        if(this.bankChargesCost != null) {
            if(map.containsKey(this.bankChargesUnit)) {
                map.put(this.bankChargesUnit, map.get(this.bankChargesUnit).add(this.bankChargesCost));
            } else {
                map.put(this.bankChargesUnit, this.bankChargesCost);
            }
        }

        /**运输费用**/
        if(map.containsKey(this.shipCostUnit)) {
            map.put(this.shipCostUnit, map.get(this.shipCostUnit).add(this.shipCost));
        } else {
            map.put(this.shipCostUnit, this.shipCost);
        }

        if(btbOrderItemList != null && btbOrderItemList.size() > 0) {
            for(BtbOrderItem item : btbOrderItemList) {
                if(map.containsKey(item.currency)) {
                    map.put(item.currency, map.get(item.currency).add(new BigDecimal(item.qty).multiply(item.price)));
                } else {
                    map.put(item.currency, new BigDecimal(item.qty).multiply(item.price));
                }
            }
            return map;
        } else {
            return map;
        }
    }

    public void validOrder(BtbOrder b) {
        if(b.btbCustom == null || b.btbCustom.id == null) {
            Validation.addError("", "客户/公司名称 未选择！");
        }
    }


    public void createLogs(String msg) {
        SimpleDateFormat formatter = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        StringBuilder message = new StringBuilder("操作人:" + Login.current().username + " 操作时间:" +
                formatter.format(new Date())).append(msg);
        new ElcukRecord("B2B订单管理", message.toString(), this.orderNo).save();
    }


    public List<String> doneUpdate(BtbOrder order) {
        List<String> logs = new ArrayList<String>();
        logs.addAll(Reflects.logFieldFade(this, "btbCustom.id", order.btbCustom.id));
        logs.addAll(Reflects.logFieldFade(this, "memo", order.memo));
        logs.addAll(Reflects.logFieldFade(this, "customShipCost", order.customShipCost));
        logs.addAll(Reflects.logFieldFade(this, "customShipUnit", order.customShipUnit));
        logs.addAll(Reflects.logFieldFade(this, "bankCharges", order.bankCharges));
        logs.addAll(Reflects.logFieldFade(this, "bankChargesOther", order.bankChargesOther));
        logs.addAll(Reflects.logFieldFade(this, "bankChargesCost", order.bankChargesCost));
        logs.addAll(Reflects.logFieldFade(this, "bankChargesUnit", order.bankChargesUnit));
        logs.addAll(Reflects.logFieldFade(this, "shipWeight", order.shipWeight));
        logs.addAll(Reflects.logFieldFade(this, "shipWeightUnit", order.shipWeightUnit));
        logs.addAll(Reflects.logFieldFade(this, "shipCost", order.shipCost));
        logs.addAll(Reflects.logFieldFade(this, "shipCostUnit", order.shipCostUnit));
        logs.addAll(Reflects.logFieldFade(this, "shipWay", order.shipWay));
        logs.addAll(Reflects.logFieldFade(this, "shipRemark", order.shipRemark));
        return logs;
    }

    public List<String> doneItemUpdate(BtbOrderItem old, BtbOrderItem item) {
        List<String> logs = new ArrayList<String>();
        logs.addAll(Reflects.logFieldFade(old, "product", item.product));
        logs.addAll(Reflects.logFieldFade(old, "qty", item.qty));
        logs.addAll(Reflects.logFieldFade(old, "price", item.price));
        logs.addAll(Reflects.logFieldFade(old, "currency", item.currency));
        return logs;
    }

}
