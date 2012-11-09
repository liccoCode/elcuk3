package controllers;

import helper.Webs;
import jobs.AmazonFBAQtySyncJob;
import jobs.AmazonOrderUpdateJob;
import jobs.FeedbackCrawlJob;
import jobs.SellingRecordGenerateJob;
import jobs.promise.SellingRecordFixPromise;
import jobs.works.ListingReviewsWork;
import models.Jobex;
import models.market.*;
import models.view.Ret;
import notifiers.Mails;
import org.joda.time.DateTime;
import play.data.validation.Validation;
import play.jobs.Job;
import play.libs.F;
import play.mvc.Before;
import play.mvc.Controller;
import play.mvc.With;
import play.utils.FastRuntimeException;

import java.io.File;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.concurrent.TimeUnit;

/**
 * Created by IntelliJ IDEA.
 * User: wyattpan
 * Date: 12/29/11
 * Time: 12:43 AM
 */
@With({GlobalExceptionHandler.class, Secure.class, GzipFilter.class})
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
            flash.error("Job %s 因 [%s] 执行失败.", job.className, Webs.E(e));
        }
        flash.success("Job %s 执行成功", job.className);
        redirect("/Jobs/index");
    }

    // -------------------- Small Fix Method --------------------

    /**
     * 对 SellingRecord 进行修补处理, 从 begin 开始, 抓取 days 天的 Amazon SellingReord 数据
     *
     * @param begin
     * @param days
     */
    public static void sellingRecordFix(Date begin, int days) {
        new SellingRecordFixPromise(begin, days).now();
        renderJSON(new Ret(true, "任务已提交"));
    }

    /**
     * 修复 SellingRecord 根据当前天数的偏移天数
     *
     * @param offset
     */
    public static void sellingRecordGenerate(int offset) {
        new SellingRecordGenerateJob(DateTime.now().minusDays(offset)).now();
        renderText("任务已提交");
    }

    /**
     * 修复某一个市场上的 Feedback 数据
     *
     * @param id
     * @param page
     */
    public static void feedbackFix(final long id, final int page) {
        new Job() {
            @Override
            public void doJob() {
                Account acc = Account.findById(id);
                FeedbackCrawlJob.fetchAccountFeedbackOnePage(acc, acc.type, page);
                Notifications.notifys(String.format("更新 Account %s 的第 %s 页 Feedback 完成.", acc.prettyName(), page));
            }
        }.now();
    }

    /**
     * 修复 Amazon Shipped Order 修复方法
     *
     * @param path 指定修复文件所在的绝对地址
     */
    public static void shippedOrderFix(String path) {
        long begin = System.currentTimeMillis();
        JobRequest job = new JobRequest();
        job.path = path;
        new AmazonOrderUpdateJob().callBack(job);
        renderJSON(new Ret(true, "耗时 " + (System.currentTimeMillis() - begin) + "ms 更新成功."));
    }
}
