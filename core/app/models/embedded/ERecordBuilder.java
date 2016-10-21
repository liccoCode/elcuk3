package models.embedded;

import models.ElcukRecord;
import models.User;
import org.apache.commons.lang.StringUtils;
import play.i18n.Messages;
import play.utils.FastRuntimeException;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * Created by IntelliJ IDEA.
 * User: wyatt
 * Date: 1/14/13
 * Time: 12:18 PM
 */
public class ERecordBuilder implements Serializable {

    private static final long serialVersionUID = -2609240025264483736L;
    private String key;
    private String keyMsg;

    private List<String> keyArgs = new ArrayList<>();
    private List<String> msgArgs = new ArrayList<>();
    private String username;
    private String fid;

    private Class owner;

    public ERecordBuilder() {
    }

    /**
     * KeyMsg 为 [key].msg
     *
     * @param key
     */
    public ERecordBuilder(String key) {
        this(key, key + ".msg");
    }

    public ERecordBuilder(String key, String keyMsg) {
        this.key = key;
        this.keyMsg = keyMsg;
    }

    public ERecordBuilder actionArgs(String... args) {
        if(args != null && args.length > 0)
            keyArgs.addAll(Arrays.asList(args));
        return this;
    }

    public ERecordBuilder msgArgs(Object... args) {
        if(args != null && args.length > 0) {
            for(Object obj : args) {
                msgArgs.add(obj.toString());
            }
        }
        return this;
    }

    public ERecordBuilder user(String username) {
        if(StringUtils.isNotBlank(username))
            this.username = username;
        return this;
    }

    public ERecordBuilder fid(String fid) {
        if(StringUtils.isBlank(fid))
            throw new FastRuntimeException("设置的外键不能为空");
        this.fid = fid;
        return this;
    }

    public ERecordBuilder fid(Long fid) {
        if(fid == null)
            throw new FastRuntimeException("设置的外键不能为空");
        this.fid = fid + "";
        return this;
    }

    public ERecordBuilder fid(Long fid, Class owner) {
        if(fid == null) {
            throw new FastRuntimeException("设置的外键不能为空");
        }
        this.fid = fid.toString();
        this.owner = owner;
        return this;
    }

    public ElcukRecord record() {
        if(StringUtils.isBlank(this.fid))
            throw new FastRuntimeException("外键不能为空");

        ElcukRecord record = new ElcukRecord(
                Messages.get(this.key, this.keyArgs.toArray()),
                Messages.get(this.keyMsg, this.msgArgs.toArray())
        );
        record.fid = this.fid;
        record.owner = this.owner;
        if(StringUtils.isNotBlank(this.username))
            record.username = this.username;
        else {
            try {
                record.username = User.username();
                // 在非访问的情况下调用则无 Session
            } catch(NullPointerException e) {
                record.username = "system";
            }
        }
        return record;
    }

    public ElcukRecord save() {
        return this.record().save();
    }

    /**
     * Email msg 有两个参数 From, To
     * Fid: 为邮件类型
     *
     * @return
     */
    public ERecordBuilder mail() {
        return new ERecordBuilder("email.record", "email.record.msg");
    }
}
