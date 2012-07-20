package models;

import com.google.gson.annotations.Expose;
import helper.J;
import org.apache.commons.lang.StringUtils;
import org.joda.time.DateTime;
import play.Logger;
import play.db.jpa.GenericModel;
import play.db.jpa.JPA;
import play.db.jpa.Model;
import play.libs.F;
import play.mvc.Scope;
import play.utils.FastRuntimeException;

import javax.persistence.Embeddable;
import javax.persistence.Entity;
import javax.persistence.Lob;
import javax.persistence.Transient;
import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

/**
 * 系统内的操作日志的记录;
 * <p/>
 * 现在支持对 Play 中的 Model, GenericModel 进行 Record 记录.
 * 1. 为 Model(GenericModel) 添加 mirror 字段, 用来保存一份数据 copy
 * 2. 添加 @PostLoad 回掉函数, 在加载每一个 Model 的时候, 同时初始化 mirror 这一份 copy
 * 3. 添加 @PostUpdate 回掉函数, 在某一个 Model 保存的时候, 利用反射将 mirror 与对象自己进行比对, 查找出 FromTo 的 Changes,记录到
 * ElcukRecord 的 jsonRecord 中(以 JSON 字符串记录).
 * <p/>
 * User: wyattpan
 * Date: 7/16/12
 * Time: 11:50 AM
 */
@Entity
public class ElcukRecord extends Model {

    public ElcukRecord() {
        this.createAt = new Date();
    }

    public ElcukRecord(String action) {
        this();
        this.action = action;
        this.username = ElcukRecord.username();
    }

    /**
     * 操作的名字
     */
    public String action;

    /**
     * 如果有的话,则记录是谁操作的.
     */
    public String username;

    /**
     * 如果需要是对象, 那么则记录 JSON 格式的对象的值, 需要如何判断, 则在具体的业务方法中使用对应的 JSON 去解析这些数据
     */
    @Lob
    public String jsonRecord;

    public Date createAt;


    /**
     * 利用反射来获取当前对象与 Mirror 对象之间的差别值(FromTo[List])
     * PS: 1/2 ElcukRecord
     *
     * @param obj
     * @return
     */
    public static List<FromTo> changes(Object obj) {
        Field[] fields = obj.getClass().getFields();

        // find mirror
        F.T2<Field, Object> mirrorAndValue = null;
        List<FromTo> changes = new ArrayList<FromTo>();
        try {
            mirrorAndValue = field(fields, "mirror", obj);
            if(mirrorAndValue == null)
                throw new FastRuntimeException(obj.getClass().getName() + " 没有实现 mirror 字段, 请添加.");
            if(!mirrorAndValue._1.getType().equals(obj.getClass()))
                throw new FastRuntimeException(obj.getClass().getName() + " Mirror 字段与实际 Model 类型不一致.");
            if(mirrorAndValue._1.getAnnotation(Transient.class) == null)
                throw new FastRuntimeException(obj.getClass().getName() + " Mirror 字段没有添加 @Transient, 起不允许保存!");
            if(mirrorAndValue._2 == null)
                throw new FastRuntimeException(obj.getClass().getName() + " Mirror 字段的值没有初始化!");


            Field[] mirrorFields = mirrorAndValue._1.getType().getFields();

            for(Field field : fields) {
                Expose expose = field.getAnnotation(Expose.class);
                if(expose == null) continue;
                if(field.getAnnotation(Transient.class) != null) continue;

                field.setAccessible(true);
                F.T2<Field, Object> fieldAndValue = new F.T2<Field, Object>(field, field.get(obj));

                // 如果不是 Primitive 则递归进去重新执行
                if(isNeedGoDeep(field)) {
                    Logger.warn("%s.%s 内部对象的记录暂时没有支持.", obj.getClass().getName(), field.getName());
                    continue;
                }

                F.T2<Field, Object> mirrorFieldAndValue = field(mirrorFields, field.getName(), mirrorAndValue._2);
                changes.addAll(oneFieldChange(fieldAndValue, mirrorFieldAndValue));
            }
        } catch(IllegalAccessException e) {
            throw new FastRuntimeException(e.getMessage());
        }
        return changes;
    }

