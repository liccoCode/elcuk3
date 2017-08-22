package models.product;

import com.google.gson.annotations.Expose;
import helper.DBUtils;
import models.User;
import org.hibernate.annotations.DynamicUpdate;
import play.data.validation.Required;
import play.data.validation.Validation;
import play.db.jpa.Model;

import javax.persistence.*;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Team 类别
 * User: Cary
 * Date: 3-4-14
 * Time: 上午11:44
 */
@Entity
@DynamicUpdate
public class Team extends Model {

    private static final long serialVersionUID = 1377319282270688661L;
    /**
     * 将用户的Team缓存起来, 不用每次判断都去 db 取(注:更新Team的时候也需要更新缓存)
     */
    private static final Map<String, Set<Team>> TEAM_CACHE = new ConcurrentHashMap<>();

    /**
     * TEAM所拥有的USER
     */
    @ManyToMany(cascade = {CascadeType.REFRESH}, mappedBy = "teams", fetch = FetchType.LAZY)
    public Set<User> users = new HashSet<>();

    @Column(nullable = false, unique = true)
    @Required
    @Expose
    public String teamId;

    @Expose
    @Required
    public String name;

    @Lob
    @Expose
    public String memo;

    @Override
    public String toString() {
        return String.format("%s:%s", this.teamId, this.name);
    }

    /**
     * 删除这个 Team, 需要进行删除检查
     */
    public void deleteTeam() {
        /**
         * 1. Category上有没有绑定的 Team
         */
        List<Category> cates = this.getObjCategorys();
        if(cates != null && cates.size() > 0) {
            Validation.addError("", String.format("拥有 %s categorys 关联, 无法删除.", cates.size()));

        }
        /**
         * 1. 用户身上有没有绑定的 Team
         */
        if(users != null && users.size() > 0) {
            Validation.addError("", String.format("拥有 %s users 关联, 无法删除.", users.size()));
        }
        if(Validation.hasErrors()) return;


        this.delete();
    }

    public static boolean exist(Long id) {
        return Team.count("id=?", id) > 0;
    }


    /**
     * 所有TEAM组
     *
     * @return
     */
    public static List<Team> allTeams() {
        return Team.findAll();
    }

    /**
     * 初始化或者获取缓存的TEAM
     *
     * @param username
     * @return
     */
    public static Set<Team> teams(String username) {
        Set<Team> teams = TEAM_CACHE.get(username);
        if(teams == null) {
            /*这里拿一个 Privileges 的备份*/
            TEAM_CACHE.put(username, new HashSet<>(User.findByUserName(username).teams));
            teams = TEAM_CACHE.get(username);
        }
        return teams;
    }

    /**
     * 更新缓存的权限
     *
     * @param username
     * @param teams
     */
    public static void updateTeams(String username, Set<Team> teams) {
        TEAM_CACHE.remove(username);
        TEAM_CACHE.put(username, teams);
    }

    /**
     * 清理缓存的team
     *
     * @param user
     */
    public static void clearUserTeamsCache(User user) {
        TEAM_CACHE.remove(user.username);
    }

    public List<String> getStrCategorys() {
        List<String> categories = new ArrayList<>();
        String sql = "select c.categoryid From Category c "
                + " where c.team_id=" + this.id;
        List<Map<String, Object>> categorys = DBUtils.rows(sql);

        for(Map<String, Object> row : categorys) {
            categories.add(row.get("categoryid").toString());
        }
        return categories;
    }

    public List<Category> getObjCategorys() {
        List<Category> categories = Category.find("team=?", this).fetch();
        return categories;
    }

    public boolean existUser(User user) {
        return user.teams.contains(this);
    }
}
