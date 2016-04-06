package helper;

/**
 * Created by IntelliJ IDEA.
 * User: cary
 * Date: 05-16-14
 * Time: 上午10:09
 */

import org.activiti.engine.ProcessEngine;
import org.activiti.engine.ProcessEngineConfiguration;
import org.activiti.engine.impl.cfg.ProcessEngineConfigurationImpl;
import org.activiti.engine.impl.context.Context;
import play.db.DB;

public class ActivitiEngine {
    public static ProcessEngine processEngine;

    public static void initEngine() {
        // 不使用配置文件，在内存中创建配置对象
        ProcessEngineConfiguration processEngineConfiguration =
                ProcessEngineConfiguration.createStandaloneInMemProcessEngineConfiguration();
        processEngineConfiguration.setDatabaseSchemaUpdate(
                ProcessEngineConfiguration.DB_SCHEMA_UPDATE_FALSE);
        processEngineConfiguration.setDataSource(DB.getDataSource());
        processEngineConfiguration.setJobExecutorActivate(true);

        processEngineConfiguration.setActivityFontName("微软雅黑");
        processEngineConfiguration.setLabelFontName("微软雅黑");
        processEngine = processEngineConfiguration.buildProcessEngine();
        Context.setProcessEngineConfiguration((ProcessEngineConfigurationImpl) processEngineConfiguration);
    }
}