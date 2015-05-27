package controllers;

import controllers.api.SystemOperation;
import helper.J;
import helper.Webs;
import jobs.PmDashboard.AbnormalFetchJob;
import jobs.categoryInfo.CategoryInfoFetchJob;
import models.CategoryAssignManagement;
import models.ElcukRecord;
import models.User;
import models.product.Category;
import models.view.Ret;
import models.view.dto.CategoryInfoDTO;
import org.apache.commons.lang.StringUtils;
import play.cache.Cache;
import play.data.validation.Validation;
import play.mvc.Controller;
import play.mvc.With;
import play.utils.FastRuntimeException;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * Created by IntelliJ IDEA.
 * User: mac
 * Date: 14-3-27
 * Time: AM10:19
 */
@With({GlobalExceptionHandler.class, Secure.class, SystemOperation.class})
public class CategoryInfos extends Controller {

    @Check("categoryinfos.show")
    public static void show(String id) {
        User user = User.findByUserName(Secure.Security.connected());
        List<Category> cates = User.getObjCategorys(user);
        if(StringUtils.isBlank(id) && cates.size() > 0) id = cates.get(0).categoryId;
        List<CategoryInfoDTO> dtos = CategoryInfoDTO.query(id);
        //Category 利润
        float categoryProfit = CategoryInfoDTO.categoryProfit(dtos);
        render(cates, dtos, categoryProfit);
    }

    /**
     * 质检员任务分配
     */
    @Check("categoryinfos.taskassign")
    public static void taskassign() {
        CategoryAssignManagement categoryAssignManagement = new CategoryAssignManagement();
        User currUser = Login.current();
        categoryAssignManagement.query();
        List<User> userList = User.findAll();
        List<String> users = new ArrayList<String>();
        for(User u : userList) users.add(u.username);
        renderArgs.put("users", J.json(users));
        List<ElcukRecord> records = ElcukRecord.find("action like '质检员任务分配' ORDER BY createAt DESC")
                .fetch();
        render(categoryAssignManagement, records, currUser);
    }

    public static void createTaskAssign(CategoryAssignManagement c) {
        Validation.required("名称", c.userName);
        if(!c.isNameCorrect(c.userName))
            Validation.addError("姓名", "用户名输入错误");
        if(Validation.hasErrors())
            renderJSON(new Ret(Webs.VJson(Validation.errors())));
        c.createTaskAssign();
        renderJSON(true);
    }

    public static void updateTaskAssign(CategoryAssignManagement c, Long id) {
        Validation.required("名称", c.userName);
        if(!c.isNameCorrect(c.userName))
            Validation.addError("姓名", "用户名输入错误");
        if(Validation.hasErrors())
            renderJSON(new Ret(Webs.VJson(Validation.errors())));
        CategoryAssignManagement.updateTaskAssign(c, id);
        renderJSON(true);
    }

    public static void deleteAssignById(Long assid) {
        CategoryAssignManagement.deleteAssignById(assid);
        renderJSON(new Ret());
    }
}
