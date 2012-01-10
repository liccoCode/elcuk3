package product;

import models.product.ProductQTY;
import models.product.Whouse;
import org.junit.Test;
import play.db.jpa.JPA;
import play.test.UnitTest;

import java.util.ArrayList;
import java.util.List;

/**
 * Warehouse Test
 * User: Wyatt
 * Date: 12-1-8
 * Time: 上午6:41
 */
public class WarehouseTest extends UnitTest {
    @Test
    public void cWh() {
        Whouse whouse = new Whouse();
        whouse.name = "wyatt";

        List<ProductQTY> qtys = new ArrayList<ProductQTY>();
        ProductQTY qty1 = new ProductQTY();
        qty1.qty = 10;
        qty1.whouse = whouse;
        qty1.save();
        JPA.em().getTransaction().commit();

        qtys.add(qty1);
    }

    @Test
    public void dWh() {
        Whouse whouse = Whouse.find("byName", "wyatt").first();
        whouse.delete();
    }
}
