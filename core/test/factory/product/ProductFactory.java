package factory.product;

import factory.ModelFactory;
import models.product.Product;

/**
 * Created by IntelliJ IDEA.
 * User: wyatt
 * Date: 6/7/13
 * Time: 4:33 PM
 */
public class ProductFactory extends ModelFactory<Product> {
    @Override
    public Product define() {
        Product sku = new Product();
        sku.sku = "sku";
        sku.declaredValue = 12f;
        sku.productName = "productName";
        sku.heigh = 21f;
        sku.lengths = 12f;
        sku.weight = 12f;
        sku.width = 12f;

        return sku;
    }
}
