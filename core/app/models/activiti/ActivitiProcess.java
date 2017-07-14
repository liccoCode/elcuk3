package models.activiti;

import com.google.gson.annotations.Expose;
import helper.ActivitiEngine;
import models.embedded.ERecordBuilder;
import org.activiti.bpmn.model.BpmnModel;
import org.activiti.engine.HistoryService;
import org.activiti.engine.RepositoryService;
import org.activiti.engine.RuntimeService;
import org.activiti.engine.TaskService;
import org.activiti.engine.history.HistoricTaskInstance;
import org.activiti.engine.history.HistoricVariableInstance;
import org.activiti.engine.history.HistoricVariableInstanceQuery;
import org.activiti.engine.impl.bpmn.diagram.ProcessDiagramGenerator;
import org.activiti.engine.runtime.ProcessInstance;
import org.activiti.engine.task.Task;
import org.apache.commons.lang.StringUtils;
import org.joda.time.DateTime;
import play.db.helper.SqlSelect;
import play.db.jpa.Model;

import javax.persistence.CascadeType;
import javax.persistence.Entity;
import javax.persistence.ManyToOne;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Map;


/**
 * 所有费用的类型
 * User: wyattpan
 * Date: 3/19/12
 * Time: 10:21 AM
 */
@Entity
public class ActivitiProcess extends Model {

    public ActivitiProcess() {
    }


    /**
     * 流程定义
     */
    @ManyToOne(cascade = {CascadeType.PERSIST})
    public ActivitiDefinition definition;

    /**
     * 对象ID
     */
    @Expose
    public Long objectId;

    /**
     * 对应的单据ID
     */
    @Expose
    public String billId;

    /**
     * 对象的网页
     */
    @Expose
    public String objectUrl;

    /*
     *流程ID
     */
    @Expose
    public String processDefinitionId;

    @Expose
    public String processInstanceId;


    @Expose
    public Date createAt;

    /**
     * 流程启动人
     */
    @Expose
    public String creator;


    /**
     * 显示流程图
     *
     * @param processDefinitionId
     * @param processInstanceId
     * @return
     */
    public static InputStream processImage(String processDefinitionId, String processInstanceId) {
        RepositoryService repositoryService1 = ActivitiEngine.processEngine.getRepositoryService();


        BpmnModel bpmnModel = repositoryService1.getBpmnModel(processDefinitionId);
        RuntimeService runtimeService = ActivitiEngine.processEngine.getRuntimeService();
        try {
            List<String> highLightedFlows = runtimeService.getActiveActivityIds(processInstanceId);
            if(highLightedFlows != null && highLightedFlows.size() > 0) {
                return ProcessDiagramGenerator.generateDiagram(bpmnModel, "png", highLightedFlows);
            } else {
                return ProcessDiagramGenerator.generatePngDiagram(bpmnModel);
            }
        } catch(Exception e) {
            return ProcessDiagramGenerator.generatePngDiagram(bpmnModel);
        }
    }


    /**
     * 查找当前用户的任务
     *
     * @param userid
     * @return
     */
    public static List<String> userTask(String userid) {
        List<String> processlist = new ArrayList<>();

        TaskService taskService = ActivitiEngine.processEngine.getTaskService(); //活动task服务
        List<Task> tasklist = taskService.createTaskQuery().taskAssignee(userid).list();

        for(Task t : tasklist) {
            processlist.add(t.getProcessInstanceId());
        }
        return processlist;
    }


    /**
     * 查找当前用户的流程
     *
     * @param userid
     * @return
     */
    public static List<ActivitiProcess> findProcess(String userid) {
        List<String> userlist = userTask(userid);
        if(userlist.size() > 0) {
            String sql = SqlSelect.whereIn("processInstanceId", userlist).toString() + " ORDER BY createAt desc ";
            List<ActivitiProcess> prolist = ActivitiProcess.find(sql).fetch();
            return prolist;
        } else {
            return new java.util.ArrayList<>();
        }
    }


    /**
     * 提交流程
     *
     * @param processDefinitionId
     * @param userid
     */
    public static void submitProcess(String processDefinitionId, String userid,
                                     java.util.Map<String, Object> variableMap,
                                     String opition) {
        TaskService taskService = ActivitiEngine.processEngine.getTaskService();
        List<Task> list = taskService.createTaskQuery().processInstanceId(processDefinitionId).taskAssignee(userid)
                .list();

        if(list != null && list.size() > 0) {
            taskService.setVariableLocal(list.get(0).getId(), "opition", opition);
            if(variableMap != null && variableMap.size() > 0) {
                taskService.complete(list.get(0).getId(), variableMap);
            } else {
                taskService.complete(list.get(0).getId());
            }
        }
    }


    /**
     * 是否有权限提交
     *
     * @param processInstanceId
     * @param userid
     */
    public static String privilegeProcess(String processInstanceId, String userid) {
        TaskService taskService = ActivitiEngine.processEngine.getTaskService();
        List<Task> list = taskService.createTaskQuery().processInstanceId(processInstanceId).active()
                .taskAssignee(userid).list();
        if(list != null && list.size() > 0) {
            return list.get(0).getName();
        } else
            return null;
    }


