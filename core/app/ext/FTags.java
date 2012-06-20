package ext;

import controllers.Secure;
import groovy.lang.Closure;
import play.exceptions.TemplateNotFoundException;
import play.templates.FastTags;
import play.templates.GroovyTemplate;
import play.templates.JavaExtensions;
import play.utils.Java;

import java.io.PrintWriter;
import java.util.Map;

/**
 * Created by IntelliJ IDEA.
 * User: wyattpan
 * Date: 6/20/12
 * Time: 4:43 PM
 */
public class FTags extends FastTags {

    /**
     * 自定义的用来在页面上检查是否有权限显示页面内容的 FastTag
     */
    public static void _ck(Map<?, ?> args, Closure body, PrintWriter out, GroovyTemplate.ExecutableTemplate template, int fromLine) {
        try {
            Boolean allow = (Boolean) Java.invokeChildOrStatic(Secure.Security.class, "check", args.get("arg"));
            if(allow) out.print(JavaExtensions.toString(body));
            // 否则不允许出现内容
        } catch(TemplateNotFoundException e) {
            throw new TemplateNotFoundException(e.getPath(), template.template, fromLine);
        } catch(Exception e) {
            throw new TemplateNotFoundException("FastTag", template.template, fromLine);
        }
    }

}
