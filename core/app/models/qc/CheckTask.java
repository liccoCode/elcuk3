package models.qc;

import com.google.gson.annotations.Expose;
import helper.DBUtils;
import helper.Reflects;
import helper.Webs;
import models.embedded.ERecordBuilder;
import models.procure.Cooperator;
import models.procure.ProcureUnit;
import models.product.Whouse;
import org.apache.commons.lang.StringUtils;
import play.data.validation.Validation;
import play.db.jpa.Model;

import javax.persistence.*;
import java.util.*;

/**
 * Created by IntelliJ IDEA.
 * User: cary
 * Date: 5/6/13
 * Time: 10:07 AM
 */
@Entity
public class CheckTask extends Model {

    /**
     * 采购计划单
     */
    @OneToOne(cascade = {CascadeType.PERSIST})
    public ProcureUnit units;

    /**
     * 货代仓库
     */
    @OneToOne
    public Whouse shipwhouse;

    @Expose
    public String sku;

    /**
     * 检测人
     */
    @Expose
    public String checkor;

    /**
     * 订单数量
     */
    @Expose
    public int planqty;

    /**
     * 实际交货数量
     */
    public int qty;

    /**
     * 实际抽检数量
     */
    @Expose
    public int pickqty;

    /**
     * 处理方式
     */
    @Enumerated(EnumType.STRING)
    public DealType dealway;

    /**
     * 质检完成时间
     */
    @Expose
    public Date endTime;

    /**
     * 质检开始时间
     */
    @Expose
    public Date startTime;

    /**
     * 是否合格结果
     */
    @Enumerated(EnumType.STRING)
    public ResultType result;
    /**
     * 是否发货
     */
    @Expose
    @Enumerated(EnumType.STRING)
    public ShipType isship;

    /**
     * 备注
     */
    @Expose
    @Lob
    public String checknote = " ";

    /**
     * 创建人
     */
    @Expose
    public java.util.Date creatat;

    /**
     * 修改人
     */
    @Expose
    public String updator;

    /**
     * 工作小时(质检工时)
     */
    @Expose
    public float workhour;

    /**
     * 质检员人数
     */
    @Expose
    public int workers;

    /**
     * 人工费
     */
    @Expose
    public float workfee;

    /**
     * 质检状态
     */
    @Enumerated(EnumType.STRING)
    public StatType checkstat = StatType.UNCHECK;

    /**
     * 确认状态
     */
    @Enumerated(EnumType.STRING)
    public ConfirmType confirmstat = ConfirmType.UNCONFIRM;

    public enum StatType {
        UNCHECK {
            @Override
            public String label() {
                return "未检";
            }
        },
        CHECKFINISH {
            @Override
            public String label() {
                return "已检-完成";
            }
        },
        CHECKNODEAL {
            @Override
            public String label() {
                return "已检-待处理";
            }
        },
        CHECKDEAL {
            @Override
            public String label() {
                return "已检-已处理";
            }
        },
        REPEATCHECK {
            @Override
            public String label() {
                return "重检";
            }
        };

        public abstract String label();
    }

    public enum ConfirmType {
        UNCONFIRM {
            @Override
            public String label() {
                return "未确认";
            }
        },
        CONFIRM {
            @Override
            public String label() {
                return "确认";
            }
        };

        public abstract String label();
    }


    public enum ShipType {
        SHIP {
            @Override
            public String label() {
                return "发货";
            }
        },
        NOTSHIP {
            @Override
            public String label() {
                return "不发货";
            }
        };

        public abstract String label();
    }


    public enum ResultType {
        AGREE {
            @Override
            public String label() {
                return "合格";
            }
        },
        NOTAGREE {
            @Override
            public String label() {
                return "不合格";
            }
        },
        OTHER {
            @Override
            public String label() {
                return "其他";
            }
        };

        public abstract String label();
    }


    public enum DealType {
        RETURN {
            @Override
            public String label() {
                return "退回工厂";
            }
        },
        WAREHOUSE {
            @Override
            public String label() {
                return "到仓库返工";
            }
        },
        HELP {
            @Override
            public String label() {
                return "仓库代返工";
            }
        };

        public abstract String label();
    }

    public enum T {
        SELF {
            @Override
            public String label() {
                return "工厂自检";
            }
        },

        SAMPLE {
            @Override
            public String label() {
                return "抽检";
            }
        },

        WHOLE {
            @Override
            public String label() {
                return "全检";
            }
        };

        public abstract String label();
    }

    /**
     * 质检方式
     */
    @Enumerated(EnumType.STRING)
    @Expose
    public T qcType;

    /**
     * 检测要求
     */
    @Lob
    @Expose
    public String qcRequires = "";

    @Transient
    public List<String> qcRequire = new ArrayList<String>();

    /**
     * 检测方法
     */
    @Lob
    @Expose
    public String qcWays = "";

    @Transient
    public List<String> qcWay = new ArrayList<String>();

    /**
     * 打印次数
     */
    @Expose
    public int printNumber;

    public enum FLAG {
        ARRAY_TO_STR,
        STR_TO_ARRAY
    }

    /**
     * List 和 String 之间的转换
     *
     * @param flag
     */
    public void arryParamSetUP(FLAG flag) {
        if(flag.equals(FLAG.ARRAY_TO_STR)) {
            this.qcRequires = StringUtils.join(this.qcRequire, Webs.SPLIT);
            this.qcWays = StringUtils.join(this.qcWay, Webs.SPLIT);
        } else {
            this.qcRequire = new ArrayList<String>();
            this.qcWay = new ArrayList<String>();

            String temp[] = StringUtils.splitByWholeSeparator(this.qcRequires, Webs.SPLIT);
            if(temp != null) Collections.addAll(this.qcRequire, temp);

            temp = StringUtils.splitByWholeSeparator(this.qcWays, Webs.SPLIT);
            if(temp != null) Collections.addAll(this.qcWay, temp);

            if(StringUtils.isBlank(this.qcRequires)) {
                this.qcRequires = " ";
            }
            if(StringUtils.isBlank(this.qcWays)) {
                this.qcWays = " ";
            }
        }
    }

