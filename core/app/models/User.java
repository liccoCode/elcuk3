package models;

import play.data.validation.Email;
import play.data.validation.Password;
import play.db.jpa.Model;
import play.libs.Crypto;

import javax.persistence.Column;
import javax.persistence.Entity;

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
    public String username;

    @Column(nullable = false)
    @Password
    public String password;

    @Email
    public String email;

    @Column(nullable = false)
    public P power;


    public boolean closed = false;

    public User() {
    }

    public User(String username, String password) {
        this.username = username;
        this.password = password;
    }

    public static User connect(String username, String password) {
        return User.find("username=? AND password=?", username, Crypto.encryptAES(password)).first();
    }
}
