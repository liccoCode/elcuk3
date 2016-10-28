package ext;

import groovy.lang.Closure;
import play.exceptions.TagInternalException;
import play.exceptions.TemplateExecutionException;
import play.exceptions.TemplateNotFoundException;
import play.templates.*;

import java.io.PrintWriter;
import java.util.HashMap;
import java.util.Map;

/**
 * Created by IntelliJ IDEA.
 * User: wyatt
 * Date: 4/16/13
 * Time: 6:16 PM
 */
public class Javascript extends FastTags {
    public static void _js(Map<?, ?> args, Closure body, PrintWriter out,
                           GroovyTemplate.ExecutableTemplate template, int fromLine) {
        if(!args.containsKey("arg") || args.get("arg") == null) {
            final String msg = "Specify a template name";
            TagInternalException internalException = new TagInternalException(msg);
            throw new TemplateExecutionException(template.template, fromLine, msg,
                    internalException);
        }

        try {
            Template tmpl = TemplateLoader.load(args.get("arg").toString());
            Map<String, Object> newArgs = new HashMap<>();
            newArgs.putAll(template.getBinding().getVariables());
            for(Object key : args.keySet()) {
                if(key.equals("arg")) continue;
                newArgs.put(key.toString(), args.get(key));
            }
            newArgs.put("_isInclude", true);
            //dont write to the response.out, need to be escaped before.
            newArgs.remove("out");

            String content = tmpl.render(newArgs);
            out.print(JavaExtensions.escapeJavaScript(content));
        } catch(TemplateNotFoundException e) {
            throw new TemplateNotFoundException(e.getPath(), template.template, fromLine);
        }
    }

}
