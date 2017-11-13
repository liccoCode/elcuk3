package controllers;

import controllers.api.SystemOperation;
import models.product.Team;
import org.apache.commons.lang.StringUtils;
import play.data.validation.Validation;
import play.mvc.Before;
import play.mvc.Controller;
import play.mvc.With;

import java.util.List;

/**
 * Created by IntelliJ IDEA.
 * User: cary
 * Date: 3/4/14
 * Time: 11:05 AM
 */
@With({GlobalExceptionHandler.class, Secure.class, SystemOperation.class})
public class Teams extends Controller {

    @Before(only = {"update", "delete"})
    public static void setUpShowPage() {
        List<Team> teams = Team.all().fetch();
        renderArgs.put("teams", teams);
    }

    @Check("teams.show")
    public static void show() {
        List<Team> teams = Team.findAll();
        render(teams);
    }

    public static void update(Team team) {
        if(!Team.exist(team.id)) {
            flash.error(String.format("Team %s 不存在!", team.teamId));
            redirect("/Teams/show");
        }
        validation.valid(team);
        if(Validation.hasErrors()) render("Teams/show.html", team);
        team.save();
        redirect("/Teams/show/" + team.id);
    }


    public static void delete(Long id) {
        Team team = Team.findById(id);
        team.deleteTeam();
        if(Validation.hasErrors()) render("Teams/show.html", team);
        flash.success("Team %s 删除成功", team.teamId);
        redirect("/Teams/show");
    }

    public static void blank(Long id) {
        Team team;
        if(id != null) {
            team = Team.findById(id);
        } else {
            team = new Team();
        }
        render(team);
    }

    public static void create(Team team) {
        if(StringUtils.isBlank(team.teamId)) Validation.addError("", "Team Id 必须填写");
        validation.valid(team);
        if(Validation.hasErrors()) render("Teams/blank.html", team);
        team.save();
        flash.success("创建成功.");
        redirect("/Teams/show/" + team.id);
    }
}
