package controllers;

import models.ElcukConfig;
import play.mvc.Controller;
import play.mvc.With;

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
}
