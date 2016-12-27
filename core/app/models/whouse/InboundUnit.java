package models.whouse;

import com.alibaba.fastjson.JSON;
import com.google.gson.annotations.Expose;
import helper.DBUtils;
import helper.Dates;
import helper.J;
import helper.Reflects;
import models.User;
import models.embedded.ERecordBuilder;
import models.procure.FBAShipment;
import models.procure.ProcureUnit;
import models.procure.Shipment;
import models.qc.CheckTaskDTO;
import org.apache.commons.lang.StringUtils;
import org.apache.commons.lang.math.NumberUtils;
import org.hibernate.annotations.DynamicUpdate;
import play.data.validation.Required;
import play.db.helper.SqlSelect;
import play.db.jpa.Model;
import play.utils.FastRuntimeException;

import javax.persistence.*;
import java.util.*;

/**
 * 入库单元
 * Created by licco on 2016/11/2.
 */
@Entity
@DynamicUpdate
public class InboundUnit extends Model {

    @ManyToOne
    public Inbound inbound;

    @OneToOne
    public ProcureUnit unit;

    /**
     * 计划交货数量
     */
    public int planQty;

    /**
     * 实际交货数量
     */
    public int qty;

    /**
     * 交货不足处理方式
     */
    @Enumerated(EnumType.STRING)
    public H handType;

    public enum H {
        Actual {
            @Override
            public String label() {
                return "按实际到货处理";
            }
        },
        Delivery {
            @Override
            public String label() {
                return "收货且创建尾货单";
            }
        };

        public abstract String label();
    }

    /**
     * fba
     */
    @ManyToOne
    public FBAShipment fba;

    /**
     * 运输方式
     */
    @Enumerated(EnumType.STRING)
    public Shipment.T type;

    /**
     * 状态
     */
    @Enumerated(EnumType.STRING)
    public S status;

    public enum S {
        Create {
            @Override
            public String label() {
                return "已创建";
            }
        },
        Receive {
            @Override
            public String label() {
                return "已收货";
            }
        },
        Check {
            @Override
            public String label() {
                return "已质检";
            }
        },
        Inbound {
            @Override
            public String label() {
                return "已入库";
            }
        },
        Abort {
            @Override
            public String label() {
                return "异常结束";
            }
        };

        public abstract String label();
    }

    /**
     * 质检结果
     */
    @Enumerated(EnumType.STRING)
    public R result;

    public enum R {
        UnCheck {
            @Override
            public String label() {
                return "未检";
            }
        },
        Qualified {
            @Override
            public String label() {
                return "合格";
            }
        },
        Unqualified {
            @Override
            public String label() {
                return "不合格";
            }
        };

        public abstract String label();
    }

    /**
     * 质检不合格处理方式
     */
    @Enumerated(EnumType.STRING)
    public W way;

    public enum W {
        Return {
            @Override
            public String label() {
                return "退货处理";
            }
        },
        PTRework {
            @Override
            public String label() {
                return "品拓返工";
            }
        },
        FactoryRework {
            @Override
            public String label() {
                return "工厂返工";
            }
        };

        public abstract String label();
    }

    /**
     * 主箱信息
     */
    @Lob
    public String mainBoxInfo;
    /**
     * 尾箱信息
     */
    @Lob
    public String lastBoxInfo;

    /**
     * 合格数
     */
    public int qualifiedQty;

    /**
     * 不合格数量
     */
    public int unqualifiedQty;

    /**
     * 实际入库数
     */
    public int inboundQty;

    /**
     * 目标仓库
     */
    @Required
    @Expose
    @ManyToOne
    public Whouse target;

    /**
     * 质检时间
     */
    public Date qcDate;

    /**
     * 质检人
     */
    @OneToOne
    public User qcUser;

    /**
     * 入库时间
     */
    public Date inboundDate;

    /**
     * 确认入库人
     */
    @OneToOne
    public User confirmUser;

    /***
     * 采购计划ID
     */
    @Transient
    public Long unitId;


    @Transient
    public CheckTaskDTO mainBox = new CheckTaskDTO();

