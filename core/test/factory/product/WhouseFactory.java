package factory.product;

import factory.FactoryBoy;
import factory.ModelFactory;
import models.market.Account;
import models.product.Whouse;

/**
 * Created by IntelliJ IDEA.
 * User: wyatt
 * Date: 8/16/13
 * Time: 10:00 AM
 */
public class WhouseFactory extends ModelFactory<Whouse> {
    @Override
    public Whouse define() {
        Whouse wh = new Whouse();
        wh.address = "UK unkonw longdon";
        wh.city = "Longdon";
        wh.country = "UK";
        wh.name = "FBA_UK";
        wh.postalCode = "DI83D";
        wh.province = "unknow";
        wh.type = Whouse.T.FBA;
        wh.account = FactoryBoy.lastOrCreate(Account.class);
        return wh;
    }
}
