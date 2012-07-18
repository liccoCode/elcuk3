package helper;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.TypeReference;
import com.alibaba.fastjson.serializer.SerializerFeature;
import com.google.gson.GsonBuilder;
import play.Play;

/**
 * 由于系统是使用了 FastJson 与 Gson, 而在测试环境下 FastJson 无法进行重新加载的问题, 所以将 JSON 操作放到一个地方
 * User: wyattpan
 * Date: 7/16/12
 * Time: 4:04 PM
 */
public class J {

    public static String json(Object obj) {
        try {
            return fastjson(obj);
        } catch(Exception e) {
            if(Play.mode.isDev()) {
                return dateTimeFormatGsonBuilder().create().toJson(obj);
            } else {
                return fastjson(obj);
            }
        }
    }


    public static <T> T from(String json, Class<T> clazz) {
        try {
            return JSON.parseObject(json, clazz);
        } catch(Exception e) {
            if(Play.mode.isDev()) {
                return dateTimeFormatGsonBuilder().create().fromJson(json, clazz);
            } else {
                return JSON.parseObject(json, clazz);
            }
        }
    }


    private static String fastjson(Object obj) {
        return JSON.toJSONStringWithDateFormat(obj, "yyyy-MM-dd HH:mm:ss",
                SerializerFeature.SkipTransientField,
                SerializerFeature.WriteEnumUsingToString,
                SerializerFeature.WriteNullBooleanAsFalse,
                SerializerFeature.WriteNullNumberAsZero,
                SerializerFeature.WriteNullListAsEmpty);
    }

    private static GsonBuilder dateTimeFormatGsonBuilder() {
        return new GsonBuilder().setDateFormat("yyyy-MM-dd HH:mm:ss");
    }

    /**
     * 直接利用 FastJson 的泛型的反序列化
     *
     * @param json
     * @param typeReference
     * @param <T>
     * @return
     */
    public static <T> T from(String json, TypeReference<T> typeReference) {
        return JSON.parseObject(json, typeReference);
    }

    /**
     * 直接利用 Gson 的 @Expose 注解
     *
     * @param obj
     * @return
     */
    public static String G(Object obj) {
        return dateTimeFormatGsonBuilder().excludeFieldsWithoutExposeAnnotation().create().toJson(obj);
    }

}
