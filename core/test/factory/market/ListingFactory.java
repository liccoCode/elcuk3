package factory.market;

import factory.FactoryBoy;
import factory.ModelFactory;
import models.market.Listing;
import models.market.M;
import models.product.Product;

/**
 * Created by IntelliJ IDEA.
 * User: wyatt
 * Date: 9/27/13
 * Time: 4:00 PM
 */
public class ListingFactory extends ModelFactory<Listing> {
    @Override
    public Listing define() {
        Listing lst = new Listing();
        lst.product = FactoryBoy.lastOrCreate(Product.class);
        lst.listingId = "B003JTAVJO_amazon.de";
        lst.asin = "B003JTAVJO";
        lst.byWho = "MFY";
        lst.market = M.AMAZON_DE;
        lst.title = "SET: RGB LED STRIP Leiste Band 2,5 METER - INKL. CONTROLLER, FERNBEDIENUNG UND NETZTEIL / MEHRFARBIG - MFYRGB-2.5";
        return lst;
    }
}
