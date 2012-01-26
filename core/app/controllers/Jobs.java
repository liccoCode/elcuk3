package controllers;

import models.Jobex;
import play.data.validation.Error;
import play.data.validation.Validation;
import play.libs.Time;
import play.mvc.Controller;
import play.mvc.With;
import play.utils.FastRuntimeException;

import java.util.List;

/**
 * Created by IntelliJ IDEA.
 * User: wyattpan
 * Date: 12/29/11
 * Time: 12:43 AM
 */
@With({Secure.class, GzipFilter.class})
@Check("root")
public class Jobs extends Controller {

    public static void index() {
        List<Jobex> jobs = Jobex.all().fetch();

        render(jobs);
    }

    public static void listing() {
        renderHtml("<h1>It`s Ok!</h1>");
    }

    public static void c(Jobex j) {
        validation.required(j.className);
        validation.required(j.duration);
        if(Validation.hasErrors()) renderJSON(validation.errorsMap());
        try {
            Time.parseDuration(j.duration);
        } catch(IllegalArgumentException e) {
            renderJSON(validation.errorsMap());
        }
        j.save();
        renderJSON(j);
    }

    public static void u(Jobex j) {
        validation.required(j.id);
        if(Validation.hasErrors()) renderJSON(validation.errorsMap());
        Jobex job = Jobex.findById(j.id);
        job.updateJobAttrs(j);
        renderJSON(job);
    }

    public static void now(long id) {
        validation.required(id);
        if(Validation.hasErrors()) renderJSON(validation.errorsMap());
        Jobex job = Jobex.findById(id);
        if(job == null) renderJSON(new Error("Job", "Job is not exist!", new String[]{}));
        try {
            job.now();
        } catch(Exception e) {
            throw new FastRuntimeException(e.getMessage());
        }
        renderJSON("{\"flag\":\"true\"}");
    }

    public static void close(long id) {
        Jobex job = Jobex.findById(id);
        job.close = true;
        job.save();
        renderJSON(job);
    }
}
