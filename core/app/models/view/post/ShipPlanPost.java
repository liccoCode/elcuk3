package models.view.post;

import models.whouse.ShipPlan;
import play.libs.F;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by IntelliJ IDEA.
 * User: licco
 * Date: 16/6/27
 * Time: 3:42 PM
 */
public class ShipPlanPost extends Post<ShipPlan> {
    public static final List<F.T2<String, String>> DATE_TYPES;

    static {
        DATE_TYPES = new ArrayList<>();
        DATE_TYPES.add(new F.T2<>("createDate", "创建时间"));
        DATE_TYPES.add(new F.T2<>("createDate", "预计运输时间"));
        DATE_TYPES.add(new F.T2<>("completeDate", "预计到达时间"));
    }

    public List<ShipPlan> query() {
        List<ShipPlan> list = new ArrayList<>();
        return list;
    }

    @Override
    public F.T2<String, List<Object>> params() {
        return null;
    }
}
