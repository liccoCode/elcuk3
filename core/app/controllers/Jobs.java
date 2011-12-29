package controllers;

import jobs.ListingUpdateJob;
import models.Jobex;
import play.Invoker;
import play.libs.F;
import play.mvc.Controller;

import java.util.concurrent.TimeUnit;

/**
 * Created by IntelliJ IDEA.
 * User: wyattpan
 * Date: 12/29/11
 * Time: 12:43 AM
 */
public class Jobs extends Controller {

    public static void listing() {
        renderHtml("<h1>It`s Ok!</h1>");
    }

    public static void c() {
        Jobex job = new Jobex();
        job.lastUpdateTime = 0;
        job.className = ListingUpdateJob.class.getName();
        job.duration = "5s";
        job.save();
        renderJSON(job);
    }

    public static void stop(long id) {
        Jobex job = Jobex.findById(id);
        job.close = true;
        job.save();
        renderJSON(job);
    }
}
