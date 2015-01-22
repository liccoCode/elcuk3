package models.market;

import helper.GTs;
import models.ElcukConfig;
import org.apache.commons.lang.math.NumberUtils;
import org.joda.time.DateTime;
import play.db.jpa.JPABase;
import play.db.jpa.Model;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.EnumType;
import javax.persistence.Enumerated;
import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.Map;

/**
 * Created by IntelliJ IDEA.
 * User: mac
 * Date: 15-1-20
 * Time: AM11:18
 */
@Entity
@org.hibernate.annotations.Entity(dynamicUpdate = true)
public class OperatorConfig extends Model {
    public static final Map<String, T> NAME_Type_MAPS;

    static {
        NAME_Type_MAPS = Collections.unmodifiableMap(
                GTs.MapBuilder.map("在库库存周转天数(TOR)", T.OPERATIONS)
                        .put("安全库存", T.OPERATIONS)
                        .put("质检时间", T.OPERATIONS)
                        .put("采货天数", T.OPERATIONS)
                        .put("合理库存周转天数", T.OPERATIONS)
                        .put("运输天数", T.SHIPMENT)
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
        SHIPMENT
    }

    /**
     * 参数类型
     */
    @Enumerated(EnumType.STRING)
    public T type;

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
            OperatorConfig config = OperatorConfig.config(nameAndTypeEntry.getKey(), nameAndTypeEntry.getValue(), 0);
            if(!config.exist()) config.save();
        }
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
