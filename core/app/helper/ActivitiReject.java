package helper;

import org.activiti.engine.ActivitiException;
import org.activiti.engine.RepositoryService;
import org.activiti.engine.impl.ProcessEngineImpl;
import org.activiti.engine.impl.RepositoryServiceImpl;
import org.activiti.engine.impl.db.DbSqlSession;
import org.activiti.engine.impl.db.DbSqlSessionFactory;
import org.activiti.engine.impl.db.ListQueryParameterObject;
import org.activiti.engine.impl.persistence.entity.*;
import org.activiti.engine.impl.pvm.process.ActivityImpl;
import org.apache.ibatis.session.SqlSession;
import org.apache.ibatis.session.SqlSessionFactory;

import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

/**
 * Created by IntelliJ IDEA.
 * User: mac
 * Date: 14-5-21
 * Time: 下午5:58
 *
 * @deprecated 无人使用
 */
public class ActivitiReject {
    public static final int I_NO_OPERATION = 0;
    public static final int I_DONE = 1;
    public static final int I_TASK_NOT_FOUND = 2;
    public static final int I_ROLLBACK = 3;

    /*
    * 实现回退方法
    */
    public static int dbBackTo(String currentTaskId, String backToTaskId) {
        int result = ActivitiReject.I_NO_OPERATION;
        SqlSession sqlSession = getSqlSession();
        TaskEntity currentTaskEntity = getCurrentTaskEntity(currentTaskId);
        HistoricTaskInstanceEntity backToHistoricTaskInstanceEntity = getHistoryTaskEntity(backToTaskId);
        if(currentTaskEntity == null
                || backToHistoricTaskInstanceEntity == null) {
            return ActivitiReject.I_TASK_NOT_FOUND;
        }
        String processDefinitionId = currentTaskEntity.getProcessDefinitionId();
        String executionId = currentTaskEntity.getExecutionId();
        String currentTaskEntityId = currentTaskEntity.getId();
        String backToHistoricTaskInstanceEntityId = backToHistoricTaskInstanceEntity
                .getId();
        String backToTaskDefinitionKey = backToHistoricTaskInstanceEntity
                .getTaskDefinitionKey();
        String backToAssignee = backToHistoricTaskInstanceEntity.getAssignee();
        boolean success = false;
        try {
// 1.
            StepOne_use_hi_taskinst_to_change_ru_task(sqlSession,
                    currentTaskEntity, backToHistoricTaskInstanceEntity);
// 2.
            StepTwo_change_ru_identitylink(sqlSession, currentTaskEntityId,
                    backToHistoricTaskInstanceEntityId, backToAssignee);
// 3.
            StepThree_change_ru_execution(sqlSession, executionId,
                    processDefinitionId, backToTaskDefinitionKey);

            success = true;
        } catch(Exception e) {
            throw new ActivitiException("dbBackTo Exception", e);

        } finally {
            if(success) {
                sqlSession.commit();
                result = ActivitiReject.I_DONE;
            } else {
                sqlSession.rollback();
                result = ActivitiReject.I_ROLLBACK;
            }
            sqlSession.close();
        }
        return result;
    }

    private static void StepThree_change_ru_execution(SqlSession sqlSession,
                                                      String executionId, String processDefinitionId,
                                                      String backToTaskDefinitionKey) throws Exception {
        List<ExecutionEntity> currentExecutionEntityList = sqlSession
                .selectList("selectExecution", executionId);
        if(currentExecutionEntityList.size() > 0) {
            ActivityImpl activity = getActivitiImp(processDefinitionId,
                    backToTaskDefinitionKey);
            Iterator<ExecutionEntity> execution = currentExecutionEntityList
                    .iterator();
            while(execution.hasNext()) {
                ExecutionEntity e = execution.next();
                e.setActivity(activity);
                p(sqlSession.update("updateExecution", e));
            }
        }
    }

