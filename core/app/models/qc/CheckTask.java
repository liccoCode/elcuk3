package models.qc;

import com.google.gson.annotations.Expose;
import com.sun.xml.bind.v2.TODO;
import helper.ActivitiEngine;
import helper.DBUtils;
import models.activiti.ActivitiDefinition;
import models.activiti.ActivitiProcess;
import models.procure.ProcureUnit;
import models.procure.ShipItem;
import models.procure.Shipment;
import models.product.Whouse;
import org.activiti.engine.RepositoryService;
import org.activiti.engine.RuntimeService;
import org.activiti.engine.repository.DeploymentBuilder;
import org.activiti.engine.runtime.ProcessInstance;
import play.data.binding.As;
import play.data.validation.Validation;
import play.db.jpa.Model;

import java.util.ArrayList;
import java.util.List;

import javax.persistence.*;
import java.util.Map;
import java.util.Date;

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

    @Expose
    public int workers;

    /**
     * 人工费
     */
    @Expose
    public float workfee;

    /**
     * 预计交货日期
     */
    @Expose
    public Date planArrivDate;

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
        List<Map<String, Object>> units = DBUtils.rows("select id from ProcureUnit where isCheck=0");
        for(Map<String, Object> unit : units) {
            Long unitid = (Long) unit.get("id");
            CheckTask task = CheckTask.find("units.id=?", unitid).first();

            if(task == null) {
                CheckTask newtask = new CheckTask();
                ProcureUnit punit = ProcureUnit.findById(unitid);

                newtask.units = punit;
                newtask.confirmstat = ConfirmType.UNCONFIRM;
                newtask.checkstat = StatType.UNCHECK;

                //查找仓库
                Whouse wh = searchWarehouse(punit.shipItems);
                if(wh != null) {
                    newtask.shipwhouse = wh;
                }

                newtask.save();
                DBUtils.execute("update ProcureUnit set isCheck=1 where id=" + unitid);
            }
        }

        List<Map<String, Object>> tasks = DBUtils.rows("select id from CheckTask where shipwhouse_id is null");
        for(Map<String, Object> task : tasks) {
            Long taskid = (Long) task.get("id");
            CheckTask checktask = CheckTask.findById(taskid);

            Whouse wh = searchWarehouse(checktask.units.shipItems);
            if(wh != null) {
                checktask.shipwhouse = wh;
                checktask.save();
            }
        }
    }


    public static Whouse searchWarehouse(List<ShipItem> shipitem) {
        List<Object> params = new ArrayList<Object>();
        if(shipitem != null && shipitem.size() > 0) {
            Shipment ment = shipitem.get(0).shipment;
            if(ment != null) {
                StringBuilder sbd = new StringBuilder(
                        " cooperator=? ");
                params.add(ment.cooper);
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

            }
        }
        return null;
    }

    /**
     * 保存质检任务且修改相关联的对应的采购计划数据
     */
    public void fullSave(String username) {
        long diff = this.endTime.getTime() - this.startTime.getTime();
        this.workhour = diff / (60 * 60 * 1000);
        this.units.attrs.qty = this.qty;
        switch(this.isship) {
            case SHIP:
                this.checkstat = StatType.CHECKFINISH;
                if(this.units.shipState == ProcureUnit.S.NOSHIP) {
                    //TODO:采购计划的“不发货处理”状态还原成 当初系统开启此不发货流程时 采购计划的“不发货处理”状态值
                    //TODO:结束不发货流程
                }
                break;
            case NOTSHIP:
                this.checkstat = StatType.CHECKNODEAL;
                //对应采购计划ID的“不发货处理”状态更新为：不发货待处理
                this.units.shipState = ProcureUnit.S.NOSHIP;
                //TODO:启动不发货流程，并进入到“采购计划不发货待处理事务”，为该采购计划的采购单的创建人 生成不发货待处理任务
                startActiviti(username);
                break;
        }
        this.save();
    }


    public void startActiviti(String username) {
        ActivitiDefinition definition = ActivitiDefinition.find("menuCode=?", ACTIVITINAME).first();
//        RepositoryService repositoryService = ActivitiEngine.processEngine.getRepositoryService();
//        DeploymentBuilder builder = repositoryService.createDeployment();
//        builder.addClasspathResource(definition.processXml);
//        builder.deploy();

        RuntimeService runtimeService = ActivitiEngine.processEngine.getRuntimeService();
        ProcessInstance processInstance = runtimeService
                .startProcessInstanceByKey(definition.processid);
        models.activiti.ActivitiProcess p = new models.activiti.ActivitiProcess();
        p.definition = definition;
        p.objectId = this.id;
        p.objectUrl = "/checktasks/showactiviti/" + this.id;
        p.processDefinitionId = processInstance.getProcessDefinitionId();
        p.processInstanceId = processInstance.getProcessInstanceId();
        p.createAt = new Date();
        p.save();
        ActivitiProcess.claimProcess(processInstance.getProcessInstanceId(), username);
    }


    public void editplanArrivDate() {
        if(this.planArrivDate != null) {
            List<ShipItem> ships = this.units.shipItems;
            for(ShipItem item : ships) {
                item.shipment.dates.planArrivDate = this.planArrivDate;
                item.save();
            }
        }
    }


    public void submitActiviti(int flow, long id, String username) {

        ActivitiProcess ap = ActivitiProcess.findById(id);
        //判断是否有权限提交流程
        String taskname = ActivitiProcess.privilegeProcess(ap.processInstanceId, username);
        java.util.Map<String, Object> variableMap = new java.util.HashMap<String, Object>();
        if(taskname.equals("采购员")) {
            //修改预计交货时间
            this.editplanArrivDate();
            if(flow == 2) {
                variableMap.put("flow", "2");
            } else {
                variableMap.put("flow", "1");
            }
        }
        if(taskname.equals("运营")) {
            variableMap.put("flow", "1");
        }
        if(taskname.equals("质检确认")) {
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
                } else {
                    //不发货
                    variableMap.put("flow", "2");
                }
            }
        }
        ActivitiProcess.submitProcess(ap.processInstanceId, username, variableMap, this.opition);
    }

}