    /**
     * 质检任务检查
     */
    public void validateRequired() {
        Validation.required("实际交货数量", this.qty);
        Validation.required("质检开始时间", this.startTime);
        Validation.required("质检结束时间", this.endTime);
    }

    public void validateRight() {
        if(this.qty < 0) Validation.addError("", "实际交货数量不能小于0");
        if(this.pickqty < 0) Validation.addError("", "实际抽检数量不能小于0");
    }

    /**
     * 产生质检任务
     */
    public static void generateTask() {
        List<Map<String, Object>> units = DBUtils
                .rows("select id from ProcureUnit where isCheck=0 AND shipType is not null");
        for(Map<String, Object> unit : units) {
            Long unitid = (Long) unit.get("id");
            CheckTask task = CheckTask.find("units.id=?", unitid).first();
            if(task == null) {
                CheckTask newtask = new CheckTask();
                ProcureUnit punit = ProcureUnit.findById(unitid);
                newtask.units = punit;
                newtask.confirmstat = ConfirmType.UNCONFIRM;
                newtask.checkstat = StatType.UNCHECK;
                if(punit.cooperator.qcLevel == Cooperator.L.MICRO) {
                    //当合作伙伴的质检级别为微检，则质检方式默认为工厂自检 其他情况需要质检员手动选择
                    newtask.qcType = T.SELF;
                }
                //根据采购计划的运输方式+运输单中的运输商 匹配对应的货代仓库
                Whouse forwardWhouse = punit.fetchForwardWhouse();
                if(forwardWhouse != null) {
                    newtask.shipwhouse = forwardWhouse;
                    newtask.checkor = forwardWhouse.user.username;
                }
                newtask.creatat = new Date();
                newtask.save();
                DBUtils.execute("update ProcureUnit set isCheck=1 where id=" + unitid);
            }
        }
    }

    /**
     * 保存质检任务且修改相关联的对应的采购计划数据
     */
    public void fullUpdate(CheckTask newCt) {
        long diff = newCt.endTime.getTime() - newCt.startTime.getTime();
        this.workhour = diff / (60 * 60 * 1000);
        this.units.attrs.qty = newCt.qty;
        switch(newCt.isship) {
            case SHIP:
                this.checkstat = StatType.CHECKFINISH;
                if(this.units.shipState == ProcureUnit.S.NOSHIPWAIT) {
                    //TODO:采购计划的“不发货处理”状态还原成 当初系统开启此不发货流程时 采购计划的“不发货处理”状态值
                    //TODO:结束不发货流程
                }
                break;
            case NOTSHIP:
                this.checkstat = StatType.CHECKNODEAL;
                //对应采购计划ID的“不发货处理”状态更新为：不发货待处理
                this.units.shipState = ProcureUnit.S.NOSHIPWAIT;
                //TODO:启动不发货流程，并进入到“采购计划不发货待处理事务”，为该采购计划的采购单的创建人 生成不发货待处理任务
                break;
        }
        this.update(newCt);
    }

    /**
     * 获取对应的质检任务查看链接
     * 1. 存在1个已检的质检任务质检信息，则查看最新质检任务的质检信息
     * 2. 存在1个以上的已检的质检任务，查看质检信息查看列表页面
     *
     * @return
     */
    public String fetchCheckTaskLink() {
        List<CheckTask> tasks = CheckTask.find("units_id=? AND checkstat is not 'UNCHECK'", this.units.id).fetch();
        if(tasks.size() == 1) return String.format("/checktasks/%s/show", tasks.get(0).id);
        if(tasks.size() > 1) return String.format("/checktasks/%s/showList", this.units.id);
        return null;
    }

    /**
     * 更新时的日志
     */
    public void beforeUpdateLog(CheckTask newCt) {
        List<String> logs = new ArrayList<String>();
        logs.addAll(Reflects.updateAndLogChanges(this, "startTime", newCt.startTime));
        logs.addAll(Reflects.updateAndLogChanges(this, "endTime", newCt.endTime));
        logs.addAll(Reflects.updateAndLogChanges(this, "isship", newCt.isship));
        logs.addAll(Reflects.updateAndLogChanges(this, "result", newCt.result));
        logs.addAll(Reflects.updateAndLogChanges(this, "pickqty", newCt.pickqty));
        logs.addAll(Reflects.updateAndLogChanges(this, "qty", newCt.qty));
        logs.addAll(Reflects.updateAndLogChanges(this, "checknote", newCt.checknote));
        if(logs.size() > 0) {
            new ERecordBuilder("checktask.update", "checktask.update.msg").msgArgs(this.id, StringUtils.join(logs, "，"))
                    .fid(this.id).save();
        }
    }

    public void update(CheckTask newCt) {
        this.beforeUpdateLog(newCt);
        this.qty = newCt.qty;
        this.pickqty = newCt.pickqty;
        if(newCt.dealway != null) this.dealway = newCt.dealway;
        if(newCt.startTime != null) this.startTime = newCt.startTime;
        if(newCt.endTime != null) this.endTime = newCt.endTime;
        if(newCt.result != null) this.result = newCt.result;
        if(newCt.isship != null) this.isship = newCt.isship;
        if(newCt.checknote != null) this.checknote = newCt.checknote;
        this.save();
    }
}
