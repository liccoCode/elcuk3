package controllers.api;

import java.lang.reflect.InvocationTargetException;
import java.util.List;
import java.util.Date;

import jobs.JobsSetup;
import jobs.SystemOperationsJob;
import play.Play;
import play.mvc.*;
import play.data.validation.*;
import play.libs.*;
import play.utils.*;

/**
 * Created by IntelliJ IDEA.
 * User: mac
 * Date: 14-10-17
 * Time: 下午3:07
 */
public class SystemOperation extends Controller {

    @Before(unless = {"login", "authenticate", "logout"})
    static void rollApi() throws Throwable {
        if(session.contains("username")) {
            boolean isprodjob = JobsSetup.isProdJob();
            if(isprodjob) {
                new SystemOperationsJob(request.actionMethod, request.url, request.controller).now();
            }
        }
    }
}
