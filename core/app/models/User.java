package models;

import org.apache.commons.collections.Predicate;
import org.apache.commons.lang.StringUtils;
import play.data.validation.Email;
import play.data.validation.Equals;
import play.data.validation.Password;
import play.data.validation.Required;
import play.db.helper.JpqlSelect;
import play.db.jpa.Model;
import play.libs.Crypto;

import javax.persistence.*;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * 系统中的用户
 * User: wyattpan
 * Date: 1/12/12
 * Time: 10:37 PM
 */
@Entity
@org.hibernate.annotations.Entity(dynamicUpdate = true)
public class User extends Model {
    public enum P {
        GUEST,
        NORMAL,
        MANAGER,
        ROOT
    }

    /**
     * 用户所拥有的权限
     */
    @ManyToMany
    public Set<Privilege> privileges = new HashSet<Privilege>();

    @Column(nullable = false, unique = true)
    @Required
    public String username;

    /**
     * 加密以后的密码
     */
    @Required
    @Password
    public String passwordDigest;

    @Transient
    public String password;

    @Transient
    @Equals("password")
    public String confirm;

    @Email
    @Required
    public String email;

    @Column(nullable = false)
    @Required
    public P power;

    /**
     * 是否为售后支持部门
     */
    public boolean isService = false;


    public boolean closed = false;

    public User() {
    }

    public User(String username, String password) {
        this.username = username;
        this.password = password;
    }

    // -------- 密码的操作 ----------
    /*
     * 1. 创建保存前加密, 创建保存后解密
     * 2. 加载完成后解密
     * 3. 更新前加密, 更新完成后解密
     */

    @PrePersist
    @PreUpdate
    public void prePersist() {
        // 密码的加密操作在保存的时候进行; 在程序内部使用时为明文密码
        this.passwordDigest = Crypto.encryptAES(this.password);
    }

    // ------------------------------

    /**
     * 当前用户还没有的权限
     *
     * @return
     */
    public boolean isHavePrivilege(Privilege privilege) {
        return this.privileges.contains(privilege);
    }

    /**
     * 增加权限
     *
     * @param privilegeId
     */
    public void addPrivileges(List<Long> privilegeId) {
        List<Privilege> privileges = Privilege.find("id IN " + JpqlSelect.inlineParam(privilegeId)).fetch();
        this.privileges = new HashSet<Privilege>();
        this.save();
        this.privileges.addAll(privileges);
        Privilege.updatePrivileges(this.username, this.privileges);
        this.save();
    }

    /**
     * 修改密码
     *
     * @param passwd
     */
    public void changePasswd(String passwd) {
        //  由于 User 会被保存在 Cache 中, 那么 User 则处于游离状态, 为了保持缓存中游离对象, 所以需要将缓存中的游离对象进行一次更新
        this.password = passwd;
        this.passwordDigest = Crypto.encryptAES(this.password);
        this.save();
    }

    /**
     * 验证用户登陆
     *
     * @param password
     * @return
     */
    public boolean authenticate(String password) {
        return !StringUtils.isBlank(this.passwordDigest) && this.passwordDigest.equals(Crypto.encryptAES(password));
    }


    @Override
    public boolean equals(Object o) {
        if(this == o) return true;
        if(o == null || getClass() != o.getClass()) return false;
        if(!super.equals(o)) return false;

        User user = (User) o;

        if(!username.equals(user.username)) return false;

        return true;
    }

    @Override
    public int hashCode() {
        int result = super.hashCode();
        result = 31 * result + username.hashCode();
        return result;
    }

    /**
     * 链接用户(登陆)
     *
     * @param username
     * @param password 明文密码
     * @return
     */
    public static User connect(String username, String password) {
        return User.find("username=? AND password=?", username, Crypto.encryptAES(password)).first();
    }

    public static User findByUserName(String username) {
        return User.find("username=?", username).first();
    }

    public static List<User> serviceUsers() {
        return User.find("isService=? AND closed=?", true, false).fetch();
    }

    public boolean getClosed() {
        return closed;
    }

    public void setClosed(boolean closed) {
        this.closed = closed;
    }

    /**
     * 过滤出没有的权限
     */
    static class UnPrivilegePrediect implements Predicate {
        private String actionName;

        UnPrivilegePrediect(String actionName) {
            this.actionName = actionName;
        }

        @Override
        public boolean evaluate(Object o) {
            Privilege pri = (Privilege) o;
            return !StringUtils.equals(this.actionName, pri.name);
        }
    }

}
