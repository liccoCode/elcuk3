package models;

import helper.GTs;
import models.procure.Shipment;
import org.apache.commons.lang.math.NumberUtils;
import org.hibernate.annotations.Cache;
import org.hibernate.annotations.CacheConcurrencyStrategy;
import org.hibernate.annotations.DynamicUpdate;
import org.joda.time.DateTime;
import play.Logger;
import play.db.jpa.Model;

import javax.persistence.Column;
import javax.persistence.Entity;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;

/**
 * Created by IntelliJ IDEA.
 * User: wyatt
 * Date: 5/15/13
 * Time: 3:01 PM
 */
@Entity
@DynamicUpdate
@Cache(usage = CacheConcurrencyStrategy.NONSTRICT_READ_WRITE)
public class ElcukConfig extends Model {
    public static final Map<String, String> MARKETS;

    public static final Map<String, String> SHIP_TYPES;

    public static final Map<String, String> DAY_TYPES;

    static {
        MARKETS = Collections.unmodifiableMap(
                GTs.MapBuilder.map("uk", "英国").put("de", "德国").put("us", "美国").
                        put("fr", "法国").put("it", "意大利").put("jp", "日本").put("ca", "加拿大").put("es", "西班牙").build());

        SHIP_TYPES = Collections.unmodifiableMap(
                GTs.MapBuilder.map(Shipment.T.SEA.name().toLowerCase(), "海运")
                        .put(Shipment.T.AIR.name().toLowerCase(), "空运")
                        .put(Shipment.T.EXPRESS.name().toLowerCase(), "快递--优惠渠道")
                        .put(Shipment.T.EXPRESS_ECO.name().toLowerCase(), "快递--经济渠道")
                        .put(Shipment.T.EXPRESS_FAST.name().toLowerCase(), "快递--快速渠道")
                        .put(Shipment.T.DEDICATED_LINE.name().toLowerCase(), "专线")
                        .build());

        DAY_TYPES = Collections.unmodifiableMap(
                GTs.MapBuilder.map("atport", "运输到港").put("clearance", "清关").put("pick", "提货")
                        .put("book", "预约").put("deliver", "派送")
                        .put("receipt", "签收").put("inbound", "入库").build());
    }

    /**
     * 对参数进行初始化
     */
    public static void init() {
        /**
         * 1. 运输参数
         * 2. 如果还有其他参数, 在这里初始化
         */
        for(Map.Entry<String, String> marketEntry : MARKETS.entrySet()) {
            for(Map.Entry<String, String> shipTypeEntry : SHIP_TYPES.entrySet()) {
                for(Map.Entry<String, String> dayTypeEntry : DAY_TYPES.entrySet()) {
                    String key = String.format("%s_%s_%s",
                            marketEntry.getKey(), shipTypeEntry.getKey(), dayTypeEntry.getKey());
                    ElcukConfig config = ElcukConfig.config(key, 0);
                    Logger.info("key: %s", key);
                    if(!config.exist()) {
                        config.calFullName(marketEntry.getKey(), shipTypeEntry.getKey(), dayTypeEntry.getKey());
                        config.save();
                    }
                }
            }
        }
    }


    @Column(unique = true)
    public String name;
    public String val;

    public String fullName;

    public Integer toInteger() {
        return NumberUtils.toInt(toStr());
    }

    public Float toFloat() {
        return NumberUtils.toFloat(toStr());
    }

    public String toStr() {
        return val;
    }

    public void calFullName(String market_key, String ship_key,String dayType_key) {
        this.fullName = String.format("%s %s %s",
                MARKETS.get(market_key), SHIP_TYPES.get(ship_key), DAY_TYPES.get(dayType_key));
        this.save();
    }

    public boolean exist() {
        return ElcukConfig.exist(this.name);
    }

    public static ElcukConfig findByName(String name) {
        return ElcukConfig.find("name=?", name).first();
    }

    public static boolean exist(String name) {
        return ElcukConfig.count("name=?", name) == 1;
    }

    public static ElcukConfig config(String name, Number val) {
        return ElcukConfig.config(name, val.toString());
    }

    public static ElcukConfig config(String name, String val) {
        ElcukConfig config = new ElcukConfig();
        config.name = name.toLowerCase();
        //TIP: 原有可以在这里对 fullName 进行自动转译, 可是 Play 不允许这么做,
        // 这样做会打破 Play 对 Class 的增强, 导致程序启动失败
        config.val = val;
        return config;
    }

    public String shipType() {
        return DAY_TYPES.get(this.name.split("_")[2]);
    }

    public static List<String> multiUpdate(Map<String, String> valsMap) {
        List<String> errorMsg = new ArrayList<String>();
        for(String key : valsMap.keySet()) {
            ElcukConfig config = ElcukConfig.findById(NumberUtils.toLong(key));
            if(config != null) {
                String val = valsMap.get(key);
                try {
                    Integer.parseInt(val);
                } catch(NumberFormatException e) {
                    errorMsg.add(val);
                    continue;
                }
                config.val = valsMap.get(key);
                config.save();
                config.editShipMentArriveDay();
            }
        }
        return errorMsg;
    }

    /**
     * 系统改了某市场+某运输方式的市场天数后，对应已产生的运输单的预计到库时间都得重新按照此时长天数更改
     */
    private synchronized void editShipMentArriveDay() {
        if(this.name != null && this.name.length() > 0) {
            String[] names = this.name.split("_");
            if(names.length >= 2) {
                Shipment.T shipt = null;
                for(Shipment.T t : Shipment.T.values()) {
                    if(t.name().toLowerCase().equals(names[1])) {
                        shipt = t;
                        break;
                    }
                }
                List<Shipment> ments = Shipment
                        .find("lower(whouse.country)=? and type=? and dates.planBeginDate>=?", names[0], shipt,
                                DateTime.now().toDate()).fetch();
                for(Shipment ment : ments) {
                    ment.calcuPlanArriveDate();
                    ment.save();
                }
            }
        }
    }
}
