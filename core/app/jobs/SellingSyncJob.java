package jobs;

import helper.Webs;
import jobs.driver.BaseJob;
import models.market.Selling;
import models.product.Product;
import org.apache.commons.lang.StringUtils;
import play.Logger;
import play.jobs.Every;

import java.util.List;

/**
 * Created by IntelliJ IDEA.
 * User: licco
 * Date: 2017/11/21
 * Time: 上午10:25
 */
@Every("1mn")
public class SellingSyncJob extends BaseJob {

    public void doit() {
        List<Selling> sellings = Selling.find("sync=? ORDER BY createDate DESC", false).fetch(100);
        sellings.forEach(selling -> {
            try {
                if(StringUtils.isNotBlank(selling.merchantSKU)) {
                    String sku = selling.merchantSKU.split(",")[0];
                    Logger.info("selling:" + selling.sellingId + " 开始执行 syncAmazonInfoFromApi 方法 "+ sku);
                    Product product = Product.findById(sku);
                    if(product != null) {
                        selling.product = product;
                        selling.save();
                    }
                }
            } catch(Exception e) {
                Logger.error(Webs.e(e));
            } finally {
                selling.sync = true;
                selling.save();
            }
        });
    }

}
