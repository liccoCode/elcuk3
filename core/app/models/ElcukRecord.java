package models;

import com.google.gson.annotations.Expose;
import helper.J;
import org.apache.commons.lang.StringUtils;
import play.db.jpa.GenericModel;
import play.db.jpa.JPA;
import play.db.jpa.Model;
import play.libs.F;
import play.mvc.Scope;
import play.utils.FastRuntimeException;

import javax.persistence.Entity;
import javax.persistence.Transient;
import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

/**
 * 系统内的操作日志的记录
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
        this.username = this.username();
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
            if(mirrorAndValue._1 == null) throw new FastRuntimeException("没有实现 mirror 字段, 请添加.");
            if(!mirrorAndValue._1.getType().equals(obj.getClass()))
                throw new FastRuntimeException("Mirror 字段与实际 Model 类型不一致.");
            if(mirrorAndValue._1.getAnnotation(Transient.class) == null)
                throw new FastRuntimeException("Mirror 字段没有添加 @Transient, 起不允许保存!");


            Field[] mirrorFields = mirrorAndValue._1.getType().getFields();

            for(Field field : fields) {
                field.setAccessible(true);
                Expose expose = field.getAnnotation(Expose.class);
                if(expose == null) continue;
                F.T2<Field, Object> mirrorFieldAndValue = field(mirrorFields, field.getName(), mirrorAndValue._2);
                Object fieldObj = field.get(obj);

                if(fieldObj.toString().equals(mirrorFieldAndValue._2.toString())) continue;
                changes.add(new FromTo(
                        String.format("%s.%s", obj.getClass().getSimpleName(), field.getName()),
                        mirrorFieldAndValue._1.get(mirrorAndValue._2).toString(),
                        field.get(obj).toString()
                ));
            }
        } catch(IllegalAccessException e) {
            throw new FastRuntimeException(e.getMessage());
        }
        return changes;
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
        record.s();
    }

    private static F.T2<Field, Object> field(Field[] fields, String name, Object currentObj) throws IllegalAccessException {
        for(Field field : fields) {
            field.setAccessible(true);
            if(name.equals(field.getName()))
                return new F.T2<Field, Object>(field, field.get(currentObj));
        }
        return null;
    }


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

        /**
         * 由于在使用 Model.save() 的时候会导致死循环(未找到 @Preupdate 等是如何触发的, 所以只能直接使用 JPA 进行 persist)
         *
         * @return
         */
        public FromTo s() {
            JPA.em().persist(this);
            return this;
        }
    }

    public ElcukRecord s() {
        JPA.em().persist(this);
        return this;
    }

    /**
     * 通过 Session 找到当前操作的登陆的用户
     *
     * @return
     */
    public String username() {
        String username = Scope.Session.current().get("username");
        if(StringUtils.isBlank(username)) return "System";
        else return username;
    }
}
