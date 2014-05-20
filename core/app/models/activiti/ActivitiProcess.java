package models.activiti;

import com.google.gson.annotations.Expose;

import javax.persistence.*;

import helper.ActivitiEngine;
import org.activiti.bpmn.model.BpmnModel;
import org.activiti.engine.RepositoryService;
import org.activiti.engine.RuntimeService;
import org.activiti.engine.TaskService;
import org.activiti.engine.impl.bpmn.diagram.ProcessDiagramGenerator;
import org.activiti.engine.task.Task;
import play.db.helper.SqlSelect;
import org.activiti.engine.HistoryService;
import org.activiti.engine.history.HistoricTaskInstance;
import play.db.jpa.Model;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.io.InputStream;


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
     * 菜单编码
     */
    @Expose
    public String menuCode;

    /**
     * 菜单名称
     */
    @Expose
    public String menuName;

    /**
     * 流程名称
     */
    @Expose
    public String processName;

    /**
     * 对象ID
     */
    @Expose
    public Long objectId;

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
     * 显示流程图
     *
     * @param pdId
     * @param piId
     * @return
     */
    public static InputStream processImage(String pdId, String piId) {
        RepositoryService repositoryService1 = ActivitiEngine.processEngine.getRepositoryService();

        BpmnModel bpmnModel = repositoryService1.getBpmnModel(pdId);
        new org.activiti.bpmn.BpmnAutoLayout(bpmnModel).execute();

        RuntimeService runtimeService = ActivitiEngine.processEngine.getRuntimeService();


        // 会通过id -> parent_id -> parent_id -> ... 循环找出所有的执行中的子流程
        List<String> highLightedFlows = runtimeService.getActiveActivityIds(piId);
        return ProcessDiagramGenerator.generateDiagram(bpmnModel, "png", highLightedFlows);
    }


    /**
     * 查找当前用户的任务
     *
     * @param userid
     * @return
     */
    public static List<String> userTask(String userid) {
        List<String> processlist = new ArrayList<String>();

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
            String sql = SqlSelect.whereIn("processInstanceId", userlist).toString();
            List<ActivitiProcess> prolist = ActivitiProcess.find(sql).fetch();
            return prolist;
        } else {
            return new java.util.ArrayList<ActivitiProcess>();
        }
    }


    /**
     * 提交流程
     *
     * @param pdId
     * @param userid
     */
    public static void submitProcess(String pdId, String userid) {


        TaskService taskService = ActivitiEngine.processEngine.getTaskService();
        List<Task> list = taskService.createTaskQuery().processInstanceId(pdId).taskAssignee(userid).list();

        if(list != null && list.size() > 0) {

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
        List<ActivitiProcess> prolist = ActivitiProcess.find(SqlSelect.whereIn("processInstanceId",
                userlist)).fetch(50);
        return prolist;
    }


    /**
     * 查找历史任务
     *
     * @param userid
     * @return
     */
    public static List<String> userHistoryTask(String userid) {
        HistoryService historyService = ActivitiEngine.processEngine.getHistoryService();

        List<HistoricTaskInstance> list = historyService.createHistoricTaskInstanceQuery().taskAssignee(userid).list();

        List<String> processlist = new ArrayList<String>();
        for(HistoricTaskInstance e : list) {
            processlist.add(e.getProcessInstanceId());
        }
        return processlist;
    }

    /**
     * 查找历史流程图
     *
     * @param pdId
     * @param piId
     * @return
     */
    public static InputStream processHistoryImage(String pdId, String piId) {
        RepositoryService repositoryService1 = ActivitiEngine.processEngine.getRepositoryService();

        BpmnModel bpmnModel = repositoryService1.getBpmnModel(pdId);
        new org.activiti.bpmn.BpmnAutoLayout(bpmnModel).execute();
        return ProcessDiagramGenerator.generatePngDiagram(bpmnModel);
    }
}
