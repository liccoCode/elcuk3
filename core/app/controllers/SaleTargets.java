package controllers;

import com.alibaba.fastjson.TypeReference;
import helper.DBUtils;
import helper.J;
import helper.Webs;
import models.SaleTarget;
import models.User;
import models.view.Ret;
import org.apache.commons.lang.NumberUtils;
import org.apache.commons.lang.StringUtils;
import play.data.validation.Validation;
import play.mvc.Controller;
import play.mvc.With;

import java.util.List;
import java.util.Map;

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

    public static void annualIndex() {
        List<SaleTarget> salesTargets = SaleTarget.find("saleTargetType=?", SaleTarget.T.YEAR).fetch();
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

    public static void createAnnual() {
        SaleTarget st = new SaleTarget();
        render(st);
    }

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

    public static void updateAnnual(SaleTarget st) {
        validation.valid(st);
        if(Validation.hasErrors()) {
            renderJSON(new Ret(Webs.V(Validation.errors())));
        }
        //这里采用SQL查询而不采用Model.findById是因为该方法查询出来的数据总是从前端表单发送过来的新数据而不是数据库内储存的旧数据
        //TODO 搞清楚Play 使用findById查询原理
        Map<String, Object> rows = DBUtils.row("SELECT targetYear FROM SaleTarget WHERE id=?", st.id);
        Integer oldTargetYear = (Integer) rows.get("targetYear");
        if(!st.isNotExist() && !st.targetYear.equals(oldTargetYear)) {
            renderJSON(new Ret(String.format("已经存在 %s 年度的销售目标！", st.targetYear)));
        } else {
            st.save();
            renderJSON(new Ret(true, "更新成功."));
        }
    }


    /**
     * 创建或更新 子销售目标
     *
     * @param jsonstr json字符串
     */
    public static void saleTarget(String jsonstr) {
        List<SaleTarget> saleTargetList = J.from(jsonstr, new TypeReference<List<SaleTarget>>() {});
        if(saleTargetList.size() == 0) renderJSON(new Ret(true, "保存成功"));

        SaleTarget father = SaleTarget.findById(saleTargetList.get(0).parentId);
        for(SaleTarget child : saleTargetList) {
            father.copySaleTarget(child, User.findByUserName(Secure.Security.connected()));
            validation.valid(child);
            if(Validation.hasErrors()) {
                renderJSON(Webs.V(Validation.errors()));
            }
            if(child.id == null) {
                child.save();
            } else {
                child.updateOld((SaleTarget) SaleTarget.findById(child.id));
            }
        }
        renderJSON(new Ret(true, "保存成功."));
    }

    public static void sales(List<SaleTarget> saleTargets) {
        /**
         * saleTargets[0].id
         * saleTargets[1].id
         * saleTargets[2].id
         */
    }
}
