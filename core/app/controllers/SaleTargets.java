package controllers;

import com.alibaba.fastjson.TypeReference;
import helper.DBUtils;
import helper.J;
import helper.Webs;
import models.SaleTarget;
import models.User;
import models.product.Team;
import models.view.Ret;
import play.data.validation.Validation;
import play.db.helper.SqlSelect;
import play.mvc.Controller;
import play.mvc.With;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * 销售目标基本操作
 * <p/>
 * Created by IntelliJ IDEA.
 * User: mac
 * Date: 14-3-11
 * Time: PM4:50
 */

@With({GlobalExceptionHandler.class, Secure.class})
public class SaleTargets extends Controller {

    @Check("saletargets.annualindex")
    public static void annualIndex() {
        List<SaleTarget> salesTargets = SaleTarget.find("saleTargetType=?", SaleTarget.T.YEAR).fetch();
        render(salesTargets);
    }

    @Check("saletargets.monthindex")
    public static void monthIndex() {
        Set<Team> teams = User.findByUserName(Secure
                .Security.connected()).teams;
        List<Long> teamIds = new ArrayList<Long>();
        for(Team team : teams) {
            teamIds.add(team.id);
        }
        List<SaleTarget> salesTargets = SaleTarget.find("saleTargetType=? AND fid in " + SqlSelect.inlineParam(teamIds)
                + "", SaleTarget.T.MONTH).fetch();
        render(salesTargets);
    }

    public static void show(Long id) {
        SaleTarget st = SaleTarget.findById(id);

        List<SaleTarget> saleTargets = SaleTarget.find("parentId=?", id).fetch();
        if(saleTargets.size() == 0) {
            saleTargets = st.beforeDetails();
        }
        render(st, saleTargets);
    }

    @Check("saletargets.createannual")
    public static void createAnnual() {
        SaleTarget st = new SaleTarget();
        render(st);
    }

    @Check("saletargets.createannual")
    public static void doCreateAnnual(SaleTarget st) {
        validation.valid(st);
        st.createuser = User.findByUserName(Secure.Security.connected());
        st.saleTargetType = SaleTarget.T.YEAR;
        if(Validation.hasErrors()) {
            render("SaleTargets/createAnnual.html", st);
        }
        //判断对象是否已经存在
        if(st.isNotExist()) {
            st.save();
            flash.success("保存成功.");
            show(st.id);
        } else {
            flash.error(String.format("已经存在 %s 年度的销售目标！", st.targetYear));
            render("SaleTargets/createAnnual.html", st);
        }
    }


    public static void updateAnnual(SaleTarget st, List<SaleTarget> childs) {
        st.validate();
        if(Validation.hasErrors()) {
            renderJSON(new Ret(Webs.V(Validation.errors())));
        }
        //这里采用SQL查询而不采用Model.findById是因为该方法查询出来的数据总是从前端表单发送过来的新数据而不是数据库内储存的旧数据
        //TODO 搞清楚Play 使用findById查询原理
        Map<String, Object> rows = DBUtils.row("SELECT targetYear FROM SaleTarget WHERE id=?", st.id);
        Integer oldTargetYear = (Integer) rows.get("targetYear");

        if(!st.isNotExist() && !st.targetYear.equals(oldTargetYear)) {
            flash.error(String.format("已经存在 %s 年度的销售目标！", st.targetYear));
            show(st.id);
        }
        if(!Validation.hasErrors()) {
            st.save();
            if(childs != null) st.saveOrUpdateChild(childs, User.findByUserName(Secure.Security.connected()));
        }
        flash.success("更新成功");
        show(st.id);
    }
}