    /**
     * 判断一个某一个字段是否需要继续递归检查其内部的 Changes
     *
     * @param field
     * @return
     */
    private static boolean isNeedGoDeep(Field field) {
        Class clazz = field.getType();
        if(clazz.isPrimitive()) return false;
        else if(clazz.isEnum()) return false;
        else if(clazz.getSuperclass().equals(Model.class) ||
                clazz.getSuperclass().equals(GenericModel.class)) return true;
        else if(clazz.getAnnotation(Embeddable.class) != null) return true;
        else return false;
    }

    /**
     * 计算一个字段的 FromTo
     *
     * @param fieldAndValue
     * @param mirrorFieldAndValue
     * @return
     */
    private static List<FromTo> oneFieldChange(F.T2<Field, Object> fieldAndValue, F.T2<Field, Object> mirrorFieldAndValue) {
        List<FromTo> oneFieldChagnes = new ArrayList<FromTo>();
        if(isFieldAndMirrorEqual(fieldAndValue._2, mirrorFieldAndValue._2)) return oneFieldChagnes;
        Object mirrorVal = mirrorFieldAndValue._2;
        Object fieldVal = fieldAndValue._2;
        if(fieldVal.getClass() == Date.class || fieldVal.getClass() == java.sql.Date.class) {
            fieldVal = new DateTime(fieldVal).toString("yyyy-MM-dd HH:mm:ss");
            mirrorVal = new DateTime(mirrorVal).toString("yyyy-MM-dd HH:mm:ss");
        }
        oneFieldChagnes.add(new FromTo(
                String.format("%s.%s", fieldAndValue._1.getDeclaringClass().getSimpleName(), fieldAndValue._1.getName()),
                mirrorVal == null ? "" : mirrorVal.toString(),
                fieldVal == null ? "" : fieldVal.toString()
        ));
        return oneFieldChagnes;
    }

    /**
     * 判断 Field 的值与 Mirror Field 的值是不是一样的.
     * ps:
     * 1. 现在兼容了 java.util.Date 与 java.sql.Date 的值比较问题
     *
     * @param field
     * @param mirror
     * @return
     */
    private static boolean isFieldAndMirrorEqual(Object field, Object mirror) {
        if(field == null && mirror != null) return false;
        if(field != null && mirror == null) return false;
        if(field == null && mirror == null) return false;
        if(field.getClass() == Date.class || field.getClass() == java.sql.Date.class) {
            return ((Date) field).getTime() == ((Date) mirror).getTime();
        } else {
            return field.toString().equals(mirror.toString());
        }
    }

    /**
     * 不同 Model 在 postUpdate 以后的操作;(基本上相同, 所以抽取出来了)
     * PS: 2/2 ElcukRecord
     *
     * @param obj
     * @param action
     * @param <T>
     */
    public static <T extends GenericModel> void postUpdate(T obj, String action) {
        List<ElcukRecord.FromTo> changes = ElcukRecord.changes(obj);
        if(changes.size() <= 0) return;
        ElcukRecord record = new ElcukRecord(action);
        record.jsonRecord = J.json(changes);
        record.record();
    }

    /**
     * 在 Fields 中寻找 name 的字段, 并接获取值
     *
     * @param fields
     * @param name
     * @param currentObj
     * @return 一个 T2, ._1: 是字段的 Field; ._2: 是此字段的值
     * @throws IllegalAccessException
     */
    private static F.T2<Field, Object> field(Field[] fields, String name, Object currentObj) throws IllegalAccessException {
        for(Field field : fields) {
            field.setAccessible(true);
            if(name.equals(field.getName()))
                return new F.T2<Field, Object>(field, field.get(currentObj));
        }
        return null;
    }


    // ----------------------------------------------------------------------------------------------------------------------


    /**
     * 用来记录值变化的 Model
     */
    public static class FromTo {
        public FromTo(String name, String from, String to) {
            this.name = name;
            this.from = from;
            this.to = to;
        }

        /**
         * 值名称
         */
        public String name;
        /**
         * 值原始值
         */
        public String from;
        /**
         * 值变化值
         */
        public String to;

        @Override
        public String toString() {
            return String.format("%s from %s to %s", this.name, this.from, this.to);
        }

    }

    /**
     * 记录这条 Elcuk2 系统日志
     *
     * @return
     */
    public ElcukRecord record() {
        JPA.em().persist(this);
        return this;
    }

    /**
     * 通过 Session 找到当前操作的登陆的用户
     *
     * @return
     */
    public static String username() {
        String username = Scope.Session.current().get("username");
        if(StringUtils.isBlank(username)) return "System";
        else return username;
    }
}
