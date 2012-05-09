package templates;

import helper.GTs;
import models.market.Feedback;
import org.junit.Test;
import play.modules.gtengineplugin.TemplateLoader;
import play.templates.Template;
import play.test.UnitTest;
import play.vfs.VirtualFile;

import java.util.HashMap;
import java.util.Map;

/**
 * 测试使用
 * User: wyattpan
 * Date: 5/9/12
 * Time: 11:20 AM
 */
public class TemplateTest extends UnitTest {
    @Test
    public void testTemplate() {
        Feedback f = Feedback.findById("303-3685104-8694754");
//        Template t = TemplateLoader.load("1", "kdfjkjdfk ${f.orderId}");
        Template t = TemplateLoader.load(VirtualFile.fromRelativePath("/app/views/Mails/feedbackWarnning.html"));
        Map<String, Object> args = new HashMap<String, Object>();
        args.put("f", f);
        t.render(args);
    }

    @Test
    public void testGts() {
        Feedback f = Feedback.findById("303-3685104-8694754");
        Map<String, Object> args = new HashMap<String, Object>();
        args.put("f", f);
        System.out.println(GTs.render("feedbackWarnning.html", args));
    }
}
