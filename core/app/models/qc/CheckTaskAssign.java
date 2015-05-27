package models.qc;

import controllers.Login;
import helper.DBUtils;
import models.ElcukRecord;
import models.Role;
import models.User;
import models.product.Category;
import models.product.Team;
import play.db.helper.SqlSelect;
import play.db.jpa.Model;

import javax.persistence.Entity;
import javax.persistence.ManyToOne;
import javax.persistence.Transient;
import java.text.SimpleDateFormat;
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

    public void createTaskAssign() {
        SimpleDateFormat formatter = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        this.user = User.findByUserName(this.userName);
        this.createDate = new Date();
        this.createrId = Login.current();
        this.save();
        StringBuilder message = new StringBuilder(
                "操作人:" + this.createrId.username + " 操作时间:" + formatter.format(this.createDate) + " 添加"
                        + this.category.categoryId + " (" + this.category.name + ") 品线负责人为：" + this.user.username);
        if(this.isCharge)
            message.append(",且为主要负责人。");
        new ElcukRecord("质检员任务分配", message.toString(), String.valueOf(this.id)).save();
    }

    public static void deleteAssignById(Long assid) {
        CheckTaskAssign c = CheckTaskAssign.findById(assid);
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        new ElcukRecord("质检员任务分配",
                "操作人:" + c.createrId.username + " 操作时间:" + sdf.format(c.createDate) + " 删除" + c.category.categoryId +
                        "(" + c.category.name + ") 品线负责人：" + c.user.username, String.valueOf(c.id)).save();
        c.delete();
    }

    public static void updateTaskAssign(CheckTaskAssign c, Long id) {
        CheckTaskAssign old = CheckTaskAssign.findById(id);
        SimpleDateFormat formatter = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        StringBuilder message = new StringBuilder();
        if(!old.user.username.equals(c.userName)) {
            message.append("操作人:" + Login.current().username + " 操作时间:" + formatter.format(new Date()) + " 修改" +
                    c.category.categoryId + "(" + c.category.name + ") 品线负责人从：" + old.user.username + "改成 " +
                    c.userName);
        }
        if(old.isCharge != c.isCharge) {
            if(message.length() > 0) {
                if(c.isCharge) {
                    message.append(",且修改为主要负责人。");
                } else {
                    message.append(",且取消其主要负责人。");
                }
            } else {
                if(c.isCharge) {
                    message.append("操作人:" + Login.current().username + " 操作时间:" + formatter.format(new Date()) + " 修改" +
                            c.category.categoryId + "(" + c.category.name + ") 品线 " + old.user.username + "为主要负责人。");
                } else {
                    message.append("操作人:" + Login.current().username + " 操作时间:" + formatter.format(new Date()) + " 修改" +
                            c.category.categoryId + "(" + c.category.name + ") 品线" + old.user.username + " 取消其主要负责人。");
                }
            }
        }
        if(message.length() > 0) {
            old.user = User.findByUserName(c.userName);
            old.isCharge = c.isCharge;
            old.save();
            new ElcukRecord("质检员任务分配", message.toString(), String.valueOf(c.id)).save();
        }
    }
}
