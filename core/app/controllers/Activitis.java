package controllers;

import controllers.api.SystemOperation;
import exception.PaymentException;
import helper.ActivitiEngine;
import models.activiti.ActivitiDefinition;
import models.activiti.ActivitiProcess;
import models.view.Ret;
import org.activiti.engine.RepositoryService;
import org.activiti.engine.repository.DeploymentBuilder;
import play.mvc.Controller;
import play.mvc.With;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by IntelliJ IDEA.
 * User: cary
 * Date: 5/10/14
 * Time: 4:25 PM
 * @deprecated
 */
@With({GlobalExceptionHandler.class, Secure.class, SystemOperation.class})
public class Activitis extends Controller {

    public static void index(Long id) {
        List<ActivitiProcess> aps = null;
        aps = ActivitiProcess.findProcess(Secure.Security.connected());

        ActivitiProcess ap = new ActivitiProcess();
        if(aps != null && aps.size() > 0) {
            ap = (ActivitiProcess) aps.get(0);
            if(id != null && id != 0L) ap = ActivitiProcess.findById(id);
        } else {
            aps = new ArrayList<ActivitiProcess>();
        }
        render(aps, ap);
    }


    public static void indexhistory(Long id) {
        List<ActivitiProcess> aps = null;
        aps = ActivitiProcess.findHistoryProcess(Secure.Security.connected());

        ActivitiProcess ap = new ActivitiProcess();
        if(aps != null && aps.size() > 0) {
            ap = (ActivitiProcess) aps.get(0);
            if(id != null && id != 0L) ap = ActivitiProcess.findById(id);
        } else {
            aps = new ArrayList<ActivitiProcess>();
        }
        render(aps, ap);
    }

    public static void indexrun(Long id) {
        List<ActivitiProcess> aps = null;
        aps = ActivitiProcess.findRunProcess(Secure.Security.connected());

        ActivitiProcess ap = new ActivitiProcess();
        if(aps != null && aps.size() > 0) {
            ap = (ActivitiProcess) aps.get(0);
            if(id != null && id != 0L) ap = ActivitiProcess.findById(id);
        } else {
            aps = new ArrayList<ActivitiProcess>();
        }
        render(aps, ap);
    }


    @Check("activitis.definition")
    public static void definition() {
        List<ActivitiDefinition> ads = ActivitiDefinition.findAll();
        render(ads);
    }

    public static void update(Long id, String menucode, String menuname, String processname, String processxml,
                              String processid) {
        try {
            ActivitiDefinition ad = ActivitiDefinition.findById(id);
            ad.menuCode = menucode;
            ad.menuName = menuname;
            ad.processName = processname;
            ad.processXml = processxml;
            ad.processid = processid;
            ad.save();

            RepositoryService repositoryService = ActivitiEngine.processEngine.getRepositoryService();
            DeploymentBuilder builder = repositoryService.createDeployment();
            builder.addClasspathResource(ad.processXml);
            builder.deploy();
            renderJSON(new Ret(true, String.format("流程 %s 的 更新成功", processname)));
        } catch(Exception e) {
            renderJSON(new Ret(false, e.getMessage()));
        }
    }

    public static void create(ActivitiDefinition ad) {
        ad.save();
        RepositoryService repositoryService = ActivitiEngine.processEngine.getRepositoryService();
        DeploymentBuilder builder = repositoryService.createDeployment();
        builder.addClasspathResource(ad.processXml);
        builder.deploy();

        flash.success(String.format("成功创建 流程模板 %s", ad.processName));
        definition();
    }

    public static void remove(Long id) {
        ActivitiDefinition ad = ActivitiDefinition.findById(id);
        if(ad == null)
            renderJSON(new Ret("不存在, 无法删除"));
        try {
            ad.delete();
        } catch(PaymentException e) {
            renderJSON(new Ret(false, e.getMessage()));
        }
        renderJSON(new Ret(true, "删除成功."));
    }


    public static void processimage(String processDefinitionId, String processInstanceId) {
        java.io.InputStream is = ActivitiProcess.processImage(processDefinitionId, processInstanceId);
        renderBinary(is);
    }


}
