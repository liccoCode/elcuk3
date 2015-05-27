package models.qc;

import helper.DBUtils;
import models.Role;
import models.User;
import models.product.Category;
import models.product.Team;
import play.db.helper.SqlSelect;
import play.db.jpa.Model;

import javax.persistence.Entity;
import javax.persistence.ManyToOne;
import javax.persistence.Transient;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Map;

/**
 * Created by licco on 15/5/25.
 */
@Entity
public class CheckTaskAssign extends Model {

    private static final long serialVersionUID = 65458353246678553L;

    @ManyToOne
    public Team team;

    @ManyToOne
    public Category category;

    @ManyToOne
    public User user;

    public boolean isCharge;

    @ManyToOne
    public User createrId;

    public Date createDate;

    @Transient
    public List<String> categorys;

    @Transient
    public List<CheckTaskAssign> assigns;

    @Transient
    public List<Team> teamlist;

    @Transient
    public String userName;

    public int buildMaxCategoryLength(Long teamId) {
        SqlSelect sql = new SqlSelect()
                .select("c.categoryId").from("Category c")
                .leftJoin("CheckTaskAssign a ON a.category_categoryId = c.categoryId")
                .where("c.team_id = ?")
                .orderBy("c.categoryId")
                .param(teamId);
        List<Map<String, Object>> rows = DBUtils
                .rows(sql.toString(), sql.getParams().toArray());
        return rows.size();
    }

    public List<Category> buildCategory(Long teamId) {
        return Category.find("team.id=?", teamId).fetch();
    }


    public List<CheckTaskAssign> buildUserList(Long teamId, String categoryId) {
        List<CheckTaskAssign> list = CheckTaskAssign.find("team.id=? AND category.categoryId=?", teamId, categoryId)
                .fetch();
        this.assigns = list;
        return list;
    }

    public String showAllRoleName() {
        String name = "";
        if(this.user.roles.size() == 0)
            return "";

        for(Role role : this.user.roles) {
            name += role.roleName + ",";
        }
        return name.substring(0, name.length() - 1);
    }

    public void query() {
        this.teamlist = Team.findAll();
    }

    public boolean isNameCorrect(String name) {
        return User.findByUserName(name) != null;
    }

    public static List<Long> showCategoryByUserName(String userName) {
        List<CheckTaskAssign> cList = CheckTaskAssign.find("user.username = ?", userName).fetch();
        List<Long> categoryList = new ArrayList<Long>();
        for(CheckTaskAssign cassign : cList) {
            categoryList.add(Long.parseLong(cassign.category.categoryId));
        }
        return categoryList;
    }
}
