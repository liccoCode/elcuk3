package models;

import play.data.validation.Error;

import java.util.List;
import java.util.Map;

/**
 * 前台返回结果使用
 * User: wyattpan
 * Date: 4/12/12
 * Time: 11:06 AM
 */
public class Ret {
    public boolean flag;
    public String message;


    public Ret(boolean flag, String message) {
        this.flag = flag;
        this.message = message;
    }

    /**
     * 提供没有 message 的 flag 设置
     *
     * @param flag
     */
    public Ret(boolean flag) {
        this(flag, "");
    }

    /**
     * 提供错误消息, 自动设置 flag 为 false
     *
     * @param message
     */
    public Ret(String message) {
        this(false, message);
    }

    public Ret() {
        this(true);
    }

    public Ret(Map<String, List<Error>> errors) {
        this(false);
        if(errors != null) {
            StringBuilder sbd = new StringBuilder();
            for(String key : errors.keySet()) {
                sbd.append(String.format("%s -> %s \r\n", key, errors.get(key)));
            }
            this.message = sbd.toString();
        }
    }
}
