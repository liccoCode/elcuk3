package models;

import com.google.gson.annotations.Expose;
import org.apache.commons.lang.StringUtils;
import org.apache.commons.lang.math.NumberUtils;
import org.joda.time.DateTime;
import play.Logger;
import play.data.validation.Required;
import play.db.jpa.GenericModel;
import play.db.jpa.JPA;
import play.db.jpa.Model;
import play.libs.F;
import play.mvc.Scope;
import play.utils.FastRuntimeException;

import javax.persistence.*;
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

    public ElcukRecord(String action, String message, String fid) {
        this.action = action;
        this.message = message;
        this.username = ElcukRecord.username();
        this.fid = fid;
    }

    public ElcukRecord(String action, String message, String username, String fid) {
        this(action, message, fid);
        this.username = username;
    }

    public ElcukRecord(String action, String message, String username, String fid, Date createAt) {
        this(action, message, username, fid);
        this.createAt = createAt;
    }

    @Required
    @Lob
    public String message;

    @Required
    @Column(length = 80)
    public String action;

    /**
     * 外键 model 的 IP
     */
    public String fid;

    /**
     * 用户名
     */
    @Required
    @Column(length = 80)
    public String username;

    /**
     * 日志的创建时间, 也就是记录时间
     */
    @Required
    public Date createAt = new Date();

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

    public long fidL() {
        return NumberUtils.toLong(this.fid);
    }

    public static List<ElcukRecord> records(String fid) {
        return ElcukRecord.find("fid=?", fid).fetch();
    }


    @Override
    public String toString() {
        final StringBuilder sb = new StringBuilder();
        sb.append("ElcukRecord");
        sb.append("{message='").append(message).append('\'');
        sb.append(", action='").append(action).append('\'');
        sb.append(", fid='").append(fid).append('\'');
        sb.append(", username='").append(username).append('\'');
        sb.append(", createAt=").append(createAt);
        sb.append('}');
        return sb.toString();
    }
}
