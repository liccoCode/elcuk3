package helper;

import net.sargue.mailgun.*;
import play.Logger;

import java.io.File;

/**
 * Created by IntelliJ IDEA.
 * User: licco
 * Date: 2018/5/28
 * Time: 下午3:40
 */
public class MailUtils {

    private Configuration configuration;

    public MailUtils() {
        configuration = new Configuration().domain("elcuk-erp.com")
                .apiKey("key-e05c19b0a420e61ab81e54b7fbf0ce99")
                .from("erp support", "support@elcuk-erp.com");
    }

    public void sendMail(String to, String subject, String content, File file) {
        Response response;
        MailBuilder builder = Mail.using(configuration)
                .to(to)
                .subject(subject)
                .text(content);
        if(file != null) {
            MultipartBuilder multipartBuilder = builder.multipart();
            multipartBuilder.attachment(file);
            response = multipartBuilder.build().send();
        } else {
            response = builder.build().send();
        }
        if(response == null || !response.isOk()) {
            Logger.error(" mailgun 发送邮件失败............");
        } else if(response.isOk()) {
            Logger.info(" mailgun 发送邮件成功............");
        }
    }
}
