package controllers.api;

import java.lang.reflect.InvocationTargetException;
import java.util.List;
import java.util.Date;

import jobs.JobsSetup;
import jobs.SystemOperationsJob;
import play.Logger;
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
            if(Play.mode.isProd()) {
                //new SystemOperationsJob(request.actionMethod, request.url, request.controller).now();
            }
        }
        //long freememory = Runtime.getRuntime().freeMemory() / 1024 / 1024;
        //long totalmemory = Runtime.getRuntime().totalMemory() / 1024 / 1024;
        //Logger.info("playurl: %s playpath:%s freememory:%s totalmemory:%s ", request.url, Play.ctxPath, freememory,
               // totalmemory);
    }
}
