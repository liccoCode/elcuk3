package controllers;

import helper.Webs;
import models.view.Ret;
import play.exceptions.TemplateExecutionException;
import play.mvc.Catch;
import play.mvc.Controller;
import play.utils.FastRuntimeException;

/**
 * 所有的异常处理在这
 * User: wyattpan
 * Date: 6/19/12
 * Time: 9:42 AM
 */
public class GlobalExceptionHandler extends Controller {

    /**
     * 所有从 Controller 中抛出来的 FastRuntimeException 统一进行如下处理, 其优先级是最高的.
     *
     * @param e
     */
    @Catch(value = FastRuntimeException.class, priority = 1)
    public static void fastRuntimeExceptionCatch(FastRuntimeException e) {
        renderJSON(new Ret(Webs.e(e)));
    }

    @Catch(value = TemplateExecutionException.class, priority = 2)
    public static void templateExecutionException(TemplateExecutionException e) {
        throw e;
    }
}
