package factory.procure;

import factory.FactoryBoy;
import factory.ModelFactory;
import factory.annotation.Factory;
import helper.Currency;
import models.market.Selling;
import models.procure.Cooperator;
import models.procure.ProcureUnit;
import models.procure.Shipment;
import models.product.Product;
import models.product.Whouse;

/**
 * Created by IntelliJ IDEA.
 * User: wyatt
 * Date: 5/30/13
 * Time: 12:24 PM
 */
public class ProcureUnitFactory extends ModelFactory<ProcureUnit> {
    @Override
    public ProcureUnit define() {
        ProcureUnit unit = base();
        unit.stage = ProcureUnit.STAGE.PLAN;
        unit.attrs.planQty = 200;
        unit.attrs.qty = 200;
        return unit;
    }

    @Factory(name = "plan")
    public ProcureUnit plan() {
        ProcureUnit unit = base();
        unit.stage = ProcureUnit.STAGE.PLAN;
        unit.shipType = Shipment.T.EXPRESS;
        return unit;
    }

    @Factory(name = "planSea")
    public ProcureUnit planSea() {
        ProcureUnit unit = base();
        unit.stage = ProcureUnit.STAGE.PLAN;
        unit.shipType = Shipment.T.SEA;
        return unit;
    }

    @Factory(name = "planAir")
    public ProcureUnit planAir() {
        ProcureUnit unit = base();
        unit.stage = ProcureUnit.STAGE.PLAN;
        unit.shipType = Shipment.T.AIR;
        return unit;
    }

    @Factory(name = "done")
    public ProcureUnit done() {
        ProcureUnit unit = base();
        unit.stage = ProcureUnit.STAGE.DONE;
        unit.attrs.qty = 300;
        return unit;
    }

    /**
     * 缺乏 Stage 与 ShipType
     *
     * @return
     */
    private ProcureUnit base() {
        ProcureUnit unit = new ProcureUnit();
        unit.selling = FactoryBoy.lastOrCreate(Selling.class);
        unit.product = FactoryBoy.lastOrCreate(Product.class);
        unit.cooperator = FactoryBoy.lastOrCreate(Cooperator.class);
        unit.whouse = FactoryBoy.lastOrCreate(Whouse.class);
        unit.attrs.planQty = 300;
        unit.attrs.price = 19f;
        unit.attrs.currency = Currency.CNY;
        return unit;
    }
}