    private static void StepTwo_change_ru_identitylink(SqlSession sqlSession,
                                                       String currentTaskEntityId,
                                                       String backToHistoricTaskInstanceEntityId, String backToAssignee)
            throws Exception {
        ListQueryParameterObject para = new ListQueryParameterObject();
        para.setParameter(currentTaskEntityId);
        List<IdentityLinkEntity> currentTaskIdentityLinkEntityList = sqlSession
                .selectList("selectIdentityLinksByTask", para);

        if(currentTaskIdentityLinkEntityList.size() > 0) {
            Iterator<IdentityLinkEntity> identityLinkEntityList = currentTaskIdentityLinkEntityList
                    .iterator();
            IdentityLinkEntity identityLinkEntity;
            TaskEntity tmpTaskEntity;
            tmpTaskEntity = new TaskEntity();
            tmpTaskEntity.setId(backToHistoricTaskInstanceEntityId);
            while(identityLinkEntityList.hasNext()) {
                identityLinkEntity = identityLinkEntityList.next();
                identityLinkEntity.setTask(tmpTaskEntity);
                identityLinkEntity.setUserId(backToAssignee);
                Map<String, Object> parameters = new HashMap<String, Object>();
                parameters.put("id", identityLinkEntity.getId());
                sqlSession.delete("deleteIdentityLink", parameters);
                sqlSession.insert("insertIdentityLink", identityLinkEntity);
            }
        }
    }

    private static void StepOne_use_hi_taskinst_to_change_ru_task(
            SqlSession sqlSession, TaskEntity currentTaskEntity,
            HistoricTaskInstanceEntity backToHistoricTaskInstanceEntity)
            throws Exception {
        sqlSession.delete("deleteTask", currentTaskEntity);
        currentTaskEntity.setName(backToHistoricTaskInstanceEntity.getName());
        currentTaskEntity.setTaskDefinitionKey(backToHistoricTaskInstanceEntity
                .getTaskDefinitionKey());
        currentTaskEntity.setId(backToHistoricTaskInstanceEntity.getId());
        sqlSession.insert("insertTask", currentTaskEntity);
    }

    public static void p(Object o) {
        System.out.println(o);
    }

    private static ActivityImpl getActivitiImp(String processDefinitionId,
                                               String taskDefinitionKey) {
        RepositoryService repositoryService = ActivitiEngine.processEngine
                .getRepositoryService();
        ProcessDefinitionEntity processDefinition = (ProcessDefinitionEntity) ((RepositoryServiceImpl) repositoryService)
                .getDeployedProcessDefinition(processDefinitionId);
        List<ActivityImpl> activitiList = processDefinition.getActivities();

        boolean b;
        Object activityId;
        for(ActivityImpl activity : activitiList) {

            activityId = activity.getId();
            b = activityId.toString().equals(taskDefinitionKey);
            if(b) {
                return activity;
            }
        }
        return null;
    }

    private static TaskEntity getCurrentTaskEntity(String id) {
        return (TaskEntity) ActivitiEngine.processEngine.getTaskService().createTaskQuery()
                .taskId(id).singleResult();
    }

    private static HistoricTaskInstanceEntity getHistoryTaskEntity(String id) {
        return (HistoricTaskInstanceEntity) ActivitiEngine.processEngine.getHistoryService()
                .createHistoricTaskInstanceQuery().taskId(id).singleResult();

    }

    private static SqlSession getSqlSession() {
        ProcessEngineImpl processEngine = (ProcessEngineImpl) ActivitiEngine.processEngine;
        DbSqlSessionFactory dbSqlSessionFactory = (DbSqlSessionFactory) processEngine
                .getProcessEngineConfiguration().getSessionFactories()
                .get(DbSqlSession.class);
        SqlSessionFactory sqlSessionFactory = dbSqlSessionFactory
                .getSqlSessionFactory();
        return sqlSessionFactory.openSession();
    }
}
