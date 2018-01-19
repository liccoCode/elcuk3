package models;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.annotations.Expose;
import controllers.Login;
import helper.DBUtils;
import helper.Webs;
import models.finance.Payment;
import models.finance.PaymentUnit;
import models.market.Listing;
import models.market.Selling;
import models.product.Category;
import models.product.Team;
import models.whouse.Whouse;
import org.apache.commons.collections.Predicate;
import org.apache.commons.lang.StringUtils;
import org.hibernate.annotations.DynamicUpdate;
import play.data.validation.*;
import play.db.helper.JpqlSelect;
import play.db.jpa.Model;
import play.libs.Crypto;
import play.mvc.Scope;
import play.utils.FastRuntimeException;

import javax.persistence.*;
import java.util.*;
import java.util.Map.Entry;

/**
 * 系统中的用户
 * User: wyattpan
 * Date: 1/12/12
 * Time: 10:37 PM
 */
@Entity
@DynamicUpdate
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
    public Set<Privilege> privileges = new HashSet<>();

    /**
     * 用户所拥有的TEAM
     */
    @ManyToMany
    public Set<Team> teams = new HashSet<>();

    /**
     * 用户所拥有的ROLE
     */
    @ManyToMany(fetch = FetchType.EAGER)
    public Set<Role> roles = new HashSet<>();

    @ManyToMany
    public List<Category> categories = new ArrayList<>();


    @OneToMany(mappedBy = "payer", fetch = FetchType.LAZY)
    public List<Payment> paymentPaied = new ArrayList<>();

    /**
     * 一个人可以拥有很多个请款单元
     */
    @OneToMany(mappedBy = "payee", fetch = FetchType.LAZY)
    public List<PaymentUnit> pamentApplies = new ArrayList<>();

    /**
     * 用户的通知
     */
    @OneToMany(mappedBy = "user", fetch = FetchType.LAZY)
    public List<Notification> notifications = new ArrayList<>();

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

    @Transient
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

    public String sex;

    /**
     * 入职日期
     */
    public Date entryDate;

    /**
     * 该用户是否被关闭
     */
    @Expose
    public boolean closed = false;

    @Transient
    public static JsonObject USER_CATEGORY;

    /**
     * 指派给用户去质检的仓库
     */
    @OneToMany
    public List<Whouse> whouses;

    /**
     * 所属公司
     */
    @Enumerated(EnumType.STRING)
    @Column(length = 20)
    public COR projectName;

    public enum COR {
        EASYACC {
            @Override
            public String label() {
                return "EasyAcc";
            }

            @Override
            public String url() {
                return "https://e.easya.cc/";
            }

            @Override
            public String pic() {
                return "fa-tablet";
            }
        },
        Ecooe {
            @Override
            public String label() {
                return "Ecooe";
            }

            @Override
            public String url() {
                return "https://ecooe.elcuk-erp.com";
            }

            @Override
            public String pic() {
                return "fa-cutlery";
            }
        },
        Brandworl {
            @Override
            public String label() {
                return "Brandworl";
            }

            @Override
            public String url() {
                return "https://brandworl.elcuk-erp.com";
            }

            @Override
            public String pic() {
                return "fa-suitcase";
            }
        },
        OUTXE {
            @Override
            public String label() {
                return "Outxe";
            }

            @Override
            public String url() {
                return "https://outxe.elcuk-erp.com";
            }

            @Override
            public String pic() {
                return "fa-hdd-o";
            }
        },
        Redimp {
            @Override
            public String label() {
                return "Redimp";
            }

            @Override
            public String url() {
                return "https://redimp.elcuk-erp.com";
            }

            @Override
            public String pic() {
                return "fa-gamepad";
            }
        },
        Visio {
            @Override
            public String label() {
                return "Visio";
            }

            @Override
            public String url() {
                return "https://visio.elcuk-erp.com";
            }

            @Override
            public String pic() {
                return "fa-briefcase";
            }
        },
        Tripetals {
            @Override
            public String label() {
                return "Tripetals";
            }

            @Override
            public String url() {
                return "https://tripetals.elcuk-erp.com";
            }

            @Override
            public String pic() {
                return "fa-female";
            }
        },
        Wisle {
            @Override
            public String label() {
                return "Wisle";
            }

            @Override
            public String url() {
                return "https://wisle.elcuk-erp.com";
            }

            @Override
            public String pic() {
                return "fa-child";
            }
        },
        Glastal {
            @Override
            public String label() {
                return "Glastal";
            }

            @Override
            public String url() {
                return "https://glastal.elcuk-erp.com";
            }

            @Override
            public String pic() {
                return "fa-cutlery";
            }
        },
        Lecone {
            @Override
            public String label() {
                return "Lecone";
            }

            @Override
            public String url() {
                return "https://lecone.elcuk-erp.com";
            }

            @Override
            public String pic() {
                return "fa-sliders";
            }
        },
        LanHai {
            @Override
            public String label() {
                return "LanHai";
            }

            @Override
            public String url() {
                return "https://lanhai.elcuk-erp.com";
            }

            @Override
            public String pic() {
                return "fa-female";
            }
        },
        MengTop {
            @Override
            public String label() {
                return "MengTop";
            }

            @Override
            public String url() {
                return "https://mengtop.elcuk-erp.com";
            }

            @Override
            public String pic() {
                return "fa-sitemap";
            }
        };

        public abstract String label();

        public abstract String url();

        public abstract String pic();
    }

    public enum D {
        Brand {
            @Override
            public String label() {
                return "品牌事业部";
            }
        },
        Audit {
            @Override
            public String label() {
                return "审计部";
            }
        },
        Finance {
            @Override
            public String label() {
                return "财务部";
            }
        },
        Develop {
            @Override
            public String label() {
                return "开发部";
            }
        },
        Shipment {
            @Override
            public String label() {
                return "物流部";
            }
        },
        Whouse {
            @Override
            public String label() {
                return "仓储部";
            }
        };

        public abstract String label();
    }

    /**
     * 所属部门
     */
    @Enumerated(EnumType.STRING)
    public D department;

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
        this.username = this.username.trim();
        // 密码的加密操作在保存的时候进行; 在程序内部使用时为明文密码
        if(StringUtils.isNotBlank(this.password))
            this.passwordDigest = Crypto.encryptAES(this.password);
    }

    /**
     * 用户更新
     */
    public void update() {
        /*
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
        List<Privilege> privilegeList = Privilege.find("id IN " + JpqlSelect.inlineParam(privilegeId)).fetch();
        if(privilegeId.size() != privilegeList.size())
            throw new FastRuntimeException("需要修改的权限数量与系统中存在的不一致, 请确通过 Web 形式修改.");
        this.privileges = new HashSet<>();
        this.save();
        this.privileges.addAll(privilegeList);
        Privilege.updatePrivileges(this.username, this.privileges);
        this.save();
    }


    /**
     * 当前用户还没有的权限
     *
     * @return
     */
    public boolean isHaveTeam(Team team) {
        return this.teams.contains(team);
    }

    public boolean isHaveCategory(Category category) {
        return this.categories.contains(category);
    }

    /**
     * 增加Team;(删除原来的, 重新添加现在的)
     *
     * @param teamId
     */
    public void addTeams(List<Long> teamId) {
        if(teamId == null || teamId.size() == 0) {
            this.teams = new HashSet<>();
            this.save();
            Team.updateTeams(this.username, this.teams);
        } else {
            List<Team> teamList = Team.find("id IN " + JpqlSelect.inlineParam(teamId)).fetch();
            if(teamId.size() != teamList.size())
                throw new FastRuntimeException("需要修改的Team数量与系统中存在的不一致, 请确通过 Web 形式修改.");
            this.teams = new HashSet<>();
            this.save();
            this.teams.addAll(teamList);
            Team.updateTeams(this.username, this.teams);
            this.save();
        }
    }

    public void addCategories(List<String> categoryId) {
        if(categoryId == null || categoryId.size() == 0) {
            this.categories = new ArrayList<>();
            Category.updateCategory(this.username, this.categories);
            this.save();
        } else {
            List<Category> categoryList = Category.find("categoryId IN " + JpqlSelect.inlineParam(categoryId)).fetch();
            if(categoryList.size() != categoryList.size())
                throw new FastRuntimeException("需要修改的Category数量与系统中存在的不一致, 请确通过 Web 形式修改.");
            this.categories = new ArrayList<>();
            this.save();
            this.categories.addAll(categoryList);
            Category.updateCategory(this.username, this.categories);
            this.save();
        }
    }

    /**
     * 增加Role;(删除原来的, 重新添加现在的)
     *
     * @param roleId
     */
    public void addRoles(List<Long> roleId) {
        if(roleId == null || roleId.size() == 0) {
            this.roles = new HashSet<>();
            this.save();
            Role.updateRoles(this.username, this.roles);
        } else {
            List<Role> roleList = Role.find("id IN " + JpqlSelect.inlineParam(roleId)).fetch();
            if(roleId.size() != roleList.size())
                throw new FastRuntimeException("需要修改的Role数量与系统中存在的不一致, 请确通过 Web 形式修改.");
            this.roles = new HashSet<>();
            this.save();
            this.roles.addAll(roleList);
            Role.updateRoles(this.username, this.roles);
            this.save();
        }
    }

    /**
     * 当前用户还没有的角色
     *
     * @return
     */
    public boolean isHaveRole(Role role) {
        return this.roles.contains(role);
    }

    /**
     * 修改密码
     *
     * @param password
     */
    public void changePasswd(String password) {
        //  由于 User 会被保存在 Cache 中, 那么 User 则处于游离状态, 为了保持缓存中游离对象, 所以需要将缓存中的游离对象进行一次更新
        this.passwordDigest = Crypto.encryptAES(password);
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
        return !StringUtils.isBlank(this.passwordDigest) && this.passwordDigest.equals(Crypto.encryptAES(password));
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
        Privilege.privileges(this.username, this.roles);
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
        else return StringUtils.lowerCase(username);
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
     * @deprecated 这段代码还需要吗?
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

        Set<User> users = new HashSet<>();
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
        Set<User> users = new HashSet<>();
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

    /**
     * User的所有Category_Id
     *
     * @return
     */
    public static List<String> getTeamCategorys(User user) {
        List<String> categories = new ArrayList<>();
        String sql = "select c.categoryid From User_Team u,Category c "
                + " where u.teams_id=c.team_id"
                + " and users_id=" + user.id;
        List<Map<String, Object>> categorys = DBUtils.rows(sql);
        if(categorys != null && categorys.size() > 0) {
            for(Map<String, Object> row : categorys) {
                categories.add(row.get("categoryid").toString());
            }
        }
        return categories;
    }


    /**
     * User的所有Category
     *
     * @return
     */
    public static List<Category> getObjCategorys(User user) {
        List<Category> categories = new ArrayList<>();
        for(Team team : user.teams) {
            List<Category> categoryList = team.getObjCategorys();
            categories.addAll(categoryList);
        }
        return categories;
    }

    /**
     * User 的 SKU 权限
     *
     * @param user
     * @return
     */
    public static List<String> getSkus(User user) {
        return Category.getSKUs(getTeamCategorys(user));
    }

    /**
     * User 的 Listing 权限
     *
     * @return
     */
    public static List<String> getListings(User user) {
        List<String> listings = new ArrayList<>();
        for(String sku : getSkus(user)) {
            listings.addAll(Listing.getAllListingBySKU(sku));
        }
        return listings;
    }

    /**
     * User 的 Selling 权限
     *
     * @param user
     * @return
     */
    public static List<String> getSellings(User user) {
        return Selling.getSellingIds(getListings(user));
    }


    /**
     * 用户登陆后将验证key值返回给 cookie
     *
     * @param username
     * @return
     */
    public static String userMd5(String username) {
        return Webs.md5(String.format("playelcuk2userauthenticate%s", username));
    }

    /**
     * 还没有通知的消息
     *
     * @return
     */
    public List<Notification> unNotifiedNotification() {
        return Notification.find("user=? AND state = 'UNCHECKED' ORDER BY createAt", this).fetch();
    }

    /**
     * 是否含有跨部门查看数据权限
     *
     * @return
     */
    public boolean isHaveCrossCop() {
        return Role.isHaveCrossCop(this);
    }

    /**
     * 是否物流人员
     *
     * @return
     */
    public boolean isShipmentRole() {
        return Role.isShipmentRole(this);
    }

}
