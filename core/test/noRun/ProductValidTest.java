package noRun;

import models.product.Product;
import org.apache.commons.codec.digest.DigestUtils;
import org.apache.commons.lang.StringUtils;
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

    @Test
    public void testHash() {
        System.out.println("The best solution until new battery technology available");
        System.out.println(DigestUtils.md5Hex("The best solution until new battery technology available"));
    }

    @Test
    public void testStringBetween() {
        String s = "\n" +
                "    $(document).ready(function(){\n" +
                "        MYO.AjaxLink({\n" +
                "            remoteAction: 'get-buyer-history',\n" +
                "            buyerEmail:  \"sj19pjqrh1kvm6n@marketplace.amazon.de\",\n" +
                "            targetID: '_myo_buyerEmail_showRepeatOrders',\n" +
                "            progressID: '_myo_buyerEmail_progressIndicator'\n" +
                "        });\n" +
                "     });\n" +
                "";
        System.out.println(StringUtils.substringBetween(s, "buyerEmail:", "targetID:").trim());
    }
}
