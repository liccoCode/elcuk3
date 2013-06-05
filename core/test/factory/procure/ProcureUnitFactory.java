package factory.procure;

import factory.ModelFactory;
import models.procure.ProcureUnit;

/**
 * Created by IntelliJ IDEA.
 * User: wyatt
 * Date: 5/30/13
 * Time: 12:24 PM
 */
public class ProcureUnitFactory extends ModelFactory<ProcureUnit> {
    @Override
    public ProcureUnit define() {
        ProcureUnit unit = new ProcureUnit();
        unit.stage = ProcureUnit.STAGE.PLAN;
        unit.attrs.planQty = 200;
        unit.attrs.qty = 200;

        return unit;
    }
}
