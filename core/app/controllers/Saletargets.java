package controllers;

import com.alibaba.fastjson.TypeReference;
import helper.J;
import helper.Webs;
import models.SaleTarget;
import models.User;
import models.view.Ret;
import play.data.validation.Validation;
import play.mvc.Controller;
import play.mvc.With;

import java.util.List;

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

    /**
     * 创建年度目标
     */
    public static void doCreateAnnual(SaleTarget st) {
        Validation.required("主题", st.theme.trim());
        Validation.required("目标时间", st.targetYear);
        st.createuser = User.findByUserName(Secure.Security.connected());
        st.saleTargetType = SaleTarget.T.YEAR;
        if(Validation.hasErrors()) {
            render("SaleTargets/createAnnual.html", st);
        }
        st.save();
        flash.success("保存成功.");
        show(st.id);
    }

    public static void updateAnnual(SaleTarget st) {
        validation.valid(st);
        if(!Validation.hasErrors()) {
            st.save();
            renderJSON(new Ret(true, "更新成功."));
        }
        renderJSON(new Ret(Webs.V(Validation.errors())));
    }


    /**
     * 创建或更新 子销售目标
     *
     * @param jsonstr json字符串
     */
    public static void saleTarget(String jsonstr) {
        List<SaleTarget> saleTargetList = J.from(jsonstr, new TypeReference<List<SaleTarget>>() {});
        SaleTarget father = SaleTarget.findById(saleTargetList.get(0).parentId);
        for(SaleTarget child : saleTargetList) {
            father.copySaleTarget(child, User.findByUserName(Secure.Security.connected()));

            validation.valid(child);
            if(Validation.hasErrors()) {
                continue;
            }
            if(child.id == null) {
                child.save();
            } else {
                /**
                 * 这里存在一个detached entity passed to persist问题，是由于 update 的时候hibernate还是去为对象会生成id
                 * 而id已经存在，所以需要将新的属性赋值给旧的对象再保存到数据库内
                 */
                SaleTarget old = SaleTarget.findById(child.id);
                old.targetYear = child.targetYear;
                old.profitMargin = child.profitMargin;
                old.saleAmounts = child.saleAmounts;
                old.targetMonth = child.targetMonth;
                old.save();
            }

        }
        if(Validation.hasErrors()) {
            renderJSON(new Ret(Webs.V(Validation.errors())));
        } else {
            renderJSON(new Ret(true, "保存成功."));
        }
    }
}
