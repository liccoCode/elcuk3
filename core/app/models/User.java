package models;

import play.data.validation.Email;
import play.data.validation.Equals;
import play.data.validation.Password;
import play.data.validation.Required;
import play.db.jpa.Model;
import play.libs.Crypto;

import javax.persistence.*;

/**
 * 系统中的用户
 * User: wyattpan
 * Date: 1/12/12
 * Time: 10:37 PM
 */
@Entity
public class User extends Model {
    public enum P {
        GUEST,
        NORMAL,
        MANAGER,
        ROOT
    }

    @Column(nullable = false, unique = true)
    @Required
    public String username;

    @Column(nullable = false)
    @Password
    @Required
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
    public void prePersist() {
        // 密码的加密操作在保存的时候进行; 在程序内部使用时为明文密码
        this.password = Crypto.encryptAES(this.password);
    }

    @PostPersist
    public void postPersist() {
        this.password = Crypto.decryptAES(this.password);
    }

    @PostLoad
    public void postLoad() {
        this.password = Crypto.decryptAES(this.password);
    }

    @PreUpdate
    public void preUpdate() {
        this.password = Crypto.encryptAES(this.password);
    }

    @PostUpdate
    public void postUpdate() {
        this.password = Crypto.decryptAES(this.password);
    }

    // ------------------------------

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

    /**
     * 修改密码
     *
     * @param passwd
     */
    public void changePasswd(String passwd) {
        //  由于 User 会被保存在 Cache 中, 那么 User 则处于游离状态, 为了保持缓存中游离对象, 所以需要将缓存中的游离对象进行一次更新
        this.password = passwd;
        this.save();
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
}
