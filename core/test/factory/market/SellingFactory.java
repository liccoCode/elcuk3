package factory.market;

import factory.FactoryBoy;
import factory.ModelFactory;
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
        selling.account = FactoryBoy.lastOrCreate(Account.class, "ide");
        AmazonProps aps = new AmazonProps();
        aps.brand = "EasyAcc";
        aps.condition_ = "NEW:NEW";
        aps.title = "title";
        aps.searchTerms = "searchTerms";

        selling.aps = aps;
        selling.asin = "ASIN";
        selling.fnSku = "FNSKU";
        selling.market = M.AMAZON_DE;
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
        selling.sid();
        return selling;
    }
}
