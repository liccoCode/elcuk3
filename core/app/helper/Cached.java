package helper;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * 系统内对进行了缓存的方法添加此注解, 进行标注, 一个 Mark 注解
 * User: wyattpan
 * Date: 6/4/12
 * Time: 5:43 PM
 */
@Retention(RetentionPolicy.SOURCE)
@Target(ElementType.METHOD)
public @interface Cached {
    /**
     * 指定缓存多长时间的 expression
     *
     * @return
     */
    String value();
}
