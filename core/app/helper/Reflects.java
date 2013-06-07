package helper;

import play.i18n.Messages;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.List;

/**
 * 反射工具方法
 * User: wyatt
 * Date: 6/7/13
 * Time: 5:55 PM
 */
public class Reflects {
    public static List<String> updateAndLogChanges(Object instance, String attr, Object value) {
        StringBuilder log = new StringBuilder();
        try {
            Field field = instance.getClass().getField(attr);
            Object oldValue = field.get(instance);
            if(oldValue == null && value == null) return new ArrayList<String>();
            if((oldValue == null) || (value == null) || (!oldValue.equals(value))) {
                String oldValueStr = oldValue == null ? "空" : oldValue.toString();
                String valueStr = value == null ? "空" : value.toString();
                if(oldValue != null && isType(oldValue, Date.class))
                    oldValueStr = Dates.date2DateTime((Date) oldValue);
                if(value != null && isType(value, Date.class))
                    valueStr = Dates.date2DateTime((Date) value);

                log.append(Messages.get(attr))
                        .append(" 从 ").append(oldValue == null ? "空" : oldValueStr)
                        .append(" 变更为 ").append(value == null ? "空" : valueStr);
                field.set(instance, value);
            }
        } catch(Exception e) {
            throw new RuntimeException(e);
        }
        if(log.length() > 0) return Arrays.asList(log.toString());
        else return new ArrayList<String>();
    }

    public static boolean isType(Object instance, Class type) {
        return instance.getClass().equals(type);
    }
}
