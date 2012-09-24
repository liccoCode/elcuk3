package jobs;

import models.Jobex;
import models.market.Selling;
import models.product.Product;
import notifiers.SystemMails;
import play.jobs.Every;
import play.jobs.Job;
import play.jobs.On;
import play.libs.F;

import java.util.ArrayList;
import java.util.List;

/**
 * <pre>
 * Product 的规范性检查.
 *  * 周期:
 * - 轮询周期: 1d
 * - Duration: 0 10 0 (* /3) * ?
 * </pre>
 * User: wyattpan
 * Date: 8/24/12
 * Time: 12:22 PM
 */
@Every("1d")
public class CheckerProductCheckJob extends Job {
    @Override
    public void doJob() {
        if(!Jobex.findByClassName(CheckerProductCheckJob.class.getName()).isExcute()) return;
        /**
         * 1. 现在仅检查 Product 的图片问题
         */
        List<Selling> skuSales = Selling.analyzesSKUAndSID("sku");
        List<F.T2<Product,Selling>> thisTimeWarnningProduct = new ArrayList<F.T2<Product, Selling>>();
        for(Selling skuSale : skuSales) {
            if(thisTimeWarnningProduct.size() >= 10) break;
            Product prod = Product.findByMerchantSKU(skuSale.merchantSKU);
            if(prod.pictureCount() >= 4) continue;
            thisTimeWarnningProduct.add(new F.T2<Product, Selling>(prod, skuSale));
        }

        SystemMails.productPicCheckermail(thisTimeWarnningProduct);
    }
}
