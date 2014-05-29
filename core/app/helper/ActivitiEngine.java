package helper;

/**
 * Created by IntelliJ IDEA.
 * User: cary
 * Date: 05-16-14
 * Time: 上午10:09
 */

import org.activiti.engine.ProcessEngineConfiguration;
import org.activiti.engine.ProcessEngine;
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
    }

}
