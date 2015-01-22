package controllers;

import controllers.api.SystemOperation;
import helper.J;
import jobs.driver.DriverJob;
import models.ElcukConfig;
import models.market.M;
import models.market.OperatorConfig;
import models.procure.Shipment;
import play.mvc.Controller;
import play.mvc.Util;
import play.mvc.With;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Created by IntelliJ IDEA.
 * User: wyatt
 * Date: 1/14/13
 * Time: 3:27 PM
 */
@With({GlobalExceptionHandler.class, Secure.class, SystemOperation.class})
public class Elcuk extends Controller {
    /**
     * 参数设置首页
     */
    @Check("elcuk.index")
    public static void index() {
        List<OperatorConfig> configurations = OperatorConfig.findAll();
        render(configurations);
    }

    /**
     * 报表相关参数设置
     */
    @Check("elcuk.index")
    public static void edit(Long id) {
        OperatorConfig config = OperatorConfig.findById(id);
        if(config.fullName().equalsIgnoreCase("SHIPMENT_运输天数")) {
            render("Elcuk/showMarketShipDay.html", config);
        } else {
            render(config);
        }
    }

    /**
     * 报表相关参数设置
     */
    @Check("elcuk.index")
    public static void update(Long id, String val) {
        OperatorConfig config = OperatorConfig.findById(id);
        config.val = val;
        config.save();
        flash.success("参数 %s 设置成功!", config.name);
        index();
    }

    /**
     * ElcukConfig 修改页面
     *
     * @param market
     * @param shipType
     * @param operatorConfigId
     */
    @Check("elcuk.index")
    public static void editShipDayConfigs(String market, Shipment.T shipType, long operatorConfigId) {

        List<ElcukConfig> configs = ElcukConfig
                .find("name like ?", M.val(market).sortName() + "_" + shipType.toString().toLowerCase() + "_%").fetch();
        render(market, shipType, operatorConfigId, configs);
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

    /**
     * 批量更新运输天数配置
     */
    @Check("elcuk.index")
    public static void updateShipDayConfigs(String vals, long operatorConfigId, String market, Shipment.T shipType) {
        HashMap<String, String> valMaps = J.from(vals, HashMap.class);
        List<String> errorMsg = ElcukConfig.multiUpdate(valMaps);
        if(errorMsg.isEmpty()) {
            flash.success("批量更新成功!");
            edit(operatorConfigId);
        } else {
            StringBuffer msg = new StringBuffer();
            for(String error : errorMsg) {
                msg.append(error).append(" ");
            }
            flash.error(String.format("出现 %s 个非法字符: [%s]", errorMsg.size(), msg.toString()));
            editShipDayConfigs(market, shipType, operatorConfigId);
        }
    }
}
