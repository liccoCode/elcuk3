package models.view.post;

import models.whouse.ShipPlan;
import play.libs.F;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by licco on 16/6/27.
 */
public class ShipPlanPost extends Post<ShipPlan>{

    public List<ShipPlan> query() {

        List<ShipPlan> list = new ArrayList<>();


        return list;
    }


    @Override
    public F.T2<String, List<Object>> params() {
        return null;
    }
}
