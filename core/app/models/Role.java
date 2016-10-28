package models;

import com.google.gson.annotations.Expose;
import org.hibernate.annotations.DynamicUpdate;
import play.data.validation.Required;
import play.data.validation.Validation;
import play.db.helper.JpqlSelect;
import play.db.jpa.GenericModel;
import play.utils.FastRuntimeException;

import javax.persistence.*;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Created by IntelliJ IDEA.
 * User: mac
 * Date: 14-4-4
 * Time: 上午10:12
 */
@Entity
@DynamicUpdate
public class Role extends GenericModel {

    /**
     * 将用户的Role缓存起来, 不用每次判断都去 db 取(注:更新Team的时候也需要更新缓存)
     */
    private static final Map<String, Set<Role>> ROLE_CACHE = new ConcurrentHashMap<>();

    /**
     * ROLE所拥有的USER
     */
    @ManyToMany(cascade = {CascadeType.REFRESH}, mappedBy = "roles",
            fetch = FetchType.LAZY)
    public Set<User> users = new HashSet<>();

    @Id
    @Expose
    @GeneratedValue
    public Long roleId;

    @Expose
    @Required
    @Column(length = 215)
    public String roleName;

    @Lob
    @Expose
    @Column(length = 512)
    public String memo;

    /**
     * 用户所拥有的权限
     */
    @ManyToMany
    public Set<Privilege> privileges = new HashSet<>();

    @Override
    public String toString() {
        return String.format("%s:%s", this.roleId, this.roleName);
    }

    /**
     * 删除这个 Role, 需要进行删除检查
     */
    public void deleteRole() {
        /**
         * 用户身上有没有绑定的 ROLE
         */
        if(users != null && users.size() > 0) {
            Validation.addError("", String.format("拥有 %s users 关联, 无法删除.", users.size()));
        }
        if(Validation.hasErrors()) return;
        this.delete();
    }

    public static boolean exist(Long id) {
        return Role.count("roleId=?", id) > 0;
    }

    /**
     * 初始化或者获取缓存的ROLE
     *
     * @param username
     * @return
     */
    public static Set<Role> roles(String username) {
        Set<Role> roles = ROLE_CACHE.get(username);
        if(roles == null) {
            ROLE_CACHE.put(username, /*这里拿一个 Privileges 的备份*/
                    new HashSet<>(User.findByUserName(username).roles));
            roles = ROLE_CACHE.get(username);
        }
        return roles;
    }

    /**
     * 更新缓存的权限
     *
     * @param username
     * @param roles
     */
    public static void updateRoles(String username, Set<Role> roles) {
        ROLE_CACHE.remove(username);
        ROLE_CACHE.put(username, roles);
    }

    /**
     * 清理缓存的role
     *
     * @param user
     */
    public static void clearUserRolesCache(User user) {
        ROLE_CACHE.remove(user.username);
    }

    public boolean existRole(User user) {
        return user.roles.contains(this);
    }

    /**
     * 当前用户还没有的权限
     *
     * @return
     */
    public boolean isHavePrivilege(Privilege privilege) {
        return this.privileges.contains(privilege);
    }

    /**
     * 角色增加权限;(删除原来的, 重新添加现在的)
     *
     * @param privilegeId
     */
    public void addPrivileges(List<Long> privilegeId) {
        List<Privilege> privileges = Privilege.find("id IN " + JpqlSelect.inlineParam(privilegeId))
                .fetch();
        if(privilegeId.size() != privileges.size())
            throw new FastRuntimeException("需要修改的权限数量与系统中存在的不一致, 请确通过 Web 形式修改.");
        this.privileges = new HashSet<>();
        this.save();
        this.privileges.addAll(privileges);
        Privilege.updateRolePrivileges(this.roleName, this.privileges);
        this.save();
    }

    public static Role findByRoleName(String rolename) {
        return Role.find("roleName=?", rolename).first();
    }

    public static boolean isPm(User user) {
        Role role = Role.find("roleName=?", "PM角色").first();
        if(role == null) return false;
        return role.existRole(user);
    }
}