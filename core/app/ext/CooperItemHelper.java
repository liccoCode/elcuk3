package ext;

import models.procure.CooperItem;
import org.apache.commons.lang.StringUtils;
import play.templates.JavaExtensions;


public class CooperItemHelper extends JavaExtensions {
    /**
     *
     *格式化产品要求，前台 popover 使用
     */
    public static String formatProductTerms(CooperItem cooperItem) {
        StringBuffer message = new StringBuffer();
        message.append("<span class='label label-info'>产品要求:</span><br>");
        if( StringUtils.isNotEmpty(cooperItem.productTerms)) {
            String[] messageArray = StringUtils.split(cooperItem.productTerms, "\n");
            for(String text : messageArray) {
                message.append("<p>").append(text).append("<p>");
            }
        }
        if( StringUtils.isNotEmpty(cooperItem.memo)) {
            message.append("<span class='label label-info'>Memo:</span><br>");
            String[] messageArray = StringUtils.split(cooperItem.memo, "\n");
            for(String text : messageArray) {
                message.append("<p>").append(text).append("<p>");
            }
        }
        return message.toString();
    }
}