    @Transient
    public CheckTaskDTO lastBox = new CheckTaskDTO();


    public void updateAttr(String attr, String value) {
        List<String> logs = new ArrayList<>();
        switch(attr) {
            case "qty":
                if(this.unit.qty() - NumberUtils.toInt(value) != 0) {
                    this.handType = H.Actual;
                }
                logs.addAll(Reflects.logFieldFade(this, attr, NumberUtils.toInt(value)));
                break;
            case "handType":
                logs.addAll(Reflects.logFieldFade(this, "handType", H.valueOf(value)));
                break;
            case "result":
                if(value.equals("Unqualified")) {
                    this.way = W.Return;
                } else {
                    this.way = null;
                }
                if(this.qualifiedQty == 0) {
                    this.qualifiedQty = this.qty;
                    this.unqualifiedQty = 0;
                }
                logs.addAll(Reflects.logFieldFade(this, "result", R.valueOf(value)));
                break;
            case "way":
                logs.addAll(Reflects.logFieldFade(this, "way", W.valueOf(value)));
                break;
            case "qualifiedQty":
                this.unqualifiedQty = this.qty - NumberUtils.toInt(value);
                logs.addAll(Reflects.logFieldFade(this, attr, NumberUtils.toInt(value)));
                break;
            case "unqualifiedQty":
                this.qualifiedQty = this.qty - NumberUtils.toInt(value);
                logs.addAll(Reflects.logFieldFade(this, attr, NumberUtils.toInt(value)));
                break;
            case "inboundQty":
                logs.addAll(Reflects.logFieldFade(this, attr, NumberUtils.toInt(value)));
                break;
            case "target":
                logs.addAll(Reflects.logFieldFade(this, attr, Whouse.findById(NumberUtils.toLong(value))));
                break;
            case "mainBox.length":
            case "mainBox.width":
            case "mainBox.height":
            case "lastBox.singleBoxWeight":
            case "lastBox.length":
            case "lastBox.width":
            case "lastBox.height":
                logs.addAll(Reflects.logFieldFade(this, attr, NumberUtils.toDouble(value)));
                break;
            default:
                throw new FastRuntimeException("不支持的属性类型!");
        }
        new ERecordBuilder("outboundrecord.update")
                .msgArgs(this.id, StringUtils.join(logs, "<br/>"))
                .fid(this.inbound.id)
                .save();
        this.save();
    }

    @PostLoad
    public void postPersist() {
        this.mainBox = JSON.parseObject(this.mainBoxInfo, CheckTaskDTO.class);
        this.lastBox = JSON.parseObject(this.lastBoxInfo, CheckTaskDTO.class);
    }

    public void marshalBoxs() {
        this.mainBoxInfo = J.json(this.mainBox);
        this.lastBoxInfo = J.json(this.lastBox);
    }

    /**
     * 采购计划对应的入库单是不是全部已结束
     *
     * @param id
     * @return
     */
    public static String isAllInbound(Long id) {
        List<Inbound> inbounds = Inbound.find("SELECT DISTINCT i FROM Inbound i LEFT JOIN i.units u " +
                "WHERE i.status <> ? AND u.unit.id = ? ", Inbound.S.End, id).fetch();
        if(inbounds.size() > 0) {
            return inbounds.get(0).id;
        } else {
            return "";
        }
    }

    public static Map<Integer, List<InboundUnit>> pageNumForTen(List<Inbound> inbounds) {
        Map<Integer, List<InboundUnit>> ten = new HashMap<>();
        int k = 0;
        for(Inbound inbound : inbounds) {
            List<InboundUnit> iu = inbound.units;
            int max = iu.size();
            for(int i = 0; i < iu.size(); i += 10) {
                int num = max - i;
                ten.put(k, iu.subList(i, num > 10 ? i + 10 : i + num));
                k++;
            }
        }
        return ten;
    }

    /**
     * 根据采购计划ID验证是否可以创建入库单
     *
     * @return
     */
    public static boolean vaildIsCreate(Long id) {
        List<InboundUnit> list = InboundUnit.find("unit.id = ? ", id).fetch();
        return list.size() == 0;
    }

}
