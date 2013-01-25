package models.embedded;

import models.ElcukRecord;
import models.User;
import org.apache.commons.lang.StringUtils;
import play.i18n.Messages;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * Created by IntelliJ IDEA.
 * User: wyatt
 * Date: 1/14/13
 * Time: 12:18 PM
 */
public class ERecordBuilder {

    private String key;
    private String keyMsg;

    private List<String> keyArgs = new ArrayList<String>();
    private List<String> msgArgs = new ArrayList<String>();
    private String username;
    private String fid;

    public ERecordBuilder() {
    }

    public ERecordBuilder(String key, String keyMsg) {
        this.key = key;
        this.keyMsg = keyMsg;
    }

    public ERecordBuilder actionArgs(String... args) {
        if(args != null)
            this.keyArgs = Arrays.asList(args);
        return this;
    }

    public ERecordBuilder msgArgs(String... args) {
        if(args != null)
            this.msgArgs = Arrays.asList(args);
        return this;
    }

    public ERecordBuilder user(String username) {
        if(StringUtils.isNotBlank(username))
            this.username = username;
        return this;
    }

    public ERecordBuilder fid(String fid) {
        if(StringUtils.isNotBlank(fid))
            this.fid = fid;
        return this;
    }

    public ElcukRecord record() {
        ElcukRecord record = new ElcukRecord(
                Messages.get(this.key, this.keyArgs.toArray()),
                Messages.get(this.keyMsg, this.msgArgs.toArray())
        );
        record.fid = this.fid;
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
