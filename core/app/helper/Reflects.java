package helper;

import org.apache.commons.lang.StringUtils;
import play.i18n.Messages;
import play.utils.FastRuntimeException;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.List;

/**
 * 反射工具方法
 * User: wyatt
 * Date: 6/7/13
 * Time: 5:55 PM
 */
public class Reflects {

    private Reflects() {
    }

    public static List<String> updateAndLogChanges(Object instance, String attr, Object value) {
        return updateAndLogChanges(instance, attr, null, value);
    }

    public static List<String> updateAndLogChanges(Object instance, String attr, String message, Object value) {
        StringBuilder log = new StringBuilder();
        try {
            Field field = instance.getClass().getField(attr);
            Object oldValue = field.get(instance);
            if(oldValue == null && value == null) return new ArrayList<>();
            if((oldValue == null) || (value == null) || (!oldValue.equals(value))) {
                String oldValueStr = oldValue == null ? "空" : oldValue.toString();
                String valueStr = value == null ? "空" : value.toString();
                if(oldValue != null && isType(oldValue, Date.class))
                    oldValueStr = Dates.date2DateTime((Date) oldValue);
                if(value != null && isType(value, Date.class))
                    valueStr = Dates.date2DateTime((Date) value);

                log.append(StringUtils.isNotBlank(message) ? message : Messages.get(attr))
                        .append(" 从 ").append(oldValue == null ? "空" : oldValueStr)
                        .append(" 变更为 ").append(value == null ? "空" : valueStr);
                field.set(instance, value);
            }
        } catch(Exception e) {
            throw new RuntimeException(e);
        }
        if(log.length() > 0) return Collections.singletonList(log.toString());
        else return new ArrayList<>();
    }

    public static boolean isType(Object instance, Class type) {
        return instance.getClass().equals(type);
    }

    public static List<String> logFieldFade(Object instance, String attr, Object value) {
        return logFieldFade(instance, attr, null, value);
    }

    /**
     * 用来记录参数变化的门面方法, 注: 仅仅实现了一级子参数, 还未利用递归实现 N 级子参数, 所以只能处理:
     * attrs.price 这一级别, 无法处理 attrs.address.name 这些级别
     *
     * @param instance 示例对象
     * @param attr     属性名称
     * @param message  格式化后的属性名称(如 productName => 产品名称)
     * @param value    值
     * @return
     */
    public static List<String> logFieldFade(Object instance, String attr, String message, Object value) {
        try {
            Field field = null;
            if(attr.contains(".")) {
                String[] attrs = StringUtils.split(attr, ".");
                field = instance.getClass().getField(attrs[0]);
                return Reflects.updateAndLogChanges(field.get(instance), attrs[1], message, value);
            } else {
                return Reflects.updateAndLogChanges(instance, attr, message, value);
            }
        } catch(Exception e) {
            throw new FastRuntimeException(Webs.E(e));
        }
    }
}