    /**
     * 流程历史信息
     *
     * @param processInstanceId
     */
    public static List<Map<String, String>> processInfo(String processInstanceId) {
        List<Map<String, String>> result = new ArrayList<>();
        if(StringUtils.isBlank(processInstanceId)) {
            return result;
        }
        TaskService taskService = ActivitiEngine.processEngine.getTaskService();
        HistoryService historyService = ActivitiEngine.processEngine.getHistoryService();

        List<HistoricTaskInstance> list = historyService.createHistoricTaskInstanceQuery()
                .processInstanceId(processInstanceId).list();
        HistoricVariableInstanceQuery detail =
                historyService.createHistoricVariableInstanceQuery()
                        .processInstanceId(processInstanceId);

        if(list != null && list.size() > 0) {
            for(HistoricTaskInstance t : list) {
                java.util.HashMap<String, String> map = new java.util.HashMap<>();
                map.put("name", t.getName());
                map.put("assignee", t.getAssignee());
                if(t.getStartTime() != null) {
                    map.put("begintime", new DateTime(t.getStartTime()).toString("yyyy-MM-dd HH:mm:ss"));
                } else {
                    map.put("begintime", "");
                }
                if(t.getEndTime() != null) {
                    map.put("endtime", new DateTime(t.getEndTime()).toString("yyyy-MM-dd HH:mm:ss"));
                } else {
                    map.put("endtime", "");
                }
                String opition = "";

                try {
                    List<org.activiti.engine.history.HistoricVariableInstance> variablelist = detail.taskId(t.getId())
                            .list();
                    if(variablelist.size() > 0) {
                        for(HistoricVariableInstance var : variablelist) {
                            if(var.getVariableName().equals("opition")) {
                                opition = (String) var.getValue();
                            }
                        }
                    }
                } catch(Exception e) {
                }
                map.put("opition", opition);
                result.add(map);
            }
            return result;
        } else
            return result;
    }


    /**
     * 启动流程后,认领完成第一步流程
     *
     * @param processDefinitionId
     * @param userid
     */
    public static void claimProcess(String processDefinitionId, String userid) {
        TaskService taskService = ActivitiEngine.processEngine.getTaskService();
        List<Task> list = taskService.createTaskQuery().processInstanceId(processDefinitionId).active().list();
        if(list != null && list.size() > 0) {
            taskService.claim(list.get(0).getId(), userid);
            taskService.complete(list.get(0).getId());
        }
    }

    /**
     * 查找历史流程
     *
     * @param userid
     * @return
     */
    public static List<ActivitiProcess> findHistoryProcess(String userid) {
        List<String> userlist = userHistoryTask(userid);
        if(userlist.size() > 0) {
            String sql = SqlSelect.whereIn("processInstanceId", userlist).toString() + " ORDER BY createAt desc ";
            List<ActivitiProcess> prolist = ActivitiProcess.find(sql).fetch(100);
            return prolist;
        } else
            return new ArrayList<>();
    }


    /**
     * 查找正在执行流程
     *
     * @param userid
     * @return
     */
    public static List<ActivitiProcess> findRunProcess(String userid) {
        List<String> userlist = userRunTask(userid);
        if(userlist.size() > 0) {
            String sql = SqlSelect.whereIn("processInstanceId", userlist).toString() + " ORDER BY createAt desc ";
            List<ActivitiProcess> prolist = ActivitiProcess.find(sql).fetch();
            return prolist;
        } else
            return new ArrayList<>();
    }


    /**
     * 查找历史任务
     *
     * @param userid
     * @return
     */
    public static List<String> userHistoryTask(String userid) {
        HistoryService historyService = ActivitiEngine.processEngine.getHistoryService();

        List<HistoricTaskInstance> list = historyService.createHistoricTaskInstanceQuery().processFinished().
                taskAssignee(userid).list();
        List<String> processlist = new ArrayList<>();
        for(HistoricTaskInstance e : list) {
            processlist.add(e.getProcessInstanceId());
        }
        return processlist;
    }


    /**
     * 查找正在执行任务
     *
     * @param userid
     * @return
     */
    public static List<String> userRunTask(String userid) {
        List<String> usertasks = userTask(userid);

        List<ProcessInstance> processInstances = ActivitiEngine.processEngine.getRuntimeService()
                .createProcessInstanceQuery()
                .involvedUser(userid)
                .list();
        List<String> processlist = new ArrayList<>();
        for(ProcessInstance e : processInstances) {
            if(!usertasks.contains(e.getProcessInstanceId())) {
                processlist.add(e.getProcessInstanceId());
            }
        }
        return processlist;
    }

    /**
     * 判断流程是否结束
     *
     * @param processInstanceId
     * @return
     */
    public static boolean isEnded(String processInstanceId) {
        RuntimeService runtimeService = ActivitiEngine.processEngine.getRuntimeService();
        ProcessInstance processInstance = runtimeService.createProcessInstanceQuery()
                .processInstanceId(processInstanceId).singleResult();
        if(processInstance == null) return true;
        return processInstance.isEnded();
    }

    public static void endTask(long processid, String type) {
        RuntimeService runtimeService = ActivitiEngine.processEngine.getRuntimeService();
        //当前 Task
        ActivitiProcess ap = ActivitiProcess.findById(processid);
        //挂起流程
        runtimeService.suspendProcessInstanceById(ap.processInstanceId);
        //删除activitiProcess的关系
        ap.delete();
        //log 记录activitiProcess的关系
        new ERecordBuilder(type).msgArgs(processid, ap.objectId, ap.billId).fid(ap.objectId).save();
    }
}
