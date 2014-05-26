package helper;

/**
 * Created by IntelliJ IDEA.
 * User: cary
 * Date: 05-16-14
 * Time: 上午10:09
 */

import org.activiti.engine.ProcessEngineConfiguration;
import org.activiti.engine.ProcessEngine;
import org.activiti.engine.RepositoryService;
import org.activiti.engine.repository.DeploymentBuilder;
import org.activiti.engine.RuntimeService;
import org.activiti.engine.runtime.ProcessInstance;

import java.util.Date;

import org.activiti.engine.impl.context.Context;
import org.activiti.engine.impl.cfg.ProcessEngineConfigurationImpl;
import play.db.DB;

public class ActivitiEngine {

    public static ProcessEngine processEngine;

    public static void initEngine() {
        // 不使用配置文件，在内存中创建配置对象
        ProcessEngineConfiguration processEngineConfiguration =
                ProcessEngineConfiguration.createStandaloneInMemProcessEngineConfiguration();
        processEngineConfiguration.setDatabaseSchemaUpdate(
                ProcessEngineConfiguration.DB_SCHEMA_UPDATE_FALSE);
        processEngineConfiguration.setDataSource(DB.datasource);
        processEngineConfiguration.setJobExecutorActivate(true);

        processEngineConfiguration.setActivityFontName("宋体");
        processEngineConfiguration.setLabelFontName("宋体");
        processEngine = processEngineConfiguration.buildProcessEngine();
        Context.setProcessEngineConfiguration((ProcessEngineConfigurationImpl) processEngineConfiguration);

        //HelloWorldProcessTest();

    }


    public static void HelloWorldProcessTest() {
        RepositoryService repositoryService = processEngine.getRepositoryService();
        DeploymentBuilder builder = repositoryService.createDeployment();
        builder.addClasspathResource("res/DemoProcess.bpmn");
        builder.deploy();
        RuntimeService runtimeService = processEngine.getRuntimeService();
        ProcessInstance processInstance = runtimeService
                .startProcessInstanceByKey("process1");

        models.activiti.ActivitiProcess p = new models.activiti.ActivitiProcess();
//        p.menuCode = "aaa";
//        p.menuName = "菜单";
//        p.processName = "质检";
        p.objectId = 2L;
        p.objectUrl = "xx";
        p.processDefinitionId = processInstance.getProcessDefinitionId();
        p.processInstanceId = processInstance.getProcessInstanceId();
        p.createAt = new Date();
        p.save();

        //ActivitiProcess.submitProcess(processInstance.getProcessInstanceId(),"wyatt");
        // ActivitiProcess.submitProcess(processInstance.getProcessInstanceId(),"su");


//        		    TaskService taskService = processEngine.getTaskService();
//        		    TaskQuery taskQuery = taskService.createTaskQuery();
//        		    List<Task> tasks = taskQuery.taskCandidateGroup("management").list();
//        		    System.out.println("用户组management的任务数：" + tasks.size());
//        		    for (Task task : tasks) {
//        		      taskService.claim(task.getId(), "kermit");
//        		      taskService.complete(task.getId());
//        		      System.out.println("用户kermit有任务 " + task.getName() + "，认领并执行！");
//        		    }
        //		    HistoricProcessInstance historicProcessInstance =
        //		        processEngine.getHistoryService()
        //		        .createHistoricProcessInstanceQuery()
        //		        .processInstanceId(processInstance.getId()).singleResult();
        //		    System.out.println("流程结束时间：" + historicProcessInstance.getEndTime());
        //		    processEngine.close();

//        System.out.println("xx::" + processInstance.getProcessDefinitionId());
//        RepositoryService repositoryService1 = processEngine.getRepositoryService();
//        java.io.InputStream is = null;
//        BpmnModel bpmnModel = repositoryService1.getBpmnModel(processInstance.getProcessDefinitionId());
//        // is = repositoryService1.getProcessDiagram(processInstance.getProcessDefinitionId());
//
//        new org.activiti.bpmn.BpmnAutoLayout(bpmnModel).execute();
//        //	is = ProcessDiagramGenerator.generatePngDiagram(bpmnModel);
//
//
//        List<String> highLightedFlows = new ArrayList<String>();
//        TaskService taskService = processEngine.getTaskService();
//        TaskQuery taskQuery = taskService.createTaskQuery();
//        List<Task> tasks = taskQuery.taskCandidateGroup("management").list();
//        System.out.println("用户组management的任务数：" + tasks.size());
//        List<String> actList = new ArrayList<String>();
//
//        System.out.println(tasks.size());
//        for(Task task : tasks) {
//            ProcessDefinitionEntity def = (ProcessDefinitionEntity) ((RepositoryServiceImpl) repositoryService)
//                    .getDeployedProcessDefinition(task.getProcessDefinitionId());
//            List<ActivityImpl> activitiList = def.getActivities();
//            //获取当前task节点ID
//            String taskId = task.getTaskDefinitionKey();
//            //将当前任务节点加入历史节点集合中
//            actList.add(taskId);
//
//            // System.out.println(taskId);
//
//            for(ActivityImpl activityImpl : activitiList) {
//
//                String id = activityImpl.getId();//usertask1
//                highLightedFlows.add(activityImpl.getId());
//
//                //                String type = (String) activityImpl.getProperty("type");//startEvent、userTask
//                //                String sourceId =  (String) activityImpl.getId();//startEvent1
//                //                List<PvmTransition> outTransitions = activityImpl.getOutgoingTransitions();// 获取从某个节点出来的所有线路
//                //                for (PvmTransition tr : outTransitions) {
//                //                        PvmActivity ac = tr.getDestination(); // 获取线路的终点节点
//                //                        String id = ac.getId();//usertask1
//                //                                highLightedFlows.add(tr.getId());
//                //
//                //                }
//            }
//        }
//
//
//        is = ProcessDiagramGenerator.generateDiagram(bpmnModel, "png", highLightedFlows);
//
//        //renderBinary(is);
    }



























}
