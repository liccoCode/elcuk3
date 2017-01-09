package ext;

import controllers.Secure;
import groovy.lang.Closure;
import helper.Webs;
import org.apache.commons.lang.StringUtils;
import play.Logger;
import play.exceptions.TemplateNotFoundException;
import play.templates.FastTags;
import play.templates.GroovyTemplate;
import play.templates.JavaExtensions;
import play.utils.FastRuntimeException;
import play.utils.Java;

import java.io.PrintWriter;
import java.util.Map;

/**
 * Created by IntelliJ IDEA.
 * User: wyattpan
 * Date: 6/20/12
 * Time: 4:43 PM
 */
@FastTags.Namespace("power")
public class FTags extends FastTags {

    /**
     * 自定义的用来在页面上检查是否有权限显示页面内容的 FastTag
     */
    public static void _ck(Map<?, ?> args, Closure body, PrintWriter out,
                           GroovyTemplate.ExecutableTemplate template, int fromLine) {
        try {
            Boolean allow = (Boolean) Java.invokeChildOrStatic(
                    Secure.Security.class, "check", args.get("arg"));
            if(allow)
                out.print(JavaExtensions.toString(body));
            // 否则不允许出现内容
        } catch(TemplateNotFoundException e) {
            throw new TemplateNotFoundException(e.getPath(), template.template, fromLine);
        } catch(Exception e) {
            Logger.error(Webs.S(e));
            throw new TemplateNotFoundException("FastTag", template.template, fromLine);
        }
    }

    /**
     * 必须的参数:
     * id, name
     * 可使用的参数:
     * value, help, label, edit
     */
    public static void _text(Map<?, ?> args, Closure body, PrintWriter out,
                             GroovyTemplate.ExecutableTemplate template, int fromLine) {
        /*
                   <div class="control-group">
           <label class="control-label" for="id">运输单号</label>

           <div class="controls">
               <div class="input-append">
                   <input disabled="" id="id" type="text" name="s.id" value="${s.id}">
               </div>
               <span class="help-inline">运输单号是自动生成的</span>
           </div>
       </div>
        */
        String id = Str(args, "id");
        String name = Str(args, "name");
        String value = Str(args, "value");
        String label = Str(args, "label");
        String help = Str(args, "help");
        String type = Str(args, "type");
        Boolean edit = True(args, "edit");
        String placeHolder = Str(args, "placeHolder");
        if(StringUtils.isBlank(name)) throw new FastRuntimeException("[name] attr is blank.");
        if(StringUtils.isBlank(id)) throw new FastRuntimeException("[id] attr is blank");
        if(StringUtils.isBlank(type)) type = "text";

        StringBuilder sbd = new StringBuilder("<div class='control-group'>")
                .append("<label class='control-label' for='").append(id).append("'>")
                .append(StringUtils.isBlank(label) ? "" : label).append("</label>")
                .append("<div class='controls'>")
                .append("<div class='input-append'>")
                .append("<input ").append(edit ? "" : "readonly").append(" id='").append(id)
                .append("' ")
                .append("type='").append(type).append("' ")
                .append("name='").append(name).append("' ")
                .append(StringUtils.isNotBlank(placeHolder) ?
                        "placeHolder='" + placeHolder + "' " : "")
                .append("value='").append(value).append("'>")
                .append("</div>");
        if(StringUtils.isNotBlank(help))
            sbd.append("<span class='help-inline'>").append(help).append("</span>");
        out.print(sbd.append("</div>"/*controlls div*/).append("</div>"/*control-group div*/)
                .toString());
    }

    public static String Str(Map<?, ?> args, String key) {
        Object val = args.get(key);
        if(val == null)
            return "";
        else
            return val.toString();
    }

    /**
     * default true
     */
    public static Boolean True(Map<?, ?> args, String key) {
        Boolean val = (Boolean) args.get(key);
        if(val == null)
            return true;
        else
            return val;
    }
}
