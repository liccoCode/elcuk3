package helper;


import org.apache.commons.io.FileUtils;
import play.Logger;

import java.io.File;
import java.io.IOException;

/**
 * Created by IntelliJ IDEA.
 * User: wyattpan
 * Date: 5/20/12
 * Time: 3:13 PM
 */
public class Devs {
    public enum T {
        /**
         * 上架 Listing 相关的错误
         */
        SALES,
        /**
         * 上传图片错误的信息
         */
        IMGUPLOAD,
        /**
         * 获取 SellingRecord 的相关的错误信息
         */
        SELLINGRECORD,
        /**
         * Jobs 任务出现错误的文件日志
         */
        JOBS_ERROR
    }

    /**
     * 需要记录下来的, 发生错误的单个文件的日志, 例如更新 Listing 失败时候的向 Amazon 获取的 html 页面
     *
     * @param fileName
     * @param content
     * @param f
     */
    public static void fileLog(String fileName, String content, T f) {
        String baseUrl = "";
        switch(f) {
            case SALES:
                baseUrl = Constant.L_LISTING;
                break;
            case IMGUPLOAD:
                baseUrl = Constant.L_IMAGEUPLOAD;
                break;
            case SELLINGRECORD:
                baseUrl = Constant.S_RECORDS;
                break;
            case JOBS_ERROR:
                baseUrl = Constant.J_LOGS;
                break;
            default:
                baseUrl = "";
        }
        try {
            FileUtils.writeStringToFile(new File(String.format("%s/%s", baseUrl, fileName)), content, "UTF-8");
        } catch(IOException e) {
            Logger.warn("Devs.fileLog write file error.", Webs.E(e));
        }
    }
}
