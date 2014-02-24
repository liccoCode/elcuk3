package models;

import com.google.gson.JsonElement;
import com.google.gson.annotations.Expose;
import controllers.Login;
import models.finance.Payment;
import models.finance.PaymentUnit;
import org.apache.commons.collections.Predicate;
import org.apache.commons.lang.StringUtils;
import play.data.validation.*;
import play.db.helper.JpqlSelect;
import play.db.jpa.Model;
import play.libs.Crypto;
import play.mvc.Scope;
import play.utils.FastRuntimeException;
import com.google.gson.JsonObject;
import com.google.gson.JsonPrimitive;

import java.util.Map.Entry;
import java.util.Iterator;

import javax.persistence.*;
import java.util.ArrayList;
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
    private static final long serialVersionUID = 4195929532608535016L;

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

    @OneToMany(mappedBy = "payer", fetch = FetchType.LAZY)
    public List<Payment> paymentPaied = new ArrayList<Payment>();

    /**
     * 一个人可以拥有很多个请款单元
     */
    @OneToMany(mappedBy = "payee", fetch = FetchType.LAZY)
    public List<PaymentUnit> pamentApplies = new ArrayList<PaymentUnit>();

    /**
     * 用户的通知
     */
    @OneToMany(mappedBy = "user", fetch = FetchType.LAZY)
    public List<Notification> notifications = new ArrayList<Notification>();

    @Column(nullable = false, unique = true)
    @Required
    @Expose
    public String username;

    /**
     * 加密以后的密码
     */
    @Required
    @Password
    public String passwordDigest;

    public String password;

    @Transient
    @Equals("password")
    public String confirm;

    @Email
    @Required
    public String email;

    @Phone
    @Expose
    public String phone;

    /**
     * 固定电话
     */
    @Phone
    @Expose
    public String tel;

    /**
     * qq 号码
     */
    @Expose
    public String qq;

    /**
     * 旺旺
     */
    @Expose
    public String wangwang;

    /**
     * 该用户是否被关闭
     */
    @Expose
    public boolean closed = false;

    @Transient
    public static JsonObject USER_CATEGORY;

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
        if(StringUtils.isNotBlank(this.password))
            this.passwordDigest = Crypto.encryptAES(this.password);
    }

    /**
     * 用户更新
     */
    public void update() {
        /**
         * 1. 验证密码是否正确
         * 2. 进行更新
         * 3. 更新缓存中的 user
         */
        if(!this.authenticate(this.password))
            throw new FastRuntimeException("密码错误");
        this.save();
        Login.updateUserCache(this);
    }

    public List<Notification> notificationFeeds(int page) {
        return notificationFeeds(page, 80);
    }

    public List<Notification> notificationFeeds(int page, int pageSize) {
        return Notification.find("user=? ORDER BY createAt DESC", this).fetch(page, pageSize);
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
     * 增加权限;(删除原来的, 重新添加现在的)
     *
     * @param privilegeId
     */
    public void addPrivileges(List<Long> privilegeId) {
        List<Privilege> privileges = Privilege.find("id IN " + JpqlSelect.inlineParam(privilegeId))
                .fetch();
        if(privilegeId.size() != privileges.size())
            throw new FastRuntimeException("需要修改的权限数量与系统中存在的不一致, 请确通过 Web 形式修改.");
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
        Login.updateUserCache(this);
    }

    /**
     * 验证用户登陆
     *
     * @param password
     * @return
     */
    public boolean authenticate(String password) {
        return !StringUtils.isBlank(this.passwordDigest) &&
                this.passwordDigest.equals(Crypto.encryptAES(password));
    }

    /**
     * 初始化这个 User 的 Notification Queue
     */
    public void login() {
        /**
         * User 相关的三个缓存 :
         * 1. user 用户缓存
         * 2. 用户权限缓存
         * 3. 用户 Notification Queue 缓存
         */
        //TODO 这里的缓存都是通过 Model 自己进行的缓存, 只能够支持单机缓存, 无法分布式.
        Privilege.privileges(this.username);
    }

    /**
     * 用户登出前的处理
     */
    @SuppressWarnings("unchecked")
    public void logout() {
        /**
         * 1. 清理 Caches 中的 user
         * 2. 清理 Privileges 缓存
         * 3. 清理 Notification Queue 缓存
         */
        Login.clearUserCache(this);
        Privilege.clearUserPrivilegesCache(this);
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
     * 当前登陆用户的名称
     *
     * @return
     */
    public static String username() {
        String username = Scope.Session.current().get("username");
        if(StringUtils.isBlank(username)) return "system";
        else return username;
    }

    public static User current() {
        return User.findByUserName(username());
    }


    /**
     * 返回所有开启的用户
     *
     * @return
     */
    public static List<User> openUsers() {
        return User.find("closed=?", false).fetch();
    }

    /**
     * TODO: @duan 这里你删除了 isProcure 但是没有搜索到这个地方使用了他, 致使采购页面无法访问, 请思考如何处理.
     * 思考：考虑采用角色 对用户分配角色 对角色分配属性
     *
     * @return
     */
    public static List<User> procurers() {
        return User.find("isProcure=?", true).fetch();
    }

    /**
     * 链接用户(登陆)
     *
     * @param username
     * @param password 明文密码
     * @return
     */
    public static User connect(String username, String password) {
        return User.find("username=? AND password=?", username, Crypto.encryptAES(password))
                .first();
    }

    public static User findByUserName(String username) {
        return User.find("username=?", username).first();
    }

    /**
     * 初始化产品线人员
     *
     * @return
     */
    public static JsonObject getUsercategor() {
        if(User.USER_CATEGORY == null || User.USER_CATEGORY.isJsonNull()) {
            //初始化运营人员权限
            User.USER_CATEGORY = new JsonObject();
            USER_CATEGORY.addProperty("80,11,82", "andy");
            USER_CATEGORY.addProperty("70,71,73", "vera");
            USER_CATEGORY.addProperty("50,72,88,89,90,91,92", "sherry");
        }
        return User.USER_CATEGORY;
    }

    /**
     * 运营人员
     *
     * @return
     */
    public static Set<User> operations(String sku) {
        String userids = "";
        if(!StringUtils.isBlank(sku)) {
            String category = sku.substring(0, 2);
            //查找相应的产品线人员
            for(Entry<String, JsonElement> stringJsonElementEntry : getUsercategor().entrySet()) {
                String key = stringJsonElementEntry.getKey();
                if(key.contains(category)) {
                    userids = stringJsonElementEntry.getValue().toString();
                    break;
                }
            }
        }

        Set<User> users = new HashSet<User>();
        for(String name : new String[]{userids}) {
            User user = User.findByUserName(name);
            if(user != null) users.add(user);
        }
        return users;
    }


    /**
     * 物流人员
     *
     * @return
     */
    public static Set<User> shipoperations() {
        Set<User> users = new HashSet<User>();
        for(String name : new String[]{"wendy"}) {
            User user = User.findByUserName(name);
            if(user != null) users.add(user);
        }
        return users;
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
