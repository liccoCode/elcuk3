package controllers;

import exception.PaymentException;
import models.finance.FeeType;
import models.view.Ret;
import play.mvc.Controller;
import play.mvc.With;

import java.util.List;

/**
 * Created by IntelliJ IDEA.
 * User: wyatt
 * Date: 1/28/13
 * Time: 4:25 PM
 */
@With({GlobalExceptionHandler.class, Secure.class})
public class FeeTypes extends Controller {

    @Check("feetypes.index")
    public static void index() {
        List<FeeType> types = FeeType.tops();
        render(types);
    }

    public static void update(String name, String memo, String nickName) {
        try {
            FeeType type = FeeType.findById(name);
            String oldMemo = type.memo;
            type.memo = memo;
            type.nickName = nickName;
            type.save();
            renderJSON(new Ret(true, String.format("FeeType %s 的 Memo 从 %s 变更为 %s", name, oldMemo,
                    memo)));
        } catch(Exception e) {
            renderJSON(new Ret(false, e.getMessage()));
        }
    }

    public static void create(FeeType ft, String parentName) {
        ft.save();
        ft.parent(FeeType.<FeeType>findById(parentName));
        flash.success(String.format("成功创建 FeeType %s", ft.name));
        index();
    }

    public static void remove(String name) {
        FeeType ft = FeeType.findById(name);
        if(ft == null)
            renderJSON(new Ret("不存在, 无法删除"));

        try {
            ft.remove();
        } catch(PaymentException e) {
            renderJSON(new Ret(false, e.getMessage()));
        }
        renderJSON(new Ret(true, "删除成功."));
    }
}
