package controllers;

import controllers.api.SystemOperation;
import helper.Webs;
import models.Jobex;
import models.market.JobRequest;
import play.data.validation.Validation;
import play.mvc.Before;
import play.mvc.Controller;
import play.mvc.With;

/**
 * Created by IntelliJ IDEA.
 * User: wyattpan
 * Date: 12/29/11
 * Time: 12:43 AM
 * @deprecated 任务被放弃
 */
@With({GlobalExceptionHandler.class, Secure.class,SystemOperation.class})
public class Jobs extends Controller {

    @Before(only = {"index", "update", "create"})
    public static void indexPage() {
        renderArgs.put("jobs", Jobex.all().<Jobex>fetch());
        renderArgs.put("jobReqs", JobRequest.find("ORDER BY requestDate DESC").<Jobex>fetch(30));
    }

    @Check("jobs.index")
    public static void index() {
        render();
    }

    public static void create(Jobex job) {
        validation.valid(job);
        job.validate();
        if(Validation.hasErrors()) render("Jobs/index.html");
        flash.success("Job %s 添加成功", job.className);
        job.save();
        redirect("/Jobs/index");
    }

    public static void update(Jobex job) {
        validation.required(job.id);
        validation.valid(job);
        job.validate();
        if(Validation.hasErrors()) render("Jobs/index.html");
        flash.success("Job %s 更新成功", job.className);
        job.save();
        redirect("/Jobs/index");
    }

    public static void now(long id) {
        validation.required(id);
        Jobex job = Jobex.findById(id);
        if(Validation.hasErrors() || job == null) {
            flash.error("JobId %s 不存在!", id);
            redirect("/Jobs/index");
        }
        try {
            job.now();
        } catch(Exception e) {
            flash.error("Job %s 因 [%s] 执行失败.", job.className, Webs.e(e));
        }
        flash.success("Job %s 执行成功", job.className);
        redirect("/Jobs/index");
    }
}
