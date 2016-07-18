package models;

import helper.GTs;
import org.apache.commons.lang.StringUtils;
import org.apache.commons.lang.math.NumberUtils;
import org.hibernate.annotations.DynamicUpdate;
import org.joda.time.DateTime;
import play.db.jpa.JPABase;
import play.db.jpa.Model;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.EnumType;
import javax.persistence.Enumerated;
import java.util.*;

/**
 * Created by IntelliJ IDEA.
 * User: mac
 * Date: 15-1-20
 * Time: AM11:18
 */
@Entity
@DynamicUpdate
public class OperatorConfig extends Model {
    public static final Map<String, T> NAME_Type_MAPS;
    public static final Map<String, String> VALUES_MAPS;

    public static Map<String, String> VALUES_SYSPARAM;

    static {
        NAME_Type_MAPS = Collections.unmodifiableMap(
                GTs.MapBuilder.map("在库库存周转天数(TOR)", T.OPERATIONS)
                        .put("安全库存", T.OPERATIONS)
                        .put("质检时间", T.OPERATIONS)
                        .put("采货天数", T.OPERATIONS)
                        .put("合理库存周转天数", T.OPERATIONS)
                        .put("运输天数", T.SHIPMENT)
                        .put("标准断货期天数", T.OPERATIONS)
                        .put("标准断货期天数区间", T.OPERATIONS)
                        .put("退货率天数", T.OPERATIONS)
                        .build()
        );
        VALUES_MAPS = Collections.unmodifiableMap(
                GTs.MapBuilder.map("在库库存周转天数(TOR)", "30")
                        .put("安全库存", "7")
                        .put("质检时间", "2")
                        .put("采货天数", "14")
                        .put("合理库存周转天数", "70")
                        .put("标准断货期天数", "90")
                        .put("标准断货期天数区间", "90-120,121-150,151")
                        .put("退货率天数", "30")
                        .build()
        );
    }

    public enum T {
        /**
         * 运营类型
         */
        OPERATIONS,
        /**
         * 物流类型
         */
        SHIPMENT,
        /**
         * 系统参数
         */
        SYSPARAM,
    }

    /**
     * 参数类型
     */
    @Enumerated(EnumType.STRING)
    public T type;

    /**
     * 参数编码
     */
    @Column(unique = true)
    public String paramcode;

    /**
     * 参数名称
     */
    @Column(unique = true)
    public String name;

    /**
     * 参数值(考虑到参数可能为多种类型的数据,所以采用 String 来存储)
     */
    public String val;

    /**
     * 最近更新时间
     */
    public Date updateAt = DateTime.now().toDate();


    public Integer integerVal() {
        return NumberUtils.toInt(toStr());
    }

    public Float floatVal() {
        return NumberUtils.toFloat(toStr());
    }

    public String toStr() {
        return val;
    }

    public static void init() {
        /**
         * 运营报表参数初始化
         */
        for(Map.Entry<String, T> nameAndTypeEntry : NAME_Type_MAPS.entrySet()) {
            OperatorConfig config = OperatorConfig.config(nameAndTypeEntry.getKey(), nameAndTypeEntry.getValue(),
                    VALUES_MAPS.get(nameAndTypeEntry.getKey()));
            if(!config.exist()) config.save();
        }

        if(VALUES_SYSPARAM == null) VALUES_SYSPARAM = new HashMap<>();
        List<OperatorConfig> configs = OperatorConfig.findAll();
        for(OperatorConfig config : configs) {
            if(!StringUtils.isBlank(config.paramcode)) {
                VALUES_SYSPARAM.put(config.paramcode, config.val);

            }
        }
    }

    public static String getVal(String param) {
        if(VALUES_SYSPARAM == null) OperatorConfig.init();
        String sysval = VALUES_SYSPARAM.get(param);
        if(StringUtils.isBlank(sysval)) sysval = "";
        return sysval;
    }

    public boolean exist() {
        return OperatorConfig.exist(this.name);
    }

    public static boolean exist(String name) {
        return OperatorConfig.count("name=?", name) == 1;
    }


    public static OperatorConfig config(String name, T type, Number val) {
        return OperatorConfig.config(name, type, val.toString());
    }


    public static OperatorConfig config(String name, T type, String val) {
        OperatorConfig config = new OperatorConfig();
        config.name = name;
        config.val = val;
        config.type = type;
        return config;
    }

    public String fullName() {
        return String.format("%s_%s", this.type, this.name);
    }

    public <T extends JPABase> List<T> childs() {
        if(this.fullName().equalsIgnoreCase("SHIPMENT_运输天数")) {
            return ElcukConfig.findAll();
        } else {
            return null;
        }
    }
}
