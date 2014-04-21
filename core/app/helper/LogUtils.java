package helper;

import org.apache.log4j.Logger;

/**
 * 日志产生类.
 * User: cary
 * Date: 14-4-21
 * Time: 下午5:39
 */
public class LogUtils {

    public static Logger JOBLOG = null;

    public static void initLog() {
        if(JOBLOG == null) {
            JOBLOG = Logger.getLogger("elcuk2Job");
        }
    }
}
