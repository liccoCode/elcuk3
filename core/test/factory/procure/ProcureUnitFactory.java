package factory.procure;

import factory.FactoryBoy;
import factory.ModelFactory;
import factory.annotation.Factory;
import helper.Currency;
import models.market.Selling;
import models.procure.ProcureUnit;
import models.product.Product;

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

    @Factory(name = "plan")
    public ProcureUnit plan() {
        ProcureUnit unit = new ProcureUnit();
        unit.stage = ProcureUnit.STAGE.PLAN;
        unit.selling = FactoryBoy.lastOrCreate(Selling.class);
        unit.product = FactoryBoy.lastOrCreate(Product.class);
        //unit.cooperator
        //whouse
        unit.attrs.planQty = 300;
        unit.attrs.price = 19f;
        unit.attrs.currency = Currency.CNY;
        return unit;
    }

    @Factory(name = "done")
    public ProcureUnit done() {
        ProcureUnit unit = FactoryBoy.build(ProcureUnit.class, "plan");
        unit.stage = ProcureUnit.STAGE.DONE;
        unit.attrs.qty = 300;
        return unit;
    }
}
