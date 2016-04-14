package models.qc;

import com.alibaba.fastjson.JSON;
import com.google.gson.annotations.Expose;
import helper.*;
import models.CategoryAssignManagement;
import models.activiti.ActivitiDefinition;
import models.activiti.ActivitiProcess;
import models.embedded.ERecordBuilder;
import models.finance.PaymentUnit;
import models.procure.Cooperator;
import models.procure.ProcureUnit;
import models.procure.ShipItem;
import models.procure.Shipment;
import models.view.dto.CheckTaskAQLDTO;
import models.whouse.InboundRecord;
import models.whouse.Whouse;
import org.activiti.engine.RuntimeService;
import org.activiti.engine.TaskService;
import org.activiti.engine.runtime.ProcessInstance;
import org.activiti.engine.task.Task;
import org.apache.commons.lang.StringUtils;
import play.cache.Cache;
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
    @Expose
    @OneToOne
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
     * 创建时间
     */
    @Expose
    public Date creatat;


    /**
     * 每人工时
     */
    @Expose
    public float workhour;

    /**
     * 人数
     */
    @Expose
    public int workers;

    /**
     * 人工费
     */
    @Expose
    public float workfee;

    /**
     * 质检取样
     */
    @Expose
    public Integer qcSample;

    /**
     * 预计交货日期
     */
    @Expose
    public Date planDeliveryDate;

    /**
     * 保存上次预计交货日期
     */
    @Expose
    public Date oldplanDeliveryDate;


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


    /**
     * 完成状态,后台记录，不显示
     */
    @Enumerated(EnumType.STRING)
    public ConfirmType finishStat = ConfirmType.UNCONFIRM;


    @Transient
    public static String ACTIVITINAME = "checktasks.fullupdate";

    /**
     * 审核意见
     */
    @Transient
    public String opition;

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

    /**
     * 申请的返工费用
     */
    @OneToOne
    public PaymentUnit reworkPay;

    /**
     * 标准箱尾箱质检信息
     */
    @Lob
    public String standBoxQctInfo;

    @Transient
    public List<CheckTaskDTO> standBoxQctInfos = new ArrayList<CheckTaskDTO>();

    /**
     * 尾箱尾箱质检信息
     */
    @Lob
    public String tailBoxQctInfo;

    @Transient
    public List<CheckTaskDTO> tailBoxQctInfos = new ArrayList<CheckTaskDTO>();

    /**
     * 不合格数量
     */
    public int unqualifiedQty;

    @Expose
    public String ac;

    @Expose
    public String re;

    /**
     * 抽检方式
     */
    @Lob
    @Expose
    public String samplingTypes = "";

    @Transient
    public List<String> samplingType = new ArrayList<String>();

    /**
     * 送检次数
     */
    @Lob
    @Expose
    public String inspectionTimes = "";

    @Transient
    public List<String> inspectionTime = new ArrayList<String>();

    /**
     * AQL 严重
     */
    @Expose
    public String cr;

    /**
     * AQL 主要
     */
    @Expose
    public String ma;

    /**
     * AQL 次要
     */
    @Expose
    public String mi;

    /**
     * AQL 不良描述
     * JSON 格式: [{"badDesc": "aaa", inspectionResult: [检验结果1, 检验结果2]}]
     */
    @Transient
    public List<CheckTaskAQLDTO> aqlBadDesc = new ArrayList<CheckTaskAQLDTO>();

    @Lob
    public String aqlBadDescs = "[]";

    public enum FLAG {
        ARRAY_TO_STR,
        STR_TO_ARRAY
    }

    /**
     * 自动生成入库记录
     */
    @PostPersist
    public void buidingInboundRecord() {
        if(this.isship == ShipType.SHIP && !InboundRecord.exist(this)) {
            new InboundRecord(this).save();
        }
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

            this.standBoxQctInfo = J.json(this.fixNullStr(this.standBoxQctInfos));
            this.tailBoxQctInfo = J.json(this.fixNullStr(this.tailBoxQctInfos));

            this.samplingTypes = StringUtils.join(this.samplingType, Webs.SPLIT);
            this.inspectionTimes = StringUtils.join(this.inspectionTime, Webs.SPLIT);
            this.aqlBadDescs = J.json(this.fixNullStr(this.aqlBadDesc));
        } else {
            this.qcRequire = new ArrayList<String>();
            this.qcWay = new ArrayList<String>();
            this.samplingType = new ArrayList<String>();
            this.inspectionTime = new ArrayList<String>();

            String temp[] = StringUtils.splitByWholeSeparator(this.qcRequires, Webs.SPLIT);
            if(temp != null) Collections.addAll(this.qcRequire, temp);

            temp = StringUtils.splitByWholeSeparator(this.qcWays, Webs.SPLIT);
            if(temp != null) Collections.addAll(this.qcWay, temp);

            temp = StringUtils.splitByWholeSeparator(this.samplingTypes, Webs.SPLIT);
            if(temp != null) Collections.addAll(this.samplingType, temp);

            temp = StringUtils.splitByWholeSeparator(this.inspectionTimes, Webs.SPLIT);
            if(temp != null) Collections.addAll(this.inspectionTime, temp);

            if(StringUtils.isNotBlank(this.standBoxQctInfo)) this.standBoxQctInfos = JSON.parseArray(this
                    .standBoxQctInfo, CheckTaskDTO.class);
            if(StringUtils.isNotBlank(this.tailBoxQctInfo)) this.tailBoxQctInfos = JSON.parseArray(this
                    .tailBoxQctInfo, CheckTaskDTO.class);
            if(StringUtils.isNotBlank(this.aqlBadDescs))
                this.aqlBadDesc = JSON.parseArray(this.aqlBadDescs, CheckTaskAQLDTO.class);
        }
    }

    /**
     * 对空字符进行处理
     *
     * @return
     */
    private List fixNullStr(List target) {
        Iterator<Object> iterator = target.iterator();
        while(iterator.hasNext()) {
            Object o = iterator.next();
            if(o == null) iterator.remove();
        }
        return target;
    }

    public void fetchSkucheck() {
        //查询出已经设置好的CheckList
        //1. Catrgory 的检测要求
        //2. SKU 的检测要求
        List<SkuCheck> parents = new ArrayList<SkuCheck>();
        List<SkuCheck> cates = SkuCheck.find("SkuName=?", this.units.product.category.categoryId).fetch();
        List<SkuCheck> skus = SkuCheck.find("SkuName=?", this.units.product.sku).fetch();
        parents.addAll(cates);
        parents.addAll(skus);

        List<SkuCheck> skuChecks = new ArrayList<SkuCheck>();
        for(SkuCheck parent : parents) {
            //当前对象下所有的子对象
            List<SkuCheck> temps = SkuCheck.find("pid=?", parent.id).fetch();
            skuChecks.addAll(temps);
        }

        for(SkuCheck check : skuChecks) {
            this.qcRequire.add(check.checkRequire);
            this.qcWay.add(check.checkMethod);
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
        if(startTime != null && endTime != null && !endTime.after(startTime)) {
            Validation.addError("", "质检开始时间不能大于结束时间");
        }
    }

    /**
     * 产生质检任务
     */
    public static void generateTask() {
        String unitcache = "checktaskprocureunitcache";

        if(StringUtils.isNotBlank(Cache.get(unitcache, String.class))) return;
        String running = "running";
        try {
            Cache.add(unitcache, running);
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
                    if(punit.cooperator != null && punit.cooperator.qcLevel == Cooperator.L.MICRO) {
                        //当合作伙伴的质检级别为微检，则质检方式默认为工厂自检 其他情况需要质检员手动选择
                        newtask.qcType = T.SELF;
                    }
                    newtask.checkor = newtask.showChecktor();
                    //根据采购计划的运输方式+运输单中的运输商 匹配对应的货代仓库
                    Whouse wh = searchWarehouse(punit);
                    if(wh != null && wh.user != null) {
                        newtask.shipwhouse = wh;
                    }

                    newtask.creatat = new Date();
                    newtask.finishStat = ConfirmType.UNCONFIRM;
                    newtask.save();
                    DBUtils.execute("update ProcureUnit set isCheck=1 where id=" + unitid);
                }
            }

            //因为运输方式经常变化，需要重新检查一次
            List<Map<String, Object>> unchecktasks = DBUtils.rows("select id from CheckTask where "
                    + "  units_id in (" +
                    "  select unit_id from ShipItem where shipment_id in (" +
                    "  select id from Shipment where type='EXPRESS'" +
                    "  )" +
                    "  ) and checkstat='UNCHECK'");
            if(unchecktasks.size() > 0) {
                checkwarehouse(unchecktasks);
            }
            unchecktasks = DBUtils.rows("select id from CheckTask where "
                    + " not exists (select 1 from ShipItem where ShipItem.unit_id=CheckTask.units_id)"
                    + " and exists (select 1 from ProcureUnit where "
                    + " ProcureUnit.id=CheckTask.units_id and shipType='EXPRESS')"
                    + " and checkstat='UNCHECK'");
            if(unchecktasks.size() > 0) {
                checkwarehouse(unchecktasks);
            }


            List<Map<String, Object>> tasks = DBUtils.rows("select id from CheckTask where shipwhouse_id is null");
            if(tasks.size() > 0) {
                checkwarehouse(tasks);
            }
        } catch(Exception e) {
            Cache.delete(unitcache);
        } finally {
            Cache.delete(unitcache);
        }
    }

    public static void checkwarehouse(List<Map<String, Object>> tasks) {
        //欧嘉货代
        Cooperator cooperator = Cooperator.findById(59l);
        for(Map<String, Object> task : tasks) {
            Long taskid = (Long) task.get("id");
            CheckTask checktask = CheckTask.findById(taskid);

            Whouse wh = searchWarehouse(checktask.units);
            if(wh != null && wh.user != null) {
                if(checktask.checkor == null || !checktask.checkor.equals(wh.user)) {
                    checktask.shipwhouse = wh;
                    checktask.checkor = wh.user.username;
                    checktask.save();
                }
            } else if(wh == null) {
                //如果是快递、空运、海运则默认为欧嘉
                wh = searchCooperWarehouse(cooperator, checktask.units.shipType);
                if(wh != null && wh.user != null) {
                    if(checktask.checkor == null || !checktask.checkor.equals(wh.user)) {
                        checktask.shipwhouse = wh;
                        checktask.checkor = wh.user.username;
                        checktask.save();
                    }
                }
            }
        }

    }


    public static Whouse searchCooperWarehouse(Cooperator cooperator, Shipment.T shiptype) {
        List<Object> params = new ArrayList<Object>();
        StringBuilder sbd = new StringBuilder(
                " cooperator=? ");
        params.add(cooperator);

        if(shiptype == Shipment.T.SEA) {
            sbd.append(" and isSEA=? ");
            params.add(true);
        }
        if(shiptype == Shipment.T.EXPRESS) {
            sbd.append(" and isEXPRESS=? ");
            params.add(true);
        }
        if(shiptype == Shipment.T.AIR) {
            sbd.append(" and isAIR=? ");
            params.add(true);
        }
        return Whouse.find(sbd.toString(), params.toArray()).first();
    }

    public static void updateExpressWarehouse(Long id) {
        //更新快递单的货代仓库
        List<Map<String, Object>> tasks = DBUtils.rows("select id from CheckTask where units_id=" + id);
        if(tasks.size() > 0) {
            for(Map<String, Object> task : tasks) {
                Long taskid = (Long) task.get("id");
                CheckTask checktask = CheckTask.findById(taskid);
                Whouse wh = CheckTask.searchWarehouse(checktask.units);
                if(wh != null && wh.user != null) {
                    checktask.shipwhouse = wh;
                    checktask.checkor = wh.user.username;
                    checktask.save();
                }
            }
        }
    }


    /**
     * 产生重检任务
     */
    public static void generateRepeatTask(CheckTask.DealType dt, long chechtaskid, long unitid) {
        CheckTask newtask = new CheckTask();
        ProcureUnit punit = ProcureUnit.findById(unitid);
        newtask.units = punit;
        newtask.confirmstat = ConfirmType.UNCONFIRM;
        //重检
        newtask.checkstat = StatType.REPEATCHECK;
        newtask.checknote = "[不发货流程" + chechtaskid + "因" + dt.label() + "自动产生重检单]";
        newtask.creatat = new Date();
        newtask.finishStat = ConfirmType.UNCONFIRM;

        //查找仓库
        Whouse wh = searchWarehouse(punit);
        if(wh != null && wh.user != null) {
            newtask.shipwhouse = wh;
            newtask.checkor = wh.user.username;
        }
        newtask.save();
    }


    public static Whouse searchWarehouse(ProcureUnit units) {
        List<ShipItem> shipitem = units.shipItems;
        List<Object> params = new ArrayList<Object>();
        if(shipitem != null && shipitem.size() > 0) {
            Shipment ment = shipitem.get(0).shipment;
            if(ment != null) {
                StringBuilder sbd = new StringBuilder(
                        " cooperator=? ");
                Cooperator cooperator = ment.cooper;
                if(cooperator == null) cooperator = Cooperator.findById(59l);
                params.add(cooperator);
                if(ment.type == Shipment.T.SEA) {
                    sbd.append(" and isSEA=? ");
                    params.add(true);
                }
                if(ment.type == Shipment.T.EXPRESS) {
                    sbd.append(" and isEXPRESS=? ");
                    params.add(true);
                }
                if(ment.type == Shipment.T.AIR) {
                    sbd.append(" and isAIR=? ");
                    params.add(true);
                }
                return Whouse.find(sbd.toString(), params.toArray()).first();

            } else {
                Cooperator cooperator = Cooperator.findById(59l);
                return searchCooperWarehouse(cooperator, units.shipType);
            }
        } else {
            Cooperator cooperator = Cooperator.findById(59l);
            return searchCooperWarehouse(cooperator, units.shipType);
        }
    }

    /**
     * 保存质检任务且修改相关联的对应的采购计划数据
     */
    public void fullUpdate(CheckTask newCt, String username) {
        this.units.attrs.qty = newCt.qty;
        if(newCt.standBoxQctInfos != null) this.standBoxQctInfos = newCt.standBoxQctInfos;
        if(newCt.tailBoxQctInfo != null) this.tailBoxQctInfo = newCt.tailBoxQctInfo;
        switch(newCt.isship) {
            case SHIP:
                this.checkstat = StatType.CHECKFINISH;
                this.finishStat = ConfirmType.CONFIRM;
                this.updateFinishStat();
                break;
            case NOTSHIP:
                this.checkstat = StatType.CHECKNODEAL;
                //对应采购计划ID的“不发货处理”状态更新为：不发货待处理
                //TODO:启动不发货流程，并进入到“采购计划不发货待处理事务”，为该采购计划的采购单的创建人 生成不发货待处理任务
                startActiviti(username);
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
        this.checkor = newCt.checkor;
        this.qcSample = newCt.qcSample;

        this.unqualifiedQty = newCt.unqualifiedQty;
        if(newCt.samplingTypes != null) this.samplingTypes = newCt.samplingTypes;
        if(newCt.samplingTypes != null) this.inspectionTimes = newCt.inspectionTimes;
        if(newCt.ac != null) this.ac = newCt.ac;
        if(newCt.re != null) this.re = newCt.re;
        if(newCt.cr != null) this.cr = newCt.cr;
        if(newCt.ma != null) this.ma = newCt.ma;
        if(newCt.mi != null) this.mi = newCt.mi;
        if(newCt.aqlBadDescs != null) this.aqlBadDescs = newCt.aqlBadDescs;

        if(newCt.dealway != null) this.dealway = newCt.dealway;
        if(newCt.startTime != null) this.startTime = newCt.startTime;
        if(newCt.endTime != null) this.endTime = newCt.endTime;
        if(newCt.result != null) this.result = newCt.result;
        if(newCt.isship != null) this.isship = newCt.isship;
        if(newCt.checknote != null) this.checknote = newCt.checknote;
        if(newCt.standBoxQctInfo != null) this.standBoxQctInfo = newCt.standBoxQctInfo;
        if(newCt.tailBoxQctInfo != null) this.tailBoxQctInfo = newCt.tailBoxQctInfo;
        if(this.result == ResultType.AGREE) {
            this.isship = ShipType.SHIP;
        }
        this.units.save();
        this.save();
    }


    public void startActiviti(String username) {
        ActivitiDefinition definition = ActivitiDefinition.find("menuCode=?", ACTIVITINAME).first();
        RuntimeService runtimeService = ActivitiEngine.processEngine.getRuntimeService();
        ProcessInstance processInstance = runtimeService
                .startProcessInstanceByKey(definition.processid);
        models.activiti.ActivitiProcess p = new models.activiti.ActivitiProcess();
        p.definition = definition;
        p.objectId = this.id;
        p.billId = String.valueOf(this.units.id);
        p.objectUrl = "/checktasks/showactiviti/" + this.id;
        p.processDefinitionId = processInstance.getProcessDefinitionId();
        p.processInstanceId = processInstance.getProcessInstanceId();
        p.createAt = new Date();
        p.creator = username;
        p.save();
        ActivitiProcess.claimProcess(processInstance.getProcessInstanceId(), username);
        //设置下一步审批人
        taskclaim(processInstance.getProcessInstanceId(), username);

        //修改为不发货待处理
        this.units.shipState = ProcureUnit.S.NOSHIPWAIT;

        this.units.save();
    }

    public void taskclaim(String processInstanceId, String username) {
        //启动流程后设置各节点的审批人
        TaskService taskService = ActivitiEngine.processEngine.getTaskService();
        Task task = taskService.createTaskQuery().processInstanceId(processInstanceId)
                .active().singleResult();
        if(task != null) {
            if(task.getName().equals("采购员")) {
                taskService.claim(task.getId(), this.units.deliveryment.handler.username);
            }
            if(task.getName().equals("运营")) {
                taskService.claim(task.getId(), this.units.handler.username);
            }
            if(task.getName().equals("质检确认")) {
                taskService.claim(task.getId(), username);
            }
        }
    }


    public void editplanArrivDate() {
        if(this.planDeliveryDate != null) {
            this.oldplanDeliveryDate = this.units.attrs.planDeliveryDate;
            this.units.attrs.planDeliveryDate = this.planDeliveryDate;
        }
    }

    /**
     * 采购确认
     *
     * @param wfee
     * @param flow
     * @return
     */
    public java.util.Map<String, Object> processProcureUnit(float wfee, int flow) {
        java.util.Map<String, Object> variableMap = new java.util.HashMap<String, Object>();
        this.units.shipState = ProcureUnit.S.NOSHIPWAIT;
        this.workfee = wfee;
        //修改预计交货时间
        this.editplanArrivDate();

        if(flow == 2) {
            variableMap.put("flow", "2");
            //已完成
            this.checkstat = StatType.CHECKFINISH;
            //合格
            this.isship = ShipType.SHIP;
            this.result = ResultType.AGREE;
            updateFinishStat();
        } else {
            variableMap.put("flow", "1");
            //已处理
            this.checkstat = StatType.CHECKDEAL;
        }

        //退回工厂或者到仓库返工 需要重检
        if(this.dealway == CheckTask.DealType.RETURN ||
                this.dealway == CheckTask.DealType.WAREHOUSE) {
            generateRepeatTask(this.dealway, this.id, this.units.id);
        }

        return variableMap;
    }

    /**
     * 质检确认
     *
     * @return
     */
    public java.util.Map<String, Object> processQc(int flow) {
        java.util.Map<String, Object> variableMap = new java.util.HashMap<String, Object>();
        //退回工厂或者到仓库返工
        //结束
        if(this.dealway == CheckTask.DealType.RETURN ||
                this.dealway == CheckTask.DealType.WAREHOUSE) {
            variableMap.put("flow", "1");
        }
        //代仓库返工
        //不发货 返回采购员
        if(this.dealway == CheckTask.DealType.HELP) {
            if(this.isship == CheckTask.ShipType.SHIP) {
                //发货
                variableMap.put("flow", "1");
                /**
                 * 已检完成
                 */
                this.checkstat = StatType.CHECKFINISH;
                updateFinishStat();
            } else {
                //不发货
                variableMap.put("flow", "2");
            }
        }

        if(flow != 0 && flow == 2) {
            variableMap.put("flow", "2");
        }

        //修改质检为确认状态
        this.units.qcConfirm = ProcureUnit.QCCONFIRM.CONFIRMED;
        return variableMap;
    }


    public void submitActiviti(ActivitiProcess ap, String taskname, float wfee, int flow, String username) {
        if(this.opition == null) this.opition = "";
        java.util.Map<String, Object> variableMap = new java.util.HashMap<String, Object>();
        if(taskname.equals("采购员")) {
            variableMap = processProcureUnit(wfee, flow);
        }
        if(taskname.equals("运营")) {
            variableMap.put("flow", "1");
            //修改运营为确认状态
            this.units.opConfirm = ProcureUnit.OPCONFIRM.CONFIRMED;
        }
        if(taskname.equals("质检确认")) {
            variableMap = processQc(flow);
            if(flow == 2) {
                this.opition = "[取消费用]" + this.opition;
            }
        }


        this.units.save();
        this.save();

        if(this.dealway != null) this.opition = "[" + this.dealway.label() + "]" + this.opition;
        ActivitiProcess.submitProcess(ap.processInstanceId, username, variableMap, this.opition);
        //设置下一步审批人
        taskclaim(ap.processInstanceId, ap.creator);
    }


    /**
     * 更新所有采购计划的质检状态为已完成
     */
    public void updateFinishStat() {
        this.finishStat = ConfirmType.CONFIRM;
        List<CheckTask> checklist = CheckTask.find("units=? and id!=?", units, this.id).fetch();
        for(CheckTask task : checklist) {
            task.finishStat = ConfirmType.CONFIRM;
            task.save();
        }
    }


    /**
     * 显示流程历史意见
     *
     * @param id
     * @param username
     * @return
     */
    public Map<String, Object> showInfo(Long id, String username) {
        ActivitiProcess ap = ActivitiProcess.find("definition.menuCode=? and objectId=?",
                CheckTask.ACTIVITINAME, id).first();
        int issubmit = 0, oldPlanQty = 0;
        String taskname = "";
        List<Whouse> whouses = null;
        ProcureUnit unit = null;
        Date oldplanDeliveryDate = null;

        List<Map<String, String>> infos = new ArrayList<Map<String, String>>();
        if(ap == null) {
            ap = new ActivitiProcess();
        } else {
            //判断是否有权限提交流程
            taskname = ActivitiProcess.privilegeProcess(ap.processInstanceId, username);
            if(StringUtils.isNotBlank(taskname)) {
                issubmit = 1;
                //如果是运营,则查询运营相关信息
                if(taskname.equals("运营")) {
                    CheckTask ct = CheckTask.findById(ap.objectId);
                    oldplanDeliveryDate = ct.oldplanDeliveryDate;
                    unit = ct.units;
                    oldPlanQty = ct.units.attrs.planQty;
                    whouses = Whouse.findByAccount(ct.units.selling.account);
                }
            }
            //查找流程历史信息
            infos = ActivitiProcess.processInfo(ap.processInstanceId);
        }

        Map<String, Object> map = new Hashtable<String, Object>();
        map.put("ap", ap);
        map.put("issubmit", issubmit);
        if(taskname != null) map.put("taskname", taskname);
        map.put("infos", infos);
        if(unit != null) map.put("unit", unit);

        map.put("oldPlanQty", oldPlanQty);
        if(whouses != null) map.put("whouses", whouses);
        if(oldplanDeliveryDate != null) map.put("oldplanDeliveryDate", oldplanDeliveryDate);

        return map;
    }

    /**
     * 将产品定位属性转换成 String 存入DB
     * 或者将 String 转换成 List
     *
     * @param flag
     */
    public void arryParamSetUPForQtInfo(FLAG flag) {
        if(flag.equals(FLAG.ARRAY_TO_STR)) {
            this.standBoxQctInfo = J.json(this.fixNullStr(this.standBoxQctInfos));
            this.tailBoxQctInfo = J.json(this.fixNullStr(this.tailBoxQctInfos));
        } else {
            if(StringUtils.isNotBlank(this.standBoxQctInfo)) this.standBoxQctInfos = JSON.parseArray(this
                    .standBoxQctInfo, CheckTaskDTO.class);
            if(StringUtils.isNotBlank(this.tailBoxQctInfo)) this.tailBoxQctInfos = JSON.parseArray(this
                    .tailBoxQctInfo, CheckTaskDTO.class);
        }
    }

    /**
     * 计算总箱数
     *
     * @return
     */
    public Integer totalBoxNum() {
        this.arryParamSetUPForQtInfo(FLAG.STR_TO_ARRAY);
        Integer totalBoxNum = 0;
        for(CheckTaskDTO checkTaskDTO : this.standBoxQctInfos) {
            totalBoxNum += checkTaskDTO.boxNum;
        }
        for(CheckTaskDTO checkTaskDTO : this.tailBoxQctInfos) {
            totalBoxNum += checkTaskDTO.boxNum;
        }
        return totalBoxNum;
    }

    /**
     * 计算总体积
     *
     * @return
     */
    public Double totalVolume() {
        this.arryParamSetUPForQtInfo(FLAG.STR_TO_ARRAY);
        Double totalVolume = 0d;
        for(CheckTaskDTO checkTaskDTO : this.standBoxQctInfos) {
            totalVolume +=
                    checkTaskDTO.length * checkTaskDTO.width * checkTaskDTO.height * checkTaskDTO.boxNum / 1000000;
        }
        for(CheckTaskDTO checkTaskDTO : this.tailBoxQctInfos) {
            totalVolume +=
                    checkTaskDTO.length * checkTaskDTO.width * checkTaskDTO.height * checkTaskDTO.boxNum / 1000000;
        }
        return totalVolume;
    }

    /**
     * 计算总重量
     *
     * @return
     */
    public Double totalWeight() {
        this.arryParamSetUPForQtInfo(FLAG.STR_TO_ARRAY);
        Double totalWeight = 0d;
        for(CheckTaskDTO checkTaskDTO : this.standBoxQctInfos) {
            totalWeight += checkTaskDTO.singleBoxWeight * checkTaskDTO.boxNum;
        }
        for(CheckTaskDTO checkTaskDTO : this.tailBoxQctInfos) {
            totalWeight += checkTaskDTO.singleBoxWeight * checkTaskDTO.boxNum;
        }
        return totalWeight;
    }

    public String showChecktor() {
        String id = this.units.product.category.categoryId;
        String name = "";
        List<CategoryAssignManagement> categoryAssignManagements = CategoryAssignManagement
                .find("category.categoryId=? AND isCharge =1", id).fetch();
        if(categoryAssignManagements.size() > 0) {
            for(CategoryAssignManagement c : categoryAssignManagements) {
                if(c.isQCrole()) {
                    name += c.user.username + ",";
                }
            }
            return name.length() > 0 ? name.substring(0, name.length() - 1) : "";
        } else {
            return "";
        }
    }
}
