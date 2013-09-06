package factory.market;

import factory.FactoryBoy;
import factory.ModelFactory;
import factory.annotation.Factory;
import models.embedded.AmazonProps;
import models.market.Account;
import models.market.M;
import models.market.PriceStrategy;
import models.market.Selling;

/**
 * Created by IntelliJ IDEA.
 * User: wyatt
 * Date: 6/7/13
 * Time: 4:28 PM
 */
public class SellingFactory extends ModelFactory<Selling> {
    @Override
    public Selling define() {
        Selling selling = new Selling();
        AmazonProps aps = new AmazonProps();
        aps.brand = "EasyAcc";
        aps.condition_ = "NEW:NEW";
        aps.title = "title";
        aps.searchTerms = "searchTerms";
        aps.standerPrice = 19.99f;
        aps.salePrice = 9.99f;

        selling.aps = aps;
        selling.asin = "ASIN";
        selling.fnSku = "FNSKU";
        selling.merchantSKU = "merchantSKU";
        selling.shippingPrice = 12f;

        PriceStrategy strategy = new PriceStrategy();
        strategy.type = PriceStrategy.T.FixedPrice;
        strategy.margin = 1f;
        strategy.cost = 29f;
        strategy.lowest = 12f;
        strategy.max = 30f;
        selling.priceStrategy = strategy;

        selling.type = Selling.T.FBA;
        return selling;
    }

    @Factory(name = "de")
    public Selling de() {
        Selling selling = define();
        selling.account = FactoryBoy.lastOrCreate(Account.class, "de");
        selling.market = M.AMAZON_DE;
        selling.sid();
        return selling;
    }

    @Factory(name = "uk")
    public Selling uk() {
        Selling selling = define();
        selling.market = M.AMAZON_UK;
        selling.account = FactoryBoy.lastOrCreate(Account.class, "uk");
        selling.sid();
        return selling;
    }

    @Factory(name = "us")
    public Selling us() {
        Selling selling = define();
        selling.market = M.AMAZON_US;
        selling.account = FactoryBoy.lastOrCreate(Account.class, "us");
        selling.sid();
        return selling;
    }

    @Override
    public Class<?>[] relationModels() {
        return super.relationModels();
    }
}
