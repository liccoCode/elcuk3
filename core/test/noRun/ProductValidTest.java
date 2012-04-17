package noRun;

import models.product.Product;
import org.junit.Assert;
import org.junit.Test;

/**
 * Created by IntelliJ IDEA.
 * User: wyattpan
 * Date: 4/17/12
 * Time: 11:00 AM
 */
public class ProductValidTest {
    @Test
    public void testValidSKU() {
        Assert.assertTrue(Product.validSKU("71KDT-PURPUL-2S"));
        Assert.assertTrue(Product.validSKU("50DCEB6-5V800U2"));
        Assert.assertTrue(Product.validSKU("71KDT-RPU-2S"));
        Assert.assertTrue(Product.validSKU("71KDT-PPU-2S"));
        Assert.assertTrue(Product.validSKU("71KDT-BPU-2S"));
        Assert.assertTrue(Product.validSKU("71KDT-BPUL-2S"));
        // not valid
        Assert.assertFalse(Product.validSKU("71-KDT-BPUL-2S"));
        Assert.assertFalse(Product.validSKU("71KDT-BPUL-2S-id"));
        Assert.assertFalse(Product.validSKU("7KDT-BPUL-2S"));
    }
}
