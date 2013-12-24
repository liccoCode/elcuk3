package controllers;

import jobs.driver.DriverJob;
import models.ElcukConfig;
import play.mvc.Controller;
import play.mvc.Util;
import play.mvc.With;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Created by IntelliJ IDEA.
 * User: wyatt
 * Date: 1/14/13
 * Time: 3:27 PM
 */
@With({GlobalExceptionHandler.class, Secure.class})
public class Elcuk extends Controller {
    public static void index() {
        render();
    }

    public static void updateConfig(String market, String shipType, String dayType, Integer val) {
        String name = String.format("%s_%s_%s", market, shipType, dayType);
        ElcukConfig config = ElcukConfig.findByName(name);
        if(config == null)
            flash.error("所选择 运输参数 不存在.");
        else {
            config.val = val.toString();
            config.save();
            flash.success("运输参数 %s 修改成功", config.fullName);
        }
        index();
    }

    public static void config(String name) {
        ElcukConfig config = ElcukConfig.findByName(name);
        renderJSON(config);
    }

    /**
     * 修复 Amazon 上已经有的 Selling 没有则无法创建
     */
//    @Check("finances.addselling")


    /**
     * 从 Amazon 的 product url 解析出需要的 market, asin 的字符串
     *
     * @param url
     * @return [0]: url, [1]: market, [2]: asin
     */
    @Util
    public static String[] parseUrl(String url) {
        Pattern ptn = null;
        if(url.contains("gp/product")) {
            ptn = Pattern.compile("http[s]?://[w]{0,3}[\\.]?(.*)/gp/product/(\\w+)");
        } else {
            ptn = Pattern.compile("http[s]?://[w]{0,3}[\\.]?(.*)/dp/(\\w+)");
        }
        Matcher matcher = ptn.matcher(url);
        List<String> parts = new ArrayList<String>();
        if(matcher.find()) {
            for(int i = 0; i <= matcher.groupCount(); i++) {
                parts.add(matcher.group(i));
            }
        }
        return parts.toArray(new String[parts.size()]);
    }

    public static void startJob() {
        new DriverJob().now();
    }
}
